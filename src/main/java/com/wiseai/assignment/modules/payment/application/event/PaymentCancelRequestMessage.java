package com.wiseai.assignment.modules.payment.application.event;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import java.time.Instant;

public record PaymentCancelRequestMessage(
    String eventId,
    Long paymentId,
    PaymentMethod paymentMethod,
    String transactionId,
    Instant createdAt) {}

