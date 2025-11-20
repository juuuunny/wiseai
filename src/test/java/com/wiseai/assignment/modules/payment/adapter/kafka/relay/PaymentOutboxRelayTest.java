package com.wiseai.assignment.modules.payment.adapter.kafka.relay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentEventOutboxEntity;
import com.wiseai.assignment.modules.payment.adapter.jpa.repository.PaymentEventOutboxJpaRepository;
import com.wiseai.assignment.modules.payment.application.event.PaymentProcessMessage;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentOutboxRelay 테스트")
class PaymentOutboxRelayTest {

  @Mock private PaymentEventOutboxJpaRepository outboxRepository;
  @Mock private KafkaTemplate<String, Object> kafkaTemplate;
  @Mock private ObjectMapper objectMapper;

  @InjectMocks private PaymentOutboxRelay paymentOutboxRelay;

  private static final String DEFAULT_EVENT_ID = "event-1";
  private static final Long DEFAULT_PAYMENT_ID = 1L;
  private static final String DEFAULT_TOPIC = "payment.process";
  private static final String DEFAULT_PAYLOAD = "{\"eventId\":\"event-1\",\"paymentId\":1}";

  @Test
  @DisplayName("PENDING 이벤트가 없으면 릴레이하지 않는다")
  void relayPendingEvents_noPendingEvents() {
    // given
    given(outboxRepository.findPendingEvents(PaymentEventOutboxEntity.OutboxStatus.PENDING, 3))
        .willReturn(List.of());

    // when
    paymentOutboxRelay.relayPendingEvents();

    // then
    verify(kafkaTemplate, never()).send(any(), any(), any());
    verify(outboxRepository, never()).save(any());
  }

  @Test
  @DisplayName("PENDING 이벤트를 Kafka로 발행하고 PUBLISHED로 마킹한다")
  void relayPendingEvents_success() throws Exception {
    // given
    PaymentEventOutboxEntity outbox =
        new PaymentEventOutboxEntity(
            DEFAULT_EVENT_ID,
            PaymentEventOutboxEntity.EventType.PAYMENT_PROCESS,
            DEFAULT_TOPIC,
            DEFAULT_PAYLOAD);

    PaymentProcessMessage message =
        new PaymentProcessMessage(
            DEFAULT_EVENT_ID,
            DEFAULT_PAYMENT_ID,
            1L,
            PaymentMethod.TOSS,
            new BigDecimal("10000"),
            Instant.now());

    given(outboxRepository.findPendingEvents(PaymentEventOutboxEntity.OutboxStatus.PENDING, 3))
        .willReturn(List.of(outbox));
    given(objectMapper.readValue(DEFAULT_PAYLOAD, PaymentProcessMessage.class)).willReturn(message);
    given(kafkaTemplate.send(eq(DEFAULT_TOPIC), eq(DEFAULT_PAYMENT_ID.toString()), any()))
        .willReturn(CompletableFuture.completedFuture(null));

    // when
    paymentOutboxRelay.relayPendingEvents();

    // then
    ArgumentCaptor<PaymentEventOutboxEntity> captor =
        ArgumentCaptor.forClass(PaymentEventOutboxEntity.class);
    verify(outboxRepository).save(captor.capture());
    assertThat(captor.getValue().getStatus())
        .isEqualTo(PaymentEventOutboxEntity.OutboxStatus.PUBLISHED);
    verify(kafkaTemplate, times(1))
        .send(eq(DEFAULT_TOPIC), eq(DEFAULT_PAYMENT_ID.toString()), any());
  }

  @Test
  @DisplayName("Kafka 발행 실패 시 재시도 횟수를 증가시킨다")
  void relayPendingEvents_fail_incrementRetry() throws Exception {
    // given
    PaymentEventOutboxEntity outbox =
        new PaymentEventOutboxEntity(
            DEFAULT_EVENT_ID,
            PaymentEventOutboxEntity.EventType.PAYMENT_PROCESS,
            DEFAULT_TOPIC,
            DEFAULT_PAYLOAD);

    PaymentProcessMessage message =
        new PaymentProcessMessage(
            DEFAULT_EVENT_ID,
            DEFAULT_PAYMENT_ID,
            1L,
            PaymentMethod.TOSS,
            new BigDecimal("10000"),
            Instant.now());

    given(outboxRepository.findPendingEvents(PaymentEventOutboxEntity.OutboxStatus.PENDING, 3))
        .willReturn(List.of(outbox));
    given(objectMapper.readValue(DEFAULT_PAYLOAD, PaymentProcessMessage.class)).willReturn(message);
    given(kafkaTemplate.send(eq(DEFAULT_TOPIC), eq(DEFAULT_PAYMENT_ID.toString()), any()))
        .willReturn(
            CompletableFuture.failedFuture(new RuntimeException("Kafka connection failed")));

    // when
    paymentOutboxRelay.relayPendingEvents();

    // then
    ArgumentCaptor<PaymentEventOutboxEntity> captor =
        ArgumentCaptor.forClass(PaymentEventOutboxEntity.class);
    verify(outboxRepository).save(captor.capture());
    assertThat(captor.getValue().getStatus())
        .isEqualTo(PaymentEventOutboxEntity.OutboxStatus.PENDING);
    assertThat(captor.getValue().getRetryCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("최대 재시도 횟수 초과 시 FAILED로 마킹한다")
  void relayPendingEvents_fail_maxRetries() throws Exception {
    // given
    PaymentEventOutboxEntity outbox =
        new PaymentEventOutboxEntity(
            DEFAULT_EVENT_ID,
            PaymentEventOutboxEntity.EventType.PAYMENT_PROCESS,
            DEFAULT_TOPIC,
            DEFAULT_PAYLOAD);
    outbox.incrementRetry();
    outbox.incrementRetry();
    outbox.incrementRetry(); // retryCount = 3

    PaymentProcessMessage message =
        new PaymentProcessMessage(
            DEFAULT_EVENT_ID,
            DEFAULT_PAYMENT_ID,
            1L,
            PaymentMethod.TOSS,
            new BigDecimal("10000"),
            Instant.now());

    given(outboxRepository.findPendingEvents(PaymentEventOutboxEntity.OutboxStatus.PENDING, 3))
        .willReturn(List.of(outbox));
    given(objectMapper.readValue(DEFAULT_PAYLOAD, PaymentProcessMessage.class)).willReturn(message);
    given(kafkaTemplate.send(eq(DEFAULT_TOPIC), eq(DEFAULT_PAYMENT_ID.toString()), any()))
        .willReturn(
            CompletableFuture.failedFuture(new RuntimeException("Kafka connection failed")));

    // when
    paymentOutboxRelay.relayPendingEvents();

    // then
    ArgumentCaptor<PaymentEventOutboxEntity> captor =
        ArgumentCaptor.forClass(PaymentEventOutboxEntity.class);
    verify(outboxRepository).save(captor.capture());
    assertThat(captor.getValue().getStatus())
        .isEqualTo(PaymentEventOutboxEntity.OutboxStatus.FAILED);
    assertThat(captor.getValue().getRetryCount()).isEqualTo(4);
  }
}
