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
@Import({MeetingRoomEntityMapper.class, MeetingRoomCommandDbAdapter.class, JpaConfig.class})
@DisplayName("MeetingRoomCommandDbAdapter 테스트")
class MeetingRoomCommandDbAdapterTest {

  @Autowired private MeetingRoomJpaRepository meetingRoomJpaRepository;

  @Autowired private MeetingRoomEntityMapper meetingRoomEntityMapper;

  @Autowired private MeetingRoomCommandDbAdapter meetingRoomCommandDbAdapter;

  // 공통 테스트 데이터
  private static final String ROOM_NAME_A = "회의실 A";
  private static final String ROOM_NAME_B = "회의실 B";
  private static final String ROOM_DESCRIPTION = "설명";
  private static final String ROOM_DESCRIPTION_UPDATED = "수정된 설명";
  private static final int CAPACITY_10 = 10;
  private static final int CAPACITY_20 = 20;
  private static final BigDecimal FEE_10000 = new BigDecimal("10000");
  private static final BigDecimal FEE_20000 = new BigDecimal("20000");
  private static final Long ROOM_ID_NOT_FOUND = 999L;

  @Test
  @DisplayName("회의실 저장 성공")
  void save_success() {
    // given
    MeetingRoom meetingRoom =
        MeetingRoom.create(ROOM_NAME_A, CAPACITY_10, FEE_10000, ROOM_DESCRIPTION);

    // when
    MeetingRoom saved = meetingRoomCommandDbAdapter.save(meetingRoom);

    // then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getName()).isEqualTo(ROOM_NAME_A);
    assertThat(saved.getCapacity()).isEqualTo(CAPACITY_10);
    assertThat(saved.getHourlyFee()).isEqualByComparingTo(FEE_10000);
    assertThat(saved.getDescription()).isEqualTo(ROOM_DESCRIPTION);
  }

  @Test
  @DisplayName("회의실 수정 성공")
  void update_success() {
    // given
    MeetingRoomEntity entity =
        new MeetingRoomEntity(ROOM_NAME_A, CAPACITY_10, FEE_10000, ROOM_DESCRIPTION);
    MeetingRoomEntity savedEntity = meetingRoomJpaRepository.save(entity);
    MeetingRoom saved = meetingRoomEntityMapper.toDomain(savedEntity);

    MeetingRoom updatedMeetingRoom =
        saved.updateInfo(ROOM_NAME_B, CAPACITY_20, FEE_20000, ROOM_DESCRIPTION_UPDATED);

    // when
    MeetingRoom updated = meetingRoomCommandDbAdapter.update(updatedMeetingRoom);

    // then
    assertThat(updated.getId()).isEqualTo(saved.getId());
    assertThat(updated.getName()).isEqualTo(ROOM_NAME_B);
    assertThat(updated.getCapacity()).isEqualTo(CAPACITY_20);
    assertThat(updated.getHourlyFee()).isEqualByComparingTo(FEE_20000);
    assertThat(updated.getDescription()).isEqualTo(ROOM_DESCRIPTION_UPDATED);
  }

  @Test
  @DisplayName("회의실 수정 실패 - 존재하지 않는 ID")
  void update_fail_notFound() {
    // given
    MeetingRoom meetingRoom =
        MeetingRoom.builder()
            .id(ROOM_ID_NOT_FOUND)
            .name(ROOM_NAME_A)
            .capacity(CAPACITY_10)
            .hourlyFee(FEE_10000)
            .description(ROOM_DESCRIPTION)
            .build();

    // when & then
    assertThatThrownBy(() -> meetingRoomCommandDbAdapter.update(meetingRoom))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("MeetingRoom not found with id: " + ROOM_ID_NOT_FOUND);
  }

  @Test
  @DisplayName("회의실 삭제 성공")
  void delete_success() {
    // given
    MeetingRoomEntity entity =
        new MeetingRoomEntity(ROOM_NAME_A, CAPACITY_10, FEE_10000, ROOM_DESCRIPTION);
    MeetingRoomEntity saved = meetingRoomJpaRepository.save(entity);
    Long id = saved.getId();

    // when
    meetingRoomCommandDbAdapter.delete(id);

    // then
    assertThat(meetingRoomJpaRepository.findById(id)).isEmpty();
  }
}
