package com.wiseai.assignment.modules.meetingroom.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import com.wiseai.assignment.modules.meetingroom.domain.exception.MeetingRoomException;
import com.wiseai.assignment.modules.meetingroom.domain.status.MeetingRoomErrorStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MeetingRoomTest {

  @Test
  @DisplayName("회의실 생성 성공")
  void createMeetingRoom_success() {
    MeetingRoom meetingRoom =
        MeetingRoom.create("Maple", 6, new BigDecimal("15000"), "화이트보드 준비");

    assertThat(meetingRoom.getName()).isEqualTo("Maple");
    assertThat(meetingRoom.getCapacity()).isEqualTo(6);
    assertThat(meetingRoom.getHourlyFee()).isEqualTo(new BigDecimal("15000"));
  }

  @Test
  @DisplayName("이름이 비어있으면 예외가 발생한다")
  void createMeetingRoom_fail_invalidName() {
    assertThatThrownBy(() -> MeetingRoom.create(" ", 6, new BigDecimal("10000"), null))
        .isInstanceOf(MeetingRoomException.class)
        .hasMessage(MeetingRoomErrorStatus.INVALID_NAME.getMessage());
  }

  @Test
  @DisplayName("수용 인원이 1명 미만이면 예외가 발생한다")
  void createMeetingRoom_fail_invalidCapacity() {
    assertThatThrownBy(() -> MeetingRoom.create("Pine", 0, new BigDecimal("12000"), null))
        .isInstanceOf(MeetingRoomException.class)
        .hasMessage(MeetingRoomErrorStatus.INVALID_CAPACITY.getMessage());
  }

  @Test
  @DisplayName("시간당 요금이 음수면 예외가 발생한다")
  void createMeetingRoom_fail_invalidFee() {
    assertThatThrownBy(() -> MeetingRoom.create("Oak", 4, new BigDecimal("-1"), null))
        .isInstanceOf(MeetingRoomException.class)
        .hasMessage(MeetingRoomErrorStatus.INVALID_HOURLY_FEE.getMessage());
  }

  @Test
  @DisplayName("회의실 정보 수정 성공")
  void updateMeetingRoom_success() {
    MeetingRoom meetingRoom =
        MeetingRoom.create("Alpha", 8, new BigDecimal("20000"), null);

    MeetingRoom updated =
        meetingRoom.updateInfo("Beta", 10, new BigDecimal("25000"), "프로젝터 준비");

    assertThat(updated.getName()).isEqualTo("Beta");
    assertThat(updated.getCapacity()).isEqualTo(10);
    assertThat(updated.getHourlyFee()).isEqualTo(new BigDecimal("25000"));
  }

  @Test
  @DisplayName("수용 인원 체크")
  void canAccommodate() {
    MeetingRoom meetingRoom = MeetingRoom.create("Alpha", 4, new BigDecimal("10000"), null);

    assertThat(meetingRoom.canAccommodate(3)).isTrue();
    assertThat(meetingRoom.canAccommodate(5)).isFalse();
  }

  @Test
  @DisplayName("이용 요금 계산")
  void calculateFee_success() {
    MeetingRoom meetingRoom = MeetingRoom.create("Alpha", 4, new BigDecimal("12000"), null);

    BigDecimal fee = meetingRoom.calculateFee(90);

    assertThat(fee).isEqualTo(new BigDecimal("18000"));
  }

  @Test
  @DisplayName("이용 시간이 0분 이하면 예외 발생")
  void calculateFee_fail_minutes() {
    MeetingRoom meetingRoom = MeetingRoom.create("Alpha", 4, new BigDecimal("12000"), null);

    assertThatThrownBy(() -> meetingRoom.calculateFee(0))
        .isInstanceOf(MeetingRoomException.class)
        .hasMessage(MeetingRoomErrorStatus.INVALID_HOURLY_FEE.getMessage());
  }
}

