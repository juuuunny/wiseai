package com.wiseai.assignment.modules.reservation.application.port.in.query;

import com.wiseai.assignment.modules.reservation.application.dto.response.ReservationResponse;

public interface GetReservationUseCase {
  ReservationResponse getReservation(Long reservationId);
}
