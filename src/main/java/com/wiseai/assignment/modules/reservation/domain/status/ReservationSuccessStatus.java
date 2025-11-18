package com.wiseai.assignment.modules.reservation.domain.status;

import org.springframework.http.HttpStatus;

import com.wiseai.assignment.modules.common.status.BaseSuccessCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationSuccessStatus implements BaseSuccessCode {
  OK_CREATE_RESERVATION(HttpStatus.CREATED, "RESERVATION-001", "예약 생성에 성공했습니다."),
  OK_GET_RESERVATION(HttpStatus.OK, "RESERVATION-002", "예약 조회에 성공했습니다."),
  OK_GET_RESERVATIONS(HttpStatus.OK, "RESERVATION-003", "예약 목록 조회에 성공했습니다."),
  OK_CANCEL_RESERVATION(HttpStatus.OK, "RESERVATION-004", "예약 취소에 성공했습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
