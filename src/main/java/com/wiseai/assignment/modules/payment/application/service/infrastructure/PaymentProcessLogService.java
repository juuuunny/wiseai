package com.wiseai.assignment.modules.payment.application.service.infrastructure;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentProcessLogEntity;
import com.wiseai.assignment.modules.payment.adapter.jpa.repository.PaymentProcessLogJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
  public void markProcessed(String eventId, Long paymentId) {
    // 이미 tryAcquire에서 저장되었으므로, 여기서는 추가 작업 없이 성공으로 간주
    // 또는 필요시 processedAt 업데이트 등의 로직 추가 가능
    log.debug("결제 이벤트 처리 완료 마킹: eventId={}, paymentId={}", eventId, paymentId);
  }

  @Transactional
  public void release(String eventId) {
    paymentProcessLogJpaRepository.deleteById(eventId);
  }
}
