package com.wiseai.assignment.modules.payment.application.port.in.command;

import java.math.BigDecimal;

import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;

public interface CreatePaymentUseCase {
  PaymentResponse createPayment(Long reservationId, PaymentMethod paymentMethod, BigDecimal amount);
}
