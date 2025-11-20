package com.wiseai.assignment.modules.payment.adapter.web.api;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.web.servlet.MockMvc;

import com.wiseai.assignment.modules.common.exception.GlobalExceptionHandler;
import com.wiseai.assignment.modules.payment.application.dto.request.CompletePaymentRequest;
import com.wiseai.assignment.modules.payment.application.dto.request.CreatePaymentRequest;
import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;
import com.wiseai.assignment.modules.payment.application.dto.response.PaymentStatusResponse;
import com.wiseai.assignment.modules.payment.application.port.in.command.CancelPaymentUseCase;
import com.wiseai.assignment.modules.payment.application.port.in.command.CompletePaymentUseCase;
import com.wiseai.assignment.modules.payment.application.port.in.command.CreatePaymentUseCase;
import com.wiseai.assignment.modules.payment.application.port.in.query.GetPaymentStatusUseCase;
import com.wiseai.assignment.modules.payment.application.port.in.query.GetPaymentUseCase;
import com.wiseai.assignment.modules.payment.application.port.in.query.GetPaymentsUseCase;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentStatus;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;
import com.wiseai.assignment.modules.security.config.SecurityConfig;
import com.wiseai.assignment.modules.security.filter.JwtFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
    controllers = PaymentController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = {
      @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = EnableWebSecurity.class),
      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
    })
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("PaymentController 테스트")
class PaymentControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private CreatePaymentUseCase createPaymentUseCase;
  @MockBean private CompletePaymentUseCase completePaymentUseCase;
  @MockBean private CancelPaymentUseCase cancelPaymentUseCase;
  @MockBean private GetPaymentUseCase getPaymentUseCase;
  @MockBean private GetPaymentsUseCase getPaymentsUseCase;
  @MockBean private GetPaymentStatusUseCase getPaymentStatusUseCase;

  private static final Long DEFAULT_RESERVATION_ID = 1L;
  private static final Long DEFAULT_PAYMENT_ID = 1L;
  private static final Long PAYMENT_ID_NOT_FOUND = 999L;
  private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("10000");
  private static final String DEFAULT_TRANSACTION_ID = "txn_12345";

  @Test
  @DisplayName("결제 생성 성공")
  void createPayment_success() throws Exception {
    // given
    CreatePaymentRequest request =
        new CreatePaymentRequest(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);

    PaymentResponse response =
        new PaymentResponse(
            DEFAULT_PAYMENT_ID,
            DEFAULT_RESERVATION_ID,
            PaymentMethod.TOSS,
            DEFAULT_AMOUNT,
            PaymentStatus.PENDING,
            null);

    given(
            createPaymentUseCase.createPayment(
                DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT))
        .willReturn(response);

    // when & then
    mockMvc
        .perform(
            post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.httpStatus").value(201))
        .andExpect(jsonPath("$.code").value("PAYMENT-001"))
        .andExpect(jsonPath("$.message").value("결제 생성에 성공했습니다."))
        .andExpect(jsonPath("$.data.id").value(DEFAULT_PAYMENT_ID))
        .andExpect(jsonPath("$.data.reservationId").value(DEFAULT_RESERVATION_ID))
        .andExpect(jsonPath("$.data.paymentMethod").value("TOSS"))
        .andExpect(jsonPath("$.data.status").value("PENDING"));
  }

  @Test
  @DisplayName("결제 완료 성공")
  void completePayment_success() throws Exception {
    // given
    CompletePaymentRequest request = new CompletePaymentRequest(DEFAULT_TRANSACTION_ID);

    PaymentResponse response =
        new PaymentResponse(
            DEFAULT_PAYMENT_ID,
            DEFAULT_RESERVATION_ID,
            PaymentMethod.TOSS,
            DEFAULT_AMOUNT,
            PaymentStatus.COMPLETED,
            DEFAULT_TRANSACTION_ID);

    given(completePaymentUseCase.completePayment(DEFAULT_PAYMENT_ID, DEFAULT_TRANSACTION_ID))
        .willReturn(response);

    // when & then
    mockMvc
        .perform(
            patch("/payments/{id}/complete", DEFAULT_PAYMENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.httpStatus").value(200))
        .andExpect(jsonPath("$.code").value("PAYMENT-002"))
        .andExpect(jsonPath("$.message").value("결제 완료에 성공했습니다."))
        .andExpect(jsonPath("$.data.status").value("COMPLETED"))
        .andExpect(jsonPath("$.data.transactionId").value(DEFAULT_TRANSACTION_ID));
  }

  @Test
  @DisplayName("결제 완료 실패 - 존재하지 않는 결제")
  void completePayment_fail_notFound() throws Exception {
    // given
    CompletePaymentRequest request = new CompletePaymentRequest(DEFAULT_TRANSACTION_ID);

    given(completePaymentUseCase.completePayment(PAYMENT_ID_NOT_FOUND, DEFAULT_TRANSACTION_ID))
        .willThrow(new PaymentException(PaymentErrorStatus.NOT_FOUND));

    // when & then
    mockMvc
        .perform(
            patch("/payments/{id}/complete", PAYMENT_ID_NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.httpStatus").value(404))
        .andExpect(jsonPath("$.code").value("PAYMENT-004"));
  }

  @Test
  @DisplayName("결제 취소 성공")
  void cancelPayment_success() throws Exception {
    // given
    PaymentResponse response =
        new PaymentResponse(
            DEFAULT_PAYMENT_ID,
            DEFAULT_RESERVATION_ID,
            PaymentMethod.TOSS,
            DEFAULT_AMOUNT,
            PaymentStatus.CANCELLED,
            null);

    given(cancelPaymentUseCase.cancelPayment(DEFAULT_PAYMENT_ID)).willReturn(response);

    // when & then
    mockMvc
        .perform(patch("/payments/{id}/cancel", DEFAULT_PAYMENT_ID))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.httpStatus").value(200))
        .andExpect(jsonPath("$.code").value("PAYMENT-003"))
        .andExpect(jsonPath("$.message").value("결제 취소에 성공했습니다."))
        .andExpect(jsonPath("$.data.status").value("CANCELLED"));
  }

  @Test
  @DisplayName("결제 취소 실패 - 존재하지 않는 결제")
  void cancelPayment_fail_notFound() throws Exception {
    // given
    given(cancelPaymentUseCase.cancelPayment(PAYMENT_ID_NOT_FOUND))
        .willThrow(new PaymentException(PaymentErrorStatus.NOT_FOUND));

    // when & then
    mockMvc
        .perform(patch("/payments/{id}/cancel", PAYMENT_ID_NOT_FOUND))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.httpStatus").value(404))
        .andExpect(jsonPath("$.code").value("PAYMENT-004"));
  }

  @Test
  @DisplayName("결제 단건 조회 성공")
  void getPayment_success() throws Exception {
    // given
    PaymentResponse response =
        new PaymentResponse(
            DEFAULT_PAYMENT_ID,
            DEFAULT_RESERVATION_ID,
            PaymentMethod.TOSS,
            DEFAULT_AMOUNT,
            PaymentStatus.PENDING,
            null);

    given(getPaymentUseCase.getPayment(DEFAULT_PAYMENT_ID)).willReturn(response);

    // when & then
    mockMvc
        .perform(get("/payments/{id}", DEFAULT_PAYMENT_ID))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.httpStatus").value(200))
        .andExpect(jsonPath("$.code").value("PAYMENT-004"))
        .andExpect(jsonPath("$.message").value("결제 조회에 성공했습니다."))
        .andExpect(jsonPath("$.data.id").value(DEFAULT_PAYMENT_ID));
  }

  @Test
  @DisplayName("결제 단건 조회 실패 - 존재하지 않는 ID")
  void getPayment_notFound() throws Exception {
    // given
    given(getPaymentUseCase.getPayment(PAYMENT_ID_NOT_FOUND))
        .willThrow(new PaymentException(PaymentErrorStatus.NOT_FOUND));

    // when & then
    mockMvc
        .perform(get("/payments/{id}", PAYMENT_ID_NOT_FOUND))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.httpStatus").value(404))
        .andExpect(jsonPath("$.code").value("PAYMENT-004"));
  }

  @Test
  @DisplayName("예약별 결제 목록 조회 성공")
  void getPaymentsByReservationId_success() throws Exception {
    // given
    PaymentResponse response1 =
        new PaymentResponse(
            DEFAULT_PAYMENT_ID,
            DEFAULT_RESERVATION_ID,
            PaymentMethod.TOSS,
            DEFAULT_AMOUNT,
            PaymentStatus.PENDING,
            null);

    given(getPaymentsUseCase.getPaymentsByReservationId(DEFAULT_RESERVATION_ID))
        .willReturn(List.of(response1));

    // when & then
    mockMvc
        .perform(get("/payments/reservations/{reservationId}", DEFAULT_RESERVATION_ID))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.httpStatus").value(200))
        .andExpect(jsonPath("$.code").value("PAYMENT-005"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].id").value(DEFAULT_PAYMENT_ID));
  }

  @Test
  @DisplayName("결제 상태 조회 성공")
  void getPaymentStatus_success() throws Exception {
    // given
    PaymentStatusResponse response =
        new PaymentStatusResponse(
            DEFAULT_PAYMENT_ID, PaymentStatus.COMPLETED, DEFAULT_TRANSACTION_ID);

    given(getPaymentStatusUseCase.getPaymentStatus(DEFAULT_PAYMENT_ID)).willReturn(response);

    // when & then
    mockMvc
        .perform(get("/payments/{paymentId}/status", DEFAULT_PAYMENT_ID))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.httpStatus").value(200))
        .andExpect(jsonPath("$.code").value("PAYMENT-006"))
        .andExpect(jsonPath("$.message").value("결제 상태 조회에 성공했습니다."))
        .andExpect(jsonPath("$.data.paymentId").value(DEFAULT_PAYMENT_ID))
        .andExpect(jsonPath("$.data.status").value("COMPLETED"))
        .andExpect(jsonPath("$.data.transactionId").value(DEFAULT_TRANSACTION_ID));
  }

  @Test
  @DisplayName("결제 상태 조회 실패 - 존재하지 않는 결제")
  void getPaymentStatus_notFound() throws Exception {
    // given
    given(getPaymentStatusUseCase.getPaymentStatus(PAYMENT_ID_NOT_FOUND))
        .willThrow(new PaymentException(PaymentErrorStatus.NOT_FOUND));

    // when & then
    mockMvc
        .perform(get("/payments/{paymentId}/status", PAYMENT_ID_NOT_FOUND))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.httpStatus").value(404))
        .andExpect(jsonPath("$.code").value("PAYMENT-004"))
        .andExpect(jsonPath("$.message").value("결제를 찾을 수 없습니다."));
  }

  @Test
  @DisplayName("결제 상태 조회 실패 - 잘못된 ID (0)")
  void getPaymentStatus_invalidId_zero() throws Exception {
    // when & then
    mockMvc
        .perform(get("/payments/{paymentId}/status", 0))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("결제 상태 조회 - PENDING 상태")
  void getPaymentStatus_pending() throws Exception {
    // given
    PaymentStatusResponse response =
        new PaymentStatusResponse(DEFAULT_PAYMENT_ID, PaymentStatus.PENDING, null);

    given(getPaymentStatusUseCase.getPaymentStatus(DEFAULT_PAYMENT_ID)).willReturn(response);

    // when & then
    mockMvc
        .perform(get("/payments/{paymentId}/status", DEFAULT_PAYMENT_ID))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("PENDING"))
        .andExpect(jsonPath("$.data.transactionId").isEmpty());
  }
}
