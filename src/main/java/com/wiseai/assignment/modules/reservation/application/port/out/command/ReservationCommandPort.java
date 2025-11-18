package com.wiseai.assignment.modules.reservation.application.port.out.command;

import com.wiseai.assignment.modules.reservation.domain.model.Reservation;

public interface ReservationCommandPort {
  Reservation save(Reservation reservation);

  Reservation update(Reservation reservation);

  void delete(Long id);
}
