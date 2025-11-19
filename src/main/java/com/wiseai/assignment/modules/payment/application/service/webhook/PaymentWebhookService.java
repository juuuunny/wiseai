package com.wiseai.assignment.modules.payment.application.service.webhook;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wiseai.assignment.modules.payment.application.port.in.webhook.HandlePaymentWebhookUseCase;
import com.wiseai.assignment.modules.payment.application.port.out.query.PaymentQueryPort;
import com.wiseai.assignment.modules.payment.application.port.out.command.PaymentCommandPort;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.model.Payment;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWebhookService implements HandlePaymentWebhookUseCase {

  private final PaymentQueryPort paymentQueryPort;
  private final PaymentCommandPort paymentCommandPort;

  @Override
  @Transactional
  public void handleTossWebhook(
      String paymentKey,
      String orderId,
      String status,
      BigDecimal totalAmount,
      String transactionId,
      String failureCode,
      String failureMessage) {
    log.debug(
        "TOSS 웹훅 처리 시작: paymentKey={}, orderId={}, status={}",
        paymentKey,
        orderId,
        status);

    // orderId에서 paymentId 추출 (예: "payment-123" 형식 가정)
    Long paymentId = extractPaymentIdFromOrderId(orderId);

    Payment payment =
        paymentQueryPort
            .findById(paymentId)
            .orElseThrow(() -> new PaymentException(PaymentErrorStatus.NOT_FOUND));

    // 결제 수단 검증
    if (payment.getPaymentMethod() != PaymentMethod.TOSS) {
      log.warn(
          "결제 수단 불일치: paymentId={}, expected=TOSS, actual={}",
          paymentId,
          payment.getPaymentMethod());
      throw new PaymentException(PaymentErrorStatus.INVALID_PAYMENT_METHOD);
    }

    // 금액 검증
    if (payment.getAmount().compareTo(totalAmount) != 0) {
      log.warn(
          "결제 금액 불일치: paymentId={}, expected={}, actual={}",
          paymentId,
          payment.getAmount(),
          totalAmount);
      throw new PaymentException(PaymentErrorStatus.INVALID_AMOUNT);
    }

    // 상태 업데이트
    Payment updatedPayment = updatePaymentStatus(payment, status, transactionId);
    paymentCommandPort.update(updatedPayment);

    log.info(
        "TOSS 웹훅 처리 완료: paymentId={}, status={}, transactionId={}",
        paymentId,
        status,
        transactionId);
  }

  @Override
  @Transactional
  public void handleKakaoWebhook(
      String paymentKey,
      String orderId,
      String status,
      BigDecimal totalAmount,
      String transactionId,
      String failureCode,
      String failureMessage) {
    log.debug(
        "KAKAO 웹훅 처리 시작: paymentKey={}, orderId={}, status={}",
        paymentKey,
        orderId,
        status);

    // orderId에서 paymentId 추출
    Long paymentId = extractPaymentIdFromOrderId(orderId);

    Payment payment =
        paymentQueryPort
            .findById(paymentId)
            .orElseThrow(() -> new PaymentException(PaymentErrorStatus.NOT_FOUND));

    // 결제 수단 검증
    if (payment.getPaymentMethod() != PaymentMethod.KAKAO) {
      log.warn(
          "결제 수단 불일치: paymentId={}, expected=KAKAO, actual={}",
          paymentId,
          payment.getPaymentMethod());
      throw new PaymentException(PaymentErrorStatus.INVALID_PAYMENT_METHOD);
    }

    // 금액 검증
    if (payment.getAmount().compareTo(totalAmount) != 0) {
      log.warn(
          "결제 금액 불일치: paymentId={}, expected={}, actual={}",
          paymentId,
          payment.getAmount(),
          totalAmount);
      throw new PaymentException(PaymentErrorStatus.INVALID_AMOUNT);
    }

    // 상태 업데이트
    Payment updatedPayment = updatePaymentStatus(payment, status, transactionId);
    paymentCommandPort.update(updatedPayment);

    log.info(
        "KAKAO 웹훅 처리 완료: paymentId={}, status={}, transactionId={}",
        paymentId,
        status,
        transactionId);
  }

  private Long extractPaymentIdFromOrderId(String orderId) {
    try {
      // orderId 형식: "payment-{paymentId}" 또는 "{paymentId}"
      if (orderId.startsWith("payment-")) {
        return Long.parseLong(orderId.substring("payment-".length()));
      }
      return Long.parseLong(orderId);
    } catch (NumberFormatException e) {
      log.error("orderId에서 paymentId 추출 실패: orderId={}", orderId, e);
      throw new PaymentException(PaymentErrorStatus.INVALID_RESERVATION_ID);
    }
  }

  private Payment updatePaymentStatus(Payment payment, String status, String transactionId) {
    // TOSS/KAKAO 웹훅 status 매핑
    // "DONE" -> COMPLETED, "CANCELED" -> CANCELLED, "FAILED" -> FAILED
    switch (status.toUpperCase()) {
      case "DONE":
      case "SUCCESS":
      case "COMPLETED":
        return payment.complete(transactionId != null ? transactionId : payment.getTransactionId());
      case "CANCELED":
      case "CANCELLED":
        return payment.cancel();
      case "FAILED":
      case "FAILURE":
        return payment.fail();
      default:
        log.warn("알 수 없는 웹훅 상태: status={}, paymentId={}", status, payment.getId());
        throw new PaymentException(PaymentErrorStatus.INVALID_STATUS);
    }
  }
}

