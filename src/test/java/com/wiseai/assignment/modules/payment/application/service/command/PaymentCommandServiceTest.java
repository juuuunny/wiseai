package com.wiseai.assignment.modules.payment.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;
import com.wiseai.assignment.modules.payment.application.port.out.command.PaymentCommandPort;
import com.wiseai.assignment.modules.payment.application.port.out.query.PaymentQueryPort;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentStatus;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.model.Payment;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCommandService 테스트")
class PaymentCommandServiceTest {

  @Mock private PaymentCommandPort paymentCommandPort;
  @Mock private PaymentQueryPort paymentQueryPort;

  @InjectMocks private PaymentCommandService paymentCommandService;

  private static final Long DEFAULT_RESERVATION_ID = 1L;
  private static final Long DEFAULT_PAYMENT_ID = 1L;
  private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("10000");
  private static final String DEFAULT_TRANSACTION_ID = "txn_12345";

  @Test
  @DisplayName("결제 생성 성공")
  void createPayment_success() {
    // given
    Payment payment = Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);
    Payment saved = payment.withId(DEFAULT_PAYMENT_ID);

    given(paymentCommandPort.save(any(Payment.class))).willReturn(saved);

    // when
    PaymentResponse result =
        paymentCommandService.createPayment(
            DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);

    // then
    assertThat(result.id()).isEqualTo(DEFAULT_PAYMENT_ID);
    assertThat(result.reservationId()).isEqualTo(DEFAULT_RESERVATION_ID);
    assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.TOSS);
    assertThat(result.amount()).isEqualByComparingTo(DEFAULT_AMOUNT);
    assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
  }

  @Test
  @DisplayName("결제 완료 성공")
  void completePayment_success() {
    // given
    Payment payment = Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);
    Payment paymentWithId = payment.withId(DEFAULT_PAYMENT_ID);
    Payment completed = paymentWithId.complete(DEFAULT_TRANSACTION_ID);

    given(paymentQueryPort.findById(DEFAULT_PAYMENT_ID)).willReturn(Optional.of(paymentWithId));
    given(paymentCommandPort.update(any(Payment.class))).willReturn(completed);

    // when
    PaymentResponse result =
        paymentCommandService.completePayment(DEFAULT_PAYMENT_ID, DEFAULT_TRANSACTION_ID);

    // then
    assertThat(result.id()).isEqualTo(DEFAULT_PAYMENT_ID);
    assertThat(result.status()).isEqualTo(PaymentStatus.COMPLETED);
    assertThat(result.transactionId()).isEqualTo(DEFAULT_TRANSACTION_ID);
  }

  @Test
  @DisplayName("결제 완료 실패 - 존재하지 않는 결제")
  void completePayment_fail_notFound() {
    // given
    given(paymentQueryPort.findById(DEFAULT_PAYMENT_ID)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
            () -> paymentCommandService.completePayment(DEFAULT_PAYMENT_ID, DEFAULT_TRANSACTION_ID))
        .isInstanceOf(PaymentException.class)
        .satisfies(
            exception -> {
              PaymentException ex = (PaymentException) exception;
              assertThat(ex.getErrorCode()).isEqualTo(PaymentErrorStatus.NOT_FOUND);
            });
  }

  @Test
  @DisplayName("결제 취소 성공")
  void cancelPayment_success() {
    // given
    Payment payment = Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);
    Payment paymentWithId = payment.withId(DEFAULT_PAYMENT_ID);
    Payment cancelled = paymentWithId.cancel();

    given(paymentQueryPort.findById(DEFAULT_PAYMENT_ID)).willReturn(Optional.of(paymentWithId));
    given(paymentCommandPort.update(any(Payment.class))).willReturn(cancelled);

    // when
    PaymentResponse result = paymentCommandService.cancelPayment(DEFAULT_PAYMENT_ID);

    // then
    assertThat(result.id()).isEqualTo(DEFAULT_PAYMENT_ID);
    assertThat(result.status()).isEqualTo(PaymentStatus.CANCELLED);
  }

  @Test
  @DisplayName("결제 취소 실패 - 존재하지 않는 결제")
  void cancelPayment_fail_notFound() {
    // given
    given(paymentQueryPort.findById(DEFAULT_PAYMENT_ID)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> paymentCommandService.cancelPayment(DEFAULT_PAYMENT_ID))
        .isInstanceOf(PaymentException.class)
        .satisfies(
            exception -> {
              PaymentException ex = (PaymentException) exception;
              assertThat(ex.getErrorCode()).isEqualTo(PaymentErrorStatus.NOT_FOUND);
            });
  }
}
