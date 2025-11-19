package com.wiseai.assignment.modules.payment.application.service.query;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;
import com.wiseai.assignment.modules.payment.application.port.in.query.GetPaymentUseCase;
import com.wiseai.assignment.modules.payment.application.port.in.query.GetPaymentsUseCase;
import com.wiseai.assignment.modules.payment.application.port.out.query.PaymentQueryPort;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentQueryService implements GetPaymentUseCase, GetPaymentsUseCase {

  private final PaymentQueryPort paymentQueryPort;

  @Override
  @Transactional(readOnly = true)
  public PaymentResponse getPayment(Long id) {
    log.debug("결제 단건 조회 요청: id={}", id);

    PaymentResponse result =
        paymentQueryPort
            .findById(id)
            .map(
                payment ->
                    new PaymentResponse(
                        payment.getId(),
                        payment.getReservationId(),
                        payment.getPaymentMethod(),
                        payment.getAmount(),
                        payment.getStatus(),
                        payment.getTransactionId()))
            .orElseThrow(
                () -> {
                  log.warn("결제를 찾을 수 없음: id={}", id);
                  return new PaymentException(PaymentErrorStatus.NOT_FOUND);
                });

    log.debug("결제 단건 조회 완료: id={}", id);
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentResponse> getPaymentsByReservationId(Long reservationId) {
    log.debug("예약별 결제 목록 조회 요청: reservationId={}", reservationId);

    List<PaymentResponse> result =
        paymentQueryPort.findByReservationId(reservationId).stream()
            .map(
                payment ->
                    new PaymentResponse(
                        payment.getId(),
                        payment.getReservationId(),
                        payment.getPaymentMethod(),
                        payment.getAmount(),
                        payment.getStatus(),
                        payment.getTransactionId()))
            .toList();

    log.debug("예약별 결제 목록 조회 완료: reservationId={}, count={}", reservationId, result.size());
    return result;
  }
}
