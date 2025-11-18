package com.wiseai.assignment.modules.reservation.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.wiseai.assignment.modules.reservation.domain.enums.ReservationStatus;
import com.wiseai.assignment.modules.reservation.domain.exception.ReservationException;
import com.wiseai.assignment.modules.reservation.domain.status.ReservationErrorStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Reservation {

  private final Long id;
  private final Long meetingRoomId;
  private final Long userId;
  private final LocalDateTime startTime;
  private final LocalDateTime endTime;
  private final ReservationStatus status;
  private final BigDecimal totalAmount;

  /**
   * 예약을 생성합니다.
   *
   * @param meetingRoomId 회의실 ID
   * @param userId 사용자 ID
   * @param startTime 시작 시간
   * @param endTime 종료 시간
   * @param totalAmount 총 결제 금액
   * @return 생성된 Reservation 객체
   */
  public static Reservation create(
      Long meetingRoomId,
      Long userId,
      LocalDateTime startTime,
      LocalDateTime endTime,
      BigDecimal totalAmount) {
    validateMeetingRoomId(meetingRoomId);
    validateUserId(userId);
    validateTimeRange(startTime, endTime);
    validateTimeUnit(startTime, endTime);
    validateTotalAmount(totalAmount);

    return Reservation.builder()
        .meetingRoomId(meetingRoomId)
        .userId(userId)
        .startTime(startTime)
        .endTime(endTime)
        .status(ReservationStatus.PENDING)
        .totalAmount(totalAmount)
        .build();
  }

  /**
   * ID를 포함한 Reservation 객체를 생성합니다.
   *
   * @param id 예약 ID
   * @return ID가 포함된 Reservation 객체
   */
  public Reservation withId(Long id) {
    return Reservation.builder()
        .id(id)
        .meetingRoomId(meetingRoomId)
        .userId(userId)
        .startTime(startTime)
        .endTime(endTime)
        .status(status)
        .totalAmount(totalAmount)
        .build();
  }

  /**
   * 예약을 취소합니다.
   *
   * @return 취소된 Reservation 객체
   */
  public Reservation cancel() {
    if (status == ReservationStatus.CANCELLED) {
      throw new ReservationException(ReservationErrorStatus.INVALID_CANCEL_STATUS);
    }
    if (status == ReservationStatus.CONFIRMED) {
      throw new ReservationException(ReservationErrorStatus.INVALID_CANCEL_STATUS);
    }

    return Reservation.builder()
        .id(id)
        .meetingRoomId(meetingRoomId)
        .userId(userId)
        .startTime(startTime)
        .endTime(endTime)
        .status(ReservationStatus.CANCELLED)
        .totalAmount(totalAmount)
        .build();
  }

  /**
   * 예약을 확정합니다 (결제 완료 후).
   *
   * @return 확정된 Reservation 객체
   */
  public Reservation confirm() {
    if (status != ReservationStatus.PENDING) {
      throw new ReservationException(ReservationErrorStatus.INVALID_CANCEL_STATUS);
    }

    return Reservation.builder()
        .id(id)
        .meetingRoomId(meetingRoomId)
        .userId(userId)
        .startTime(startTime)
        .endTime(endTime)
        .status(ReservationStatus.CONFIRMED)
        .totalAmount(totalAmount)
        .build();
  }

  /**
   * 다른 예약과 시간이 겹치는지 확인합니다.
   *
   * @param other 다른 예약
   * @return 겹치면 true, 아니면 false
   */
  public boolean overlapsWith(Reservation other) {
    if (other == null) {
      return false;
    }
    if (!meetingRoomId.equals(other.meetingRoomId)) {
      return false;
    }
    return startTime.isBefore(other.endTime) && other.startTime.isBefore(endTime);
  }

  /**
   * 예약이 취소 가능한 상태인지 확인합니다.
   *
   * @return 취소 가능하면 true, 아니면 false
   */
  public boolean canCancel() {
    return status == ReservationStatus.PENDING;
  }

  private static void validateMeetingRoomId(Long meetingRoomId) {
    if (meetingRoomId == null || meetingRoomId < 1) {
      throw new ReservationException(ReservationErrorStatus.INVALID_MEETING_ROOM);
    }
  }

  private static void validateUserId(Long userId) {
    if (userId == null || userId < 1) {
      throw new ReservationException(ReservationErrorStatus.INVALID_USER);
    }
  }

  private static void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
    if (startTime == null || endTime == null) {
      throw new ReservationException(ReservationErrorStatus.INVALID_START_TIME);
    }
    if (!startTime.isBefore(endTime)) {
      throw new ReservationException(ReservationErrorStatus.INVALID_START_TIME);
    }
  }

  private static void validateTimeUnit(LocalDateTime startTime, LocalDateTime endTime) {
    if (startTime == null || endTime == null) {
      throw new ReservationException(ReservationErrorStatus.INVALID_TIME_UNIT);
    }
    int startMinute = startTime.getMinute();
    int endMinute = endTime.getMinute();

    if (startMinute != 0 && startMinute != 30) {
      throw new ReservationException(ReservationErrorStatus.INVALID_TIME_UNIT);
    }
    if (endMinute != 0 && endMinute != 30) {
      throw new ReservationException(ReservationErrorStatus.INVALID_TIME_UNIT);
    }
  }

  private static void validateTotalAmount(BigDecimal totalAmount) {
    if (totalAmount == null || totalAmount.signum() < 0) {
      throw new ReservationException(ReservationErrorStatus.INVALID_TOTAL_AMOUNT);
    }
  }
}
