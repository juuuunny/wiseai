package com.wiseai.assignment.modules.reservation.application.port.in.command;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.wiseai.assignment.modules.reservation.application.dto.response.ReservationResponse;

public interface CreateReservationUseCase {
  ReservationResponse createReservation(
      Long meetingRoomId,
      Long userId,
      LocalDateTime startTime,
      LocalDateTime endTime,
      BigDecimal totalAmount);
}
