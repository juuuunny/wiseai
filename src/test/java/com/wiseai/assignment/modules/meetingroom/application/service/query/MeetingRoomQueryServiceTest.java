package com.wiseai.assignment.modules.meetingroom.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wiseai.assignment.modules.meetingroom.application.dto.response.MeetingRoomResponse;
import com.wiseai.assignment.modules.meetingroom.application.port.out.query.MeetingRoomQueryPort;
import com.wiseai.assignment.modules.meetingroom.domain.exception.MeetingRoomException;
import com.wiseai.assignment.modules.meetingroom.domain.model.MeetingRoom;
import com.wiseai.assignment.modules.meetingroom.domain.status.MeetingRoomErrorStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("MeetingRoomQueryService 테스트")
class MeetingRoomQueryServiceTest {

  @Mock private MeetingRoomQueryPort meetingRoomQueryPort;

  @InjectMocks private MeetingRoomQueryService meetingRoomQueryService;

  @Test
  @DisplayName("전체 회의실 조회 성공")
  void getAllMeetingRooms_success() {
    // given
    MeetingRoom room1 =
        MeetingRoom.builder()
            .id(1L)
            .name("회의실 A")
            .capacity(10)
            .hourlyFee(new BigDecimal("10000"))
            .description("설명1")
            .build();
    MeetingRoom room2 =
        MeetingRoom.builder()
            .id(2L)
            .name("회의실 B")
            .capacity(20)
            .hourlyFee(new BigDecimal("20000"))
            .description("설명2")
            .build();

    given(meetingRoomQueryPort.findAll()).willReturn(List.of(room1, room2));

    // when
    List<MeetingRoomResponse> result = meetingRoomQueryService.getAllMeetingRooms();

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).id()).isEqualTo(1L);
    assertThat(result.get(0).name()).isEqualTo("회의실 A");
    assertThat(result.get(0).capacity()).isEqualTo(10);
    assertThat(result.get(0).hourlyFee()).isEqualByComparingTo(new BigDecimal("10000"));
    assertThat(result.get(0).description()).isEqualTo("설명1");

    assertThat(result.get(1).id()).isEqualTo(2L);
    assertThat(result.get(1).name()).isEqualTo("회의실 B");
    assertThat(result.get(1).capacity()).isEqualTo(20);
    assertThat(result.get(1).hourlyFee()).isEqualByComparingTo(new BigDecimal("20000"));
    assertThat(result.get(1).description()).isEqualTo("설명2");
  }

  @Test
  @DisplayName("전체 회의실 조회 - 빈 리스트")
  void getAllMeetingRooms_empty() {
    // given
    given(meetingRoomQueryPort.findAll()).willReturn(List.of());

    // when
    List<MeetingRoomResponse> result = meetingRoomQueryService.getAllMeetingRooms();

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("단건 회의실 조회 성공")
  void getMeetingRoom_success() {
    // given
    Long id = 1L;
    MeetingRoom meetingRoom =
        MeetingRoom.builder()
            .id(id)
            .name("회의실 A")
            .capacity(10)
            .hourlyFee(new BigDecimal("10000"))
            .description("설명")
            .build();

    given(meetingRoomQueryPort.findById(id)).willReturn(Optional.of(meetingRoom));

    // when
    MeetingRoomResponse result = meetingRoomQueryService.getMeetingRoom(id);

    // then
    assertThat(result.id()).isEqualTo(id);
    assertThat(result.name()).isEqualTo("회의실 A");
    assertThat(result.capacity()).isEqualTo(10);
    assertThat(result.hourlyFee()).isEqualByComparingTo(new BigDecimal("10000"));
    assertThat(result.description()).isEqualTo("설명");
  }

  @Test
  @DisplayName("단건 회의실 조회 실패 - 존재하지 않는 ID")
  void getMeetingRoom_notFound() {
    // given
    Long id = 999L;
    given(meetingRoomQueryPort.findById(id)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> meetingRoomQueryService.getMeetingRoom(id))
        .isInstanceOf(MeetingRoomException.class)
        .satisfies(
            exception -> {
              MeetingRoomException ex = (MeetingRoomException) exception;
              assertThat(ex.getErrorCode()).isEqualTo(MeetingRoomErrorStatus.NOT_FOUND);
            });
  }
}

