package com.wiseai.assignment.modules.reservation.adapter.jpa.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.wiseai.assignment.modules.common.config.adapter.persistence.JpaConfig;
import com.wiseai.assignment.modules.reservation.adapter.jpa.entity.ReservationEntity;
import com.wiseai.assignment.modules.reservation.adapter.jpa.mapper.ReservationEntityMapper;
import com.wiseai.assignment.modules.reservation.adapter.jpa.repository.ReservationJpaRepository;
import com.wiseai.assignment.modules.reservation.domain.enums.ReservationStatus;
import com.wiseai.assignment.modules.reservation.domain.model.Reservation;

@DataJpaTest
@Import({ReservationEntityMapper.class, ReservationCommandDbAdapter.class, JpaConfig.class})
@DisplayName("ReservationCommandDbAdapter 테스트")
class ReservationCommandDbAdapterTest {

  @Autowired private ReservationJpaRepository reservationJpaRepository;

  @Autowired private ReservationEntityMapper reservationEntityMapper;

  @Autowired private ReservationCommandDbAdapter reservationCommandDbAdapter;

  // 공통 테스트 데이터
  private static final int TEST_YEAR = 2024;
  private static final int TEST_MONTH = 1;
  private static final int TEST_DAY = 1;
  private static final Long DEFAULT_MEETING_ROOM_ID = 1L;
  private static final Long DEFAULT_USER_ID = 1L;
  private static final Long RESERVATION_ID_NOT_FOUND = 999L;
  private static final LocalDateTime DEFAULT_START_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 10, 0);
  private static final LocalDateTime DEFAULT_END_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 11, 0);
  private static final BigDecimal DEFAULT_TOTAL_AMOUNT = new BigDecimal("10000");

  @Test
  @DisplayName("예약 저장 성공")
  void save_success() {
    // given
    Reservation reservation =
        Reservation.create(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);

    // when
    Reservation saved = reservationCommandDbAdapter.save(reservation);

    // then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getMeetingRoomId()).isEqualTo(DEFAULT_MEETING_ROOM_ID);
    assertThat(saved.getUserId()).isEqualTo(DEFAULT_USER_ID);
    assertThat(saved.getStartTime()).isEqualTo(DEFAULT_START_TIME);
    assertThat(saved.getEndTime()).isEqualTo(DEFAULT_END_TIME);
    assertThat(saved.getStatus()).isEqualTo(ReservationStatus.PENDING);
    assertThat(saved.getTotalAmount()).isEqualByComparingTo(DEFAULT_TOTAL_AMOUNT);
  }

  @Test
  @DisplayName("예약 수정 성공 - 상태 변경")
  void update_success() {
    // given
    ReservationEntity entity =
        new ReservationEntity(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            ReservationStatus.PENDING,
            DEFAULT_TOTAL_AMOUNT);
    ReservationEntity savedEntity = reservationJpaRepository.save(entity);
    Reservation saved = reservationEntityMapper.toDomain(savedEntity);

    Reservation confirmed = saved.withId(saved.getId()).confirm();

    // when
    Reservation updated = reservationCommandDbAdapter.update(confirmed);

    // then
    assertThat(updated.getId()).isEqualTo(saved.getId());
    assertThat(updated.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    assertThat(updated.getMeetingRoomId()).isEqualTo(DEFAULT_MEETING_ROOM_ID);
    assertThat(updated.getUserId()).isEqualTo(DEFAULT_USER_ID);
  }

  @Test
  @DisplayName("예약 수정 실패 - 존재하지 않는 ID")
  void update_fail_notFound() {
    // given
    Reservation reservation =
        Reservation.builder()
            .id(RESERVATION_ID_NOT_FOUND)
            .meetingRoomId(DEFAULT_MEETING_ROOM_ID)
            .userId(DEFAULT_USER_ID)
            .startTime(DEFAULT_START_TIME)
            .endTime(DEFAULT_END_TIME)
            .status(ReservationStatus.PENDING)
            .totalAmount(DEFAULT_TOTAL_AMOUNT)
            .build();

    // when & then
    assertThatThrownBy(() -> reservationCommandDbAdapter.update(reservation))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Reservation not found with id: " + RESERVATION_ID_NOT_FOUND);
  }

  @Test
  @DisplayName("예약 삭제 성공")
  void delete_success() {
    // given
    ReservationEntity entity =
        new ReservationEntity(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            ReservationStatus.PENDING,
            DEFAULT_TOTAL_AMOUNT);
    ReservationEntity saved = reservationJpaRepository.save(entity);
    Long id = saved.getId();

    // when
    reservationCommandDbAdapter.delete(id);

    // then
    assertThat(reservationJpaRepository.findById(id)).isEmpty();
  }
}
