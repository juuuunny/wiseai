package com.wiseai.assignment.modules.payment.adapter.jpa.entity;

import com.wiseai.assignment.modules.common.base.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_event_outbox")
@Getter
@NoArgsConstructor
public class PaymentEventOutboxEntity extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String eventId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private EventType eventType;

  @Column(nullable = false)
  private String topic;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private OutboxStatus status;

  @Column private Integer retryCount;

  public PaymentEventOutboxEntity(
      String eventId, EventType eventType, String topic, String payload) {
    this.eventId = eventId;
    this.eventType = eventType;
    this.topic = topic;
    this.payload = payload;
    this.status = OutboxStatus.PENDING;
    this.retryCount = 0;
  }

  public void markPublished() {
    this.status = OutboxStatus.PUBLISHED;
  }

  public void incrementRetry() {
    this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
  }

  public void markFailed() {
    this.status = OutboxStatus.FAILED;
  }

  public enum EventType {
    PAYMENT_PROCESS,
    PAYMENT_CANCEL
  }

  public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
  }
}
