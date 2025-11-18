package com.wiseai.assignment.modules.reservation.adapter.jpa.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.reservation.adapter.jpa.mapper.ReservationEntityMapper;
import com.wiseai.assignment.modules.reservation.adapter.jpa.repository.ReservationJpaRepository;
import com.wiseai.assignment.modules.reservation.application.port.out.query.ReservationQueryPort;
import com.wiseai.assignment.modules.reservation.domain.enums.ReservationStatus;
import com.wiseai.assignment.modules.reservation.domain.model.Reservation;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservationQueryDbAdapter implements ReservationQueryPort {

  private final ReservationJpaRepository reservationJpaRepository;
  private final ReservationEntityMapper reservationEntityMapper;

  @Override
  public Optional<Reservation> findById(Long id) {
    return reservationJpaRepository.findById(id).map(reservationEntityMapper::toDomain);
  }

  @Override
  public List<Reservation> findByUserId(Long userId) {
    return reservationJpaRepository.findByUserIdOrderByStartTimeDesc(userId).stream()
        .map(reservationEntityMapper::toDomain)
        .toList();
  }

  @Override
  public List<Reservation> findOverlappingReservations(
      Long meetingRoomId, LocalDateTime startTime, LocalDateTime endTime) {
    return reservationJpaRepository
        .findOverlappingReservations(meetingRoomId, startTime, endTime, ReservationStatus.CANCELLED)
        .stream()
        .map(reservationEntityMapper::toDomain)
        .toList();
  }
}
