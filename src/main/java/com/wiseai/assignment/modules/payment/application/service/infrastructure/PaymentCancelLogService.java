package com.wiseai.assignment.modules.payment.application.service.infrastructure;

import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentCancelLogEntity;
import com.wiseai.assignment.modules.payment.adapter.jpa.repository.PaymentCancelLogJpaRepository;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCancelLogService {

  private final PaymentCancelLogJpaRepository paymentCancelLogJpaRepository;

  @Transactional(readOnly = true)
  public boolean isProcessed(String eventId) {
    return paymentCancelLogJpaRepository.existsById(eventId);
  }

  @Transactional
  public boolean tryAcquire(String eventId, Long paymentId, PaymentMethod paymentMethod) {
    try {
      paymentCancelLogJpaRepository.save(
          new PaymentCancelLogEntity(eventId, paymentId, paymentMethod, LocalDateTime.now()));
      return true;
    } catch (DataIntegrityViolationException e) {
      log.warn("이미 처리된 결제 취소 이벤트: eventId={}, paymentId={}", eventId, paymentId);
      return false;
    }
  }

  @Transactional
  public void markProcessed(String eventId, Long paymentId, PaymentMethod method) {
    // 이미 tryAcquire에서 저장되었으므로, 여기서는 method 업데이트만 수행
    paymentCancelLogJpaRepository
        .findById(eventId)
        .ifPresent(
            entity -> {
              // PaymentMethod 업데이트는 엔티티에 setter가 필요하므로 일단 로그만
              log.debug(
                  "결제 취소 이벤트 처리 완료 마킹: eventId={}, paymentId={}, method={}",
                  eventId,
                  paymentId,
                  method);
            });
  }

  @Transactional
  public void release(String eventId) {
    paymentCancelLogJpaRepository.deleteById(eventId);
  }
}

