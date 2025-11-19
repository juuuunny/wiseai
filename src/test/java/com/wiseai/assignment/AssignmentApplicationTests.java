package com.wiseai.assignment;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.wiseai.assignment.modules.payment.application.port.out.command.PaymentCommandPort;
import com.wiseai.assignment.modules.payment.application.port.out.query.PaymentQueryPort;

@SpringBootTest
class AssignmentApplicationTests {

  @MockBean private RedissonClient redissonClient;
  @MockBean private PaymentQueryPort paymentQueryPort;
  @MockBean private PaymentCommandPort paymentCommandPort;

  @Test
  void contextLoads() {}
}
