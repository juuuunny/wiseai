package com.wiseai.assignment.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.wiseai.assignment.modules.meetingroom.application.port.out.command.MeetingRoomCommandPort;
import com.wiseai.assignment.modules.meetingroom.domain.model.MeetingRoom;
import com.wiseai.assignment.modules.payment.adapter.kafka.listener.PaymentCancelListener;
import com.wiseai.assignment.modules.payment.adapter.kafka.listener.PaymentProcessListener;
import com.wiseai.assignment.modules.payment.adapter.kafka.relay.PaymentOutboxRelay;
import com.wiseai.assignment.modules.payment.application.service.event.PaymentCancelEventProducer;
import com.wiseai.assignment.modules.payment.application.service.event.PaymentDlqProducer;
import com.wiseai.assignment.modules.payment.application.service.event.PaymentEventProducer;
import com.wiseai.assignment.modules.reservation.application.dto.response.ReservationResponse;
import com.wiseai.assignment.modules.reservation.application.port.in.command.CreateReservationUseCase;
import com.wiseai.assignment.modules.reservation.domain.exception.ReservationException;
import com.wiseai.assignment.modules.reservation.domain.status.ReservationErrorStatus;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Deadlock 방지 통합 테스트")
class DeadlockPreventionIntegrationTest {

  @Autowired private CreateReservationUseCase createReservationUseCase;
  @Autowired private MeetingRoomCommandPort meetingRoomCommandPort;

  @MockBean private org.redisson.api.RedissonClient redissonClient;
  @MockBean private PaymentEventProducer paymentEventProducer;
  @MockBean private PaymentCancelEventProducer paymentCancelEventProducer;
  @MockBean private PaymentDlqProducer paymentDlqProducer;
  @MockBean private PaymentOutboxRelay paymentOutboxRelay;
  @MockBean private PaymentProcessListener paymentProcessListener;
  @MockBean private PaymentCancelListener paymentCancelListener;

  private Long meetingRoomId1;
  private Long meetingRoomId2;
  private Long userId1;
  private Long userId2;
  private BigDecimal totalAmount;

  @BeforeEach
  void setUp() {
    MeetingRoom room1 = MeetingRoom.create("회의실 A", 10, new BigDecimal("10000"), "테스트용 회의실 A");
    MeetingRoom room2 = MeetingRoom.create("회의실 B", 10, new BigDecimal("10000"), "테스트용 회의실 B");
    meetingRoomId1 = meetingRoomCommandPort.save(room1).getId();
    meetingRoomId2 = meetingRoomCommandPort.save(room2).getId();

    userId1 = 1L;
    userId2 = 2L;
    totalAmount = new BigDecimal("10000");
  }

  @Test
  @DisplayName("시나리오 1: 사용자 A(시간 1→2 변경) vs 사용자 B(시간 2→1 변경) - Deadlock 방지")
  void scenario1_timeSwapDeadlockPrevention() throws InterruptedException {
    LocalDateTime time1 = LocalDateTime.of(2024, 12, 1, 10, 0);
    LocalDateTime time2 = LocalDateTime.of(2024, 12, 1, 11, 0);

    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch latch = new CountDownLatch(2);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);

    // 사용자 A: time1 → time2로 예약 시도
    executor.submit(
        () -> {
          try {
            createReservationUseCase.createReservation(
                meetingRoomId1, userId1, time1, time2, totalAmount);
            successCount.incrementAndGet();
          } catch (Exception e) {
            failureCount.incrementAndGet();
          } finally {
            latch.countDown();
          }
        });

    // 사용자 B: time2 → time1로 예약 시도 (같은 회의실)
    executor.submit(
        () -> {
          try {
            createReservationUseCase.createReservation(
                meetingRoomId1, userId2, time2, time1, totalAmount);
            successCount.incrementAndGet();
          } catch (Exception e) {
            failureCount.incrementAndGet();
          } finally {
            latch.countDown();
          }
        });

    assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();

    // 하나는 성공하고 하나는 실패해야 함 (시간 범위 검증 실패 또는 중복 예약)
    assertThat(successCount.get() + failureCount.get()).isEqualTo(2);

    executor.shutdown();
  }

  @Test
  @DisplayName("시나리오 2: 결제 처리 중 동일 회의실 예약 시도 - Deadlock 방지")
  void scenario2_paymentProcessingConcurrentReservation() throws InterruptedException {
    LocalDateTime startTime = LocalDateTime.of(2024, 12, 1, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 12, 1, 11, 0);

    // 첫 번째 예약 생성
    ReservationResponse firstReservation =
        createReservationUseCase.createReservation(
            meetingRoomId1, userId1, startTime, endTime, totalAmount);

    assertThat(firstReservation).isNotNull();

    ExecutorService executor = Executors.newFixedThreadPool(3);
    CountDownLatch latch = new CountDownLatch(3);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);

    // 동시에 같은 시간대 예약 시도 (3개)
    for (int i = 0; i < 3; i++) {
      final int index = i;
      executor.submit(
          () -> {
            try {
              createReservationUseCase.createReservation(
                  meetingRoomId1, userId2 + index, startTime, endTime, totalAmount);
              successCount.incrementAndGet();
            } catch (ReservationException e) {
              if (e.getErrorCode() == ReservationErrorStatus.DUPLICATE_RESERVATION) {
                failureCount.incrementAndGet();
              } else {
                failureCount.incrementAndGet();
              }
            } catch (Exception e) {
              failureCount.incrementAndGet();
            } finally {
              latch.countDown();
            }
          });
    }

    assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();

    // 모든 시도가 실패해야 함 (이미 예약이 존재하므로)
    assertThat(successCount.get()).isEqualTo(0);
    assertThat(failureCount.get()).isEqualTo(3);

    executor.shutdown();
  }

  @Test
  @DisplayName("동시 예약 생성 시 Lock 순서 일관성 테스트")
  void concurrentReservationLockOrderConsistency() throws InterruptedException {
    LocalDateTime startTime = LocalDateTime.of(2024, 12, 1, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 12, 1, 11, 0);

    int threadCount = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);

    // 동시에 같은 시간대 예약 시도
    for (int i = 0; i < threadCount; i++) {
      final int index = i;
      executor.submit(
          () -> {
            try {
              createReservationUseCase.createReservation(
                  meetingRoomId1, userId1 + index, startTime, endTime, totalAmount);
              successCount.incrementAndGet();
            } catch (Exception e) {
              failureCount.incrementAndGet();
            } finally {
              latch.countDown();
            }
          });
    }

    assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();

    // 하나만 성공하고 나머지는 실패해야 함
    assertThat(successCount.get()).isEqualTo(1);
    assertThat(failureCount.get()).isEqualTo(threadCount - 1);

    executor.shutdown();
  }
}
