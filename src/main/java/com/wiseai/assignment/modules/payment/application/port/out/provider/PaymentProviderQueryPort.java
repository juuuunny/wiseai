package com.wiseai.assignment.modules.payment.application.port.out.provider;

import java.util.Optional;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.model.PaymentProvider;

/**
 * 결제사 정보 조회 Port
 *
 * <p>결제사 정보를 조회하는 인터페이스입니다.
 */
public interface PaymentProviderQueryPort {

  Optional<PaymentProvider> findByPaymentMethod(PaymentMethod paymentMethod);
}
