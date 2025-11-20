package com.wiseai.assignment.modules.payment.application.event;

import java.time.Instant;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;

public record PaymentCancelRequestMessage(
    String eventId,
    Long paymentId,
    PaymentMethod paymentMethod,
    String transactionId,
    Instant createdAt) {}
