package com.wiseai.assignment.modules.payment.adapter.web.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.wiseai.assignment.modules.common.dto.response.SuccessResponse;
import com.wiseai.assignment.modules.payment.application.dto.request.CompletePaymentRequest;
import com.wiseai.assignment.modules.payment.application.dto.request.CreatePaymentRequest;
import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@Tag(name = "Payment", description = "결제 관리 API")
public interface PaymentApi {

  @Operation(summary = "결제 생성", description = "새로운 결제를 생성합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "결제 생성 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
  })
  @PostMapping("/payments")
  ResponseEntity<SuccessResponse<PaymentResponse>> createPayment(
      @Valid @RequestBody CreatePaymentRequest request);

  @Operation(summary = "결제 완료", description = "결제를 완료합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "결제 완료 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 결제 상태"),
    @ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음")
  })
  @PatchMapping("/payments/{id}/complete")
  ResponseEntity<SuccessResponse<PaymentResponse>> completePayment(
      @PathVariable @Min(1) Long id, @Valid @RequestBody CompletePaymentRequest request);

  @Operation(summary = "결제 취소", description = "결제를 취소합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "결제 취소 성공"),
    @ApiResponse(responseCode = "400", description = "취소할 수 없는 결제 상태"),
    @ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음")
  })
  @PatchMapping("/payments/{id}/cancel")
  ResponseEntity<SuccessResponse<PaymentResponse>> cancelPayment(@PathVariable @Min(1) Long id);

  @Operation(summary = "결제 단건 조회", description = "ID로 특정 결제를 조회합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "조회 성공"),
    @ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음")
  })
  @GetMapping("/payments/{id}")
  ResponseEntity<SuccessResponse<PaymentResponse>> getPayment(@PathVariable @Min(1) Long id);

  @Operation(summary = "예약별 결제 목록 조회", description = "예약 ID로 결제 목록을 조회합니다.")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공")})
  @GetMapping("/payments/reservations/{reservationId}")
  ResponseEntity<SuccessResponse<List<PaymentResponse>>> getPaymentsByReservationId(
      @PathVariable @Min(1) Long reservationId);
}
