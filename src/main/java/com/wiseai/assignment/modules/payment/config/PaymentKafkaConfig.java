package com.wiseai.assignment.modules.payment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
@EnableKafka
@EnableConfigurationProperties(PaymentKafkaTopicsProperties.class)
@org.springframework.context.annotation.Profile("!test")
public class PaymentKafkaConfig {

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
      ConsumerFactory<String, Object> consumerFactory,
      KafkaTemplate<String, Object> kafkaTemplate,
      PaymentKafkaTopicsProperties topicsProperties) {
    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);

    DeadLetterPublishingRecoverer recoverer =
        new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (record, ex) -> {
              String topic = record.topic();
              if (topic.equals(topicsProperties.getProcess())) {
                return new org.apache.kafka.common.TopicPartition(
                    topicsProperties.getProcessDlq(), record.partition());
              }
              if (topic.equals(topicsProperties.getCancel())) {
                return new org.apache.kafka.common.TopicPartition(
                    topicsProperties.getCancelDlq(), record.partition());
              }
              return new org.apache.kafka.common.TopicPartition(topic + ".DLT", record.partition());
            });

    ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
    backOff.setInitialInterval(1000L);
    backOff.setMultiplier(2.0);
    backOff.setMaxInterval(8000L);

    factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, backOff));

    return factory;
  }
}
