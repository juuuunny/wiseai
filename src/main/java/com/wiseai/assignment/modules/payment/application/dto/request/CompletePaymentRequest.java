package com.wiseai.assignment.modules.payment.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 결제 완료를 위한 요청 객체
 *
 * @param transactionId 거래 ID
 */
public record CompletePaymentRequest(@NotBlank(message = "거래 ID는 필수입니다.") String transactionId) {}
