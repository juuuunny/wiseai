package com.wiseai.assignment.modules.payment.application.dto.request;

import java.math.BigDecimal;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 결제 생성을 위한 요청 객체
 *
 * @param reservationId 예약 ID
 * @param paymentMethod 결제 수단
 * @param amount 결제 금액
 */
public record CreatePaymentRequest(
    @NotNull(message = "예약 ID는 필수입니다.") @Min(value = 1, message = "예약 ID는 1 이상이어야 합니다.")
        Long reservationId,
    @NotNull(message = "결제 수단은 필수입니다.") PaymentMethod paymentMethod,
    @NotNull(message = "결제 금액은 필수입니다.")
        @DecimalMin(value = "0.0", inclusive = false, message = "결제 금액은 0보다 커야 합니다.")
        BigDecimal amount) {}
