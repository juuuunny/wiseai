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

/**
 * 가상계좌 결제 게이트웨이 (C사)
 *
 * <p>가상계좌 결제를 처리합니다.
 */
@Slf4j
@Component
public class VirtualAccountPaymentGateway implements PaymentGateway {

  private final RestClient restClient;
  private final String gatewayUrl;

  public VirtualAccountPaymentGateway(
      RestClient.Builder restClientBuilder,
      @Value("${payment.gateway.virtual-account.url:http://wiremock:8080/virtual-account/payments}")
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
    log.debug("가상계좌 결제 처리 시작: amount={}, orderId={}", amount, orderId);

    return CompletableFuture.supplyAsync(
        () -> {
          try {
            var request = new VirtualAccountPaymentRequest(amount, orderId);
            var response =
                restClient
                    .post()
                    .uri("/")
                    .body(request)
                    .retrieve()
                    .body(VirtualAccountPaymentResponse.class);

            if (response == null || response.transactionId() == null) {
              log.error("가상계좌 결제 처리 실패: 응답이 null입니다. amount={}, orderId={}", amount, orderId);
              return PaymentResult.failure("가상계좌 결제 처리 실패: 응답이 null입니다.");
            }

            log.debug("가상계좌 결제 처리 완료: transactionId={}", response.transactionId());
            // 가상계좌 결제사 응답을 PaymentResult 공통 모델로 변환
            return PaymentResult.success(response.transactionId(), amount);
          } catch (Exception e) {
            log.error("가상계좌 결제 처리 중 오류 발생: amount={}, orderId={}", amount, orderId, e);
            return PaymentResult.failure("가상계좌 결제 처리 실패: " + e.getMessage());
          }
        });
  }

  @Override
  public CompletableFuture<Boolean> cancelPayment(String transactionId) {
    log.debug("가상계좌 결제 취소 시작: transactionId={}", transactionId);

    return CompletableFuture.supplyAsync(
        () -> {
          try {
            restClient.post().uri("/" + transactionId + "/cancel").retrieve().toBodilessEntity();

            log.debug("가상계좌 결제 취소 완료: transactionId={}", transactionId);
            return true;
          } catch (Exception e) {
            log.error("가상계좌 결제 취소 중 오류 발생: transactionId={}", transactionId, e);
            return false;
          }
        });
  }

  @Override
  public PaymentMethod getSupportedPaymentMethod() {
    return PaymentMethod.VIRTUAL_ACCOUNT;
  }

  private record VirtualAccountPaymentRequest(BigDecimal amount, Long orderId) {}

  private record VirtualAccountPaymentResponse(String transactionId, String status) {}
}
