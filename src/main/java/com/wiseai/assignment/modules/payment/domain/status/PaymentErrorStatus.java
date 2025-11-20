package com.wiseai.assignment.modules.payment.domain.status;

import org.springframework.http.HttpStatus;

import com.wiseai.assignment.modules.common.status.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorStatus implements BaseErrorCode {
  INVALID_RESERVATION_ID(HttpStatus.BAD_REQUEST, "PAYMENT-001", "유효하지 않은 예약 ID입니다."),
  INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "PAYMENT-002", "결제 금액은 0보다 커야 합니다."),
  INVALID_PAYMENT_METHOD(HttpStatus.BAD_REQUEST, "PAYMENT-003", "유효하지 않은 결제 수단입니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT-004", "결제를 찾을 수 없습니다."),
  INVALID_STATUS(HttpStatus.BAD_REQUEST, "PAYMENT-005", "유효하지 않은 결제 상태입니다."),
  INVALID_TRANSACTION_ID(HttpStatus.BAD_REQUEST, "PAYMENT-006", "유효하지 않은 거래 ID입니다."),
  PAYMENT_GATEWAY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT-007", "결제 게이트웨이 오류가 발생했습니다."),
  INVALID_REQUEST(HttpStatus.BAD_REQUEST, "PAYMENT-008", "잘못된 요청입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
