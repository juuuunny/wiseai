package com.wiseai.assignment.modules.reservation.application.port.in.query;

import java.util.List;

import com.wiseai.assignment.modules.reservation.application.dto.response.ReservationResponse;

public interface GetReservationsUseCase {
  List<ReservationResponse> getReservationsByUserId(Long userId);
}
