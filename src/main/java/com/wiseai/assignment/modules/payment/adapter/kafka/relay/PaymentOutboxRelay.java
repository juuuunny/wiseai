package com.wiseai.assignment.modules.payment.adapter.kafka.relay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentEventOutboxEntity;
import com.wiseai.assignment.modules.payment.adapter.jpa.repository.PaymentEventOutboxJpaRepository;
import com.wiseai.assignment.modules.payment.application.event.PaymentCancelRequestMessage;
import com.wiseai.assignment.modules.payment.application.event.PaymentProcessMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutboxRelay {

  private static final int MAX_RETRIES = 3;

  private final PaymentEventOutboxJpaRepository outboxRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Scheduled(fixedDelay = 1000) // 1초마다 실행
  @Transactional
  public void relayPendingEvents() {
    var pendingEvents =
        outboxRepository.findPendingEvents(
            PaymentEventOutboxEntity.OutboxStatus.PENDING, MAX_RETRIES);

    if (pendingEvents.isEmpty()) {
      return;
    }

    log.debug("Outbox 이벤트 릴레이 시작: count={}", pendingEvents.size());

    for (PaymentEventOutboxEntity outbox : pendingEvents) {
      try {
        publishEvent(outbox);
        outbox.markPublished();
        outboxRepository.save(outbox);
        log.debug(
            "Outbox 이벤트 발행 성공: eventId={}, type={}",
            outbox.getEventId(),
            outbox.getEventType());
      } catch (Exception e) {
        log.error(
            "Outbox 이벤트 발행 실패: eventId={}, type={}, retryCount={}",
            outbox.getEventId(),
            outbox.getEventType(),
            outbox.getRetryCount(),
            e);
        outbox.incrementRetry();
        if (outbox.getRetryCount() >= MAX_RETRIES) {
          outbox.markFailed();
          log.error(
              "Outbox 이벤트 최대 재시도 초과: eventId={}, type={}",
              outbox.getEventId(),
              outbox.getEventType());
        }
        outboxRepository.save(outbox);
      }
    }
  }

  private void publishEvent(PaymentEventOutboxEntity outbox)
      throws JsonProcessingException, InterruptedException, java.util.concurrent.ExecutionException {
    String key = extractKey(outbox);
    Object message = deserializeMessage(outbox);

    kafkaTemplate
        .send(outbox.getTopic(), key, message)
        .get(); // 동기적으로 발행하여 실패 시 예외 발생
  }

  private String extractKey(PaymentEventOutboxEntity outbox) throws JsonProcessingException {
    if (outbox.getEventType() == PaymentEventOutboxEntity.EventType.PAYMENT_PROCESS) {
      PaymentProcessMessage message =
          objectMapper.readValue(outbox.getPayload(), PaymentProcessMessage.class);
      return message.paymentId().toString();
    } else if (outbox.getEventType() == PaymentEventOutboxEntity.EventType.PAYMENT_CANCEL) {
      PaymentCancelRequestMessage message =
          objectMapper.readValue(outbox.getPayload(), PaymentCancelRequestMessage.class);
      return message.paymentId().toString();
    }
    throw new IllegalArgumentException("알 수 없는 이벤트 타입: " + outbox.getEventType());
  }

  private Object deserializeMessage(PaymentEventOutboxEntity outbox)
      throws JsonProcessingException {
    if (outbox.getEventType() == PaymentEventOutboxEntity.EventType.PAYMENT_PROCESS) {
      return objectMapper.readValue(outbox.getPayload(), PaymentProcessMessage.class);
    } else if (outbox.getEventType() == PaymentEventOutboxEntity.EventType.PAYMENT_CANCEL) {
      return objectMapper.readValue(outbox.getPayload(), PaymentCancelRequestMessage.class);
    }
    throw new IllegalArgumentException("알 수 없는 이벤트 타입: " + outbox.getEventType());
  }
}

