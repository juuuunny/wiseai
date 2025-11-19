package com.wiseai.assignment.modules.reservation.application.service.command;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wiseai.assignment.modules.common.support.lock.DistributedLock;
import com.wiseai.assignment.modules.reservation.application.dto.response.ReservationResponse;
import com.wiseai.assignment.modules.reservation.application.port.in.command.CancelReservationUseCase;
import com.wiseai.assignment.modules.reservation.application.port.in.command.CreateReservationUseCase;
import com.wiseai.assignment.modules.reservation.application.port.out.command.ReservationCommandPort;
import com.wiseai.assignment.modules.reservation.application.port.out.query.ReservationQueryPort;
import com.wiseai.assignment.modules.reservation.domain.exception.ReservationException;
import com.wiseai.assignment.modules.reservation.domain.model.Reservation;
import com.wiseai.assignment.modules.reservation.domain.status.ReservationErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationCommandService
    implements CreateReservationUseCase, CancelReservationUseCase {

  private final ReservationCommandPort reservationCommandPort;
  private final ReservationQueryPort reservationQueryPort;

  @Override
  @DistributedLock(
      key =
          "'lock:reservation:' + #meetingRoomId + ':' + T(java.time.format.DateTimeFormatter).ofPattern('yyyyMMddHHmm').format(#startTime)",
      waitTime = 200L,
      leaseTime = 3000L,
      retry = 3)
  @Transactional
  public ReservationResponse createReservation(
      Long meetingRoomId,
      Long userId,
      LocalDateTime startTime,
      LocalDateTime endTime,
      BigDecimal totalAmount) {
    log.debug(
        "예약 생성 요청: meetingRoomId={}, userId={}, startTime={}, endTime={}",
        meetingRoomId,
        userId,
        startTime,
        endTime);

    // 중복 예약 체크
    var overlappingReservations =
        reservationQueryPort.findOverlappingReservations(meetingRoomId, startTime, endTime);
    if (!overlappingReservations.isEmpty()) {
      log.warn(
          "겹치는 예약이 존재함: meetingRoomId={}, startTime={}, endTime={}",
          meetingRoomId,
          startTime,
          endTime);
      throw new ReservationException(ReservationErrorStatus.DUPLICATE_RESERVATION);
    }

    Reservation reservation =
        Reservation.create(meetingRoomId, userId, startTime, endTime, totalAmount);
    Reservation saved = reservationCommandPort.save(reservation);

    log.debug("예약 생성 완료: reservationId={}", saved.getId());
    return toResponse(saved);
  }

  @Override
  @Transactional
  public ReservationResponse cancelReservation(Long reservationId, Long userId) {
    log.debug("예약 취소 요청: reservationId={}, userId={}", reservationId, userId);

    Reservation reservation =
        reservationQueryPort
            .findById(reservationId)
            .orElseThrow(
                () -> {
                  log.warn("예약을 찾을 수 없음: reservationId={}", reservationId);
                  return new ReservationException(ReservationErrorStatus.NOT_FOUND);
                });

    // 본인 예약인지 확인
    if (!reservation.getUserId().equals(userId)) {
      log.warn("본인 예약이 아님: reservationId={}, userId={}", reservationId, userId);
      throw new ReservationException(ReservationErrorStatus.UNAUTHORIZED);
    }

    Reservation cancelled = reservation.cancel();
    Reservation updated = reservationCommandPort.update(cancelled);

    log.debug("예약 취소 완료: reservationId={}", updated.getId());
    return toResponse(updated);
  }

  private ReservationResponse toResponse(Reservation reservation) {
    return new ReservationResponse(
        reservation.getId(),
        reservation.getMeetingRoomId(),
        reservation.getUserId(),
        reservation.getStartTime(),
        reservation.getEndTime(),
        reservation.getStatus(),
        reservation.getTotalAmount());
  }
}
