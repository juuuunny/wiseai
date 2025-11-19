package com.wiseai.assignment.modules.payment.adapter.jpa.mapper;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentEntity;
import com.wiseai.assignment.modules.payment.domain.model.Payment;

@Component
public class PaymentEntityMapper {

  public Payment toDomain(PaymentEntity entity) {
    Payment payment =
        Payment.builder()
            .reservationId(entity.getReservationId())
            .paymentMethod(entity.getPaymentMethod())
            .amount(entity.getAmount())
            .status(entity.getStatus())
            .transactionId(entity.getTransactionId())
            .build();
    // ID가 있는 경우에만 설정
    if (entity.getId() != null) {
      return payment.withId(entity.getId());
    }
    return payment;
  }

  public PaymentEntity toEntity(Payment payment) {
    PaymentEntity entity =
        new PaymentEntity(
            payment.getReservationId(),
            payment.getPaymentMethod(),
            payment.getAmount(),
            payment.getStatus(),
            payment.getTransactionId());
    // ID가 있는 경우에만 설정 (update 시)
    if (payment.getId() != null) {
      entity.setId(payment.getId());
    }
    return entity;
  }

  public void updateEntity(PaymentEntity entity, Payment payment) {
    entity.updateStatus(payment.getStatus());
    if (payment.getTransactionId() != null) {
      entity.updateTransactionId(payment.getTransactionId());
    }
  }
}
