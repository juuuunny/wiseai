package com.wiseai.assignment.modules.payment.adapter.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.wiseai.assignment.modules.common.dto.response.SuccessResponse;
import com.wiseai.assignment.modules.common.status.CommonSuccessStatus;
import com.wiseai.assignment.modules.payment.adapter.web.request.KakaoWebhookRequest;
import com.wiseai.assignment.modules.payment.adapter.web.request.TossWebhookRequest;
import com.wiseai.assignment.modules.payment.application.port.in.webhook.HandlePaymentWebhookUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentWebhookController implements PaymentWebhookApi {

  private final HandlePaymentWebhookUseCase handlePaymentWebhookUseCase;

  @Override
  public ResponseEntity<SuccessResponse<Void>> handleTossWebhook(TossWebhookRequest request) {
    log.info(
        "TOSS 웹훅 수신: paymentKey={}, orderId={}, status={}",
        request.paymentKey(),
        request.orderId(),
        request.status());

    handlePaymentWebhookUseCase.handleTossWebhook(
        request.paymentKey(),
        request.orderId(),
        request.status(),
        request.totalAmount(),
        request.transactionId(),
        request.failureCode(),
        request.failureMessage());

    log.debug("TOSS 웹훅 처리 완료: paymentKey={}", request.paymentKey());
    return ResponseEntity.status(HttpStatus.OK)
        .body(SuccessResponse.of(CommonSuccessStatus.OK, null));
  }

  @Override
  public ResponseEntity<SuccessResponse<Void>> handleKakaoWebhook(KakaoWebhookRequest request) {
    log.info(
        "KAKAO 웹훅 수신: paymentKey={}, orderId={}, status={}",
        request.paymentKey(),
        request.orderId(),
        request.status());

    handlePaymentWebhookUseCase.handleKakaoWebhook(
        request.paymentKey(),
        request.orderId(),
        request.status(),
        request.totalAmount(),
        request.transactionId(),
        request.failureCode(),
        request.failureMessage());

    log.debug("KAKAO 웹훅 처리 완료: paymentKey={}", request.paymentKey());
    return ResponseEntity.status(HttpStatus.OK)
        .body(SuccessResponse.of(CommonSuccessStatus.OK, null));
  }
}

