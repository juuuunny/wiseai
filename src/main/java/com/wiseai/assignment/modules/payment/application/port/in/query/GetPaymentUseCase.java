package com.wiseai.assignment.modules.payment.application.port.in.query;

import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;

public interface GetPaymentUseCase {
  PaymentResponse getPayment(Long id);
}
