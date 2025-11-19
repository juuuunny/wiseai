package com.wiseai.assignment.modules.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "payment.kafka.topics")
public class PaymentKafkaTopicsProperties {

  /** 결제 처리 토픽. */
  private String process = "payment.process";

  /** 결제 처리 DLQ 토픽. */
  private String processDlq = "payment.process.dlq";

  /** 결제 취소 토픽. */
  private String cancel = "payment.cancel";

  /** 결제 취소 DLQ 토픽. */
  private String cancelDlq = "payment.cancel.dlq";
}
