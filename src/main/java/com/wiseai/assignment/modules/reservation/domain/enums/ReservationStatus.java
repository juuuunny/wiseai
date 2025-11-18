package com.wiseai.assignment.modules.reservation.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 예약 상태를 나타내는 열거형. */
@Getter
@RequiredArgsConstructor
public enum ReservationStatus {
  PENDING("결제 대기"),
  CONFIRMED("예약 확정"),
  CANCELLED("예약 취소");

  private final String description;
}
