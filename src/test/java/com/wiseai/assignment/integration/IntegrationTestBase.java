package com.wiseai.assignment.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.wiseai.assignment.modules.payment.application.port.out.command.PaymentCommandPort;
import com.wiseai.assignment.modules.payment.application.port.out.query.PaymentQueryPort;

import org.redisson.api.RedissonClient;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestBase {

  @MockBean protected RedissonClient redissonClient;
  @MockBean protected PaymentQueryPort paymentQueryPort;
  @MockBean protected PaymentCommandPort paymentCommandPort;
}

