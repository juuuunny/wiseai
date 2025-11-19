package com.wiseai.assignment.modules.payment.adapter.jpa.repository;

import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentProcessLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentProcessLogJpaRepository
    extends JpaRepository<PaymentProcessLogEntity, String> {}

