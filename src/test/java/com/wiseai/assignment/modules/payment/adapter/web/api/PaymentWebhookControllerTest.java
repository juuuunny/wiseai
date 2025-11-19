package com.wiseai.assignment.modules.payment.adapter.web.api;

import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

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
import com.wiseai.assignment.modules.payment.adapter.web.request.KakaoWebhookRequest;
import com.wiseai.assignment.modules.payment.adapter.web.request.TossWebhookRequest;
import com.wiseai.assignment.modules.payment.application.port.in.webhook.HandlePaymentWebhookUseCase;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;
import com.wiseai.assignment.modules.security.config.SecurityConfig;
import com.wiseai.assignment.modules.security.filter.JwtFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
    controllers = PaymentWebhookController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = {
      @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = EnableWebSecurity.class),
      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
    })
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("PaymentWebhookController 테스트")
class PaymentWebhookControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private HandlePaymentWebhookUseCase handlePaymentWebhookUseCase;

  @Autowired private ObjectMapper objectMapper;

  private static final String PAYMENT_KEY = "payment-key-123";
  private static final String ORDER_ID = "payment-1";
  private static final String STATUS_DONE = "DONE";
  private static final String STATUS_FAILED = "FAILED";
  private static final BigDecimal TOTAL_AMOUNT = new BigDecimal("10000");
  private static final String TRANSACTION_ID = "txn-12345";

  @Test
  @DisplayName("TOSS 웹훅 수신 성공")
  void handleTossWebhook_success() throws Exception {
    // given
    TossWebhookRequest request =
        new TossWebhookRequest(
            PAYMENT_KEY, ORDER_ID, STATUS_DONE, TOTAL_AMOUNT, TRANSACTION_ID, null, null);

    doNothing()
        .when(handlePaymentWebhookUseCase)
        .handleTossWebhook(
            PAYMENT_KEY, ORDER_ID, STATUS_DONE, TOTAL_AMOUNT, TRANSACTION_ID, null, null);

    // when & then
    mockMvc
        .perform(
            post("/webhooks/payments/toss")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.httpStatus").value(200))
        .andExpect(jsonPath("$.code").value("COMMON-200"))
        .andExpect(jsonPath("$.message").value("성공입니다."));
  }

  @Test
  @DisplayName("TOSS 웹훅 수신 실패 - 결제를 찾을 수 없음")
  void handleTossWebhook_notFound() throws Exception {
    // given
    TossWebhookRequest request =
        new TossWebhookRequest(
            PAYMENT_KEY, ORDER_ID, STATUS_DONE, TOTAL_AMOUNT, TRANSACTION_ID, null, null);

    doThrow(new PaymentException(PaymentErrorStatus.NOT_FOUND))
        .when(handlePaymentWebhookUseCase)
        .handleTossWebhook(
            PAYMENT_KEY, ORDER_ID, STATUS_DONE, TOTAL_AMOUNT, TRANSACTION_ID, null, null);

    // when & then
    mockMvc
        .perform(
            post("/webhooks/payments/toss")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.httpStatus").value(404))
        .andExpect(jsonPath("$.code").value("PAYMENT-004"))
        .andExpect(jsonPath("$.message").value("결제를 찾을 수 없습니다."));
  }

  @Test
  @DisplayName("TOSS 웹훅 수신 실패 - 유효성 검사 실패 (paymentKey 누락)")
  void handleTossWebhook_validationFailed_missingPaymentKey() throws Exception {
    // given
    TossWebhookRequest request =
        new TossWebhookRequest(
            null, ORDER_ID, STATUS_DONE, TOTAL_AMOUNT, TRANSACTION_ID, null, null);

    // when & then
    mockMvc
        .perform(
            post("/webhooks/payments/toss")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("KAKAO 웹훅 수신 성공")
  void handleKakaoWebhook_success() throws Exception {
    // given
    KakaoWebhookRequest request =
        new KakaoWebhookRequest(
            PAYMENT_KEY, ORDER_ID, STATUS_DONE, TOTAL_AMOUNT, TRANSACTION_ID, null, null);

    doNothing()
        .when(handlePaymentWebhookUseCase)
        .handleKakaoWebhook(
            PAYMENT_KEY, ORDER_ID, STATUS_DONE, TOTAL_AMOUNT, TRANSACTION_ID, null, null);

    // when & then
    mockMvc
        .perform(
            post("/webhooks/payments/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.httpStatus").value(200))
        .andExpect(jsonPath("$.code").value("COMMON-200"))
        .andExpect(jsonPath("$.message").value("성공입니다."));
  }

  @Test
  @DisplayName("KAKAO 웹훅 수신 실패 - 결제를 찾을 수 없음")
  void handleKakaoWebhook_notFound() throws Exception {
    // given
    KakaoWebhookRequest request =
        new KakaoWebhookRequest(
            PAYMENT_KEY, ORDER_ID, STATUS_DONE, TOTAL_AMOUNT, TRANSACTION_ID, null, null);

    doThrow(new PaymentException(PaymentErrorStatus.NOT_FOUND))
        .when(handlePaymentWebhookUseCase)
        .handleKakaoWebhook(
            PAYMENT_KEY, ORDER_ID, STATUS_DONE, TOTAL_AMOUNT, TRANSACTION_ID, null, null);

    // when & then
    mockMvc
        .perform(
            post("/webhooks/payments/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.httpStatus").value(404))
        .andExpect(jsonPath("$.code").value("PAYMENT-004"))
        .andExpect(jsonPath("$.message").value("결제를 찾을 수 없습니다."));
  }

  @Test
  @DisplayName("KAKAO 웹훅 수신 실패 - 유효성 검사 실패 (orderId 누락)")
  void handleKakaoWebhook_validationFailed_missingOrderId() throws Exception {
    // given
    KakaoWebhookRequest request =
        new KakaoWebhookRequest(
            PAYMENT_KEY, null, STATUS_DONE, TOTAL_AMOUNT, TRANSACTION_ID, null, null);

    // when & then
    mockMvc
        .perform(
            post("/webhooks/payments/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }
}
