package com.wiseai.assignment.modules.payment.application.dto.response;

import java.math.BigDecimal;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentStatus;

/**
 * 결제 응답 객체
 *
 * @param id 결제 ID
 * @param reservationId 예약 ID
 * @param paymentMethod 결제 수단
 * @param amount 결제 금액
 * @param status 결제 상태
 * @param transactionId 거래 ID
 */
public record PaymentResponse(
    Long id,
    Long reservationId,
    PaymentMethod paymentMethod,
    BigDecimal amount,
    PaymentStatus status,
    String transactionId) {}
