package com.wiseai.assignment.modules.payment.application.service.event;

import com.wiseai.assignment.modules.payment.application.event.PaymentCancelRequestMessage;
import com.wiseai.assignment.modules.payment.config.PaymentKafkaTopicsProperties;
import com.wiseai.assignment.modules.payment.domain.model.Payment;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelEventProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final PaymentKafkaTopicsProperties topicsProperties;

  public void publishCancellationRequested(Payment payment) {
    PaymentCancelRequestMessage message =
        new PaymentCancelRequestMessage(
            UUID.randomUUID().toString(),
            payment.getId(),
            payment.getPaymentMethod(),
            payment.getTransactionId(),
            Instant.now());

    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              send(message);
            }
          });
    } else {
      send(message);
    }
  }

  private void send(PaymentCancelRequestMessage message) {
    kafkaTemplate
        .send(
            topicsProperties.getCancel(),
            message.paymentId().toString(),
            message)
        .whenComplete(
            (result, ex) -> {
              if (ex != null) {
                log.error(
                    "결제 취소 이벤트 발행 실패: paymentId={}, eventId={}",
                    message.paymentId(),
                    message.eventId(),
                    ex);
              } else {
                log.debug(
                    "결제 취소 이벤트 발행 성공: paymentId={}, eventId={}",
                    message.paymentId(),
                    message.eventId());
              }
            });
  }
}

