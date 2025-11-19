package com.wiseai.assignment.modules.payment.application.port.in.command;

import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;

public interface CancelPaymentUseCase {
  PaymentResponse cancelPayment(Long id);
}
