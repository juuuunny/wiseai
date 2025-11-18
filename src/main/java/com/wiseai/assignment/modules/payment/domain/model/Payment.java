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

  public static Payment create(Long reservationId, PaymentMethod paymentMethod, BigDecimal amount) {
    validateReservationId(reservationId);
    validateAmount(amount);
    validatePaymentMethod(paymentMethod);

    return Payment.builder()
        .reservationId(reservationId)
        .paymentMethod(paymentMethod)
        .amount(amount)
        .status(PaymentStatus.PENDING)
        .build();
  }

  private static void validateReservationId(Long reservationId) {
    if (reservationId == null || reservationId < 1) {
      throw new PaymentException(PaymentErrorStatus.INVALID_RESERVATION_ID);
    }
  }

  private static void validateAmount(BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) {
      throw new PaymentException(PaymentErrorStatus.INVALID_AMOUNT);
    }
  }

  private static void validatePaymentMethod(PaymentMethod paymentMethod) {
    if (paymentMethod == null) {
      throw new PaymentException(PaymentErrorStatus.INVALID_PAYMENT_METHOD);
    }
  }

  public Payment withId(Long id) {
    return Payment.builder()
        .id(id)
        .reservationId(reservationId)
        .paymentMethod(paymentMethod)
        .amount(amount)
        .status(status)
        .transactionId(transactionId)
        .build();
  }

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
