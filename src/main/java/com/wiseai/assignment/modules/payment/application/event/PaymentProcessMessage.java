package com.wiseai.assignment.modules.payment.application.event;

import java.math.BigDecimal;
import java.time.Instant;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;

public record PaymentProcessMessage(
    String eventId,
    Long paymentId,
    Long reservationId,
    PaymentMethod paymentMethod,
    BigDecimal amount,
    Instant createdAt) {}
