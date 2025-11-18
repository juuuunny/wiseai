package com.wiseai.assignment.modules.reservation.adapter.jpa.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wiseai.assignment.modules.reservation.adapter.jpa.entity.ReservationEntity;
import com.wiseai.assignment.modules.reservation.domain.enums.ReservationStatus;

public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, Long> {
  Optional<ReservationEntity> findById(Long id);

  List<ReservationEntity> findByUserIdOrderByStartTimeDesc(Long userId);

  @Query(
      "SELECT r FROM ReservationEntity r "
          + "WHERE r.meetingRoomId = :meetingRoomId "
          + "AND r.status != :cancelledStatus "
          + "AND ((r.startTime < :endTime AND r.endTime > :startTime))")
  List<ReservationEntity> findOverlappingReservations(
      @Param("meetingRoomId") Long meetingRoomId,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime,
      @Param("cancelledStatus") ReservationStatus cancelledStatus);
}
