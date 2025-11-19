package com.wiseai.assignment.modules.reservation.application.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;
import com.wiseai.assignment.modules.payment.application.port.in.command.CreatePaymentUseCase;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.reservation.application.port.in.command.ProcessReservationPaymentUseCase;
import com.wiseai.assignment.modules.reservation.application.port.out.query.ReservationQueryPort;
import com.wiseai.assignment.modules.reservation.domain.enums.ReservationStatus;
import com.wiseai.assignment.modules.reservation.domain.exception.ReservationException;
import com.wiseai.assignment.modules.reservation.domain.model.Reservation;
import com.wiseai.assignment.modules.reservation.domain.status.ReservationErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationPaymentService implements ProcessReservationPaymentUseCase {

  private final ReservationQueryPort reservationQueryPort;
  private final CreatePaymentUseCase createPaymentUseCase;

  @Override
  @Transactional
  public PaymentResponse processPayment(Long reservationId, PaymentMethod paymentMethod) {
    log.debug("예약 결제 처리 요청: reservationId={}, paymentMethod={}", reservationId, paymentMethod);

    Reservation reservation =
        reservationQueryPort
            .findById(reservationId)
            .orElseThrow(
                () -> {
                  log.warn("예약을 찾을 수 없음: reservationId={}", reservationId);
                  return new ReservationException(ReservationErrorStatus.NOT_FOUND);
                });

    if (reservation.getStatus() != ReservationStatus.PENDING) {
      log.warn(
          "결제 가능한 상태가 아님: reservationId={}, status={}", reservationId, reservation.getStatus());
      throw new ReservationException(ReservationErrorStatus.INVALID_PAYMENT_STATUS);
    }

    PaymentResponse paymentResponse =
        createPaymentUseCase.createPayment(
            reservationId, paymentMethod, reservation.getTotalAmount());

    log.info("예약 결제 처리 완료: reservationId={}, paymentId={}", reservationId, paymentResponse.id());
    return paymentResponse;
  }
}
