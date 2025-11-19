package com.wiseai.assignment.modules.payment.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;
import com.wiseai.assignment.modules.payment.application.port.out.query.PaymentQueryPort;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentStatus;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.model.Payment;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentQueryService 테스트")
class PaymentQueryServiceTest {

  @Mock private PaymentQueryPort paymentQueryPort;

  @InjectMocks private PaymentQueryService paymentQueryService;

  private static final Long DEFAULT_RESERVATION_ID = 1L;
  private static final Long DEFAULT_PAYMENT_ID = 1L;
  private static final Long SECOND_PAYMENT_ID = 2L;
  private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("10000");
  private static final BigDecimal SECOND_AMOUNT = new BigDecimal("15000");

  @Test
  @DisplayName("결제 단건 조회 성공")
  void getPayment_success() {
    // given
    Payment payment = Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);
    Payment paymentWithId = payment.withId(DEFAULT_PAYMENT_ID);

    given(paymentQueryPort.findById(DEFAULT_PAYMENT_ID)).willReturn(Optional.of(paymentWithId));

    // when
    PaymentResponse result = paymentQueryService.getPayment(DEFAULT_PAYMENT_ID);

    // then
    assertThat(result.id()).isEqualTo(DEFAULT_PAYMENT_ID);
    assertThat(result.reservationId()).isEqualTo(DEFAULT_RESERVATION_ID);
    assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.TOSS);
    assertThat(result.amount()).isEqualByComparingTo(DEFAULT_AMOUNT);
    assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
  }

  @Test
  @DisplayName("결제 단건 조회 실패 - 존재하지 않는 ID")
  void getPayment_notFound() {
    // given
    Long notFoundId = 999L;
    given(paymentQueryPort.findById(notFoundId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> paymentQueryService.getPayment(notFoundId))
        .isInstanceOf(PaymentException.class)
        .satisfies(
            exception -> {
              PaymentException ex = (PaymentException) exception;
              assertThat(ex.getErrorCode()).isEqualTo(PaymentErrorStatus.NOT_FOUND);
            });
  }

  @Test
  @DisplayName("예약별 결제 목록 조회 성공")
  void getPaymentsByReservationId_success() {
    // given
    Payment payment1 = Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);
    payment1 = payment1.withId(DEFAULT_PAYMENT_ID);

    Payment payment2 = Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.KAKAO, SECOND_AMOUNT);
    payment2 = payment2.withId(SECOND_PAYMENT_ID);

    given(paymentQueryPort.findByReservationId(DEFAULT_RESERVATION_ID))
        .willReturn(List.of(payment1, payment2));

    // when
    List<PaymentResponse> result =
        paymentQueryService.getPaymentsByReservationId(DEFAULT_RESERVATION_ID);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).id()).isEqualTo(DEFAULT_PAYMENT_ID);
    assertThat(result.get(1).id()).isEqualTo(SECOND_PAYMENT_ID);
  }

  @Test
  @DisplayName("예약별 결제 목록 조회 - 빈 리스트")
  void getPaymentsByReservationId_empty() {
    // given
    given(paymentQueryPort.findByReservationId(DEFAULT_RESERVATION_ID)).willReturn(List.of());

    // when
    List<PaymentResponse> result =
        paymentQueryService.getPaymentsByReservationId(DEFAULT_RESERVATION_ID);

    // then
    assertThat(result).isEmpty();
  }
}
