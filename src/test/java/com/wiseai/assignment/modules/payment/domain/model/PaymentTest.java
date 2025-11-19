package com.wiseai.assignment.modules.payment.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentStatus;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;

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

  @Test
  @DisplayName("결제 생성 실패 - 잘못된 예약 ID")
  void createPayment_fail_invalidReservationId() {
    // when & then
    assertThatThrownBy(() -> Payment.create(0L, PaymentMethod.TOSS, DEFAULT_AMOUNT))
        .isInstanceOf(PaymentException.class)
        .hasMessage(PaymentErrorStatus.INVALID_RESERVATION_ID.getMessage());
  }

  @Test
  @DisplayName("결제 생성 실패 - 잘못된 금액")
  void createPayment_fail_invalidAmount() {
    // when & then
    assertThatThrownBy(
            () ->
                Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, new BigDecimal("-1000")))
        .isInstanceOf(PaymentException.class)
        .hasMessage(PaymentErrorStatus.INVALID_AMOUNT.getMessage());
  }

  @Test
  @DisplayName("결제 완료 실패 - 잘못된 상태")
  void completePayment_fail_invalidStatus() {
    // given
    Payment payment = Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);
    Payment completed = payment.withId(DEFAULT_PAYMENT_ID).complete("txn_123");

    // when & then
    assertThatThrownBy(() -> completed.complete("txn_456"))
        .isInstanceOf(PaymentException.class)
        .hasMessage(PaymentErrorStatus.INVALID_STATUS.getMessage());
  }

  @Test
  @DisplayName("결제 완료 실패 - 빈 거래 ID")
  void completePayment_fail_emptyTransactionId() {
    // given
    Payment payment = Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);
    Payment paymentWithId = payment.withId(DEFAULT_PAYMENT_ID);

    // when & then
    assertThatThrownBy(() -> paymentWithId.complete(""))
        .isInstanceOf(PaymentException.class)
        .hasMessage(PaymentErrorStatus.INVALID_TRANSACTION_ID.getMessage());
  }

  @Test
  @DisplayName("결제 취소 성공")
  void cancelPayment_success() {
    // given
    Payment payment = Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);
    Payment paymentWithId = payment.withId(DEFAULT_PAYMENT_ID);

    // when
    Payment cancelled = paymentWithId.cancel();

    // then
    assertThat(cancelled.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
  }

  @Test
  @DisplayName("결제 취소 성공 - 완료된 결제")
  void cancelPayment_success_completed() {
    Payment completed =
        Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT)
            .withId(DEFAULT_PAYMENT_ID)
            .complete("txn_123");

    Payment cancelled = completed.cancel();

    assertThat(cancelled.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
  }

  @Test
  @DisplayName("결제 취소 실패 - 이미 취소됨")
  void cancelPayment_fail_alreadyCancelled() {
    Payment cancelled =
        Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT)
            .withId(DEFAULT_PAYMENT_ID)
            .cancel();

    assertThatThrownBy(cancelled::cancel)
        .isInstanceOf(PaymentException.class)
        .hasMessage(PaymentErrorStatus.INVALID_STATUS.getMessage());
  }
}
