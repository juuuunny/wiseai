package com.wiseai.assignment.integration.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;

import com.wiseai.assignment.modules.payment.adapter.kafka.listener.PaymentCancelListener;
import com.wiseai.assignment.modules.payment.adapter.kafka.listener.PaymentProcessListener;
import com.wiseai.assignment.modules.payment.adapter.kafka.relay.PaymentOutboxRelay;
import com.wiseai.assignment.modules.payment.application.service.event.PaymentCancelEventProducer;
import com.wiseai.assignment.modules.payment.application.service.event.PaymentDlqProducer;
import com.wiseai.assignment.modules.payment.application.service.event.PaymentEventProducer;

/**
 * 통합 테스트용 설정 클래스
 *
 * <p>Kafka 관련 컴포넌트를 Mock 처리하여 외부 의존성 없이 테스트할 수 있도록 합니다.
 */
@TestConfiguration
@Profile("test")
public class IntegrationTestConfig {

  @MockBean private PaymentEventProducer paymentEventProducer;
  @MockBean private PaymentCancelEventProducer paymentCancelEventProducer;
  @MockBean private PaymentDlqProducer paymentDlqProducer;
  @MockBean private PaymentOutboxRelay paymentOutboxRelay;
  @MockBean private PaymentProcessListener paymentProcessListener;
  @MockBean private PaymentCancelListener paymentCancelListener;
}
