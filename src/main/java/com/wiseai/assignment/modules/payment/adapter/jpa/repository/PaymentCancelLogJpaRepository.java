package com.wiseai.assignment.modules.payment.adapter.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentCancelLogEntity;

public interface PaymentCancelLogJpaRepository
    extends JpaRepository<PaymentCancelLogEntity, String> {}
