package com.wiseai.assignment.modules.reservation.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wiseai.assignment.modules.reservation.application.dto.response.ReservationResponse;
import com.wiseai.assignment.modules.reservation.application.port.out.command.ReservationCommandPort;
import com.wiseai.assignment.modules.reservation.application.port.out.query.ReservationQueryPort;
import com.wiseai.assignment.modules.reservation.domain.enums.ReservationStatus;
import com.wiseai.assignment.modules.reservation.domain.exception.ReservationException;
import com.wiseai.assignment.modules.reservation.domain.model.Reservation;
import com.wiseai.assignment.modules.reservation.domain.status.ReservationErrorStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationCommandService 테스트")
class ReservationCommandServiceTest {

  @Mock private ReservationCommandPort reservationCommandPort;
  @Mock private ReservationQueryPort reservationQueryPort;

  @InjectMocks private ReservationCommandService reservationCommandService;

  // 공통 테스트 데이터
  private static final int TEST_YEAR = 2024;
  private static final int TEST_MONTH = 1;
  private static final int TEST_DAY = 1;
  private static final Long DEFAULT_MEETING_ROOM_ID = 1L;
  private static final Long DEFAULT_USER_ID = 1L;
  private static final Long OTHER_USER_ID = 2L;
  private static final Long DEFAULT_RESERVATION_ID = 1L;
  private static final LocalDateTime DEFAULT_START_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 10, 0);
  private static final LocalDateTime DEFAULT_END_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 11, 0);
  private static final BigDecimal DEFAULT_TOTAL_AMOUNT = new BigDecimal("10000");

  @Test
  @DisplayName("예약 생성 성공")
  void createReservation_success() {
    // given
    Reservation reservation =
        Reservation.create(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);
    Reservation saved = reservation.withId(DEFAULT_RESERVATION_ID);

    given(
            reservationQueryPort.findOverlappingReservations(
                DEFAULT_MEETING_ROOM_ID, DEFAULT_START_TIME, DEFAULT_END_TIME))
        .willReturn(Collections.emptyList());
    given(reservationCommandPort.save(any(Reservation.class))).willReturn(saved);

    // when
    ReservationResponse result =
        reservationCommandService.createReservation(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);

    // then
    assertThat(result.id()).isEqualTo(DEFAULT_RESERVATION_ID);
    assertThat(result.meetingRoomId()).isEqualTo(DEFAULT_MEETING_ROOM_ID);
    assertThat(result.userId()).isEqualTo(DEFAULT_USER_ID);
    assertThat(result.status()).isEqualTo(ReservationStatus.PENDING);
    assertThat(result.totalAmount()).isEqualByComparingTo(DEFAULT_TOTAL_AMOUNT);
  }

  @Test
  @DisplayName("예약 생성 실패 - 겹치는 예약 존재")
  void createReservation_fail_duplicate() {
    // given
    Reservation existingReservation =
        Reservation.create(
            DEFAULT_MEETING_ROOM_ID,
            OTHER_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);
    existingReservation = existingReservation.withId(2L);

    given(
            reservationQueryPort.findOverlappingReservations(
                DEFAULT_MEETING_ROOM_ID, DEFAULT_START_TIME, DEFAULT_END_TIME))
        .willReturn(List.of(existingReservation));

    // when & then
    assertThatThrownBy(
            () ->
                reservationCommandService.createReservation(
                    DEFAULT_MEETING_ROOM_ID,
                    DEFAULT_USER_ID,
                    DEFAULT_START_TIME,
                    DEFAULT_END_TIME,
                    DEFAULT_TOTAL_AMOUNT))
        .isInstanceOf(ReservationException.class)
        .satisfies(
            exception -> {
              ReservationException ex = (ReservationException) exception;
              assertThat(ex.getErrorCode()).isEqualTo(ReservationErrorStatus.DUPLICATE_RESERVATION);
            });
  }

  @Test
  @DisplayName("예약 취소 성공")
  void cancelReservation_success() {
    // given
    Reservation reservation =
        Reservation.create(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);
    Reservation reservationWithId = reservation.withId(DEFAULT_RESERVATION_ID);
    Reservation cancelled = reservationWithId.cancel();

    given(reservationQueryPort.findById(DEFAULT_RESERVATION_ID))
        .willReturn(Optional.of(reservationWithId));
    given(reservationCommandPort.update(any(Reservation.class))).willReturn(cancelled);

    // when
    ReservationResponse result =
        reservationCommandService.cancelReservation(DEFAULT_RESERVATION_ID, DEFAULT_USER_ID);

    // then
    assertThat(result.id()).isEqualTo(DEFAULT_RESERVATION_ID);
    assertThat(result.status()).isEqualTo(ReservationStatus.CANCELLED);
  }

  @Test
  @DisplayName("예약 취소 실패 - 존재하지 않는 예약")
  void cancelReservation_fail_notFound() {
    // given
    given(reservationQueryPort.findById(DEFAULT_RESERVATION_ID)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
            () ->
                reservationCommandService.cancelReservation(
                    DEFAULT_RESERVATION_ID, DEFAULT_USER_ID))
        .isInstanceOf(ReservationException.class)
        .satisfies(
            exception -> {
              ReservationException ex = (ReservationException) exception;
              assertThat(ex.getErrorCode()).isEqualTo(ReservationErrorStatus.NOT_FOUND);
            });
  }

  @Test
  @DisplayName("예약 취소 실패 - 본인 예약이 아님")
  void cancelReservation_fail_unauthorized() {
    // given
    Reservation reservation =
        Reservation.create(
            DEFAULT_MEETING_ROOM_ID,
            OTHER_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);
    Reservation reservationWithId = reservation.withId(DEFAULT_RESERVATION_ID);

    given(reservationQueryPort.findById(DEFAULT_RESERVATION_ID))
        .willReturn(Optional.of(reservationWithId));

    // when & then
    assertThatThrownBy(
            () ->
                reservationCommandService.cancelReservation(
                    DEFAULT_RESERVATION_ID, DEFAULT_USER_ID))
        .isInstanceOf(ReservationException.class)
        .satisfies(
            exception -> {
              ReservationException ex = (ReservationException) exception;
              assertThat(ex.getErrorCode()).isEqualTo(ReservationErrorStatus.UNAUTHORIZED);
            });
  }
}
