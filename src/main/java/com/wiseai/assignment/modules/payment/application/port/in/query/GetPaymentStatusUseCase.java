package com.wiseai.assignment.modules.payment.application.port.in.query;

import com.wiseai.assignment.modules.payment.application.dto.response.PaymentStatusResponse;

public interface GetPaymentStatusUseCase {
  PaymentStatusResponse getPaymentStatus(Long paymentId);
}
