package com.wiseai.assignment.modules.payment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
@EnableConfigurationProperties(PaymentKafkaTopicsProperties.class)
public class PaymentKafkaConfig {}

