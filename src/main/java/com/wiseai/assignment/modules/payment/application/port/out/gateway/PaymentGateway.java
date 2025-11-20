package com.wiseai.assignment.modules.payment.application.port.out.gateway;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.model.PaymentResult;

/**
 * 결제 게이트웨이 인터페이스 (Strategy Pattern)
 *
 * <p>각 결제사별로 다른 결제 처리 로직을 구현합니다. 각 결제사별 상이한 응답을 PaymentResult 공통 모델로 변환하여 반환합니다.
 */
public interface PaymentGateway {
  /**
   * 결제를 처리합니다.
   *
   * @param amount 결제 금액
   * @param orderId 주문 ID (예약 ID)
   * @return 결제 결과 (공통 모델)
   */
  CompletableFuture<PaymentResult> processPayment(BigDecimal amount, Long orderId);

  /**
   * 결제를 취소합니다.
   *
   * @param transactionId 거래 ID
   * @return 취소 성공 여부
   */
  CompletableFuture<Boolean> cancelPayment(String transactionId);

  /**
   * 이 게이트웨이가 지원하는 결제 수단을 반환합니다.
   *
   * @return 결제 수단
   */
  PaymentMethod getSupportedPaymentMethod();
}
