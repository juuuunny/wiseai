package com.wiseai.assignment.modules.payment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PaymentKafkaTopicsProperties.class)
public class PaymentKafkaConfig {}

