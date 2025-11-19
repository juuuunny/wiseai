package com.wiseai.assignment.modules.payment.adapter.web.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.wiseai.assignment.modules.common.dto.response.SuccessResponse;
import com.wiseai.assignment.modules.payment.application.dto.request.CompletePaymentRequest;
import com.wiseai.assignment.modules.payment.application.dto.request.CreatePaymentRequest;
import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;
import com.wiseai.assignment.modules.payment.application.dto.response.PaymentStatusResponse;
import com.wiseai.assignment.modules.payment.application.port.in.command.CancelPaymentUseCase;
import com.wiseai.assignment.modules.payment.application.port.in.command.CompletePaymentUseCase;
import com.wiseai.assignment.modules.payment.application.port.in.command.CreatePaymentUseCase;
import com.wiseai.assignment.modules.payment.application.port.in.query.GetPaymentStatusUseCase;
import com.wiseai.assignment.modules.payment.application.port.in.query.GetPaymentUseCase;
import com.wiseai.assignment.modules.payment.application.port.in.query.GetPaymentsUseCase;
import com.wiseai.assignment.modules.payment.domain.status.PaymentSuccessStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

  private final CreatePaymentUseCase createPaymentUseCase;
  private final CompletePaymentUseCase completePaymentUseCase;
  private final CancelPaymentUseCase cancelPaymentUseCase;
  private final GetPaymentUseCase getPaymentUseCase;
  private final GetPaymentsUseCase getPaymentsUseCase;
  private final GetPaymentStatusUseCase getPaymentStatusUseCase;

  @Override
  public ResponseEntity<SuccessResponse<PaymentResponse>> createPayment(
      CreatePaymentRequest request) {
    log.debug("결제 생성 API 요청: reservationId={}", request.reservationId());

    PaymentResponse response =
        createPaymentUseCase.createPayment(
            request.reservationId(), request.paymentMethod(), request.amount());

    log.debug("결제 생성 완료: paymentId={}", response.id());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(SuccessResponse.of(PaymentSuccessStatus.OK_CREATE_PAYMENT, response));
  }

  @Override
  public ResponseEntity<SuccessResponse<PaymentResponse>> completePayment(
      Long id, CompletePaymentRequest request) {
    log.debug("결제 완료 API 요청: paymentId={}, transactionId={}", id, request.transactionId());

    PaymentResponse response = completePaymentUseCase.completePayment(id, request.transactionId());

    log.debug("결제 완료 완료: paymentId={}", response.id());
    return ResponseEntity.ok(
        SuccessResponse.of(PaymentSuccessStatus.OK_COMPLETE_PAYMENT, response));
  }

  @Override
  public ResponseEntity<SuccessResponse<PaymentResponse>> cancelPayment(Long id) {
    log.debug("결제 취소 API 요청: paymentId={}", id);

    PaymentResponse response = cancelPaymentUseCase.cancelPayment(id);

    log.debug("결제 취소 완료: paymentId={}", response.id());
    return ResponseEntity.ok(SuccessResponse.of(PaymentSuccessStatus.OK_CANCEL_PAYMENT, response));
  }

  @Override
  public ResponseEntity<SuccessResponse<PaymentResponse>> getPayment(Long id) {
    log.debug("결제 단건 조회 API 요청: id={}", id);
    PaymentResponse response = getPaymentUseCase.getPayment(id);
    log.debug("결제 단건 조회 완료: id={}", id);
    return ResponseEntity.ok(SuccessResponse.of(PaymentSuccessStatus.OK_GET_PAYMENT, response));
  }

  @Override
  public ResponseEntity<SuccessResponse<List<PaymentResponse>>> getPaymentsByReservationId(
      Long reservationId) {
    log.debug("예약별 결제 목록 조회 API 요청: reservationId={}", reservationId);
    List<PaymentResponse> response = getPaymentsUseCase.getPaymentsByReservationId(reservationId);
    log.debug("예약별 결제 목록 조회 완료: reservationId={}, count={}", reservationId, response.size());
    return ResponseEntity.ok(SuccessResponse.of(PaymentSuccessStatus.OK_GET_PAYMENTS, response));
  }

  @Override
  public ResponseEntity<SuccessResponse<PaymentStatusResponse>> getPaymentStatus(Long paymentId) {
    log.debug("결제 상태 조회 API 요청: paymentId={}", paymentId);
    PaymentStatusResponse response = getPaymentStatusUseCase.getPaymentStatus(paymentId);
    log.debug("결제 상태 조회 완료: paymentId={}, status={}", paymentId, response.status());
    return ResponseEntity.ok(
        SuccessResponse.of(PaymentSuccessStatus.OK_GET_PAYMENT_STATUS, response));
  }
}
