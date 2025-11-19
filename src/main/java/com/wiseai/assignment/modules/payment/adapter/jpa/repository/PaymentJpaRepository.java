package com.wiseai.assignment.modules.payment.adapter.jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentEntity;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {
  List<PaymentEntity> findByReservationIdOrderByIdDesc(Long reservationId);
}
