package com.wiseai.assignment.modules.reservation.adapter.jpa.impl;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.reservation.adapter.jpa.entity.ReservationEntity;
import com.wiseai.assignment.modules.reservation.adapter.jpa.mapper.ReservationEntityMapper;
import com.wiseai.assignment.modules.reservation.adapter.jpa.repository.ReservationJpaRepository;
import com.wiseai.assignment.modules.reservation.application.port.out.command.ReservationCommandPort;
import com.wiseai.assignment.modules.reservation.domain.model.Reservation;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservationCommandDbAdapter implements ReservationCommandPort {

  private final ReservationJpaRepository reservationJpaRepository;
  private final ReservationEntityMapper reservationEntityMapper;

  @Override
  public Reservation save(Reservation reservation) {
    ReservationEntity entity = reservationEntityMapper.toEntity(reservation);
    ReservationEntity saved = reservationJpaRepository.save(entity);
    return reservationEntityMapper.toDomain(saved);
  }

  @Override
  public Reservation update(Reservation reservation) {
    ReservationEntity entity =
        reservationJpaRepository
            .findById(reservation.getId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Reservation not found with id: " + reservation.getId()));
    reservationEntityMapper.updateEntity(entity, reservation);
    ReservationEntity updated = reservationJpaRepository.save(entity);
    return reservationEntityMapper.toDomain(updated);
  }

  @Override
  public void delete(Long id) {
    reservationJpaRepository.deleteById(id);
  }
}
