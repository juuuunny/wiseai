package com.wiseai.assignment.modules.payment.application.event;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentProcessMessage(
    String eventId,
    Long paymentId,
    Long reservationId,
    PaymentMethod paymentMethod,
    BigDecimal amount,
    Instant createdAt) {}

