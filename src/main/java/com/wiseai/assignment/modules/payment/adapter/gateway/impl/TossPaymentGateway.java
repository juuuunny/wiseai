package com.wiseai.assignment.modules.payment.adapter.gateway.impl;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.wiseai.assignment.modules.payment.application.port.out.gateway.PaymentGateway;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TossPaymentGateway implements PaymentGateway {

  private final RestClient restClient;
  private final String gatewayUrl;

  public TossPaymentGateway(
      RestClient.Builder restClientBuilder,
      @Value("${payment.gateway.toss.url:http://wiremock:8080/toss/payments}") String gatewayUrl) {
    this.restClient =
        restClientBuilder
            .baseUrl(gatewayUrl)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
    this.gatewayUrl = gatewayUrl;
  }

  @Override
  public CompletableFuture<String> processPayment(BigDecimal amount, Long orderId) {
    log.debug("TOSS 결제 처리 시작: amount={}, orderId={}", amount, orderId);

    return CompletableFuture.supplyAsync(
        () -> {
          try {
            var request = new TossPaymentRequest(amount, orderId);
            var response =
                restClient.post().uri("/").body(request).retrieve().body(TossPaymentResponse.class);

            if (response == null || response.transactionId() == null) {
              throw new RuntimeException("TOSS 결제 처리 실패: 응답이 null입니다.");
            }

            log.debug("TOSS 결제 처리 완료: transactionId={}", response.transactionId());
            return response.transactionId();
          } catch (Exception e) {
            log.error("TOSS 결제 처리 중 오류 발생: amount={}, orderId={}", amount, orderId, e);
            throw new RuntimeException("TOSS 결제 처리 실패", e);
          }
        });
  }

  @Override
  public CompletableFuture<Boolean> cancelPayment(String transactionId) {
    log.debug("TOSS 결제 취소 시작: transactionId={}", transactionId);

    return CompletableFuture.supplyAsync(
        () -> {
          try {
            restClient.post().uri("/" + transactionId + "/cancel").retrieve().toBodilessEntity();

            log.debug("TOSS 결제 취소 완료: transactionId={}", transactionId);
            return true;
          } catch (Exception e) {
            log.error("TOSS 결제 취소 중 오류 발생: transactionId={}", transactionId, e);
            return false;
          }
        });
  }

  @Override
  public PaymentMethod getSupportedPaymentMethod() {
    return PaymentMethod.TOSS;
  }

  private record TossPaymentRequest(BigDecimal amount, Long orderId) {}

  private record TossPaymentResponse(String transactionId, String status) {}
}
