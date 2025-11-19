package com.wiseai.assignment.modules.payment.adapter.jpa.repository;

import java.util.List;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentEventOutboxEntity;
import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentEventOutboxEntity.OutboxStatus;

@Repository
public interface PaymentEventOutboxJpaRepository
    extends JpaRepository<PaymentEventOutboxEntity, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT e FROM PaymentEventOutboxEntity e "
          + "WHERE e.status = :status AND e.retryCount < :maxRetries "
          + "ORDER BY e.createdAt ASC")
  List<PaymentEventOutboxEntity> findPendingEvents(
      @Param("status") OutboxStatus status, @Param("maxRetries") int maxRetries);
}
