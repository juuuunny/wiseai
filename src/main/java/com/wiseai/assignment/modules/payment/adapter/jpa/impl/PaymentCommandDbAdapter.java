package com.wiseai.assignment.modules.payment.adapter.jpa.impl;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentEntity;
import com.wiseai.assignment.modules.payment.adapter.jpa.mapper.PaymentEntityMapper;
import com.wiseai.assignment.modules.payment.adapter.jpa.repository.PaymentJpaRepository;
import com.wiseai.assignment.modules.payment.application.port.out.command.PaymentCommandPort;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.model.Payment;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentCommandDbAdapter implements PaymentCommandPort {

  private final PaymentJpaRepository paymentJpaRepository;
  private final PaymentEntityMapper paymentEntityMapper;

  @Override
  public Payment save(Payment payment) {
    PaymentEntity entity = paymentEntityMapper.toEntity(payment);
    PaymentEntity saved = paymentJpaRepository.save(entity);
    return paymentEntityMapper.toDomain(saved);
  }

  @Override
  public Payment update(Payment payment) {
    PaymentEntity entity =
        paymentJpaRepository
            .findById(payment.getId())
            .orElseThrow(() -> new PaymentException(PaymentErrorStatus.NOT_FOUND));
    paymentEntityMapper.updateEntity(entity, payment);
    PaymentEntity updated = paymentJpaRepository.save(entity);
    return paymentEntityMapper.toDomain(updated);
  }

  @Override
  public void delete(Long id) {
    paymentJpaRepository.deleteById(id);
  }
}
