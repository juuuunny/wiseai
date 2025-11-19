package com.wiseai.assignment.modules.payment.application.service.event;

import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentCancelLogEntity;
import com.wiseai.assignment.modules.payment.adapter.jpa.repository.PaymentCancelLogJpaRepository;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCancelLogService {

  private final PaymentCancelLogJpaRepository paymentCancelLogJpaRepository;

  @Transactional(readOnly = true)
  public boolean isProcessed(String eventId) {
    return paymentCancelLogJpaRepository.existsById(eventId);
  }

  @Transactional
  public void markProcessed(String eventId, Long paymentId, PaymentMethod method) {
    paymentCancelLogJpaRepository.save(
        new PaymentCancelLogEntity(eventId, paymentId, method, LocalDateTime.now()));
  }
}

