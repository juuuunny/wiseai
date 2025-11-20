package com.wiseai.assignment.modules.payment.adapter.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.wiseai.assignment.modules.common.dto.response.SuccessResponse;
import com.wiseai.assignment.modules.common.status.CommonSuccessStatus;
import com.wiseai.assignment.modules.payment.adapter.web.request.KakaoWebhookRequest;
import com.wiseai.assignment.modules.payment.adapter.web.request.TossWebhookRequest;
import com.wiseai.assignment.modules.payment.application.port.in.webhook.HandlePaymentWebhookUseCase;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentWebhookController implements PaymentWebhookApi {

  private final HandlePaymentWebhookUseCase handlePaymentWebhookUseCase;
  private final ObjectMapper objectMapper;
  private final Validator validator;
  private final Validator validator;

  @Override
  public ResponseEntity<SuccessResponse<Void>> handleWebhook(String provider, Object request) {
    log.info("웹훅 수신: provider={}", provider);

    try {
      PaymentMethod paymentMethod = parseProvider(provider);

      switch (paymentMethod) {
        case TOSS:
          handleTossWebhook(request);
          break;
        case KAKAO:
          handleKakaoWebhook(request);
          break;
        case CARD:
          handleCardWebhook(request);
          break;
        case VIRTUAL_ACCOUNT:
          handleVirtualAccountWebhook(request);
          break;
        default:
          throw new PaymentException(PaymentErrorStatus.INVALID_PAYMENT_METHOD);
      }

      log.debug("웹훅 처리 완료: provider={}", provider);
      return ResponseEntity.status(HttpStatus.OK)
          .body(SuccessResponse.of(CommonSuccessStatus.OK, null));
    } catch (PaymentException e) {
      throw e;
    } catch (Exception e) {
      log.error("웹훅 처리 중 오류 발생: provider={}", provider, e);
      throw new PaymentException(PaymentErrorStatus.INVALID_PAYMENT_METHOD);
    }
  }

  private PaymentMethod parseProvider(String provider) {
    return switch (provider.toLowerCase()) {
      case "toss" -> PaymentMethod.TOSS;
      case "kakao" -> PaymentMethod.KAKAO;
      case "card" -> PaymentMethod.CARD;
      case "virtual-account", "virtualaccount" -> PaymentMethod.VIRTUAL_ACCOUNT;
      default -> throw new PaymentException(PaymentErrorStatus.INVALID_PAYMENT_METHOD);
    };
  }

  private void handleTossWebhook(Object request) {
    TossWebhookRequest tossRequest = objectMapper.convertValue(request, TossWebhookRequest.class);

    // 유효성 검사
    var violations = validator.validate(tossRequest);
    if (!violations.isEmpty()) {
      log.warn("TOSS 웹훅 유효성 검사 실패: violations={}", violations);
      throw new PaymentException(PaymentErrorStatus.INVALID_REQUEST);
    }

    log.info(
        "TOSS 웹훅 처리: paymentKey={}, orderId={}, status={}",
        tossRequest.paymentKey(),
        tossRequest.orderId(),
        tossRequest.status());

    handlePaymentWebhookUseCase.handleTossWebhook(
        tossRequest.paymentKey(),
        tossRequest.orderId(),
        tossRequest.status(),
        tossRequest.totalAmount(),
        tossRequest.transactionId(),
        tossRequest.failureCode(),
        tossRequest.failureMessage());
  }

  private void handleKakaoWebhook(Object request) {
    KakaoWebhookRequest kakaoRequest =
        objectMapper.convertValue(request, KakaoWebhookRequest.class);

    // 유효성 검사
    var violations = validator.validate(kakaoRequest);
    if (!violations.isEmpty()) {
      log.warn("KAKAO 웹훅 유효성 검사 실패: violations={}", violations);
      throw new PaymentException(PaymentErrorStatus.INVALID_REQUEST);
    }

    log.info(
        "KAKAO 웹훅 처리: paymentKey={}, orderId={}, status={}",
        kakaoRequest.paymentKey(),
        kakaoRequest.orderId(),
        kakaoRequest.status());

    handlePaymentWebhookUseCase.handleKakaoWebhook(
        kakaoRequest.paymentKey(),
        kakaoRequest.orderId(),
        kakaoRequest.status(),
        kakaoRequest.totalAmount(),
        kakaoRequest.transactionId(),
        kakaoRequest.failureCode(),
        kakaoRequest.failureMessage());
  }

  private void handleCardWebhook(Object request) {
    // TODO: 신용카드 웹훅 처리 로직 구현
    log.info("신용카드 웹훅 처리: request={}", request);
    // 현재는 기본 처리만 수행
  }

  private void handleVirtualAccountWebhook(Object request) {
    // TODO: 가상계좌 웹훅 처리 로직 구현
    log.info("가상계좌 웹훅 처리: request={}", request);
    // 현재는 기본 처리만 수행
  }
}
