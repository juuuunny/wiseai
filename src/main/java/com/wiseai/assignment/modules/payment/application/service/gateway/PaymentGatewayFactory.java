package com.wiseai.assignment.modules.payment.application.service.gateway;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.payment.application.port.out.gateway.PaymentGateway;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;

import lombok.RequiredArgsConstructor;

/**
 * 결제 게이트웨이 팩토리 (Strategy Pattern)
 *
 * <p>결제 수단에 따라 적절한 결제 게이트웨이를 반환합니다.
 */
@Component
@RequiredArgsConstructor
public class PaymentGatewayFactory {

  private final List<PaymentGateway> paymentGateways;
  private Map<PaymentMethod, PaymentGateway> gatewayMap;

  /**
   * 결제 수단에 해당하는 결제 게이트웨이를 반환합니다.
   *
   * @param paymentMethod 결제 수단
   * @return 결제 게이트웨이
   * @throws PaymentException 지원하지 않는 결제 수단인 경우
   */
  public PaymentGateway getGateway(PaymentMethod paymentMethod) {
    if (gatewayMap == null) {
      gatewayMap =
          paymentGateways.stream()
              .collect(
                  Collectors.toMap(PaymentGateway::getSupportedPaymentMethod, Function.identity()));
    }

    PaymentGateway gateway = gatewayMap.get(paymentMethod);
    if (gateway == null) {
      throw new PaymentException(PaymentErrorStatus.INVALID_PAYMENT_METHOD);
    }

    return gateway;
  }
}
