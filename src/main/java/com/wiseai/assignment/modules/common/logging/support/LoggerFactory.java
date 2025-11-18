package com.wiseai.assignment.modules.common.logging.support;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;

/**
 * Lightweight structured logging facade used by legacy modules. This implementation wraps {@link
 * org.slf4j.Logger} so that existing logging calls keep working without the original dependency.
 */
public final class LoggerFactory {

  private static final ApiLogger API_LOGGER = new ApiLogger();
  private static final ServiceLogger SERVICE_LOGGER = new ServiceLogger();
  private static final RedisLogger REDIS_LOGGER = new RedisLogger();
  private static final CommonLogger COMMON_LOGGER = new CommonLogger();
  private static final DomainLogger DOMAIN_LOGGER = new DomainLogger();

  private LoggerFactory() {}

  public static ApiLogger api() {
    return API_LOGGER;
  }

  public static ServiceLogger service() {
    return SERVICE_LOGGER;
  }

  public static RedisLogger redis() {
    return REDIS_LOGGER;
  }

  public static CommonLogger common() {
    return COMMON_LOGGER;
  }

  public static DomainLogger domain() {
    return DOMAIN_LOGGER;
  }

  private static long elapsedMillis(Instant startTime) {
    return Duration.between(startTime, Instant.now()).toMillis();
  }

  public static final class ApiLogger {
    private final Logger log = org.slf4j.LoggerFactory.getLogger("API");

    public Instant logRequest(String message) {
      Instant start = Instant.now();
      log.info(message);
      return start;
    }

    public void logResponse(String message, Instant startTime) {
      log.info("{} ({} ms)", message, elapsedMillis(startTime));
    }

    public void logInfo(String message) {
      log.info(message);
    }
  }

  public static final class ServiceLogger {
    private final Logger log = org.slf4j.LoggerFactory.getLogger("SERVICE");

    public Instant logStart(String useCase, String message) {
      Instant start = Instant.now();
      log.info("[{}] {}", useCase, message);
      return start;
    }

    public void logSuccess(String useCase, String message, Instant startTime) {
      log.info("[{}] {} ({} ms)", useCase, message, elapsedMillis(startTime));
    }

    public void logWarning(String useCase, String message) {
      log.warn("[{}] {}", useCase, message);
    }

    public void logInfo(String useCase, String message) {
      log.info("[{}] {}", useCase, message);
    }

    public void logException(String useCase, String message, Throwable throwable) {
      log.error("[{}] {}", useCase, message, throwable);
    }
  }

  public static final class RedisLogger {
    private final Logger log = org.slf4j.LoggerFactory.getLogger("REDIS");

    public void logSaveOrUpdate(Object key, String message) {
      log.info("[{}] {}", key, message);
    }

    public void logExist(Object key, String message) {
      log.info("[{}] {}", key, message);
    }

    public void logInfo(Object key, String message) {
      log.info("[{}] {}", key, message);
    }

    public void logWarning(Object key, String message) {
      log.warn("[{}] {}", key, message);
    }

    public void logError(Object key, String message, Throwable throwable) {
      log.error("[{}] {}", key, message, throwable);
    }

    public Instant logQueryStart(Object key, String message) {
      Instant start = Instant.now();
      log.info("[{}] {}", key, message);
      return start;
    }

    public void logQueryEnd(Object key, String message, Instant startTime) {
      log.info("[{}] {} ({} ms)", key, message, elapsedMillis(startTime));
    }

    public void logDelete(Object key, String message) {
      log.info("[{}] {}", key, message);
    }
  }

  public static final class CommonLogger {
    private final Logger log = org.slf4j.LoggerFactory.getLogger("COMMON");

    public void logInfo(String source, String message) {
      log.info("[{}] {}", source, message);
    }

    public void logWarning(String source, String message) {
      log.warn("[{}] {}", source, message);
    }

    public void logError(String source, String message) {
      log.error("[{}] {}", source, message);
    }

    public void logError(String source, String message, Throwable throwable) {
      log.error("[{}] {}", source, message, throwable);
    }
  }

  public static final class DomainLogger {
    private final Logger log = org.slf4j.LoggerFactory.getLogger("DOMAIN");

    public void logRuleViolation(String source, String message) {
      log.warn("[{}] {}", source, message);
    }
  }
}
