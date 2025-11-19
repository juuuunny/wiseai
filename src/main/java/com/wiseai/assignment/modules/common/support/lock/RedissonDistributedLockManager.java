package com.wiseai.assignment.modules.common.support.lock;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.common.exception.BusinessException;
import com.wiseai.assignment.modules.common.exception.CommonException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Redisson 분산락을 실행하여 락을 잡을 경우 수행하고, 실패 시 재시도하며, 예외를 명확하게 처리한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonDistributedLockManager {
  private final RedissonClient redissonClient;

  /**
   * 지정된 키에 대해 분산 락을 획득한 뒤, 성공적으로 락을 획득하면 주어진 작업을 실행하고 그 결과를 반환합니다.
   *
   * <p>락 획득을 위해 최대 waitTime(밀리초) 동안 대기하며, 락을 획득하면 leaseTime(밀리초) 동안 소유합니다. 락 획득에 실패할 경우 최대
   * retryCount만큼 재시도합니다. 모든 시도에 실패하면 LockAcquisitionException이 발생합니다.
   *
   * @param key 분산 락을 식별하는 고유 키
   * @param waitTime 락 획득을 위해 대기할 최대 시간(밀리초)
   * @param leaseTime 락을 소유할 임대 시간(밀리초)
   * @param retryCount 락 획득 재시도 횟수
   * @param action 락 획득 후 실행할 작업
   * @return 작업 실행 결과
   * @throws LockAcquisitionException 락 획득 실패 또는 예외 발생 시
   * @throws BusinessException 작업 실행 중 비즈니스 예외 발생 시
   * @throws CommonException 작업 실행 중 공통 예외 발생 시
   */
  public <T> T execute(
      String key, long waitTime, long leaseTime, int retryCount, Supplier<T> action) {
    log.debug(
        "[분산락] 락 실행 시작 - key: {} waitTime: {}ms leaseTime: {}ms retryCount: {}",
        key,
        waitTime,
        leaseTime,
        retryCount);
    RLock lock = redissonClient.getLock(key);
    log.trace(
        "[분산락] Redisson 락 객체 생성 완료 - key: {} class: {}", key, lock.getClass().getSimpleName());

    return attemptLockAcquisition(key, lock, waitTime, leaseTime, retryCount, action);
  }

  /** 락 획득을 시도하고 성공 시 작업을 실행합니다. */
  private <T> T attemptLockAcquisition(
      String key, RLock lock, long waitTime, long leaseTime, int retryCount, Supplier<T> action) {
    int attempts = 0;

    while (attempts <= retryCount) {
      log.debug(
          "[분산락] 락 획득 시도 - key: {} attempt: {}/{} waitTime: {}ms leaseTime: {}ms",
          key,
          attempts + 1,
          retryCount + 1,
          waitTime,
          leaseTime);

      boolean acquired = acquireLock(key, lock, waitTime, leaseTime);

      if (acquired) {
        log.info("[분산락] 락 획득 성공 - key: {} attempt: {}/{}", key, attempts + 1, retryCount + 1);
        try {
          T result = action.get();
          log.info("[분산락] 락 기반 작업 성공 완료 - key: {}", key);
          return result;
        } catch (BusinessException | CommonException e) {
          // 비즈니스/공통 예외는 그대로 전파 (로깅은 AOP에서 처리)
          throw e;
        } catch (Exception e) {
          log.error(
              "[분산락] 락 기반 작업 실행 중 예외 발생 - key: {} exception: {}",
              key,
              e.getClass().getSimpleName(),
              e);
          throw e;
        } finally {
          // 락을 실제로 획득한 경우에만 해제
          if (lock.isHeldByCurrentThread()) {
            releaseLock(key, lock);
          }
        }
      }

      attempts++;
      if (attempts <= retryCount) {
        log.warn("[분산락] 락 획득 실패, 재시도 예정 - key: {} attempt: {}/{}", key, attempts, retryCount);
        performBackoff(key, attempts);
      }
    }

    log.error("[분산락] 락 획득 최대 재시도 횟수 초과 - key: {} totalAttempts: {}", key, attempts);
    throw new LockAcquisitionException(
        String.format(
            "다른 사용자가 해당 자원에 접근 중입니다. (key: %s, 시도 횟수: %d) 잠시 후 다시 시도해주세요.", key, attempts));
  }

  /** 락 획득만 담당합니다. (락 해제는 하지 않음) 락 해제는 호출하는 메서드에서 finally 블록을 통해 처리됩니다. */
  @SuppressWarnings("java:S2222") // 락 해제는 호출자에서 처리됨
  private boolean acquireLock(String key, RLock lock, long waitTime, long leaseTime) {
    try {
      boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
      log.trace(
          "[분산락] tryLock 결과 - key: {} acquired: {} waitTime: {}ms leaseTime: {}ms",
          key,
          acquired,
          waitTime,
          leaseTime);
      return acquired;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error(
          "[분산락] 락 획득 중 스레드 인터럽트 발생 - key: {} waitTime: {}ms leaseTime: {}ms",
          key,
          waitTime,
          leaseTime,
          e);
      throw new LockAcquisitionException(
          String.format(
              "스레드 인터럽트로 인해 락 획득 실패 (key: %s, waitTime: %dms, leaseTime: %dms)",
              key, waitTime, leaseTime),
          e);
    } catch (Exception e) {
      log.error(
          "[분산락] 락 획득 중 예외 발생 - key: {} waitTime: {}ms leaseTime: {}ms exception: {}",
          key,
          waitTime,
          leaseTime,
          e.getClass().getSimpleName(),
          e);
      throw new LockAcquisitionException(
          String.format(
              "분산 락 획득 중 예외 발생 (key: %s, exception: %s)", key, e.getClass().getSimpleName()),
          e);
    }
  }

  private void performBackoff(String key, int attempts) {
    // 지수 백오프: 100ms, 200ms, 400ms, 800ms...
    long backoffMs = Math.min(100L * (1L << (attempts - 1)), 5000L); // 최대 5초
    log.debug("[분산락] 백오프 대기 시작 - key: {} attempt: {} backoffMs: {}ms", key, attempts, backoffMs);
    try {
      TimeUnit.MILLISECONDS.sleep(backoffMs);
      log.trace("[분산락] 백오프 대기 완료 - key: {} attempt: {} backoffMs: {}ms", key, attempts, backoffMs);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error(
          "[분산락] 백오프 대기 중 스레드 인터럽트 발생 - key: {} attempt: {} backoffMs: {}ms",
          key,
          attempts,
          backoffMs,
          e);
      throw new LockAcquisitionException(
          String.format(
              "백오프 대기 중 인터럽트 발생 (key: %s, attempt: %d, backoffMs: %dms)", key, attempts, backoffMs),
          e);
    }
  }

  /**
   * 비동기로 분산 락을 획득하고 작업을 실행합니다.
   *
   * @param key 분산 락을 식별하는 고유 키
   * @param waitTime 락 획득을 위해 대기할 최대 시간(밀리초)
   * @param leaseTime 락을 소유할 임대 시간(밀리초)
   * @param retryCount 락 획득 재시도 횟수
   * @param action 락 획득 후 실행할 작업
   * @return CompletableFuture로 래핑된 작업 실행 결과
   */
  public <T> CompletableFuture<T> executeAsync(
      String key, long waitTime, long leaseTime, int retryCount, Supplier<T> action) {
    return CompletableFuture.supplyAsync(
        () -> execute(key, waitTime, leaseTime, retryCount, action));
  }

  /**
   * 현재 스레드가 보유한 분산 락을 해제합니다.
   *
   * @param key 해제할 락의 식별자
   * @param lock 해제할 Redisson 락 객체
   */
  private void releaseLock(String key, RLock lock) {
    try {
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
        log.debug("[분산락] 락 해제 완료 - key: {}", key);
      } else {
        log.warn("[분산락] 락 해제 시도 실패 (현재 스레드가 락을 보유하지 않음) - key: {}", key);
      }
    } catch (IllegalMonitorStateException e) {
      log.warn("[분산락] 락 해제 실패 (잘못된 모니터 상태) - key: {} message: {}", key, e.getMessage());
      // IllegalMonitorStateException은 이미 해제된 락을 다시 해제하려고 할 때 발생
      // 이는 치명적이지 않으므로 로그만 남기고 계속 진행
    } catch (Exception e) {
      log.error("[분산락] 락 해제 중 예외 발생 - key: {} exception: {}", key, e.getClass().getSimpleName(), e);
      // 락 해제 실패는 치명적이지 않지만 로그는 남겨야 함
    }
  }
}
