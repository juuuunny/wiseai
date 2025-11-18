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

  // 공통 테스트 데이터
  private static final Long ROOM_ID_1 = 1L;
  private static final Long ROOM_ID_2 = 2L;
  private static final String ROOM_NAME_A = "회의실 A";
  private static final String ROOM_NAME_B = "회의실 B";
  private static final String ROOM_DESCRIPTION_1 = "설명1";
  private static final String ROOM_DESCRIPTION_2 = "설명2";
  private static final String ROOM_DESCRIPTION = "설명";
  private static final int CAPACITY_10 = 10;
  private static final int CAPACITY_20 = 20;
  private static final BigDecimal FEE_10000 = new BigDecimal("10000");
  private static final BigDecimal FEE_20000 = new BigDecimal("20000");

  @Test
  @DisplayName("전체 회의실 조회 성공")
  void getAllMeetingRooms_success() {
    // given
    MeetingRoom room1 =
        MeetingRoom.builder()
            .id(ROOM_ID_1)
            .name(ROOM_NAME_A)
            .capacity(CAPACITY_10)
            .hourlyFee(FEE_10000)
            .description(ROOM_DESCRIPTION_1)
            .build();
    MeetingRoom room2 =
        MeetingRoom.builder()
            .id(ROOM_ID_2)
            .name(ROOM_NAME_B)
            .capacity(CAPACITY_20)
            .hourlyFee(FEE_20000)
            .description(ROOM_DESCRIPTION_2)
            .build();

    given(meetingRoomQueryPort.findAll()).willReturn(List.of(room1, room2));

    // when
    List<MeetingRoomResponse> result = meetingRoomQueryService.getAllMeetingRooms();

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).id()).isEqualTo(ROOM_ID_1);
    assertThat(result.get(0).name()).isEqualTo(ROOM_NAME_A);
    assertThat(result.get(0).capacity()).isEqualTo(CAPACITY_10);
    assertThat(result.get(0).hourlyFee()).isEqualByComparingTo(FEE_10000);
    assertThat(result.get(0).description()).isEqualTo(ROOM_DESCRIPTION_1);

    assertThat(result.get(1).id()).isEqualTo(ROOM_ID_2);
    assertThat(result.get(1).name()).isEqualTo(ROOM_NAME_B);
    assertThat(result.get(1).capacity()).isEqualTo(CAPACITY_20);
    assertThat(result.get(1).hourlyFee()).isEqualByComparingTo(FEE_20000);
    assertThat(result.get(1).description()).isEqualTo(ROOM_DESCRIPTION_2);
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
    Long id = ROOM_ID_1;
    MeetingRoom meetingRoom =
        MeetingRoom.builder()
            .id(id)
            .name(ROOM_NAME_A)
            .capacity(CAPACITY_10)
            .hourlyFee(FEE_10000)
            .description(ROOM_DESCRIPTION)
            .build();

    given(meetingRoomQueryPort.findById(id)).willReturn(Optional.of(meetingRoom));

    // when
    MeetingRoomResponse result = meetingRoomQueryService.getMeetingRoom(id);

    // then
    assertThat(result.id()).isEqualTo(id);
    assertThat(result.name()).isEqualTo(ROOM_NAME_A);
    assertThat(result.capacity()).isEqualTo(CAPACITY_10);
    assertThat(result.hourlyFee()).isEqualByComparingTo(FEE_10000);
    assertThat(result.description()).isEqualTo(ROOM_DESCRIPTION);
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
