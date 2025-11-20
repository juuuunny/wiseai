package com.wiseai.assignment.modules.payment.adapter.gateway.impl;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.wiseai.assignment.modules.payment.application.port.out.gateway.PaymentGateway;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.model.PaymentResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KakaoPaymentGateway implements PaymentGateway {

  private final RestClient restClient;
  private final String gatewayUrl;

  public KakaoPaymentGateway(
      RestClient.Builder restClientBuilder,
      @Value("${payment.gateway.kakao.url:http://wiremock:8080/kakao/payments}")
          String gatewayUrl) {
    this.restClient =
        restClientBuilder
            .baseUrl(gatewayUrl)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
    this.gatewayUrl = gatewayUrl;
  }

  @Override
  public CompletableFuture<PaymentResult> processPayment(BigDecimal amount, Long orderId) {
    log.debug("KAKAO 결제 처리 시작: amount={}, orderId={}", amount, orderId);

    return CompletableFuture.supplyAsync(
        () -> {
          try {
            var request = new KakaoPaymentRequest(amount, orderId);
            var response =
                restClient
                    .post()
                    .uri("/")
                    .body(request)
                    .retrieve()
                    .body(KakaoPaymentResponse.class);

            if (response == null || response.transactionId() == null) {
              log.error("KAKAO 결제 처리 실패: 응답이 null입니다. amount={}, orderId={}", amount, orderId);
              return PaymentResult.failure("KAKAO 결제 처리 실패: 응답이 null입니다.");
            }

            log.debug("KAKAO 결제 처리 완료: transactionId={}", response.transactionId());
            // KAKAO 결제사 응답을 PaymentResult 공통 모델로 변환
            return PaymentResult.success(response.transactionId(), amount);
          } catch (Exception e) {
            log.error("KAKAO 결제 처리 중 오류 발생: amount={}, orderId={}", amount, orderId, e);
            return PaymentResult.failure("KAKAO 결제 처리 실패: " + e.getMessage());
          }
        });
  }

  @Override
  public CompletableFuture<Boolean> cancelPayment(String transactionId) {
    log.debug("KAKAO 결제 취소 시작: transactionId={}", transactionId);

    return CompletableFuture.supplyAsync(
        () -> {
          try {
            restClient.post().uri("/" + transactionId + "/cancel").retrieve().toBodilessEntity();

            log.debug("KAKAO 결제 취소 완료: transactionId={}", transactionId);
            return true;
          } catch (Exception e) {
            log.error("KAKAO 결제 취소 중 오류 발생: transactionId={}", transactionId, e);
            return false;
          }
        });
  }

  @Override
  public PaymentMethod getSupportedPaymentMethod() {
    return PaymentMethod.KAKAO;
  }

  private record KakaoPaymentRequest(BigDecimal amount, Long orderId) {}

  private record KakaoPaymentResponse(String transactionId, String status) {}
}
