package com.wiseai.assignment.modules.payment.application.service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentEventOutboxEntity;
import com.wiseai.assignment.modules.payment.adapter.jpa.repository.PaymentEventOutboxJpaRepository;
import com.wiseai.assignment.modules.payment.application.event.PaymentCancelRequestMessage;
import com.wiseai.assignment.modules.payment.config.PaymentKafkaTopicsProperties;
import com.wiseai.assignment.modules.payment.domain.model.Payment;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelEventProducer {

  private final PaymentEventOutboxJpaRepository outboxRepository;
  private final PaymentKafkaTopicsProperties topicsProperties;
  private final ObjectMapper objectMapper;

  @Transactional
  public void publishPaymentCancelRequested(Payment payment) {
    String eventId = UUID.randomUUID().toString();
    PaymentCancelRequestMessage message =
        new PaymentCancelRequestMessage(
            eventId,
            payment.getId(),
            payment.getPaymentMethod(),
            payment.getTransactionId(),
            Instant.now());

    try {
      String payload = objectMapper.writeValueAsString(message);
      PaymentEventOutboxEntity outbox =
          new PaymentEventOutboxEntity(
              eventId,
              PaymentEventOutboxEntity.EventType.PAYMENT_CANCEL,
              topicsProperties.getCancel(),
              payload);
      outboxRepository.save(outbox);
      log.debug(
          "결제 취소 이벤트 Outbox 저장: eventId={}, paymentId={}", eventId, payment.getId());
    } catch (JsonProcessingException e) {
      log.error(
          "결제 취소 이벤트 직렬화 실패: paymentId={}, eventId={}", payment.getId(), eventId, e);
      throw new RuntimeException("이벤트 직렬화 실패", e);
    }
  }
}

