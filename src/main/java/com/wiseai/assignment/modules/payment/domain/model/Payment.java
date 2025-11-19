package com.wiseai.assignment.modules.payment.domain.model;

import java.math.BigDecimal;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentStatus;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Payment {

  private final Long id;
  private final Long reservationId;
  private final PaymentMethod paymentMethod;
  private final BigDecimal amount;
  private final PaymentStatus status;
  private final String transactionId;

  public Payment complete(String transactionId) {
    if (status != PaymentStatus.PENDING) {
      throw new PaymentException(PaymentErrorStatus.INVALID_STATUS);
    }
    if (transactionId == null || transactionId.isBlank()) {
      throw new PaymentException(PaymentErrorStatus.INVALID_TRANSACTION_ID);
    }

    return Payment.builder()
        .id(id)
        .reservationId(reservationId)
        .paymentMethod(paymentMethod)
        .amount(amount)
        .status(PaymentStatus.COMPLETED)
        .transactionId(transactionId)
        .build();
  }

  public Payment cancel() {
    if (status == PaymentStatus.CANCELLED) {
      throw new PaymentException(PaymentErrorStatus.INVALID_STATUS);
    }

    return Payment.builder()
        .id(id)
        .reservationId(reservationId)
        .paymentMethod(paymentMethod)
        .amount(amount)
        .status(PaymentStatus.CANCELLED)
        .transactionId(transactionId)
        .build();
  }

  public Payment fail() {
    if (status != PaymentStatus.PENDING) {
      throw new PaymentException(PaymentErrorStatus.INVALID_STATUS);
    }

    return Payment.builder()
        .id(id)
        .reservationId(reservationId)
        .paymentMethod(paymentMethod)
        .amount(amount)
        .status(PaymentStatus.FAILED)
        .transactionId(transactionId)
        .build();
  }
}
