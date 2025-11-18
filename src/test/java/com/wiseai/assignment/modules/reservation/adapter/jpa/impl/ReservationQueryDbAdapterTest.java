package com.wiseai.assignment.modules.reservation.adapter.jpa.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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
@Import({ReservationEntityMapper.class, ReservationQueryDbAdapter.class, JpaConfig.class})
@DisplayName("ReservationQueryDbAdapter 테스트")
class ReservationQueryDbAdapterTest {

  @Autowired private ReservationJpaRepository reservationJpaRepository;

  @Autowired private ReservationQueryDbAdapter reservationQueryDbAdapter;

  // 공통 테스트 데이터
  private static final int TEST_YEAR = 2024;
  private static final int TEST_MONTH = 1;
  private static final int TEST_DAY = 1;
  private static final Long DEFAULT_MEETING_ROOM_ID = 1L;
  private static final Long OTHER_MEETING_ROOM_ID = 2L;
  private static final Long DEFAULT_USER_ID = 1L;
  private static final Long OTHER_USER_ID = 2L;
  private static final Long RESERVATION_ID_NOT_FOUND = 999L;
  private static final LocalDateTime DEFAULT_START_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 10, 0);
  private static final LocalDateTime DEFAULT_END_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 11, 0);
  private static final LocalDateTime OVERLAP_START_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 10, 30);
  private static final LocalDateTime OVERLAP_END_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 11, 30);
  private static final LocalDateTime NON_OVERLAP_START_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 11, 0);
  private static final LocalDateTime NON_OVERLAP_END_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 12, 0);
  private static final BigDecimal DEFAULT_TOTAL_AMOUNT = new BigDecimal("10000");
  private static final BigDecimal SECOND_TOTAL_AMOUNT = new BigDecimal("15000");

  @BeforeEach
  void setUp() {
    reservationJpaRepository.deleteAll();
  }

  @Test
  @DisplayName("ID로 예약 조회 성공")
  void findById_success() {
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
    Reservation result = reservationQueryDbAdapter.findById(id).orElseThrow();

    // then
    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getMeetingRoomId()).isEqualTo(DEFAULT_MEETING_ROOM_ID);
    assertThat(result.getUserId()).isEqualTo(DEFAULT_USER_ID);
    assertThat(result.getStartTime()).isEqualTo(DEFAULT_START_TIME);
    assertThat(result.getEndTime()).isEqualTo(DEFAULT_END_TIME);
    assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING);
    assertThat(result.getTotalAmount()).isEqualByComparingTo(DEFAULT_TOTAL_AMOUNT);
  }

  @Test
  @DisplayName("ID로 예약 조회 실패 - 존재하지 않는 ID")
  void findById_notFound() {
    // when
    var result = reservationQueryDbAdapter.findById(RESERVATION_ID_NOT_FOUND);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("사용자 ID로 예약 조회 성공")
  void findByUserId_success() {
    // given
    ReservationEntity entity1 =
        new ReservationEntity(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            ReservationStatus.PENDING,
            DEFAULT_TOTAL_AMOUNT);
    ReservationEntity entity2 =
        new ReservationEntity(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            NON_OVERLAP_START_TIME,
            NON_OVERLAP_END_TIME,
            ReservationStatus.CONFIRMED,
            SECOND_TOTAL_AMOUNT);
    ReservationEntity entity3 =
        new ReservationEntity(
            DEFAULT_MEETING_ROOM_ID,
            OTHER_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            ReservationStatus.PENDING,
            DEFAULT_TOTAL_AMOUNT);
    reservationJpaRepository.save(entity1);
    reservationJpaRepository.save(entity2);
    reservationJpaRepository.save(entity3);

    // when
    List<Reservation> result = reservationQueryDbAdapter.findByUserId(DEFAULT_USER_ID);

    // then
    assertThat(result).hasSize(2);
    // 시작 시간 내림차순 정렬 확인 (최신순)
    assertThat(result.get(0).getStartTime()).isEqualTo(NON_OVERLAP_START_TIME);
    assertThat(result.get(1).getStartTime()).isEqualTo(DEFAULT_START_TIME);
  }

  @Test
  @DisplayName("사용자 ID로 예약 조회 - 빈 리스트")
  void findByUserId_empty() {
    // when
    List<Reservation> result = reservationQueryDbAdapter.findByUserId(DEFAULT_USER_ID);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("겹치는 예약 조회 성공 - 시간 겹침")
  void findOverlappingReservations_success_overlapping() {
    // given
    ReservationEntity entity1 =
        new ReservationEntity(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            ReservationStatus.PENDING,
            DEFAULT_TOTAL_AMOUNT);
    ReservationEntity entity2 =
        new ReservationEntity(
            DEFAULT_MEETING_ROOM_ID,
            OTHER_USER_ID,
            OVERLAP_START_TIME,
            OVERLAP_END_TIME,
            ReservationStatus.CONFIRMED,
            SECOND_TOTAL_AMOUNT);
    ReservationEntity entity3 =
        new ReservationEntity(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            NON_OVERLAP_START_TIME,
            NON_OVERLAP_END_TIME,
            ReservationStatus.PENDING,
            DEFAULT_TOTAL_AMOUNT);
    reservationJpaRepository.save(entity1);
    reservationJpaRepository.save(entity2);
    reservationJpaRepository.save(entity3);

    // when
    List<Reservation> result =
        reservationQueryDbAdapter.findOverlappingReservations(
            DEFAULT_MEETING_ROOM_ID, DEFAULT_START_TIME, DEFAULT_END_TIME);

    // then
    assertThat(result).hasSize(2); // entity1과 entity2가 겹침
    assertThat(result)
        .extracting(Reservation::getId)
        .containsExactlyInAnyOrder(entity1.getId(), entity2.getId());
  }

  @Test
  @DisplayName("겹치는 예약 조회 성공 - 취소된 예약 제외")
  void findOverlappingReservations_success_excludeCancelled() {
    // given
    ReservationEntity entity1 =
        new ReservationEntity(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            ReservationStatus.PENDING,
            DEFAULT_TOTAL_AMOUNT);
    ReservationEntity entity2 =
        new ReservationEntity(
            DEFAULT_MEETING_ROOM_ID,
            OTHER_USER_ID,
            OVERLAP_START_TIME,
            OVERLAP_END_TIME,
            ReservationStatus.CANCELLED,
            SECOND_TOTAL_AMOUNT);
    reservationJpaRepository.save(entity1);
    reservationJpaRepository.save(entity2);

    // when
    List<Reservation> result =
        reservationQueryDbAdapter.findOverlappingReservations(
            DEFAULT_MEETING_ROOM_ID, DEFAULT_START_TIME, DEFAULT_END_TIME);

    // then
    assertThat(result).hasSize(1); // 취소된 예약은 제외
    assertThat(result.get(0).getId()).isEqualTo(entity1.getId());
  }

  @Test
  @DisplayName("겹치는 예약 조회 성공 - 다른 회의실은 제외")
  void findOverlappingReservations_success_differentMeetingRoom() {
    // given
    ReservationEntity entity1 =
        new ReservationEntity(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            ReservationStatus.PENDING,
            DEFAULT_TOTAL_AMOUNT);
    ReservationEntity entity2 =
        new ReservationEntity(
            OTHER_MEETING_ROOM_ID,
            OTHER_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            ReservationStatus.PENDING,
            SECOND_TOTAL_AMOUNT);
    reservationJpaRepository.save(entity1);
    reservationJpaRepository.save(entity2);

    // when
    List<Reservation> result =
        reservationQueryDbAdapter.findOverlappingReservations(
            DEFAULT_MEETING_ROOM_ID, DEFAULT_START_TIME, DEFAULT_END_TIME);

    // then
    assertThat(result).hasSize(1); // 다른 회의실은 제외
    assertThat(result.get(0).getId()).isEqualTo(entity1.getId());
  }

  @Test
  @DisplayName("겹치는 예약 조회 - 빈 리스트")
  void findOverlappingReservations_empty() {
    // when
    List<Reservation> result =
        reservationQueryDbAdapter.findOverlappingReservations(
            DEFAULT_MEETING_ROOM_ID, DEFAULT_START_TIME, DEFAULT_END_TIME);

    // then
    assertThat(result).isEmpty();
  }
}
