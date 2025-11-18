package com.wiseai.assignment.modules.reservation.application.port.out.query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.wiseai.assignment.modules.reservation.domain.model.Reservation;

public interface ReservationQueryPort {
  Optional<Reservation> findById(Long id);

  List<Reservation> findByUserId(Long userId);

  List<Reservation> findOverlappingReservations(
      Long meetingRoomId, LocalDateTime startTime, LocalDateTime endTime);
}
