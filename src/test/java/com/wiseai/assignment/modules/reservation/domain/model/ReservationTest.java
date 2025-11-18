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

  private static final int TEST_YEAR = 2024;
  private static final int TEST_MONTH = 1;
  private static final int TEST_DAY = 1;

  // 공통 테스트 데이터
  private static final Long DEFAULT_MEETING_ROOM_ID = 1L;
  private static final Long DEFAULT_USER_ID = 1L;
  private static final Long DEFAULT_RESERVATION_ID = 1L;
  private static final LocalDateTime DEFAULT_START_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 10, 0);
  private static final LocalDateTime DEFAULT_END_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 11, 0);
  private static final BigDecimal DEFAULT_TOTAL_AMOUNT = new BigDecimal("10000");
  private static final BigDecimal SECOND_TOTAL_AMOUNT = new BigDecimal("15000");

  @Test
  @DisplayName("예약 생성 성공")
  void createReservation_success() {
    // given
    Long meetingRoomId = DEFAULT_MEETING_ROOM_ID;
    Long userId = DEFAULT_USER_ID;
    LocalDateTime startTime = DEFAULT_START_TIME;
    LocalDateTime endTime = DEFAULT_END_TIME;
    BigDecimal totalAmount = DEFAULT_TOTAL_AMOUNT;

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
    Long userId = DEFAULT_USER_ID;
    LocalDateTime startTime = DEFAULT_START_TIME;
    LocalDateTime endTime = DEFAULT_END_TIME;
    BigDecimal totalAmount = DEFAULT_TOTAL_AMOUNT;

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
    Long meetingRoomId = DEFAULT_MEETING_ROOM_ID;
    Long invalidUserId = -1L;
    LocalDateTime startTime = DEFAULT_START_TIME;
    LocalDateTime endTime = DEFAULT_END_TIME;
    BigDecimal totalAmount = DEFAULT_TOTAL_AMOUNT;

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
    Long meetingRoomId = DEFAULT_MEETING_ROOM_ID;
    Long userId = DEFAULT_USER_ID;
    LocalDateTime startTime = LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 10, 0);
    BigDecimal totalAmount = DEFAULT_TOTAL_AMOUNT;

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
    Long meetingRoomId = DEFAULT_MEETING_ROOM_ID;
    Long userId = DEFAULT_USER_ID;
    LocalDateTime startTime = DEFAULT_START_TIME;
    LocalDateTime endTime = DEFAULT_START_TIME;
    BigDecimal totalAmount = DEFAULT_TOTAL_AMOUNT;

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
    Long meetingRoomId = DEFAULT_MEETING_ROOM_ID;
    Long userId = DEFAULT_USER_ID;
    LocalDateTime startTime = LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 10, 15);
    LocalDateTime endTime = DEFAULT_END_TIME;
    BigDecimal totalAmount = DEFAULT_TOTAL_AMOUNT;

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
    Long meetingRoomId = DEFAULT_MEETING_ROOM_ID;
    Long userId = DEFAULT_USER_ID;
    LocalDateTime startTime = DEFAULT_START_TIME;
    LocalDateTime endTime = LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 11, 45);
    BigDecimal totalAmount = DEFAULT_TOTAL_AMOUNT;

    // when & then
    assertThatThrownBy(
            () -> Reservation.create(meetingRoomId, userId, startTime, endTime, totalAmount))
        .isInstanceOf(ReservationException.class)
        .hasMessage(ReservationErrorStatus.INVALID_TIME_UNIT.getMessage());
  }

  @Test
  @DisplayName("예약 생성 성공 - 30분 단위 시간")
  void createReservation_success_30minUnit() {
    LocalDateTime startTime = LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 10, 30);
    LocalDateTime endTime = LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 11, 30);

    Reservation reservation =
        Reservation.create(
            DEFAULT_MEETING_ROOM_ID, DEFAULT_USER_ID, startTime, endTime, DEFAULT_TOTAL_AMOUNT);

    assertThat(reservation.getStartTime()).isEqualTo(startTime);
    assertThat(reservation.getEndTime()).isEqualTo(endTime);
  }

  @Test
  @DisplayName("예약 생성 실패 - 잘못된 총 금액")
  void createReservation_fail_invalidTotalAmount() {
    // given
    Long meetingRoomId = DEFAULT_MEETING_ROOM_ID;
    Long userId = DEFAULT_USER_ID;
    LocalDateTime startTime = DEFAULT_START_TIME;
    LocalDateTime endTime = DEFAULT_END_TIME;
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
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);
    Reservation reservationWithId = reservation.withId(DEFAULT_RESERVATION_ID);

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
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);
    Reservation cancelled = reservation.withId(DEFAULT_RESERVATION_ID).cancel();

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
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);
    Reservation confirmed = reservation.withId(DEFAULT_RESERVATION_ID).confirm();

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
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);
    Reservation reservationWithId = reservation.withId(DEFAULT_RESERVATION_ID);

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
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);
    Reservation confirmed = reservation.withId(DEFAULT_RESERVATION_ID).confirm();

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
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);
    Reservation reservation2 =
        Reservation.create(
            DEFAULT_MEETING_ROOM_ID,
            2L,
            LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 10, 30),
            LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 11, 30),
            SECOND_TOTAL_AMOUNT);

    assertThat(reservation1.overlapsWith(reservation2)).isTrue();
  }

  @Test
  @DisplayName("예약 중복 확인 - 겹치지 않는 시간대")
  void overlapsWith_notOverlapping() {
    Reservation reservation1 =
        Reservation.create(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);
    Reservation reservation2 =
        Reservation.create(
            DEFAULT_MEETING_ROOM_ID,
            2L,
            LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 11, 0),
            LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 12, 0),
            SECOND_TOTAL_AMOUNT);

    assertThat(reservation1.overlapsWith(reservation2)).isFalse();
  }

  @Test
  @DisplayName("예약 중복 확인 - 다른 회의실")
  void overlapsWith_differentMeetingRoom() {
    Reservation reservation1 =
        Reservation.create(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);
    Reservation reservation2 =
        Reservation.create(2L, 2L, DEFAULT_START_TIME, DEFAULT_END_TIME, SECOND_TOTAL_AMOUNT);

    assertThat(reservation1.overlapsWith(reservation2)).isFalse();
  }

  @Test
  @DisplayName("예약 중복 확인 - null 파라미터")
  void overlapsWith_null() {
    Reservation reservation =
        Reservation.create(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);

    assertThat(reservation.overlapsWith(null)).isFalse();
  }

  @Test
  @DisplayName("예약 취소 가능 여부 확인")
  void canCancel() {
    Reservation pending =
        Reservation.create(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);
    Reservation confirmed = pending.withId(DEFAULT_RESERVATION_ID).confirm();

    assertThat(pending.canCancel()).isTrue();
    assertThat(confirmed.canCancel()).isFalse();
  }
}
