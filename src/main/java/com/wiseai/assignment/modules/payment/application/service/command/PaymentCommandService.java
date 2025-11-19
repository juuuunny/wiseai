package com.wiseai.assignment.modules.payment.application.service.command;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wiseai.assignment.modules.payment.application.dto.response.PaymentResponse;
import com.wiseai.assignment.modules.payment.application.port.in.command.CancelPaymentUseCase;
import com.wiseai.assignment.modules.payment.application.port.in.command.CompletePaymentUseCase;
import com.wiseai.assignment.modules.payment.application.port.in.command.CreatePaymentUseCase;
import com.wiseai.assignment.modules.payment.application.port.out.command.PaymentCommandPort;
import com.wiseai.assignment.modules.payment.application.port.out.gateway.PaymentGateway;
import com.wiseai.assignment.modules.payment.application.port.out.query.PaymentQueryPort;
import com.wiseai.assignment.modules.payment.application.service.gateway.PaymentGatewayFactory;
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
  private final PaymentGatewayFactory paymentGatewayFactory;

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

    // PaymentGateway를 통해 실제 결제 처리 (비동기)
    PaymentGateway gateway = paymentGatewayFactory.getGateway(paymentMethod);
    gateway
        .processPayment(amount, reservationId)
        .thenAccept(
            transactionId -> {
              try {
                Payment completed = saved.complete(transactionId);
                paymentCommandPort.update(completed);
                log.debug("결제 처리 완료: paymentId={}, transactionId={}", saved.getId(), transactionId);
              } catch (Exception e) {
                log.error("결제 완료 처리 중 오류: paymentId={}", saved.getId(), e);
                Payment failed = saved.fail();
                paymentCommandPort.update(failed);
              }
            })
        .exceptionally(
            ex -> {
              log.error("결제 처리 중 오류 발생: paymentId={}", saved.getId(), ex);
              Payment failed = saved.fail();
              paymentCommandPort.update(failed);
              return null;
            });

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

    // PaymentGateway를 통해 실제 결제 취소 처리
    if (payment.getTransactionId() != null) {
      PaymentGateway gateway = paymentGatewayFactory.getGateway(payment.getPaymentMethod());
      gateway
          .cancelPayment(payment.getTransactionId())
          .thenAccept(
              success -> {
                if (success) {
                  log.debug("결제 게이트웨이 취소 성공: paymentId={}", id);
                } else {
                  log.warn("결제 게이트웨이 취소 실패: paymentId={}", id);
                }
              })
          .exceptionally(
              ex -> {
                log.error("결제 게이트웨이 취소 중 오류: paymentId={}", id, ex);
                return null;
              });
    }

    Payment cancelled = payment.cancel();
    Payment updated = paymentCommandPort.update(cancelled);

    log.debug("결제 취소 완료: paymentId={}", updated.getId());
    return toResponse(updated);
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
