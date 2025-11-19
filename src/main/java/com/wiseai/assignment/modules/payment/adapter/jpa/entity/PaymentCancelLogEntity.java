package com.wiseai.assignment.modules.payment.adapter.jpa.entity;

import java.time.LocalDateTime;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_cancel_log")
@Getter
@NoArgsConstructor
public class PaymentCancelLogEntity {

  @Id private String eventId;

  @Column(nullable = false)
  private Long paymentId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  @Column(nullable = false)
  private LocalDateTime processedAt;

  public PaymentCancelLogEntity(
      String eventId, Long paymentId, PaymentMethod paymentMethod, LocalDateTime processedAt) {
    this.eventId = eventId;
    this.paymentId = paymentId;
    this.paymentMethod = paymentMethod;
    this.processedAt = processedAt;
  }
}
