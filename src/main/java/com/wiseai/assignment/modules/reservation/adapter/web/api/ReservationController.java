package com.wiseai.assignment.modules.reservation.adapter.web.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.wiseai.assignment.modules.common.dto.response.SuccessResponse;
import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;
import com.wiseai.assignment.modules.reservation.application.dto.request.CreateReservationRequest;
import com.wiseai.assignment.modules.reservation.application.dto.request.ProcessReservationPaymentRequest;
import com.wiseai.assignment.modules.reservation.application.dto.response.ReservationResponse;
import com.wiseai.assignment.modules.reservation.application.port.in.command.CancelReservationUseCase;
import com.wiseai.assignment.modules.reservation.application.port.in.command.CreateReservationUseCase;
import com.wiseai.assignment.modules.reservation.application.port.in.command.ProcessReservationPaymentUseCase;
import com.wiseai.assignment.modules.reservation.application.port.in.query.GetReservationUseCase;
import com.wiseai.assignment.modules.reservation.application.port.in.query.GetReservationsUseCase;
import com.wiseai.assignment.modules.reservation.domain.status.ReservationSuccessStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReservationController implements ReservationApi {

  private final CreateReservationUseCase createReservationUseCase;
  private final GetReservationUseCase getReservationUseCase;
  private final GetReservationsUseCase getReservationsUseCase;
  private final CancelReservationUseCase cancelReservationUseCase;
  private final ProcessReservationPaymentUseCase processReservationPaymentUseCase;

  @Override
  public ResponseEntity<SuccessResponse<ReservationResponse>> createReservation(
      CreateReservationRequest request) {
    log.debug("예약 생성 API 요청: meetingRoomId={}", request.meetingRoomId());

    // TODO: Security Context에서 userId 가져오기 (현재는 임시로 1L 사용)
    Long userId = 1L;

    ReservationResponse response =
        createReservationUseCase.createReservation(
            request.meetingRoomId(),
            userId,
            request.startTime(),
            request.endTime(),
            request.totalAmount());

    log.debug("예약 생성 완료: reservationId={}", response.id());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(SuccessResponse.of(ReservationSuccessStatus.OK_CREATE_RESERVATION, response));
  }

  @Override
  public ResponseEntity<SuccessResponse<ReservationResponse>> getReservation(Long id) {
    log.debug("예약 단건 조회 API 요청: id={}", id);
    ReservationResponse response = getReservationUseCase.getReservation(id);
    log.debug("예약 단건 조회 완료: id={}", id);
    return ResponseEntity.ok(
        SuccessResponse.of(ReservationSuccessStatus.OK_GET_RESERVATION, response));
  }

  @Override
  public ResponseEntity<SuccessResponse<List<ReservationResponse>>> getReservationsByUserId(
      Long userId) {
    log.debug("사용자별 예약 목록 조회 API 요청: userId={}", userId);
    List<ReservationResponse> response = getReservationsUseCase.getReservationsByUserId(userId);
    log.debug("사용자별 예약 목록 조회 완료: userId={}, count={}", userId, response.size());
    return ResponseEntity.ok(
        SuccessResponse.of(ReservationSuccessStatus.OK_GET_RESERVATIONS, response));
  }

  @Override
  public ResponseEntity<SuccessResponse<ReservationResponse>> cancelReservation(
      Long id, Long userId) {
    log.debug("예약 취소 API 요청: reservationId={}, userId={}", id, userId);
    ReservationResponse response = cancelReservationUseCase.cancelReservation(id, userId);
    log.debug("예약 취소 완료: reservationId={}", id);
    return ResponseEntity.ok(
        SuccessResponse.of(ReservationSuccessStatus.OK_CANCEL_RESERVATION, response));
  }

  @Override
  public ResponseEntity<SuccessResponse<PaymentResponse>> processReservationPayment(
      Long id, ProcessReservationPaymentRequest request) {
    log.debug("예약 결제 처리 API 요청: reservationId={}, paymentMethod={}", id, request.paymentMethod());
    PaymentResponse response =
        processReservationPaymentUseCase.processPayment(id, request.paymentMethod());
    log.debug("예약 결제 처리 완료: reservationId={}, paymentId={}", id, response.id());
    return ResponseEntity.ok(
        SuccessResponse.of(ReservationSuccessStatus.OK_PROCESS_PAYMENT, response));
  }
}
