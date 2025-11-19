package com.wiseai.assignment.modules.payment.application.service.event;

import com.wiseai.assignment.modules.payment.application.event.PaymentCancelRequestMessage;
import com.wiseai.assignment.modules.payment.application.port.out.command.PaymentCommandPort;
import com.wiseai.assignment.modules.payment.application.port.out.gateway.PaymentGateway;
import com.wiseai.assignment.modules.payment.application.port.out.query.PaymentQueryPort;
import com.wiseai.assignment.modules.payment.application.service.gateway.PaymentGatewayFactory;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.model.Payment;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelListener {

  private final PaymentQueryPort paymentQueryPort;
  private final PaymentCommandPort paymentCommandPort;
  private final PaymentGatewayFactory paymentGatewayFactory;
  private final PaymentCancelLogService paymentCancelLogService;

  @KafkaListener(topics = "${payment.kafka.topics.cancel}")
  public void handleCancellation(PaymentCancelRequestMessage message) {
    log.debug(
        "결제 취소 이벤트 수신: eventId={}, paymentId={}",
        message.eventId(),
        message.paymentId());

    if (paymentCancelLogService.isProcessed(message.eventId())) {
      log.debug(
          "이미 처리된 결제 취소 이벤트 무시: eventId={}, paymentId={}",
          message.eventId(),
          message.paymentId());
      return;
    }

    Payment payment =
        paymentQueryPort
            .findById(message.paymentId())
            .orElseThrow(
                () -> new PaymentException(PaymentErrorStatus.NOT_FOUND));

    PaymentGateway gateway = paymentGatewayFactory.getGateway(message.paymentMethod());
    CompletableFuture<Boolean> cancelFuture =
        gateway.cancelPayment(message.transactionId());

    boolean cancelled = cancelFuture.join();
    if (!cancelled) {
      throw new RuntimeException("결제 취소 실패: paymentId=" + message.paymentId());
    }

    Payment cancelledPayment = payment.cancel();
    paymentCommandPort.update(cancelledPayment);
    paymentCancelLogService.markProcessed(
        message.eventId(), message.paymentId(), message.paymentMethod());
    log.debug("결제 취소 완료: paymentId={}", message.paymentId());
  }
}

