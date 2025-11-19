package com.wiseai.assignment.modules.payment.application.service.event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.wiseai.assignment.modules.payment.application.event.PaymentCancelRequestMessage;
import com.wiseai.assignment.modules.payment.application.port.out.command.PaymentCommandPort;
import com.wiseai.assignment.modules.payment.application.port.out.gateway.PaymentGateway;
import com.wiseai.assignment.modules.payment.application.port.out.query.PaymentQueryPort;
import com.wiseai.assignment.modules.payment.application.service.gateway.PaymentGatewayFactory;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.model.Payment;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCancelListener 테스트")
class PaymentCancelListenerTest {

  @Mock private PaymentQueryPort paymentQueryPort;
  @Mock private PaymentCommandPort paymentCommandPort;
  @Mock private PaymentGatewayFactory paymentGatewayFactory;
  @Mock private PaymentGateway paymentGateway;
  @Mock private PaymentCancelLogService paymentCancelLogService;

  @InjectMocks private PaymentCancelListener paymentCancelListener;

  private static final Long DEFAULT_PAYMENT_ID = 1L;
  private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("10000");
  private static final String DEFAULT_TRANSACTION_ID = "txn-1";

  @Test
  @DisplayName("결제 취소 성공")
  void handleCancellation_success() {
    Payment payment =
        Payment.create(1L, PaymentMethod.TOSS, DEFAULT_AMOUNT)
            .withId(DEFAULT_PAYMENT_ID)
            .complete(DEFAULT_TRANSACTION_ID);

    PaymentCancelRequestMessage message =
        new PaymentCancelRequestMessage(
            "event-1",
            DEFAULT_PAYMENT_ID,
            PaymentMethod.TOSS,
            DEFAULT_TRANSACTION_ID,
            Instant.now());

    given(paymentCancelLogService.isProcessed("event-1")).willReturn(false);
    given(paymentQueryPort.findById(DEFAULT_PAYMENT_ID)).willReturn(Optional.of(payment));
    given(paymentGatewayFactory.getGateway(PaymentMethod.TOSS)).willReturn(paymentGateway);
    given(paymentGateway.cancelPayment(DEFAULT_TRANSACTION_ID))
        .willReturn(CompletableFuture.completedFuture(true));

    paymentCancelListener.handleCancellation(message);

    verify(paymentCommandPort).update(any(Payment.class));
    verify(paymentCancelLogService).markProcessed("event-1", DEFAULT_PAYMENT_ID, PaymentMethod.TOSS);
  }

  @Test
  @DisplayName("이미 처리된 이벤트 무시")
  void handleCancellation_duplicate() {
    PaymentCancelRequestMessage message =
        new PaymentCancelRequestMessage(
            "event-dup",
            DEFAULT_PAYMENT_ID,
            PaymentMethod.TOSS,
            DEFAULT_TRANSACTION_ID,
            Instant.now());

    given(paymentCancelLogService.isProcessed("event-dup")).willReturn(true);

    paymentCancelListener.handleCancellation(message);

    verify(paymentGatewayFactory, never()).getGateway(PaymentMethod.TOSS);
    verify(paymentCommandPort, never()).update(any());
  }

  @Test
  @DisplayName("PG 취소 실패 시 예외 발생")
  void handleCancellation_fail() {
    Payment payment =
        Payment.create(1L, PaymentMethod.TOSS, DEFAULT_AMOUNT)
            .withId(DEFAULT_PAYMENT_ID)
            .complete(DEFAULT_TRANSACTION_ID);

    PaymentCancelRequestMessage message =
        new PaymentCancelRequestMessage(
            "event-fail",
            DEFAULT_PAYMENT_ID,
            PaymentMethod.TOSS,
            DEFAULT_TRANSACTION_ID,
            Instant.now());

    given(paymentCancelLogService.isProcessed("event-fail")).willReturn(false);
    given(paymentQueryPort.findById(DEFAULT_PAYMENT_ID)).willReturn(Optional.of(payment));
    given(paymentGatewayFactory.getGateway(PaymentMethod.TOSS)).willReturn(paymentGateway);
    given(paymentGateway.cancelPayment(DEFAULT_TRANSACTION_ID))
        .willReturn(CompletableFuture.completedFuture(false));

    assertThatThrownBy(() -> paymentCancelListener.handleCancellation(message))
        .isInstanceOf(RuntimeException.class);
  }
}

