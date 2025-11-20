package com.wiseai.assignment.modules.payment.adapter.web.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.wiseai.assignment.modules.common.dto.response.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Payment Webhook", description = "결제사 웹훅 수신 API")
public interface PaymentWebhookApi {

  @Operation(
      summary = "결제사 웹훅 수신",
      description = "결제사로부터 결제 상태 변경 알림을 수신합니다. (provider: toss, kakao, card, virtual-account)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "웹훅 처리 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 웹훅 데이터"),
    @ApiResponse(responseCode = "401", description = "웹훅 서명 검증 실패"),
    @ApiResponse(responseCode = "404", description = "지원하지 않는 결제사")
  })
  @PostMapping("/webhooks/payments/{provider}")
  ResponseEntity<SuccessResponse<Void>> handleWebhook(
      @Parameter(description = "결제사 (toss, kakao, card, virtual-account)", required = true)
          @PathVariable
          @NotBlank
          String provider,
      @Valid @RequestBody Object request);
}
