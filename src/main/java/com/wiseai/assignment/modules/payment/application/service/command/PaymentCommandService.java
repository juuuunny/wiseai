package com.wiseai.assignment.modules.payment.application.service.command;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

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
  private final PlatformTransactionManager transactionManager;

  private TransactionTemplate getTransactionTemplate() {
    return new TransactionTemplate(transactionManager);
  }

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
    Long paymentId = saved.getId();

    // PaymentGateway를 통해 실제 결제 처리 (비동기)
    PaymentGateway gateway = paymentGatewayFactory.getGateway(paymentMethod);
    gateway
        .processPayment(amount, reservationId)
        .thenAccept(
            transactionId -> {
              try {
                updatePaymentStatus(paymentId, transactionId, true);
                log.debug("결제 처리 완료: paymentId={}, transactionId={}", paymentId, transactionId);
              } catch (Exception e) {
                log.error("결제 완료 처리 중 오류: paymentId={}", paymentId, e);
                updatePaymentStatus(paymentId, null, false);
              }
            })
        .exceptionally(
            ex -> {
              log.error("결제 처리 중 오류 발생: paymentId={}", paymentId, ex);
              updatePaymentStatus(paymentId, null, false);
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

  /**
   * 비동기 콜백에서 Payment 상태를 업데이트합니다. 별도 트랜잭션에서 실행됩니다.
   *
   * @param paymentId 결제 ID
   * @param transactionId 거래 ID (성공 시)
   * @param success 성공 여부
   */
  public void updatePaymentStatus(Long paymentId, String transactionId, boolean success) {
    getTransactionTemplate().executeWithoutResult(
        status -> {
          Payment payment =
              paymentQueryPort
                  .findById(paymentId)
                  .orElseThrow(
                      () -> {
                        log.warn("결제를 찾을 수 없음: paymentId={}", paymentId);
                        return new PaymentException(PaymentErrorStatus.NOT_FOUND);
                      });

          Payment updated;
          if (success) {
            updated = payment.complete(transactionId);
          } else {
            updated = payment.fail();
          }

          paymentCommandPort.update(updated);
        });
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
