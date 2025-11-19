package com.wiseai.assignment.modules.payment.adapter.jpa.repository;

import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentCancelLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCancelLogJpaRepository extends JpaRepository<PaymentCancelLogEntity, String> {}

