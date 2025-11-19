package com.wiseai.assignment.modules.payment.application.port.in.webhook;

import java.math.BigDecimal;

public interface HandlePaymentWebhookUseCase {

  void handleTossWebhook(
      String paymentKey,
      String orderId,
      String status,
      BigDecimal totalAmount,
      String transactionId,
      String failureCode,
      String failureMessage);

  void handleKakaoWebhook(
      String paymentKey,
      String orderId,
      String status,
      BigDecimal totalAmount,
      String transactionId,
      String failureCode,
      String failureMessage);
}
