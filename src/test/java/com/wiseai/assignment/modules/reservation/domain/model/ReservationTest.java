package com.wiseai.assignment.modules.reservation.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.wiseai.assignment.modules.reservation.domain.enums.ReservationStatus;
import com.wiseai.assignment.modules.reservation.domain.exception.ReservationException;
import com.wiseai.assignment.modules.reservation.domain.status.ReservationErrorStatus;

class ReservationTest {

  @Test
  @DisplayName("예약 생성 성공")
  void createReservation_success() {
    // given
    Long meetingRoomId = 1L;
    Long userId = 1L;
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 11, 0);
    BigDecimal totalAmount = new BigDecimal("10000");

    // when
    Reservation reservation =
        Reservation.create(meetingRoomId, userId, startTime, endTime, totalAmount);

    // then
    assertThat(reservation.getMeetingRoomId()).isEqualTo(meetingRoomId);
    assertThat(reservation.getUserId()).isEqualTo(userId);
    assertThat(reservation.getStartTime()).isEqualTo(startTime);
    assertThat(reservation.getEndTime()).isEqualTo(endTime);
    assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
    assertThat(reservation.getTotalAmount()).isEqualTo(totalAmount);
  }

  @Test
  @DisplayName("예약 생성 실패 - 잘못된 회의실 ID")
  void createReservation_fail_invalidMeetingRoomId() {
    // given
    Long invalidMeetingRoomId = 0L;
    Long userId = 1L;
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 11, 0);
    BigDecimal totalAmount = new BigDecimal("10000");

    // when & then
    assertThatThrownBy(
            () -> Reservation.create(invalidMeetingRoomId, userId, startTime, endTime, totalAmount))
        .isInstanceOf(ReservationException.class)
        .hasMessage(ReservationErrorStatus.INVALID_MEETING_ROOM.getMessage());
  }

  @Test
  @DisplayName("예약 생성 실패 - 잘못된 사용자 ID")
  void createReservation_fail_invalidUserId() {
    // given
    Long meetingRoomId = 1L;
    Long invalidUserId = -1L;
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 11, 0);
    BigDecimal totalAmount = new BigDecimal("10000");

    // when & then
    assertThatThrownBy(
            () -> Reservation.create(meetingRoomId, invalidUserId, startTime, endTime, totalAmount))
        .isInstanceOf(ReservationException.class)
        .hasMessage(ReservationErrorStatus.INVALID_USER.getMessage());
  }

  @Test
  @DisplayName("예약 생성 실패 - 시작 시간이 종료 시간보다 늦음")
  void createReservation_fail_invalidTimeRange() {
    // given
    Long meetingRoomId = 1L;
    Long userId = 1L;
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 10, 0);
    BigDecimal totalAmount = new BigDecimal("10000");

    // when & then
    assertThatThrownBy(
            () -> Reservation.create(meetingRoomId, userId, startTime, endTime, totalAmount))
        .isInstanceOf(ReservationException.class)
        .hasMessage(ReservationErrorStatus.INVALID_START_TIME.getMessage());
  }

  @Test
  @DisplayName("예약 생성 실패 - 시작 시간과 종료 시간이 같음")
  void createReservation_fail_sameStartAndEndTime() {
    // given
    Long meetingRoomId = 1L;
    Long userId = 1L;
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 10, 0);
    BigDecimal totalAmount = new BigDecimal("10000");

    // when & then
    assertThatThrownBy(
            () -> Reservation.create(meetingRoomId, userId, startTime, endTime, totalAmount))
        .isInstanceOf(ReservationException.class)
        .hasMessage(ReservationErrorStatus.INVALID_START_TIME.getMessage());
  }

  @Test
  @DisplayName("예약 생성 실패 - 정시/30분 단위가 아닌 시작 시간")
  void createReservation_fail_invalidTimeUnit_startTime() {
    // given
    Long meetingRoomId = 1L;
    Long userId = 1L;
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 15);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 11, 0);
    BigDecimal totalAmount = new BigDecimal("10000");

    // when & then
    assertThatThrownBy(
            () -> Reservation.create(meetingRoomId, userId, startTime, endTime, totalAmount))
        .isInstanceOf(ReservationException.class)
        .hasMessage(ReservationErrorStatus.INVALID_TIME_UNIT.getMessage());
  }

  @Test
  @DisplayName("예약 생성 실패 - 정시/30분 단위가 아닌 종료 시간")
  void createReservation_fail_invalidTimeUnit_endTime() {
    // given
    Long meetingRoomId = 1L;
    Long userId = 1L;
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 11, 45);
    BigDecimal totalAmount = new BigDecimal("10000");

    // when & then
    assertThatThrownBy(
            () -> Reservation.create(meetingRoomId, userId, startTime, endTime, totalAmount))
        .isInstanceOf(ReservationException.class)
        .hasMessage(ReservationErrorStatus.INVALID_TIME_UNIT.getMessage());
  }

  @Test
  @DisplayName("예약 생성 성공 - 30분 단위 시간")
  void createReservation_success_30minUnit() {
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 30);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 11, 30);

    Reservation reservation =
        Reservation.create(1L, 1L, startTime, endTime, new BigDecimal("10000"));

    assertThat(reservation.getStartTime()).isEqualTo(startTime);
    assertThat(reservation.getEndTime()).isEqualTo(endTime);
  }

  @Test
  @DisplayName("예약 생성 실패 - 잘못된 총 금액")
  void createReservation_fail_invalidTotalAmount() {
    // given
    Long meetingRoomId = 1L;
    Long userId = 1L;
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 11, 0);
    BigDecimal negativeAmount = new BigDecimal("-1000");

    // when & then
    assertThatThrownBy(
            () -> Reservation.create(meetingRoomId, userId, startTime, endTime, negativeAmount))
        .isInstanceOf(ReservationException.class)
        .hasMessage(ReservationErrorStatus.INVALID_TOTAL_AMOUNT.getMessage());
  }

  @Test
  @DisplayName("예약 취소 성공")
  void cancelReservation_success() {
    // given
    Reservation reservation =
        Reservation.create(
            1L,
            1L,
            LocalDateTime.of(2024, 1, 1, 10, 0),
            LocalDateTime.of(2024, 1, 1, 11, 0),
            new BigDecimal("10000"));
    Reservation reservationWithId = reservation.withId(1L);

    // when
    Reservation cancelled = reservationWithId.cancel();

    // then
    assertThat(cancelled.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    assertThat(cancelled.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("예약 취소 실패 - 이미 취소된 예약")
  void cancelReservation_fail_alreadyCancelled() {
    // given
    Reservation reservation =
        Reservation.create(
            1L,
            1L,
            LocalDateTime.of(2024, 1, 1, 10, 0),
            LocalDateTime.of(2024, 1, 1, 11, 0),
            new BigDecimal("10000"));
    Reservation cancelled = reservation.withId(1L).cancel();

    // when & then
    assertThatThrownBy(() -> cancelled.cancel())
        .isInstanceOf(ReservationException.class)
        .hasMessage(ReservationErrorStatus.INVALID_CANCEL_STATUS.getMessage());
  }

  @Test
  @DisplayName("예약 취소 실패 - 이미 확정된 예약")
  void cancelReservation_fail_alreadyConfirmed() {
    // given
    Reservation reservation =
        Reservation.create(
            1L,
            1L,
            LocalDateTime.of(2024, 1, 1, 10, 0),
            LocalDateTime.of(2024, 1, 1, 11, 0),
            new BigDecimal("10000"));
    Reservation confirmed = reservation.withId(1L).confirm();

    // when & then
    assertThatThrownBy(() -> confirmed.cancel())
        .isInstanceOf(ReservationException.class)
        .hasMessage(ReservationErrorStatus.INVALID_CANCEL_STATUS.getMessage());
  }

  @Test
  @DisplayName("예약 확정 성공")
  void confirmReservation_success() {
    // given
    Reservation reservation =
        Reservation.create(
            1L,
            1L,
            LocalDateTime.of(2024, 1, 1, 10, 0),
            LocalDateTime.of(2024, 1, 1, 11, 0),
            new BigDecimal("10000"));
    Reservation reservationWithId = reservation.withId(1L);

    // when
    Reservation confirmed = reservationWithId.confirm();

    // then
    assertThat(confirmed.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    assertThat(confirmed.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("예약 확정 실패 - PENDING 상태가 아닌 예약")
  void confirmReservation_fail_notPending() {
    // given
    Reservation reservation =
        Reservation.create(
            1L,
            1L,
            LocalDateTime.of(2024, 1, 1, 10, 0),
            LocalDateTime.of(2024, 1, 1, 11, 0),
            new BigDecimal("10000"));
    Reservation confirmed = reservation.withId(1L).confirm();

    // when & then
    assertThatThrownBy(() -> confirmed.confirm())
        .isInstanceOf(ReservationException.class)
        .hasMessage(ReservationErrorStatus.INVALID_CANCEL_STATUS.getMessage());
  }

  @Test
  @DisplayName("예약 중복 확인 - 겹치는 시간대")
  void overlapsWith_overlapping() {
    Reservation reservation1 =
        Reservation.create(
            1L,
            1L,
            LocalDateTime.of(2024, 1, 1, 10, 0),
            LocalDateTime.of(2024, 1, 1, 11, 0),
            new BigDecimal("10000"));
    Reservation reservation2 =
        Reservation.create(
            1L,
            2L,
            LocalDateTime.of(2024, 1, 1, 10, 30),
            LocalDateTime.of(2024, 1, 1, 11, 30),
            new BigDecimal("15000"));

    assertThat(reservation1.overlapsWith(reservation2)).isTrue();
  }

  @Test
  @DisplayName("예약 중복 확인 - 겹치지 않는 시간대")
  void overlapsWith_notOverlapping() {
    Reservation reservation1 =
        Reservation.create(
            1L,
            1L,
            LocalDateTime.of(2024, 1, 1, 10, 0),
            LocalDateTime.of(2024, 1, 1, 11, 0),
            new BigDecimal("10000"));
    Reservation reservation2 =
        Reservation.create(
            1L,
            2L,
            LocalDateTime.of(2024, 1, 1, 11, 0),
            LocalDateTime.of(2024, 1, 1, 12, 0),
            new BigDecimal("15000"));

    assertThat(reservation1.overlapsWith(reservation2)).isFalse();
  }

  @Test
  @DisplayName("예약 중복 확인 - 다른 회의실")
  void overlapsWith_differentMeetingRoom() {
    Reservation reservation1 =
        Reservation.create(
            1L,
            1L,
            LocalDateTime.of(2024, 1, 1, 10, 0),
            LocalDateTime.of(2024, 1, 1, 11, 0),
            new BigDecimal("10000"));
    Reservation reservation2 =
        Reservation.create(
            2L,
            2L,
            LocalDateTime.of(2024, 1, 1, 10, 0),
            LocalDateTime.of(2024, 1, 1, 11, 0),
            new BigDecimal("15000"));

    assertThat(reservation1.overlapsWith(reservation2)).isFalse();
  }

  @Test
  @DisplayName("예약 중복 확인 - null 파라미터")
  void overlapsWith_null() {
    Reservation reservation =
        Reservation.create(
            1L,
            1L,
            LocalDateTime.of(2024, 1, 1, 10, 0),
            LocalDateTime.of(2024, 1, 1, 11, 0),
            new BigDecimal("10000"));

    assertThat(reservation.overlapsWith(null)).isFalse();
  }

  @Test
  @DisplayName("예약 취소 가능 여부 확인")
  void canCancel() {
    Reservation pending =
        Reservation.create(
            1L,
            1L,
            LocalDateTime.of(2024, 1, 1, 10, 0),
            LocalDateTime.of(2024, 1, 1, 11, 0),
            new BigDecimal("10000"));
    Reservation confirmed = pending.withId(1L).confirm();

    assertThat(pending.canCancel()).isTrue();
    assertThat(confirmed.canCancel()).isFalse();
  }
}
