package com.wiseai.assignment.modules.payment.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentStatus;

class PaymentTest {

  private static final Long DEFAULT_RESERVATION_ID = 1L;
  private static final Long DEFAULT_PAYMENT_ID = 1L;
  private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("10000");

  @Test
  @DisplayName("결제 생성 성공")
  void createPayment_success() {
    // when
    Payment payment = Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);

    // then
    assertThat(payment.getReservationId()).isEqualTo(DEFAULT_RESERVATION_ID);
    assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.TOSS);
    assertThat(payment.getAmount()).isEqualByComparingTo(DEFAULT_AMOUNT);
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
  }

  @Test
  @DisplayName("결제 완료 성공")
  void completePayment_success() {
    // given
    Payment payment = Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);
    Payment paymentWithId = payment.withId(DEFAULT_PAYMENT_ID);
    String transactionId = "txn_12345";

    // when
    Payment completed = paymentWithId.complete(transactionId);

    // then
    assertThat(completed.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    assertThat(completed.getTransactionId()).isEqualTo(transactionId);
  }

  @Test
  @DisplayName("결제 실패 성공")
  void failPayment_success() {
    // given
    Payment payment = Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);
    Payment paymentWithId = payment.withId(DEFAULT_PAYMENT_ID);

    // when
    Payment failed = paymentWithId.fail();

    // then
    assertThat(failed.getStatus()).isEqualTo(PaymentStatus.FAILED);
  }
}
