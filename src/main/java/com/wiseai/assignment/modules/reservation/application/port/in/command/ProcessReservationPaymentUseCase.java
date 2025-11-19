package com.wiseai.assignment.modules.reservation.application.port.in.command;

import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;

public interface ProcessReservationPaymentUseCase {
  PaymentResponse processPayment(Long reservationId, PaymentMethod paymentMethod);
}

