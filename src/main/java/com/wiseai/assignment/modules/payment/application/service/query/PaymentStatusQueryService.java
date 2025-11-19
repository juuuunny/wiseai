package com.wiseai.assignment.modules.payment.application.service.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wiseai.assignment.modules.payment.application.dto.response.PaymentStatusResponse;
import com.wiseai.assignment.modules.payment.application.port.in.query.GetPaymentStatusUseCase;
import com.wiseai.assignment.modules.payment.application.port.out.query.PaymentQueryPort;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.model.Payment;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentStatusQueryService implements GetPaymentStatusUseCase {

  private final PaymentQueryPort paymentQueryPort;

  @Override
  @Transactional(readOnly = true)
  public PaymentStatusResponse getPaymentStatus(Long paymentId) {
    log.debug("결제 상태 조회 요청: paymentId={}", paymentId);

    Payment payment =
        paymentQueryPort
            .findById(paymentId)
            .orElseThrow(() -> new PaymentException(PaymentErrorStatus.NOT_FOUND));

    log.debug("결제 상태 조회 완료: paymentId={}, status={}", paymentId, payment.getStatus());
    return new PaymentStatusResponse(
        payment.getId(), payment.getStatus(), payment.getTransactionId());
  }
}
