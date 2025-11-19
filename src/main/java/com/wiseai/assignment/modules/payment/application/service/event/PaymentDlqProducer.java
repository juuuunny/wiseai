package com.wiseai.assignment.modules.payment.application.service.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.payment.application.event.PaymentProcessMessage;
import com.wiseai.assignment.modules.payment.config.PaymentKafkaTopicsProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentDlqProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final PaymentKafkaTopicsProperties topicsProperties;

  public void publishProcessFailure(PaymentProcessMessage message, Throwable cause) {
    kafkaTemplate
        .send(topicsProperties.getProcessDlq(), message.paymentId().toString(), message)
        .whenComplete(
            (result, ex) -> {
              if (ex != null) {
                log.error(
                    "결제 처리 DLQ 발행 실패: paymentId={}, eventId={}",
                    message.paymentId(),
                    message.eventId(),
                    ex);
              } else {
                log.warn(
                    "결제 처리 DLQ 발행 완료: paymentId={}, eventId={}, reason={}",
                    message.paymentId(),
                    message.eventId(),
                    cause.getMessage());
              }
            });
  }
}
