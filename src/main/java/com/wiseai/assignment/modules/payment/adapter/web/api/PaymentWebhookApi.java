package com.wiseai.assignment.modules.payment.adapter.web.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.wiseai.assignment.modules.common.dto.response.SuccessResponse;
import com.wiseai.assignment.modules.payment.adapter.web.request.KakaoWebhookRequest;
import com.wiseai.assignment.modules.payment.adapter.web.request.TossWebhookRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Payment Webhook", description = "결제사 웹훅 수신 API")
public interface PaymentWebhookApi {

  @Operation(summary = "TOSS 결제 웹훅 수신", description = "TOSS 결제사로부터 결제 상태 변경 알림을 수신합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "웹훅 처리 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 웹훅 데이터"),
    @ApiResponse(responseCode = "401", description = "웹훅 서명 검증 실패")
  })
  @PostMapping("/webhooks/payments/toss")
  ResponseEntity<SuccessResponse<Void>> handleTossWebhook(
      @Valid @RequestBody TossWebhookRequest request);

  @Operation(summary = "KAKAO 결제 웹훅 수신", description = "KAKAO 결제사로부터 결제 상태 변경 알림을 수신합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "웹훅 처리 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 웹훅 데이터"),
    @ApiResponse(responseCode = "401", description = "웹훅 서명 검증 실패")
  })
  @PostMapping("/webhooks/payments/kakao")
  ResponseEntity<SuccessResponse<Void>> handleKakaoWebhook(
      @Valid @RequestBody KakaoWebhookRequest request);
}
