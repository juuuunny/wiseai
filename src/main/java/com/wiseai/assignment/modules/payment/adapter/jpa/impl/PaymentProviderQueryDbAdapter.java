package com.wiseai.assignment.modules.payment.adapter.jpa.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.payment.adapter.jpa.mapper.PaymentProviderEntityMapper;
import com.wiseai.assignment.modules.payment.adapter.jpa.repository.PaymentProviderJpaRepository;
import com.wiseai.assignment.modules.payment.application.port.out.provider.PaymentProviderQueryPort;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.model.PaymentProvider;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentProviderQueryDbAdapter implements PaymentProviderQueryPort {

  private final PaymentProviderJpaRepository paymentProviderJpaRepository;
  private final PaymentProviderEntityMapper paymentProviderEntityMapper;

  @Override
  public Optional<PaymentProvider> findByPaymentMethod(PaymentMethod paymentMethod) {
    return paymentProviderJpaRepository
        .findByPaymentMethodAndActiveTrue(paymentMethod)
        .map(paymentProviderEntityMapper::toDomain);
  }
}
