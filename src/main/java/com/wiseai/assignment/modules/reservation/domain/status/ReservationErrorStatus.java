package com.wiseai.assignment.modules.reservation.domain.status;

import org.springframework.http.HttpStatus;

import com.wiseai.assignment.modules.common.status.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 예약 도메인에서 공통으로 사용하는 에러 코드. */
@Getter
@RequiredArgsConstructor
public enum ReservationErrorStatus implements BaseErrorCode {
  INVALID_START_TIME(HttpStatus.BAD_REQUEST, "RESERVATION-001", "시작 시간은 종료 시간보다 이전이어야 합니다."),
  INVALID_TIME_UNIT(HttpStatus.BAD_REQUEST, "RESERVATION-002", "예약 시간은 정시(00분) 또는 30분 단위로만 가능합니다."),
  DUPLICATE_RESERVATION(HttpStatus.CONFLICT, "RESERVATION-003", "해당 시간대에 이미 예약이 존재합니다."),
  INVALID_MEETING_ROOM(HttpStatus.BAD_REQUEST, "RESERVATION-004", "유효하지 않은 회의실입니다."),
  INVALID_USER(HttpStatus.BAD_REQUEST, "RESERVATION-005", "유효하지 않은 사용자입니다."),
  INVALID_TOTAL_AMOUNT(HttpStatus.BAD_REQUEST, "RESERVATION-008", "총 결제 금액은 0 이상이어야 합니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION-006", "예약을 찾을 수 없습니다."),
  INVALID_CANCEL_STATUS(HttpStatus.BAD_REQUEST, "RESERVATION-007", "취소할 수 없는 예약 상태입니다."),
  UNAUTHORIZED(HttpStatus.FORBIDDEN, "RESERVATION-009", "본인의 예약만 취소할 수 있습니다."),
  INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "RESERVATION-010", "결제 가능한 예약 상태가 아닙니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
