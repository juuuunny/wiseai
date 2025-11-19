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

    Payment payment =
        paymentQueryPort
            .findById(message.paymentId())
            .orElseThrow(
                () -> new PaymentException(PaymentErrorStatus.NOT_FOUND));

    if (paymentProcessLogService.isProcessed(message.eventId())) {
      log.debug(
          "이미 처리된 결제 이벤트 무시: eventId={}, paymentId={}",
          message.eventId(),
          message.paymentId());
      return;
    }

    PaymentGateway gateway = paymentGatewayFactory.getGateway(payment.getPaymentMethod());

    String transactionId =
        gateway.processPayment(payment.getAmount(), payment.getReservationId()).join();
    Payment completed = payment.complete(transactionId);
    paymentCommandPort.update(completed);
    paymentProcessLogService.markProcessed(message.eventId(), message.paymentId());
    log.debug(
        "결제 처리 성공: paymentId={}, transactionId={}",
        message.paymentId(),
        transactionId);
  }
}

