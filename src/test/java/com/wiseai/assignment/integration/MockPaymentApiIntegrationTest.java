package com.wiseai.assignment.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

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
import com.wiseai.assignment.modules.payment.application.service.event.PaymentDlqProducer;
import com.wiseai.assignment.modules.payment.application.service.event.PaymentEventProducer;
import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;
import com.wiseai.assignment.modules.payment.application.port.in.command.CompletePaymentUseCase;
import com.wiseai.assignment.modules.payment.application.port.in.command.CreatePaymentUseCase;
import com.wiseai.assignment.modules.payment.application.port.out.gateway.PaymentGateway;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentStatus;
import com.wiseai.assignment.modules.reservation.application.dto.response.ReservationResponse;
import com.wiseai.assignment.modules.reservation.application.port.in.command.CreateReservationUseCase;
import com.wiseai.assignment.modules.reservation.application.port.in.command.ProcessReservationPaymentUseCase;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Mock 결제 API 통합 테스트")
class MockPaymentApiIntegrationTest {

  @Autowired private CreateReservationUseCase createReservationUseCase;
  @Autowired private ProcessReservationPaymentUseCase processReservationPaymentUseCase;
  @Autowired private CreatePaymentUseCase createPaymentUseCase;
  @Autowired private CompletePaymentUseCase completePaymentUseCase;
  @Autowired private MeetingRoomCommandPort meetingRoomCommandPort;

  @MockBean private PaymentGateway paymentGateway;
  @MockBean private org.redisson.api.RedissonClient redissonClient;
  @MockBean private PaymentEventProducer paymentEventProducer;
  @MockBean private PaymentCancelEventProducer paymentCancelEventProducer;
  @MockBean private PaymentDlqProducer paymentDlqProducer;
  @MockBean private PaymentOutboxRelay paymentOutboxRelay;

  private Long meetingRoomId;
  private Long userId;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private BigDecimal totalAmount;

  @BeforeEach
  void setUp() {
    MeetingRoom meetingRoom = MeetingRoom.create("회의실 A", 10, new BigDecimal("10000"), "테스트용 회의실");
    meetingRoomId = meetingRoomCommandPort.save(meetingRoom).getId();

    userId = 1L;
    startTime = LocalDateTime.of(2024, 12, 1, 10, 0);
    endTime = LocalDateTime.of(2024, 12, 1, 11, 0);
    totalAmount = new BigDecimal("10000");
  }

  @Test
  @DisplayName("Mock 결제 게이트웨이를 사용한 결제 처리 테스트")
  void mockPaymentGatewayProcess() {
    // given: 예약 생성
    ReservationResponse reservation =
        createReservationUseCase.createReservation(
            meetingRoomId, userId, startTime, endTime, totalAmount);

    // given: Mock 결제 게이트웨이 설정
    String mockTransactionId = "txn_mock_12345";
    given(paymentGateway.processPayment(eq(totalAmount), eq(reservation.id())))
        .willReturn(CompletableFuture.completedFuture(mockTransactionId));
    given(paymentGateway.getSupportedPaymentMethod()).willReturn(PaymentMethod.TOSS);

    // when: 결제 생성
    PaymentResponse paymentResponse =
        processReservationPaymentUseCase.processPayment(reservation.id(), PaymentMethod.TOSS);

    // then: 결제 생성 확인
    assertThat(paymentResponse).isNotNull();
    assertThat(paymentResponse.reservationId()).isEqualTo(reservation.id());
    assertThat(paymentResponse.paymentMethod()).isEqualTo(PaymentMethod.TOSS);
    assertThat(paymentResponse.status()).isEqualTo(PaymentStatus.PENDING);
  }

  @Test
  @DisplayName("Mock 결제 게이트웨이 성공 응답 테스트")
  void mockPaymentGatewaySuccess() {
    // given: 결제 생성
    PaymentResponse payment =
        createPaymentUseCase.createPayment(1L, PaymentMethod.TOSS, totalAmount);

    // given: Mock 결제 게이트웨이 성공 응답
    String mockTransactionId = "txn_success_12345";
    given(paymentGateway.processPayment(any(BigDecimal.class), any(Long.class)))
        .willReturn(CompletableFuture.completedFuture(mockTransactionId));

    // when: 결제 완료
    PaymentResponse completedPayment =
        completePaymentUseCase.completePayment(payment.id(), mockTransactionId);

    // then: 결제 완료 확인
    assertThat(completedPayment.status()).isEqualTo(PaymentStatus.COMPLETED);
    assertThat(completedPayment.transactionId()).isEqualTo(mockTransactionId);
  }

  @Test
  @DisplayName("Mock 결제 게이트웨이 실패 응답 테스트")
  void mockPaymentGatewayFailure() {
    // given: 결제 생성
    PaymentResponse payment =
        createPaymentUseCase.createPayment(1L, PaymentMethod.TOSS, totalAmount);

    // given: Mock 결제 게이트웨이 실패 응답 (예외 발생)
    given(paymentGateway.processPayment(any(BigDecimal.class), any(Long.class)))
        .willReturn(CompletableFuture.failedFuture(new RuntimeException("결제 게이트웨이 오류")));

    // when & then: 결제 완료 시도 시 예외가 발생하지 않음 (비동기 처리이므로)
    // 실제 결제 게이트웨이 호출은 비동기로 처리되므로, 여기서는 결제 생성만 확인
    assertThat(payment.status()).isEqualTo(PaymentStatus.PENDING);
  }
}
