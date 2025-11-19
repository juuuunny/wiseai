package com.wiseai.assignment.modules.payment.application.service.event;

import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentProcessLogEntity;
import com.wiseai.assignment.modules.payment.adapter.jpa.repository.PaymentProcessLogJpaRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessLogService {

  private final PaymentProcessLogJpaRepository paymentProcessLogJpaRepository;

  @Transactional
  public boolean tryAcquire(String eventId, Long paymentId) {
    try {
      paymentProcessLogJpaRepository.save(
          new PaymentProcessLogEntity(eventId, paymentId, LocalDateTime.now()));
      return true;
    } catch (DataIntegrityViolationException e) {
      log.warn("이미 처리된 결제 이벤트: eventId={}, paymentId={}", eventId, paymentId);
      return false;
    }
  }

  @Transactional
  public void release(String eventId) {
    paymentProcessLogJpaRepository.deleteById(eventId);
  }
}

