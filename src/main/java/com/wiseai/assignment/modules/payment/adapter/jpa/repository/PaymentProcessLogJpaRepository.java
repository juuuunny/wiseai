package com.wiseai.assignment.modules.payment.adapter.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentProcessLogEntity;

public interface PaymentProcessLogJpaRepository
    extends JpaRepository<PaymentProcessLogEntity, String> {}
