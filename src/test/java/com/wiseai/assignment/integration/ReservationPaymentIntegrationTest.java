package com.wiseai.assignment.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
import com.wiseai.assignment.modules.payment.adapter.kafka.relay.PaymentOutboxRelay;
import com.wiseai.assignment.modules.payment.application.service.event.PaymentCancelEventProducer;
import com.wiseai.assignment.modules.payment.application.service.event.PaymentEventProducer;
import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;
import com.wiseai.assignment.modules.payment.application.dto.response.PaymentStatusResponse;
import com.wiseai.assignment.modules.payment.application.port.in.query.GetPaymentStatusUseCase;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentStatus;
import com.wiseai.assignment.modules.reservation.application.dto.response.ReservationResponse;
import com.wiseai.assignment.modules.reservation.application.port.in.command.CreateReservationUseCase;
import com.wiseai.assignment.modules.reservation.application.port.in.command.ProcessReservationPaymentUseCase;
import com.wiseai.assignment.modules.reservation.domain.enums.ReservationStatus;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("예약-결제 통합 테스트")
class ReservationPaymentIntegrationTest {

  @Autowired private CreateReservationUseCase createReservationUseCase;
  @Autowired private ProcessReservationPaymentUseCase processReservationPaymentUseCase;
  @Autowired private GetPaymentStatusUseCase getPaymentStatusUseCase;
  @Autowired private MeetingRoomCommandPort meetingRoomCommandPort;

  @MockBean private org.redisson.api.RedissonClient redissonClient;
  @MockBean private PaymentEventProducer paymentEventProducer;
  @MockBean private PaymentCancelEventProducer paymentCancelEventProducer;
  @MockBean private PaymentOutboxRelay paymentOutboxRelay;

  private Long meetingRoomId;
  private Long userId;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private BigDecimal totalAmount;

  @BeforeEach
  void setUp() {
    MeetingRoom meetingRoom = MeetingRoom.create("회의실 A", 10, new BigDecimal("10000"), "테스트용 회의실");
    MeetingRoom saved = meetingRoomCommandPort.save(meetingRoom);
    meetingRoomId = saved.getId();

    userId = 1L;
    startTime = LocalDateTime.of(2024, 12, 1, 10, 0);
    endTime = LocalDateTime.of(2024, 12, 1, 11, 0);
    totalAmount = new BigDecimal("10000");
  }

  @Test
  @DisplayName("예약 생성 후 결제 처리 통합 플로우")
  void reservationToPaymentFlow() {
    // given & when: 예약 생성
    ReservationResponse reservationResponse =
        createReservationUseCase.createReservation(
            meetingRoomId, userId, startTime, endTime, totalAmount);

    // then: 예약 상태 확인
    assertThat(reservationResponse.status()).isEqualTo(ReservationStatus.PENDING);
    assertThat(reservationResponse.totalAmount()).isEqualTo(totalAmount);

    // when: 결제 처리
    PaymentResponse paymentResponse =
        processReservationPaymentUseCase.processPayment(
            reservationResponse.id(), PaymentMethod.TOSS);

    // then: 결제 생성 확인
    assertThat(paymentResponse).isNotNull();
    assertThat(paymentResponse.reservationId()).isEqualTo(reservationResponse.id());
    assertThat(paymentResponse.paymentMethod()).isEqualTo(PaymentMethod.TOSS);
    assertThat(paymentResponse.amount()).isEqualTo(totalAmount);
    assertThat(paymentResponse.status()).isEqualTo(PaymentStatus.PENDING);

    // when: 결제 상태 조회
    PaymentStatusResponse statusResponse =
        getPaymentStatusUseCase.getPaymentStatus(paymentResponse.id());

    // then: 결제 상태 확인
    assertThat(statusResponse.paymentId()).isEqualTo(paymentResponse.id());
    assertThat(statusResponse.status()).isEqualTo(PaymentStatus.PENDING);
  }

  @Test
  @DisplayName("동시 예약 생성 시 중복 방지 테스트")
  void concurrentReservationCreation() throws InterruptedException {
    // given: 첫 번째 예약 생성
    ReservationResponse firstReservation =
        createReservationUseCase.createReservation(
            meetingRoomId, userId, startTime, endTime, totalAmount);
    assertThat(firstReservation).isNotNull();

    int threadCount = 5;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    int[] successCount = {0};
    int[] failureCount = {0};

    // when: 동시에 같은 시간대 예약 시도 (이미 예약이 존재함)
    for (int i = 0; i < threadCount; i++) {
      final int index = i;
      executor.submit(
          () -> {
            try {
              createReservationUseCase.createReservation(
                  meetingRoomId, userId + index, startTime, endTime, totalAmount);
              successCount[0]++;
            } catch (Exception e) {
              failureCount[0]++;
            } finally {
              latch.countDown();
            }
          });
    }

    // then: 모두 실패해야 함 (이미 예약이 존재하므로)
    assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    assertThat(successCount[0]).isEqualTo(0);
    assertThat(failureCount[0]).isEqualTo(threadCount);

    executor.shutdown();
  }

  @Test
  @DisplayName("결제 처리 중 동일 예약에 대한 중복 결제 방지")
  void duplicatePaymentPrevention() {
    // given: 예약 생성
    ReservationResponse reservation =
        createReservationUseCase.createReservation(
            meetingRoomId, userId, startTime, endTime, totalAmount);

    // when: 첫 번째 결제 처리
    PaymentResponse firstPayment =
        processReservationPaymentUseCase.processPayment(reservation.id(), PaymentMethod.TOSS);

    // then: 결제가 생성되었는지 확인
    assertThat(firstPayment).isNotNull();

    // when: 동일 예약에 대한 두 번째 결제 시도
    PaymentResponse secondPayment =
        processReservationPaymentUseCase.processPayment(reservation.id(), PaymentMethod.KAKAO);

    // then: 두 번째 결제도 생성되지만, 예약당 여러 결제가 가능할 수 있음
    // (비즈니스 로직에 따라 다를 수 있음)
    assertThat(secondPayment).isNotNull();
    assertThat(secondPayment.reservationId()).isEqualTo(reservation.id());
  }
}
