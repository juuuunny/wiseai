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

import com.wiseai.assignment.modules.meetingroom.adapter.jpa.entity.MeetingRoomEntity;
import com.wiseai.assignment.modules.meetingroom.adapter.jpa.mapper.MeetingRoomEntityMapper;
import com.wiseai.assignment.modules.meetingroom.adapter.jpa.repository.MeetingRoomJpaRepository;
import com.wiseai.assignment.modules.meetingroom.domain.model.MeetingRoom;

@DataJpaTest
@Import({
  MeetingRoomEntityMapper.class,
  MeetingRoomQueryDbAdapter.class,
  com.wiseai.assignment.modules.common.config.adapter.persistence.JpaConfig.class
})
@DisplayName("MeetingRoomQueryDbAdapter 테스트")
class MeetingRoomQueryDbAdapterTest {

  @Autowired private MeetingRoomJpaRepository meetingRoomJpaRepository;

  @Autowired private MeetingRoomQueryDbAdapter meetingRoomQueryDbAdapter;

  @BeforeEach
  void setUp() {
    meetingRoomJpaRepository.deleteAll();
  }

  @Test
  @DisplayName("전체 회의실 조회 성공")
  void findAll_success() {
    // given
    MeetingRoomEntity entity1 = new MeetingRoomEntity("회의실 A", 10, new BigDecimal("10000"), "설명1");
    MeetingRoomEntity entity2 = new MeetingRoomEntity("회의실 B", 20, new BigDecimal("20000"), "설명2");
    meetingRoomJpaRepository.save(entity1);
    meetingRoomJpaRepository.save(entity2);

    // when
    List<MeetingRoom> result = meetingRoomQueryDbAdapter.findAll();

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getName()).isEqualTo("회의실 A");
    assertThat(result.get(1).getName()).isEqualTo("회의실 B");
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
    MeetingRoomEntity entity = new MeetingRoomEntity("회의실 A", 10, new BigDecimal("10000"), "설명");
    MeetingRoomEntity saved = meetingRoomJpaRepository.save(entity);
    Long id = saved.getId();

    // when
    MeetingRoom result = meetingRoomQueryDbAdapter.findById(id).orElseThrow();

    // then
    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getName()).isEqualTo("회의실 A");
    assertThat(result.getCapacity()).isEqualTo(10);
    assertThat(result.getHourlyFee()).isEqualByComparingTo(new BigDecimal("10000"));
    assertThat(result.getDescription()).isEqualTo("설명");
  }

  @Test
  @DisplayName("ID로 회의실 조회 실패 - 존재하지 않는 ID")
  void findById_notFound() {
    // when
    var result = meetingRoomQueryDbAdapter.findById(999L);

    // then
    assertThat(result).isEmpty();
  }
}
