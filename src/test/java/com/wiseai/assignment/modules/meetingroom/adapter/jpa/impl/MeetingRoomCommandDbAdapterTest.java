package com.wiseai.assignment.modules.meetingroom.adapter.jpa.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

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
@Import({
  MeetingRoomEntityMapper.class,
  MeetingRoomCommandDbAdapter.class,
  JpaConfig.class
})
@DisplayName("MeetingRoomCommandDbAdapter 테스트")
class MeetingRoomCommandDbAdapterTest {

  @Autowired private MeetingRoomJpaRepository meetingRoomJpaRepository;

  @Autowired private MeetingRoomEntityMapper meetingRoomEntityMapper;

  @Autowired private MeetingRoomCommandDbAdapter meetingRoomCommandDbAdapter;

  @Test
  @DisplayName("회의실 저장 성공")
  void save_success() {
    // given
    MeetingRoom meetingRoom = MeetingRoom.create("회의실 A", 10, new BigDecimal("10000"), "설명");

    // when
    MeetingRoom saved = meetingRoomCommandDbAdapter.save(meetingRoom);

    // then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getName()).isEqualTo("회의실 A");
    assertThat(saved.getCapacity()).isEqualTo(10);
    assertThat(saved.getHourlyFee()).isEqualByComparingTo(new BigDecimal("10000"));
    assertThat(saved.getDescription()).isEqualTo("설명");
  }

  @Test
  @DisplayName("회의실 수정 성공")
  void update_success() {
    // given
    MeetingRoomEntity entity = new MeetingRoomEntity("회의실 A", 10, new BigDecimal("10000"), "설명");
    MeetingRoomEntity savedEntity = meetingRoomJpaRepository.save(entity);
    MeetingRoom saved = meetingRoomEntityMapper.toDomain(savedEntity);

    MeetingRoom updatedMeetingRoom =
        saved.updateInfo("회의실 B", 20, new BigDecimal("20000"), "수정된 설명");

    // when
    MeetingRoom updated = meetingRoomCommandDbAdapter.update(updatedMeetingRoom);

    // then
    assertThat(updated.getId()).isEqualTo(saved.getId());
    assertThat(updated.getName()).isEqualTo("회의실 B");
    assertThat(updated.getCapacity()).isEqualTo(20);
    assertThat(updated.getHourlyFee()).isEqualByComparingTo(new BigDecimal("20000"));
    assertThat(updated.getDescription()).isEqualTo("수정된 설명");
  }

  @Test
  @DisplayName("회의실 수정 실패 - 존재하지 않는 ID")
  void update_fail_notFound() {
    // given
    MeetingRoom meetingRoom =
        MeetingRoom.builder()
            .id(999L)
            .name("회의실 A")
            .capacity(10)
            .hourlyFee(new BigDecimal("10000"))
            .description("설명")
            .build();

    // when & then
    assertThatThrownBy(() -> meetingRoomCommandDbAdapter.update(meetingRoom))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("MeetingRoom not found with id: 999");
  }

  @Test
  @DisplayName("회의실 삭제 성공")
  void delete_success() {
    // given
    MeetingRoomEntity entity = new MeetingRoomEntity("회의실 A", 10, new BigDecimal("10000"), "설명");
    MeetingRoomEntity saved = meetingRoomJpaRepository.save(entity);
    Long id = saved.getId();

    // when
    meetingRoomCommandDbAdapter.delete(id);

    // then
    assertThat(meetingRoomJpaRepository.findById(id)).isEmpty();
  }
}
