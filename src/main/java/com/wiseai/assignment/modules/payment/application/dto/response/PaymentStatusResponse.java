package com.wiseai.assignment.modules.payment.application.dto.response;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentStatus;

public record PaymentStatusResponse(Long paymentId, PaymentStatus status, String transactionId) {}
