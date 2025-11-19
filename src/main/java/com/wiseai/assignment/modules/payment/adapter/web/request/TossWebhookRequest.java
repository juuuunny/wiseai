package com.wiseai.assignment.modules.payment.adapter.web.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TossWebhookRequest(
    @NotBlank(message = "결제 키는 필수입니다.") String paymentKey,
    @NotBlank(message = "주문 ID는 필수입니다.") String orderId,
    @NotBlank(message = "결제 상태는 필수입니다.") String status,
    @NotNull(message = "결제 금액은 필수입니다.") BigDecimal totalAmount,
    String transactionId,
    String failureCode,
    String failureMessage) {}
