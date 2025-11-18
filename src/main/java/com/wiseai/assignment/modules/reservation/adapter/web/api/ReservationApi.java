package com.wiseai.assignment.modules.reservation.adapter.web.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.wiseai.assignment.modules.common.dto.response.SuccessResponse;
import com.wiseai.assignment.modules.reservation.application.dto.request.CreateReservationRequest;
import com.wiseai.assignment.modules.reservation.application.dto.response.ReservationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@Tag(name = "Reservation", description = "예약 관리 API")
public interface ReservationApi {

  @Operation(summary = "예약 생성", description = "새로운 예약을 생성합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "예약 생성 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
    @ApiResponse(responseCode = "409", description = "겹치는 예약 존재")
  })
  @PostMapping("/reservations")
  ResponseEntity<SuccessResponse<ReservationResponse>> createReservation(
      @Valid @RequestBody CreateReservationRequest request);

  @Operation(summary = "예약 단건 조회", description = "ID로 특정 예약을 조회합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "조회 성공"),
    @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
  })
  @GetMapping("/reservations/{id}")
  ResponseEntity<SuccessResponse<ReservationResponse>> getReservation(
      @PathVariable @Min(1) Long id);

  @Operation(summary = "사용자별 예약 목록 조회", description = "사용자 ID로 예약 목록을 조회합니다.")
  @GetMapping("/reservations/users/{userId}")
  ResponseEntity<SuccessResponse<List<ReservationResponse>>> getReservationsByUserId(
      @PathVariable @Min(1) Long userId);

  @Operation(summary = "예약 취소", description = "예약을 취소합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "취소 성공"),
    @ApiResponse(responseCode = "403", description = "권한 없음"),
    @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
  })
  @DeleteMapping("/reservations/{id}/users/{userId}")
  ResponseEntity<SuccessResponse<ReservationResponse>> cancelReservation(
      @PathVariable @Min(1) Long id, @PathVariable @Min(1) Long userId);
}
