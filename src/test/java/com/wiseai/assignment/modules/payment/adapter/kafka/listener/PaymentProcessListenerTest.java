package com.wiseai.assignment.modules.payment.adapter.kafka.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wiseai.assignment.modules.payment.application.event.PaymentProcessMessage;
import com.wiseai.assignment.modules.payment.application.port.out.command.PaymentCommandPort;
import com.wiseai.assignment.modules.payment.application.port.out.gateway.PaymentGateway;
import com.wiseai.assignment.modules.payment.application.port.out.query.PaymentQueryPort;
import com.wiseai.assignment.modules.payment.application.service.gateway.PaymentGatewayFactory;
import com.wiseai.assignment.modules.payment.application.service.infrastructure.PaymentProcessLogService;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentStatus;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.model.Payment;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentProcessListener 테스트")
class PaymentProcessListenerTest {

  @Mock private PaymentQueryPort paymentQueryPort;
  @Mock private PaymentCommandPort paymentCommandPort;
  @Mock private PaymentGatewayFactory paymentGatewayFactory;
  @Mock private PaymentGateway paymentGateway;
  @Mock private PaymentProcessLogService paymentProcessLogService;

  @InjectMocks private PaymentProcessListener paymentProcessListener;

  private static final Long DEFAULT_PAYMENT_ID = 1L;
  private static final Long DEFAULT_RESERVATION_ID = 1L;
  private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("10000");
  private static final String DEFAULT_EVENT_ID = "event-1";
  private static final String DEFAULT_TRANSACTION_ID = "txn-1";

  @Test
  @DisplayName("결제 처리 성공 시 상태가 COMPLETED로 변경되고 멱등 로그가 처리 완료된다")
  void handleMessage_success() {
    // given
    Payment payment = Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);
    Payment paymentWithId = payment.withId(DEFAULT_PAYMENT_ID);

    PaymentProcessMessage message =
        new PaymentProcessMessage(
            DEFAULT_EVENT_ID,
            DEFAULT_PAYMENT_ID,
            DEFAULT_RESERVATION_ID,
            PaymentMethod.TOSS,
            DEFAULT_AMOUNT,
            Instant.now());

    given(paymentQueryPort.findById(DEFAULT_PAYMENT_ID)).willReturn(Optional.of(paymentWithId));
    given(paymentProcessLogService.isProcessed(DEFAULT_EVENT_ID)).willReturn(false); // 아직 처리 안 됨
    given(paymentProcessLogService.tryAcquire(DEFAULT_EVENT_ID, DEFAULT_PAYMENT_ID))
        .willReturn(true); // 선점 성공
    given(paymentGatewayFactory.getGateway(PaymentMethod.TOSS)).willReturn(paymentGateway);
    given(paymentGateway.processPayment(DEFAULT_AMOUNT, DEFAULT_RESERVATION_ID))
        .willReturn(CompletableFuture.completedFuture(DEFAULT_TRANSACTION_ID));

    // when
    paymentProcessListener.handleMessage(message);

    // then
    ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentCommandPort).update(captor.capture());
    Payment updated = captor.getValue();
    assertThat(updated.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    assertThat(updated.getTransactionId()).isEqualTo(DEFAULT_TRANSACTION_ID);
    verify(paymentProcessLogService).markProcessed(DEFAULT_EVENT_ID, DEFAULT_PAYMENT_ID);
    verify(paymentProcessLogService, never()).release(any()); // 성공 시 release 호출 안 됨
  }

  @Test
  @DisplayName("중복 이벤트는 무시된다")
  void handleMessage_duplicate() {
    // given
    Payment payment = Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);
    Payment paymentWithId = payment.withId(DEFAULT_PAYMENT_ID);

    PaymentProcessMessage message =
        new PaymentProcessMessage(
            DEFAULT_EVENT_ID,
            DEFAULT_PAYMENT_ID,
            DEFAULT_RESERVATION_ID,
            PaymentMethod.TOSS,
            DEFAULT_AMOUNT,
            Instant.now());

    given(paymentQueryPort.findById(DEFAULT_PAYMENT_ID)).willReturn(Optional.of(paymentWithId));
    given(paymentProcessLogService.isProcessed(DEFAULT_EVENT_ID)).willReturn(true); // 이미 처리됨

    // when
    paymentProcessListener.handleMessage(message);

    // then
    verify(paymentProcessLogService, never()).tryAcquire(any(), any());
    verify(paymentGatewayFactory, never()).getGateway(any());
    verify(paymentCommandPort, never()).update(any());
    verify(paymentProcessLogService, never()).markProcessed(any(), any());
  }

  @Test
  @DisplayName("결제 처리 실패 시 멱등 로그가 해제되고 예외가 다시 던져진다")
  void handleMessage_fail_rethrowsException() {
    // given
    Payment payment = Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);
    Payment paymentWithId = payment.withId(DEFAULT_PAYMENT_ID);

    PaymentProcessMessage message =
        new PaymentProcessMessage(
            DEFAULT_EVENT_ID,
            DEFAULT_PAYMENT_ID,
            DEFAULT_RESERVATION_ID,
            PaymentMethod.TOSS,
            DEFAULT_AMOUNT,
            Instant.now());

    given(paymentQueryPort.findById(DEFAULT_PAYMENT_ID)).willReturn(Optional.of(paymentWithId));
    given(paymentProcessLogService.isProcessed(DEFAULT_EVENT_ID)).willReturn(false);
    given(paymentProcessLogService.tryAcquire(DEFAULT_EVENT_ID, DEFAULT_PAYMENT_ID))
        .willReturn(true);
    given(paymentGatewayFactory.getGateway(PaymentMethod.TOSS)).willReturn(paymentGateway);
    given(paymentGateway.processPayment(DEFAULT_AMOUNT, DEFAULT_RESERVATION_ID))
        .willReturn(CompletableFuture.failedFuture(new RuntimeException("PG Error")));

    // when & then
    assertThatThrownBy(() -> paymentProcessListener.handleMessage(message))
        .isInstanceOf(PaymentException.class)
        .satisfies(
            e ->
                assertThat(((PaymentException) e).getErrorCode())
                    .isEqualTo(PaymentErrorStatus.PAYMENT_GATEWAY_ERROR));

    verify(paymentProcessLogService).release(DEFAULT_EVENT_ID); // 실패 시 release 호출
    verify(paymentCommandPort, never()).update(any()); // 업데이트 호출 안 됨
    verify(paymentProcessLogService, never()).markProcessed(any(), any()); // 마킹 호출 안 됨
  }

  @Test
  @DisplayName("결제를 찾을 수 없는 경우 예외가 발생한다")
  void handleMessage_fail_notFound() {
    // given
    PaymentProcessMessage message =
        new PaymentProcessMessage(
            DEFAULT_EVENT_ID,
            DEFAULT_PAYMENT_ID,
            DEFAULT_RESERVATION_ID,
            PaymentMethod.TOSS,
            DEFAULT_AMOUNT,
            Instant.now());

    given(paymentQueryPort.findById(DEFAULT_PAYMENT_ID)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> paymentProcessListener.handleMessage(message))
        .isInstanceOf(PaymentException.class)
        .satisfies(
            e ->
                assertThat(((PaymentException) e).getErrorCode())
                    .isEqualTo(PaymentErrorStatus.NOT_FOUND));

    verify(paymentProcessLogService, never()).tryAcquire(any(), any());
    verify(paymentProcessLogService, never()).release(any());
    verify(paymentCommandPort, never()).update(any());
  }
}
