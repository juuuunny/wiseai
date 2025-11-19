package com.wiseai.assignment.modules.payment.application.service.event;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentEventOutboxEntity;
import com.wiseai.assignment.modules.payment.adapter.jpa.repository.PaymentEventOutboxJpaRepository;
import com.wiseai.assignment.modules.payment.application.event.PaymentProcessMessage;
import com.wiseai.assignment.modules.payment.config.PaymentKafkaTopicsProperties;
import com.wiseai.assignment.modules.payment.domain.model.Payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

  private final PaymentEventOutboxJpaRepository outboxRepository;
  private final PaymentKafkaTopicsProperties topicsProperties;
  private final ObjectMapper objectMapper;

  @Transactional
  public void publishPaymentRequested(Payment payment) {
    String eventId = UUID.randomUUID().toString();
    PaymentProcessMessage message =
        new PaymentProcessMessage(
            eventId,
            payment.getId(),
            payment.getReservationId(),
            payment.getPaymentMethod(),
            payment.getAmount(),
            Instant.now());

    try {
      String payload = objectMapper.writeValueAsString(message);
      PaymentEventOutboxEntity outbox =
          new PaymentEventOutboxEntity(
              eventId,
              PaymentEventOutboxEntity.EventType.PAYMENT_PROCESS,
              topicsProperties.getProcess(),
              payload);
      outboxRepository.save(outbox);
      log.debug("결제 처리 이벤트 Outbox 저장: eventId={}, paymentId={}", eventId, payment.getId());
    } catch (JsonProcessingException e) {
      log.error("결제 처리 이벤트 직렬화 실패: paymentId={}, eventId={}", payment.getId(), eventId, e);
      throw new RuntimeException("이벤트 직렬화 실패", e);
    }
  }
}
