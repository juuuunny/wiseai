package com.wiseai.assignment.modules.reservation.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.wiseai.assignment.modules.reservation.domain.enums.ReservationStatus;

public record ReservationResponse(
    Long id,
    Long meetingRoomId,
    Long userId,
    LocalDateTime startTime,
    LocalDateTime endTime,
    ReservationStatus status,
    BigDecimal totalAmount) {}
