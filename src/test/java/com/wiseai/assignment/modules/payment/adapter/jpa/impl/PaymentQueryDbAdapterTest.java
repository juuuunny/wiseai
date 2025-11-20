package com.wiseai.assignment.modules.payment.adapter.jpa.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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
import com.wiseai.assignment.modules.payment.domain.model.Payment;

@DataJpaTest
@Import({PaymentEntityMapper.class, PaymentQueryDbAdapter.class, JpaConfig.class})
@DisplayName("PaymentQueryDbAdapter 테스트")
class PaymentQueryDbAdapterTest {

  @Autowired private PaymentJpaRepository paymentJpaRepository;

  @Autowired private PaymentQueryDbAdapter paymentQueryDbAdapter;

  private static final Long DEFAULT_RESERVATION_ID = 1L;
  private static final Long OTHER_RESERVATION_ID = 2L;
  private static final Long PAYMENT_ID_NOT_FOUND = 999L;
  private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("10000");
  private static final BigDecimal SECOND_AMOUNT = new BigDecimal("15000");

  @BeforeEach
  void setUp() {
    paymentJpaRepository.deleteAll();
  }

  @Test
  @DisplayName("ID로 결제 조회 성공")
  void findById_success() {
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
    Payment result = paymentQueryDbAdapter.findById(id).orElseThrow();

    // then
    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getReservationId()).isEqualTo(DEFAULT_RESERVATION_ID);
    assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.TOSS);
    assertThat(result.getAmount()).isEqualByComparingTo(DEFAULT_AMOUNT);
    assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
  }

  @Test
  @DisplayName("ID로 결제 조회 실패 - 존재하지 않는 ID")
  void findById_notFound() {
    // when
    var result = paymentQueryDbAdapter.findById(PAYMENT_ID_NOT_FOUND);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("예약 ID로 결제 조회 성공")
  void findByReservationId_success() {
    // given
    PaymentEntity entity1 =
        new PaymentEntity(
            DEFAULT_RESERVATION_ID,
            PaymentMethod.TOSS,
            DEFAULT_AMOUNT,
            PaymentStatus.PENDING,
            null);
    PaymentEntity entity2 =
        new PaymentEntity(
            DEFAULT_RESERVATION_ID,
            PaymentMethod.KAKAO,
            SECOND_AMOUNT,
            PaymentStatus.COMPLETED,
            "txn_123");
    PaymentEntity entity3 =
        new PaymentEntity(
            OTHER_RESERVATION_ID, PaymentMethod.TOSS, DEFAULT_AMOUNT, PaymentStatus.PENDING, null);

    paymentJpaRepository.save(entity1);
    paymentJpaRepository.save(entity2);
    paymentJpaRepository.save(entity3);

    // when
    List<Payment> result = paymentQueryDbAdapter.findByReservationId(DEFAULT_RESERVATION_ID);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getReservationId()).isEqualTo(DEFAULT_RESERVATION_ID);
    assertThat(result.get(1).getReservationId()).isEqualTo(DEFAULT_RESERVATION_ID);
  }

  @Test
  @DisplayName("예약 ID로 결제 조회 - 빈 리스트")
  void findByReservationId_empty() {
    // when
    List<Payment> result = paymentQueryDbAdapter.findByReservationId(DEFAULT_RESERVATION_ID);

    // then
    assertThat(result).isEmpty();
  }
}
