package com.wiseai.assignment.modules.payment.adapter.jpa.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.wiseai.assignment.modules.common.base.BaseTimeEntity;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
public class PaymentEntity extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long reservationId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentMethod paymentMethod;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentStatus status;

  @Column(length = 100)
  private String transactionId;

  public PaymentEntity(
      Long reservationId,
      PaymentMethod paymentMethod,
      BigDecimal amount,
      PaymentStatus status,
      String transactionId) {
    this.reservationId = reservationId;
    this.paymentMethod = paymentMethod;
    this.amount = amount;
    this.status = status;
    this.transactionId = transactionId;
  }

  public void updateStatus(PaymentStatus status) {
    this.status = status;
  }

  public void updateTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
