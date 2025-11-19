package com.wiseai.assignment.modules.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "payment.kafka.topics")
public class PaymentKafkaTopicsProperties {

  /** 결제 처리 토픽. */
  private String process = "payment.process";
}

