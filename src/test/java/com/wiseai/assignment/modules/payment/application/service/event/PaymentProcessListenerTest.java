package com.wiseai.assignment.modules.payment.application.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentCaptor;
import com.wiseai.assignment.modules.payment.application.event.PaymentProcessMessage;
import com.wiseai.assignment.modules.payment.application.port.out.command.PaymentCommandPort;
import com.wiseai.assignment.modules.payment.application.port.out.gateway.PaymentGateway;
import com.wiseai.assignment.modules.payment.application.port.out.query.PaymentQueryPort;
import com.wiseai.assignment.modules.payment.application.service.gateway.PaymentGatewayFactory;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentStatus;
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
@DisplayName("PaymentProcessListener 테스트")
class PaymentProcessListenerTest {

  @Mock private PaymentQueryPort paymentQueryPort;
  @Mock private PaymentCommandPort paymentCommandPort;
  @Mock private PaymentGatewayFactory paymentGatewayFactory;
  @Mock private PaymentGateway paymentGateway;
  @Mock private PaymentProcessLogService paymentProcessLogService;
  @Mock private PaymentDlqProducer paymentDlqProducer;

  @InjectMocks private PaymentProcessListener paymentProcessListener;

  private static final Long DEFAULT_PAYMENT_ID = 1L;
  private static final Long DEFAULT_RESERVATION_ID = 1L;
  private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("10000");

  @Test
  @DisplayName("결제 처리 성공 시 상태가 COMPLETED로 변경된다")
  void handleMessage_success() {
    Payment payment = Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);
    Payment paymentWithId = payment.withId(DEFAULT_PAYMENT_ID);

    PaymentProcessMessage message =
        new PaymentProcessMessage(
            "event-1",
            DEFAULT_PAYMENT_ID,
            DEFAULT_RESERVATION_ID,
            PaymentMethod.TOSS,
            DEFAULT_AMOUNT,
            Instant.now());

    given(paymentQueryPort.findById(DEFAULT_PAYMENT_ID)).willReturn(Optional.of(paymentWithId));
    given(paymentGatewayFactory.getGateway(PaymentMethod.TOSS)).willReturn(paymentGateway);
    given(paymentGateway.processPayment(DEFAULT_AMOUNT, DEFAULT_RESERVATION_ID))
        .willReturn(CompletableFuture.completedFuture("txn-1"));
    given(paymentProcessLogService.isProcessed("event-1")).willReturn(false);

    paymentProcessListener.handleMessage(message);

    ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentCommandPort).update(captor.capture());
    Payment updated = captor.getValue();
    assertThat(updated.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    assertThat(updated.getTransactionId()).isEqualTo("txn-1");
    verify(paymentProcessLogService).markProcessed("event-1", DEFAULT_PAYMENT_ID);
  }

  @Test
  @DisplayName("중복 이벤트는 무시된다")
  void handleMessage_duplicate() {
    PaymentProcessMessage message =
        new PaymentProcessMessage(
            "event-dup",
            DEFAULT_PAYMENT_ID,
            DEFAULT_RESERVATION_ID,
            PaymentMethod.TOSS,
            DEFAULT_AMOUNT,
            Instant.now());

    Payment payment =
        Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT)
            .withId(DEFAULT_PAYMENT_ID);

    given(paymentQueryPort.findById(DEFAULT_PAYMENT_ID)).willReturn(Optional.of(payment));
    given(paymentProcessLogService.isProcessed("event-dup")).willReturn(true);

    paymentProcessListener.handleMessage(message);

    verify(paymentGatewayFactory, never()).getGateway(PaymentMethod.TOSS);
    verify(paymentCommandPort, never()).update(any());
  }

}

