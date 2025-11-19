package com.wiseai.assignment.modules.payment.adapter.kafka.listener;

import com.wiseai.assignment.modules.payment.application.event.PaymentCancelRequestMessage;
import com.wiseai.assignment.modules.payment.application.port.out.command.PaymentCommandPort;
import com.wiseai.assignment.modules.payment.application.port.out.gateway.PaymentGateway;
import com.wiseai.assignment.modules.payment.application.port.out.query.PaymentQueryPort;
import com.wiseai.assignment.modules.payment.application.service.gateway.PaymentGatewayFactory;
import com.wiseai.assignment.modules.payment.application.service.infrastructure.PaymentCancelLogService;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.model.Payment;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelListener {

  private final PaymentQueryPort paymentQueryPort;
  private final PaymentCommandPort paymentCommandPort;
  private final PaymentGatewayFactory paymentGatewayFactory;
  private final PaymentCancelLogService paymentCancelLogService;

  @KafkaListener(topics = "${payment.kafka.topics.cancel}", groupId = "${spring.kafka.consumer.group-id}")
  @Transactional
  public void handleCancellation(PaymentCancelRequestMessage message) {
    log.info(
        "결제 취소 이벤트 수신: eventId={}, paymentId={}, transactionId={}",
        message.eventId(),
        message.paymentId(),
        message.transactionId());

    Payment payment =
        paymentQueryPort
            .findById(message.paymentId())
            .orElseThrow(() -> new PaymentException(PaymentErrorStatus.NOT_FOUND));

    if (paymentCancelLogService.isProcessed(message.eventId())) {
      log.debug(
          "이미 처리된 결제 취소 이벤트 무시: eventId={}, paymentId={}",
          message.eventId(),
          message.paymentId());
      return;
    }

    // 멱등성 로그 선점
    paymentCancelLogService.tryAcquire(
        message.eventId(), message.paymentId(), payment.getPaymentMethod());

    try {
      PaymentGateway gateway = paymentGatewayFactory.getGateway(payment.getPaymentMethod());

      // PG 취소 처리
      Boolean success = gateway.cancelPayment(message.transactionId()).join();

      if (!success) {
        throw new PaymentException(PaymentErrorStatus.PAYMENT_GATEWAY_ERROR);
      }

      Payment cancelledPayment = payment.cancel();
      paymentCommandPort.update(cancelledPayment);
      paymentCancelLogService.markProcessed(
          message.eventId(), message.paymentId(), message.paymentMethod());
      log.info("결제 취소 완료: paymentId={}", message.paymentId());
    } catch (Exception e) {
      log.error(
          "결제 취소 중 오류 발생: eventId={}, paymentId={}, error={}",
          message.eventId(),
          message.paymentId(),
          e.getMessage());
      // 멱등성 로그 해제 (재시도 가능하도록)
      paymentCancelLogService.release(message.eventId());
      // 예외를 다시 던져서 DefaultErrorHandler의 재시도 및 DLQ 로직을 트리거
      throw new PaymentException(PaymentErrorStatus.PAYMENT_GATEWAY_ERROR);
    }
  }
}

