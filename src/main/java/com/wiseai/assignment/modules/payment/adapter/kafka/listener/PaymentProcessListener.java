package com.wiseai.assignment.modules.payment.adapter.kafka.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.wiseai.assignment.modules.payment.application.event.PaymentProcessMessage;
import com.wiseai.assignment.modules.payment.application.port.out.command.PaymentCommandPort;
import com.wiseai.assignment.modules.payment.application.port.out.gateway.PaymentGateway;
import com.wiseai.assignment.modules.payment.application.port.out.query.PaymentQueryPort;
import com.wiseai.assignment.modules.payment.application.service.gateway.PaymentGatewayFactory;
import com.wiseai.assignment.modules.payment.application.service.infrastructure.PaymentProcessLogService;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.model.Payment;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;
import com.wiseai.assignment.modules.reservation.application.port.out.command.ReservationCommandPort;
import com.wiseai.assignment.modules.reservation.application.port.out.query.ReservationQueryPort;
import com.wiseai.assignment.modules.reservation.domain.exception.ReservationException;
import com.wiseai.assignment.modules.reservation.domain.model.Reservation;
import com.wiseai.assignment.modules.reservation.domain.status.ReservationErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProcessListener {

  private final PaymentQueryPort paymentQueryPort;
  private final PaymentCommandPort paymentCommandPort;
  private final PaymentGatewayFactory paymentGatewayFactory;
  private final PaymentProcessLogService paymentProcessLogService;
  private final ReservationQueryPort reservationQueryPort;
  private final ReservationCommandPort reservationCommandPort;

  @KafkaListener(
      topics = "${payment.kafka.topics.process}",
      groupId = "${spring.kafka.consumer.group-id}")
  @Transactional
  public void handleMessage(PaymentProcessMessage message) {
    log.info(
        "결제 처리 이벤트 수신: eventId={}, paymentId={}, method={}",
        message.eventId(),
        message.paymentId(),
        message.paymentMethod());

    Payment payment =
        paymentQueryPort
            .findById(message.paymentId())
            .orElseThrow(() -> new PaymentException(PaymentErrorStatus.NOT_FOUND));

    if (paymentProcessLogService.isProcessed(message.eventId())) {
      log.debug(
          "이미 처리된 결제 이벤트 무시: eventId={}, paymentId={}", message.eventId(), message.paymentId());
      return;
    }

    // 멱등성 로그 선점
    paymentProcessLogService.tryAcquire(message.eventId(), message.paymentId());

    try {
      PaymentGateway gateway = paymentGatewayFactory.getGateway(payment.getPaymentMethod());

      String transactionId =
          gateway.processPayment(payment.getAmount(), payment.getReservationId()).join();
      Payment completed = payment.complete(transactionId);
      paymentCommandPort.update(completed);

      // 결제 완료 시 예약 상태 확정
      Reservation reservation =
          reservationQueryPort
              .findById(payment.getReservationId())
              .orElseThrow(
                  () -> {
                    log.error(
                        "예약을 찾을 수 없음: reservationId={}, paymentId={}",
                        payment.getReservationId(),
                        message.paymentId());
                    return new ReservationException(ReservationErrorStatus.NOT_FOUND);
                  });

      Reservation confirmed = reservation.confirm();
      reservationCommandPort.update(confirmed);

      paymentProcessLogService.markProcessed(message.eventId(), message.paymentId());
      log.info(
          "결제 처리 및 예약 확정 완료: paymentId={}, transactionId={}, reservationId={}",
          message.paymentId(),
          transactionId,
          payment.getReservationId());
    } catch (Exception e) {
      log.error(
          "결제 처리 중 오류 발생: eventId={}, paymentId={}, error={}",
          message.eventId(),
          message.paymentId(),
          e.getMessage());
      // 멱등성 로그 해제 (재시도 가능하도록)
      paymentProcessLogService.release(message.eventId());
      // 예외를 다시 던져서 DefaultErrorHandler의 재시도 및 DLQ 로직을 트리거
      throw new PaymentException(PaymentErrorStatus.PAYMENT_GATEWAY_ERROR);
    }
  }
}
