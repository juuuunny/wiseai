package com.wiseai.assignment.modules.payment.domain.model;

import java.math.BigDecimal;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentStatus;

import lombok.Builder;
import lombok.Getter;

/**
 * 결제 결과 공통 모델
 *
 * <p>각 결제사별로 다른 응답을 이 공통 모델로 변환하여 사용합니다.
 */
@Getter
@Builder
public class PaymentResult {
  private final String transactionId;
  private final PaymentStatus status;
  private final BigDecimal amount;
  private final String message;

  /**
   * 결제 성공 결과를 생성합니다.
   *
   * @param transactionId 거래 ID
   * @param amount 결제 금액
   * @return PaymentResult
   */
  public static PaymentResult success(String transactionId, BigDecimal amount) {
    return PaymentResult.builder()
        .transactionId(transactionId)
        .status(PaymentStatus.SUCCESS)
        .amount(amount)
        .message("결제가 성공적으로 처리되었습니다.")
        .build();
  }

  /**
   * 결제 실패 결과를 생성합니다.
   *
   * @param message 실패 메시지
   * @return PaymentResult
   */
  public static PaymentResult failure(String message) {
    return PaymentResult.builder()
        .transactionId(null)
        .status(PaymentStatus.FAILED)
        .amount(null)
        .message(message)
        .build();
  }

  /**
   * 결제가 성공했는지 확인합니다.
   *
   * @return 성공 여부
   */
  public boolean isSuccess() {
    return status == PaymentStatus.SUCCESS && transactionId != null;
  }
}
