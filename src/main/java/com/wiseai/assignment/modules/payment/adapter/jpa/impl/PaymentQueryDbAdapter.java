package com.wiseai.assignment.modules.payment.adapter.jpa.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.payment.adapter.jpa.mapper.PaymentEntityMapper;
import com.wiseai.assignment.modules.payment.adapter.jpa.repository.PaymentJpaRepository;
import com.wiseai.assignment.modules.payment.application.port.out.query.PaymentQueryPort;
import com.wiseai.assignment.modules.payment.domain.model.Payment;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentQueryDbAdapter implements PaymentQueryPort {

  private final PaymentJpaRepository paymentJpaRepository;
  private final PaymentEntityMapper paymentEntityMapper;

  @Override
  public Optional<Payment> findById(Long id) {
    return paymentJpaRepository.findById(id).map(paymentEntityMapper::toDomain);
  }

  @Override
  public List<Payment> findByReservationId(Long reservationId) {
    return paymentJpaRepository.findByReservationIdOrderByIdDesc(reservationId).stream()
        .map(paymentEntityMapper::toDomain)
        .toList();
  }
}
