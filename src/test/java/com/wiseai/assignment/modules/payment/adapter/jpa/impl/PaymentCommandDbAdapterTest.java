package com.wiseai.assignment.modules.payment.adapter.jpa.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.wiseai.assignment.modules.common.config.adapter.persistence.JpaConfig;
import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentEntity;
import com.wiseai.assignment.modules.payment.adapter.jpa.mapper.PaymentEntityMapper;
import com.wiseai.assignment.modules.payment.adapter.jpa.repository.PaymentJpaRepository;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;
import com.wiseai.assignment.modules.payment.domain.enums.PaymentStatus;
import com.wiseai.assignment.modules.payment.domain.exception.PaymentException;
import com.wiseai.assignment.modules.payment.domain.model.Payment;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;

@DataJpaTest
@Import({PaymentEntityMapper.class, PaymentCommandDbAdapter.class, JpaConfig.class})
@DisplayName("PaymentCommandDbAdapter 테스트")
class PaymentCommandDbAdapterTest {

  @Autowired private PaymentJpaRepository paymentJpaRepository;

  @Autowired private PaymentEntityMapper paymentEntityMapper;

  @Autowired private PaymentCommandDbAdapter paymentCommandDbAdapter;

  private static final Long DEFAULT_RESERVATION_ID = 1L;
  private static final Long PAYMENT_ID_NOT_FOUND = 999L;
  private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("10000");
  private static final String DEFAULT_TRANSACTION_ID = "txn_12345";

  @Test
  @DisplayName("결제 저장 성공")
  void save_success() {
    // given
    Payment payment = Payment.create(DEFAULT_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT);

    // when
    Payment saved = paymentCommandDbAdapter.save(payment);

    // then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getReservationId()).isEqualTo(DEFAULT_RESERVATION_ID);
    assertThat(saved.getPaymentMethod()).isEqualTo(PaymentMethod.TOSS);
    assertThat(saved.getAmount()).isEqualByComparingTo(DEFAULT_AMOUNT);
    assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
  }

  @Test
  @DisplayName("결제 수정 성공 - 상태 변경")
  void update_success() {
    // given
    PaymentEntity entity =
        new PaymentEntity(
            DEFAULT_RESERVATION_ID,
            PaymentMethod.TOSS,
            DEFAULT_AMOUNT,
            PaymentStatus.PENDING,
            null);
    PaymentEntity savedEntity = paymentJpaRepository.save(entity);
    Payment saved = paymentEntityMapper.toDomain(savedEntity);

    Payment completed = saved.complete(DEFAULT_TRANSACTION_ID);

    // when
    Payment updated = paymentCommandDbAdapter.update(completed);

    // then
    assertThat(updated.getId()).isEqualTo(saved.getId());
    assertThat(updated.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    assertThat(updated.getTransactionId()).isEqualTo(DEFAULT_TRANSACTION_ID);
    assertThat(updated.getReservationId()).isEqualTo(DEFAULT_RESERVATION_ID);
  }

  @Test
  @DisplayName("결제 수정 실패 - 존재하지 않는 ID")
  void update_fail_notFound() {
    // given
    Payment payment =
        Payment.builder()
            .id(PAYMENT_ID_NOT_FOUND)
            .reservationId(DEFAULT_RESERVATION_ID)
            .paymentMethod(PaymentMethod.TOSS)
            .amount(DEFAULT_AMOUNT)
            .status(PaymentStatus.PENDING)
            .build();

    // when & then
    assertThatThrownBy(() -> paymentCommandDbAdapter.update(payment))
        .isInstanceOf(PaymentException.class)
        .satisfies(
            exception -> {
              PaymentException ex = (PaymentException) exception;
              assertThat(ex.getErrorCode()).isEqualTo(PaymentErrorStatus.NOT_FOUND);
            });
  }

  @Test
  @DisplayName("결제 삭제 성공")
  void delete_success() {
    // given
    PaymentEntity entity =
        new PaymentEntity(
            DEFAULT_RESERVATION_ID,
            PaymentMethod.TOSS,
            DEFAULT_AMOUNT,
            PaymentStatus.PENDING,
            null);
    PaymentEntity saved = paymentJpaRepository.save(entity);
    Long id = saved.getId();

    // when
    paymentCommandDbAdapter.delete(id);

    // then
    assertThat(paymentJpaRepository.findById(id)).isEmpty();
  }
}
