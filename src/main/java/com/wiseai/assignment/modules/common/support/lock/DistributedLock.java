package com.wiseai.assignment.modules.common.support.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Placeholder annotation for distributed locking. Actual AOP implementation can be added later; the
 * annotation itself allows code to compile.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
  String key();

  long waitTime() default 0L;

  long leaseTime() default 0L;

  int retry() default 0;
}
