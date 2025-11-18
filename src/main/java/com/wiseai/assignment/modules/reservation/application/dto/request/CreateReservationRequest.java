package com.wiseai.assignment.modules.reservation.application.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 예약 생성을 위한 요청 객체
 *
 * @param meetingRoomId 회의실 ID
 * @param startTime 시작 시간
 * @param endTime 종료 시간
 * @param totalAmount 총 결제 금액
 */
public record CreateReservationRequest(
    @NotNull(message = "회의실 ID는 필수입니다.") @Min(value = 1, message = "회의실 ID는 1 이상이어야 합니다.")
        Long meetingRoomId,
    @NotNull(message = "시작 시간은 필수입니다.") LocalDateTime startTime,
    @NotNull(message = "종료 시간은 필수입니다.") LocalDateTime endTime,
    @NotNull(message = "총 결제 금액은 필수입니다.")
        @DecimalMin(value = "0.0", inclusive = true, message = "총 결제 금액은 0 이상이어야 합니다.")
        BigDecimal totalAmount) {}
