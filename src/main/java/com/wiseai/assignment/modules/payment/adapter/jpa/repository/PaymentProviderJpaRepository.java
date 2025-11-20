package com.wiseai.assignment.modules.payment.adapter.jpa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentProviderEntity;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;

public interface PaymentProviderJpaRepository extends JpaRepository<PaymentProviderEntity, Long> {

  Optional<PaymentProviderEntity> findByPaymentMethodAndActiveTrue(PaymentMethod paymentMethod);

  Optional<PaymentProviderEntity> findByName(String name);
}
