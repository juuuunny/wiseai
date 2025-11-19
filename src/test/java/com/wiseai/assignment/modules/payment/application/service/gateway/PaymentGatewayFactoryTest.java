package com.wiseai.assignment.modules.payment.application.service.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wiseai.assignment.modules.payment.application.port.out.gateway.PaymentGateway;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentGatewayFactory 테스트")
class PaymentGatewayFactoryTest {

  @Mock private PaymentGateway tossPaymentGateway;
  @Mock private PaymentGateway kakaoPaymentGateway;

  @InjectMocks private PaymentGatewayFactory paymentGatewayFactory;

  @Test
  @DisplayName("TOSS 결제 게이트웨이 조회 성공")
  void getGateway_toss_success() {
    // given
    paymentGatewayFactory =
        new PaymentGatewayFactory(List.of(tossPaymentGateway, kakaoPaymentGateway));
    given(tossPaymentGateway.getSupportedPaymentMethod()).willReturn(PaymentMethod.TOSS);
    given(kakaoPaymentGateway.getSupportedPaymentMethod()).willReturn(PaymentMethod.KAKAO);

    // when
    PaymentGateway result = paymentGatewayFactory.getGateway(PaymentMethod.TOSS);

    // then
    assertThat(result).isEqualTo(tossPaymentGateway);
  }

  @Test
  @DisplayName("KAKAO 결제 게이트웨이 조회 성공")
  void getGateway_kakao_success() {
    // given
    paymentGatewayFactory =
        new PaymentGatewayFactory(List.of(tossPaymentGateway, kakaoPaymentGateway));
    given(tossPaymentGateway.getSupportedPaymentMethod()).willReturn(PaymentMethod.TOSS);
    given(kakaoPaymentGateway.getSupportedPaymentMethod()).willReturn(PaymentMethod.KAKAO);

    // when
    PaymentGateway result = paymentGatewayFactory.getGateway(PaymentMethod.KAKAO);

    // then
    assertThat(result).isEqualTo(kakaoPaymentGateway);
  }

  @Test
  @DisplayName("지원하지 않는 결제 수단 조회 실패")
  void getGateway_unsupported_fail() {
    // given
    paymentGatewayFactory = new PaymentGatewayFactory(List.of());

    // when & then
    assertThatThrownBy(() -> paymentGatewayFactory.getGateway(PaymentMethod.TOSS))
        .isInstanceOf(PaymentException.class)
        .satisfies(
            exception -> {
              PaymentException ex = (PaymentException) exception;
              assertThat(ex.getErrorCode()).isEqualTo(PaymentErrorStatus.INVALID_PAYMENT_METHOD);
            });
  }
}
