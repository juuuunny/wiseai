package com.wiseai.assignment.modules.reservation.application.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 예약 생성을 위한 요청 객체
 *
 * @param meetingRoomId 회의실 ID
 * @param startTime 시작 시간 (정시(00분) 또는 30분 단위)
 * @param endTime 종료 시간 (정시(00분) 또는 30분 단위)
 * @param totalAmount 총 결제 금액
 */
public record CreateReservationRequest(
    @NotNull(message = "회의실 ID는 필수입니다.")
        @Min(value = 1, message = "회의실 ID는 1 이상이어야 합니다.")
        @Schema(description = "회의실 ID", example = "1")
        Long meetingRoomId,
    @NotNull(message = "시작 시간은 필수입니다.")
        @Schema(description = "시작 시간 (정시(00분) 또는 30분 단위)", example = "2025-11-20T20:00:00")
        LocalDateTime startTime,
    @NotNull(message = "종료 시간은 필수입니다.")
        @Schema(description = "종료 시간 (정시(00분) 또는 30분 단위)", example = "2025-11-20T20:30:00")
        LocalDateTime endTime,
    @NotNull(message = "총 결제 금액은 필수입니다.")
        @DecimalMin(value = "0.0", inclusive = true, message = "총 결제 금액은 0 이상이어야 합니다.")
        @Schema(description = "총 결제 금액", example = "0")
        BigDecimal totalAmount) {}
