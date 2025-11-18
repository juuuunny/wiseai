package com.wiseai.assignment.modules.payment.domain.model;

import java.math.BigDecimal;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentStatus;

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
    return Payment.builder()
        .reservationId(reservationId)
        .paymentMethod(paymentMethod)
        .amount(amount)
        .status(PaymentStatus.PENDING)
        .build();
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
