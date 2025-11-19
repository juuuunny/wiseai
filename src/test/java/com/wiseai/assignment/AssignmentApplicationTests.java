package com.wiseai.assignment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.data.redis.repositories.enabled=false"})
class AssignmentApplicationTests {

  @Test
  void contextLoads() {}
}
