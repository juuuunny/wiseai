package com.wiseai.assignment.modules.payment.adapter.jpa.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_process_log")
@Getter
@NoArgsConstructor
public class PaymentProcessLogEntity {

  @Id private String eventId;

  @Column(nullable = false)
  private Long paymentId;

  @Column(nullable = false)
  private LocalDateTime processedAt;

  public PaymentProcessLogEntity(String eventId, Long paymentId, LocalDateTime processedAt) {
    this.eventId = eventId;
    this.paymentId = paymentId;
    this.processedAt = processedAt;
  }
}
