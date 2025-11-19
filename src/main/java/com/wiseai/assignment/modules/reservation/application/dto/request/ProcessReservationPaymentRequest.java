package com.wiseai.assignment.modules.reservation.application.dto.request;

import jakarta.validation.constraints.NotNull;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;

public record ProcessReservationPaymentRequest(@NotNull PaymentMethod paymentMethod) {}
