package com.wiseai.assignment.modules.reservation.application.dto.request;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;

import jakarta.validation.constraints.NotNull;

public record ProcessReservationPaymentRequest(@NotNull PaymentMethod paymentMethod) {}

