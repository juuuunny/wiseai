package com.wiseai.assignment.modules.payment.application.service.command;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;
import com.wiseai.assignment.modules.payment.application.port.in.command.CancelPaymentUseCase;
import com.wiseai.assignment.modules.payment.application.port.in.command.CompletePaymentUseCase;
import com.wiseai.assignment.modules.payment.application.port.in.command.CreatePaymentUseCase;
import com.wiseai.assignment.modules.payment.application.port.out.command.PaymentCommandPort;
import com.wiseai.assignment.modules.payment.application.port.out.query.PaymentQueryPort;
import com.wiseai.assignment.modules.payment.application.service.event.PaymentEventProducer;
import com.wiseai.assignment.modules.payment.application.service.event.PaymentCancelEventProducer;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.model.Payment;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCommandService
    implements CreatePaymentUseCase, CompletePaymentUseCase, CancelPaymentUseCase {

  private final PaymentCommandPort paymentCommandPort;
  private final PaymentQueryPort paymentQueryPort;
  private final PaymentEventProducer paymentEventProducer;
  private final PaymentCancelEventProducer paymentCancelEventProducer;

  @Override
  @Transactional
  public PaymentResponse createPayment(
      Long reservationId, PaymentMethod paymentMethod, BigDecimal amount) {
    log.debug(
        "결제 생성 요청: reservationId={}, paymentMethod={}, amount={}",
        reservationId,
        paymentMethod,
        amount);

    Payment payment = Payment.create(reservationId, paymentMethod, amount);
    Payment saved = paymentCommandPort.save(payment);

    paymentEventProducer.publishPaymentRequested(saved);

    log.debug("결제 생성 완료: paymentId={}", saved.getId());
    return toResponse(saved);
  }

  @Override
  @Transactional
  public PaymentResponse completePayment(Long id, String transactionId) {
    log.debug("결제 완료 요청: paymentId={}, transactionId={}", id, transactionId);

    Payment payment =
        paymentQueryPort
            .findById(id)
            .orElseThrow(
                () -> {
                  log.warn("결제를 찾을 수 없음: paymentId={}", id);
                  return new PaymentException(PaymentErrorStatus.NOT_FOUND);
                });

    Payment completed = payment.complete(transactionId);
    Payment updated = paymentCommandPort.update(completed);

    log.debug("결제 완료 완료: paymentId={}", updated.getId());
    return toResponse(updated);
  }

  @Override
  @Transactional
  public PaymentResponse cancelPayment(Long id) {
    log.debug("결제 취소 요청: paymentId={}", id);

    Payment payment =
        paymentQueryPort
            .findById(id)
            .orElseThrow(
                () -> {
                  log.warn("결제를 찾을 수 없음: paymentId={}", id);
                  return new PaymentException(PaymentErrorStatus.NOT_FOUND);
                });

    if (payment.getTransactionId() == null) {
      log.warn("거래 ID 없이 결제 취소 요청: paymentId={}", id);
      throw new PaymentException(PaymentErrorStatus.INVALID_STATUS);
    }

    paymentCancelEventProducer.publishPaymentCancelRequested(payment);

    log.debug("결제 취소 이벤트 발행 완료: paymentId={}", payment.getId());
    return toResponse(payment);
  }

  private PaymentResponse toResponse(Payment payment) {
    return new PaymentResponse(
        payment.getId(),
        payment.getReservationId(),
        payment.getPaymentMethod(),
        payment.getAmount(),
        payment.getStatus(),
        payment.getTransactionId());
  }
}
