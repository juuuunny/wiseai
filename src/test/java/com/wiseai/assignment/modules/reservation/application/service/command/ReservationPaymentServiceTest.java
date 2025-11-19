package com.wiseai.assignment.modules.reservation.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;
import com.wiseai.assignment.modules.payment.application.port.in.command.CreatePaymentUseCase;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentStatus;
import com.wiseai.assignment.modules.reservation.application.port.out.query.ReservationQueryPort;
import com.wiseai.assignment.modules.reservation.domain.enums.ReservationStatus;
import com.wiseai.assignment.modules.reservation.domain.exception.ReservationException;
import com.wiseai.assignment.modules.reservation.domain.model.Reservation;
import com.wiseai.assignment.modules.reservation.domain.status.ReservationErrorStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationPaymentService 테스트")
class ReservationPaymentServiceTest {

  @Mock private ReservationQueryPort reservationQueryPort;
  @Mock private CreatePaymentUseCase createPaymentUseCase;

  @InjectMocks private ReservationPaymentService reservationPaymentService;

  private static final Long RESERVATION_ID = 1L;
  private static final Long MEETING_ROOM_ID = 1L;
  private static final Long USER_ID = 1L;
  private static final LocalDateTime START_TIME = LocalDateTime.of(2024, 1, 1, 10, 0);
  private static final LocalDateTime END_TIME = LocalDateTime.of(2024, 1, 1, 11, 0);
  private static final BigDecimal TOTAL_AMOUNT = new BigDecimal("10000");
  private static final PaymentMethod PAYMENT_METHOD = PaymentMethod.TOSS;

  @Test
  @DisplayName("예약 결제 처리 성공")
  void processPayment_success() {
    // given
    Reservation reservation =
        Reservation.builder()
            .id(RESERVATION_ID)
            .meetingRoomId(MEETING_ROOM_ID)
            .userId(USER_ID)
            .startTime(START_TIME)
            .endTime(END_TIME)
            .status(ReservationStatus.PENDING)
            .totalAmount(TOTAL_AMOUNT)
            .build();

    PaymentResponse paymentResponse =
        new PaymentResponse(
            1L, RESERVATION_ID, PAYMENT_METHOD, TOTAL_AMOUNT, PaymentStatus.PENDING, null);

    given(reservationQueryPort.findById(RESERVATION_ID)).willReturn(Optional.of(reservation));
    given(createPaymentUseCase.createPayment(RESERVATION_ID, PAYMENT_METHOD, TOTAL_AMOUNT))
        .willReturn(paymentResponse);

    // when
    PaymentResponse result = reservationPaymentService.processPayment(RESERVATION_ID, PAYMENT_METHOD);

    // then
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.reservationId()).isEqualTo(RESERVATION_ID);
    assertThat(result.paymentMethod()).isEqualTo(PAYMENT_METHOD);
    assertThat(result.amount()).isEqualTo(TOTAL_AMOUNT);
  }

  @Test
  @DisplayName("예약 결제 처리 실패 - 예약을 찾을 수 없음")
  void processPayment_fail_notFound() {
    // given
    given(reservationQueryPort.findById(RESERVATION_ID)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> reservationPaymentService.processPayment(RESERVATION_ID, PAYMENT_METHOD))
        .isInstanceOf(ReservationException.class)
        .extracting("errorCode")
        .isEqualTo(ReservationErrorStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("예약 결제 처리 실패 - 결제 불가능한 상태")
  void processPayment_fail_invalidStatus() {
    // given
    Reservation reservation =
        Reservation.builder()
            .id(RESERVATION_ID)
            .meetingRoomId(MEETING_ROOM_ID)
            .userId(USER_ID)
            .startTime(START_TIME)
            .endTime(END_TIME)
            .status(ReservationStatus.CONFIRMED)
            .totalAmount(TOTAL_AMOUNT)
            .build();

    given(reservationQueryPort.findById(RESERVATION_ID)).willReturn(Optional.of(reservation));

    // when & then
    assertThatThrownBy(() -> reservationPaymentService.processPayment(RESERVATION_ID, PAYMENT_METHOD))
        .isInstanceOf(ReservationException.class)
        .extracting("errorCode")
        .isEqualTo(ReservationErrorStatus.INVALID_PAYMENT_STATUS);
  }
}

