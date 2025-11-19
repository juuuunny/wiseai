package com.wiseai.assignment.modules.payment.application.service.event;

import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentProcessLogEntity;
import com.wiseai.assignment.modules.payment.adapter.jpa.repository.PaymentProcessLogJpaRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessLogService {

  private final PaymentProcessLogJpaRepository paymentProcessLogJpaRepository;

  @Transactional(readOnly = true)
  public boolean isProcessed(String eventId) {
    return paymentProcessLogJpaRepository.existsById(eventId);
  }

  @Transactional
  public void markProcessed(String eventId, Long paymentId) {
    paymentProcessLogJpaRepository.save(
        new PaymentProcessLogEntity(eventId, paymentId, LocalDateTime.now()));
  }
}

