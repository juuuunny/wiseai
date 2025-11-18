package com.wiseai.assignment.modules.reservation.application.service.query;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wiseai.assignment.modules.reservation.application.dto.response.ReservationResponse;
import com.wiseai.assignment.modules.reservation.application.port.in.query.GetReservationUseCase;
import com.wiseai.assignment.modules.reservation.application.port.in.query.GetReservationsUseCase;
import com.wiseai.assignment.modules.reservation.application.port.out.query.ReservationQueryPort;
import com.wiseai.assignment.modules.reservation.domain.exception.ReservationException;
import com.wiseai.assignment.modules.reservation.domain.model.Reservation;
import com.wiseai.assignment.modules.reservation.domain.status.ReservationErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationQueryService implements GetReservationUseCase, GetReservationsUseCase {

  private final ReservationQueryPort reservationQueryPort;

  @Override
  @Transactional(readOnly = true)
  public ReservationResponse getReservation(Long reservationId) {
    log.debug("예약 단건 조회 요청: reservationId={}", reservationId);

    ReservationResponse result =
        reservationQueryPort
            .findById(reservationId)
            .map(this::toResponse)
            .orElseThrow(
                () -> {
                  log.warn("예약을 찾을 수 없음: reservationId={}", reservationId);
                  return new ReservationException(ReservationErrorStatus.NOT_FOUND);
                });

    log.debug("예약 단건 조회 완료: reservationId={}", reservationId);
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReservationResponse> getReservationsByUserId(Long userId) {
    log.debug("사용자별 예약 목록 조회 요청: userId={}", userId);

    List<ReservationResponse> result =
        reservationQueryPort.findByUserId(userId).stream().map(this::toResponse).toList();

    log.debug("사용자별 예약 목록 조회 완료: userId={}, count={}", userId, result.size());
    return result;
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
