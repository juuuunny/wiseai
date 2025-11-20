package com.wiseai.assignment.modules.payment.domain.enums;

public enum PaymentStatus {
  PENDING,
  SUCCESS,
  COMPLETED, // SUCCESS와 동일한 의미 (하위 호환성 유지)
  FAILED,
  CANCELLED
}
