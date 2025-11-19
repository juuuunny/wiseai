package com.wiseai.assignment.modules.payment.application.port.in.query;

import java.util.List;

import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;

public interface GetPaymentsUseCase {
  List<PaymentResponse> getPaymentsByReservationId(Long reservationId);
}
