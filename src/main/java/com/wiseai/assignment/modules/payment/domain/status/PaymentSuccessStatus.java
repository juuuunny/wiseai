package com.wiseai.assignment.modules.payment.domain.status;

import org.springframework.http.HttpStatus;

import com.wiseai.assignment.modules.common.status.BaseSuccessCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentSuccessStatus implements BaseSuccessCode {
  OK_CREATE_PAYMENT(HttpStatus.CREATED, "PAYMENT-001", "결제 생성에 성공했습니다."),
  OK_COMPLETE_PAYMENT(HttpStatus.OK, "PAYMENT-002", "결제 완료에 성공했습니다."),
  OK_CANCEL_PAYMENT(HttpStatus.OK, "PAYMENT-003", "결제 취소에 성공했습니다."),
  OK_GET_PAYMENT(HttpStatus.OK, "PAYMENT-004", "결제 조회에 성공했습니다."),
  OK_GET_PAYMENTS(HttpStatus.OK, "PAYMENT-005", "결제 목록 조회에 성공했습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
