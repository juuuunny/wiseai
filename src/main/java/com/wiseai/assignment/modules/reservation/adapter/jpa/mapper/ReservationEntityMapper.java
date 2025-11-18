package com.wiseai.assignment.modules.reservation.adapter.jpa.mapper;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.reservation.adapter.jpa.entity.ReservationEntity;
import com.wiseai.assignment.modules.reservation.domain.model.Reservation;

@Component
public class ReservationEntityMapper {

  public Reservation toDomain(ReservationEntity entity) {
    Reservation reservation =
        Reservation.builder()
            .meetingRoomId(entity.getMeetingRoomId())
            .userId(entity.getUserId())
            .startTime(entity.getStartTime())
            .endTime(entity.getEndTime())
            .status(entity.getStatus())
            .totalAmount(entity.getTotalAmount())
            .build();
    // ID가 있는 경우에만 설정
    if (entity.getId() != null) {
      return reservation.withId(entity.getId());
    }
    return reservation;
  }

  public ReservationEntity toEntity(Reservation reservation) {
    ReservationEntity entity =
        new ReservationEntity(
            reservation.getMeetingRoomId(),
            reservation.getUserId(),
            reservation.getStartTime(),
            reservation.getEndTime(),
            reservation.getStatus(),
            reservation.getTotalAmount());
    // ID가 있는 경우에만 설정 (update 시)
    if (reservation.getId() != null) {
      entity.setId(reservation.getId());
    }
    return entity;
  }

  public void updateEntity(ReservationEntity entity, Reservation reservation) {
    entity.updateStatus(reservation.getStatus());
  }
}
