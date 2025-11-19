package com.wiseai.assignment.modules.payment.application.service.event;

import com.wiseai.assignment.modules.payment.application.event.PaymentProcessMessage;
import com.wiseai.assignment.modules.payment.application.port.out.command.PaymentCommandPort;
import com.wiseai.assignment.modules.payment.application.port.out.gateway.PaymentGateway;
import com.wiseai.assignment.modules.payment.application.port.out.query.PaymentQueryPort;
import com.wiseai.assignment.modules.payment.application.service.gateway.PaymentGatewayFactory;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.model.Payment;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProcessListener {

  private final PaymentQueryPort paymentQueryPort;
  private final PaymentCommandPort paymentCommandPort;
  private final PaymentGatewayFactory paymentGatewayFactory;
  private final PaymentProcessLogService paymentProcessLogService;
  private final PaymentDlqProducer paymentDlqProducer;

  @KafkaListener(topics = "${payment.kafka.topics.process}")
  public void handleMessage(PaymentProcessMessage message) {
    log.debug(
        "결제 처리 이벤트 수신: eventId={}, paymentId={}, method={}",
        message.eventId(),
        message.paymentId(),
        message.paymentMethod());

    if (!paymentProcessLogService.tryAcquire(message.eventId(), message.paymentId())) {
      return;
    }

    Payment payment =
        paymentQueryPort
            .findById(message.paymentId())
            .orElseThrow(
                () -> new PaymentException(PaymentErrorStatus.NOT_FOUND));

    PaymentGateway gateway = paymentGatewayFactory.getGateway(payment.getPaymentMethod());

    try {
      String transactionId =
          gateway.processPayment(payment.getAmount(), payment.getReservationId()).join();
      Payment completed = payment.complete(transactionId);
      paymentCommandPort.update(completed);
      log.debug(
          "결제 처리 성공: paymentId={}, transactionId={}",
          message.paymentId(),
          transactionId);
    } catch (Exception e) {
      paymentProcessLogService.release(message.eventId());
      Payment failed = payment.fail();
      paymentCommandPort.update(failed);
      paymentDlqProducer.publishProcessFailure(message, e);
      log.error("결제 처리 실패 - DLQ로 이동: paymentId={}", message.paymentId(), e);
    }
  }
}

