package com.wiseai.assignment.modules.reservation.application.port.in.command;

import com.wiseai.assignment.modules.reservation.application.dto.response.ReservationResponse;

public interface CancelReservationUseCase {
  ReservationResponse cancelReservation(Long reservationId, Long userId);
}
