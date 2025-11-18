package com.wiseai.assignment.modules.meetingroom.adapter.jpa.impl;

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
import com.wiseai.assignment.modules.meetingroom.adapter.jpa.entity.MeetingRoomEntity;
import com.wiseai.assignment.modules.meetingroom.adapter.jpa.mapper.MeetingRoomEntityMapper;
import com.wiseai.assignment.modules.meetingroom.adapter.jpa.repository.MeetingRoomJpaRepository;
import com.wiseai.assignment.modules.meetingroom.domain.model.MeetingRoom;

@DataJpaTest
@Import({MeetingRoomEntityMapper.class, MeetingRoomQueryDbAdapter.class, JpaConfig.class})
@DisplayName("MeetingRoomQueryDbAdapter 테스트")
class MeetingRoomQueryDbAdapterTest {

  @Autowired private MeetingRoomJpaRepository meetingRoomJpaRepository;

  @Autowired private MeetingRoomQueryDbAdapter meetingRoomQueryDbAdapter;

  // 공통 테스트 데이터
  private static final String ROOM_NAME_A = "회의실 A";
  private static final String ROOM_NAME_B = "회의실 B";
  private static final String ROOM_DESCRIPTION_1 = "설명1";
  private static final String ROOM_DESCRIPTION_2 = "설명2";
  private static final String ROOM_DESCRIPTION = "설명";
  private static final int CAPACITY_10 = 10;
  private static final int CAPACITY_20 = 20;
  private static final BigDecimal FEE_10000 = new BigDecimal("10000");
  private static final BigDecimal FEE_20000 = new BigDecimal("20000");
  private static final Long ROOM_ID_NOT_FOUND = 999L;

  @BeforeEach
  void setUp() {
    meetingRoomJpaRepository.deleteAll();
  }

  @Test
  @DisplayName("전체 회의실 조회 성공")
  void findAll_success() {
    // given
    MeetingRoomEntity entity1 =
        new MeetingRoomEntity(ROOM_NAME_A, CAPACITY_10, FEE_10000, ROOM_DESCRIPTION_1);
    MeetingRoomEntity entity2 =
        new MeetingRoomEntity(ROOM_NAME_B, CAPACITY_20, FEE_20000, ROOM_DESCRIPTION_2);
    meetingRoomJpaRepository.save(entity1);
    meetingRoomJpaRepository.save(entity2);

    // when
    List<MeetingRoom> result = meetingRoomQueryDbAdapter.findAll();

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getName()).isEqualTo(ROOM_NAME_A);
    assertThat(result.get(1).getName()).isEqualTo(ROOM_NAME_B);
  }

  @Test
  @DisplayName("전체 회의실 조회 - 빈 리스트")
  void findAll_empty() {
    // when
    List<MeetingRoom> result = meetingRoomQueryDbAdapter.findAll();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("ID로 회의실 조회 성공")
  void findById_success() {
    // given
    MeetingRoomEntity entity =
        new MeetingRoomEntity(ROOM_NAME_A, CAPACITY_10, FEE_10000, ROOM_DESCRIPTION);
    MeetingRoomEntity saved = meetingRoomJpaRepository.save(entity);
    Long id = saved.getId();

    // when
    MeetingRoom result = meetingRoomQueryDbAdapter.findById(id).orElseThrow();

    // then
    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getName()).isEqualTo(ROOM_NAME_A);
    assertThat(result.getCapacity()).isEqualTo(CAPACITY_10);
    assertThat(result.getHourlyFee()).isEqualByComparingTo(FEE_10000);
    assertThat(result.getDescription()).isEqualTo(ROOM_DESCRIPTION);
  }

  @Test
  @DisplayName("ID로 회의실 조회 실패 - 존재하지 않는 ID")
  void findById_notFound() {
    // when
    var result = meetingRoomQueryDbAdapter.findById(ROOM_ID_NOT_FOUND);

    // then
    assertThat(result).isEmpty();
  }
}
