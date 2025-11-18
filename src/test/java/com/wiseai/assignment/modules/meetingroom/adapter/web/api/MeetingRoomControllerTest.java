package com.wiseai.assignment.modules.meetingroom.adapter.web.api;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.web.servlet.MockMvc;

import com.wiseai.assignment.modules.common.exception.GlobalExceptionHandler;
import com.wiseai.assignment.modules.meetingroom.application.dto.response.MeetingRoomResponse;
import com.wiseai.assignment.modules.meetingroom.application.port.in.query.GetMeetingRoomUseCase;
import com.wiseai.assignment.modules.meetingroom.application.port.in.query.GetMeetingRoomsUseCase;
import com.wiseai.assignment.modules.meetingroom.domain.exception.MeetingRoomException;
import com.wiseai.assignment.modules.meetingroom.domain.status.MeetingRoomErrorStatus;
import com.wiseai.assignment.modules.security.config.SecurityConfig;
import com.wiseai.assignment.modules.security.filter.JwtFilter;

@WebMvcTest(
    controllers = MeetingRoomController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = {
      @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = EnableWebSecurity.class),
      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
    })
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("MeetingRoomController 테스트")
class MeetingRoomControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private GetMeetingRoomsUseCase getMeetingRoomsUseCase;
  @MockBean private GetMeetingRoomUseCase getMeetingRoomUseCase;

  // 공통 테스트 데이터
  private static final Long ROOM_ID_1 = 1L;
  private static final Long ROOM_ID_2 = 2L;
  private static final Long ROOM_ID_NOT_FOUND = 999L;
  private static final String ROOM_NAME_A = "회의실 A";
  private static final String ROOM_NAME_B = "회의실 B";
  private static final String ROOM_DESCRIPTION_1 = "설명1";
  private static final String ROOM_DESCRIPTION_2 = "설명2";
  private static final String ROOM_DESCRIPTION = "설명";
  private static final int CAPACITY_10 = 10;
  private static final int CAPACITY_20 = 20;
  private static final BigDecimal FEE_10000 = new BigDecimal("10000");
  private static final BigDecimal FEE_20000 = new BigDecimal("20000");
  private static final int JSON_FEE_10000 = 10000;
  private static final int JSON_FEE_20000 = 20000;

  @Test
  @DisplayName("회의실 목록 조회 성공")
  void getMeetingRooms_success() throws Exception {
    // given
    MeetingRoomResponse room1 =
        new MeetingRoomResponse(ROOM_ID_1, ROOM_NAME_A, CAPACITY_10, FEE_10000, ROOM_DESCRIPTION_1);
    MeetingRoomResponse room2 =
        new MeetingRoomResponse(ROOM_ID_2, ROOM_NAME_B, CAPACITY_20, FEE_20000, ROOM_DESCRIPTION_2);

    given(getMeetingRoomsUseCase.getAllMeetingRooms()).willReturn(List.of(room1, room2));

    // when & then
    mockMvc
        .perform(get("/meeting-rooms"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.httpStatus").value(200))
        .andExpect(jsonPath("$.code").value("MEETINGROOM-001"))
        .andExpect(jsonPath("$.message").value("회의실 목록 조회에 성공했습니다."))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].id").value(ROOM_ID_1))
        .andExpect(jsonPath("$.data[0].name").value(ROOM_NAME_A))
        .andExpect(jsonPath("$.data[0].capacity").value(CAPACITY_10))
        .andExpect(jsonPath("$.data[0].hourlyFee").value(JSON_FEE_10000))
        .andExpect(jsonPath("$.data[0].description").value(ROOM_DESCRIPTION_1))
        .andExpect(jsonPath("$.data[1].id").value(ROOM_ID_2))
        .andExpect(jsonPath("$.data[1].name").value(ROOM_NAME_B))
        .andExpect(jsonPath("$.data[1].capacity").value(CAPACITY_20))
        .andExpect(jsonPath("$.data[1].hourlyFee").value(JSON_FEE_20000))
        .andExpect(jsonPath("$.data[1].description").value(ROOM_DESCRIPTION_2));
  }

  @Test
  @DisplayName("회의실 목록 조회 - 빈 리스트")
  void getMeetingRooms_empty() throws Exception {
    // given
    given(getMeetingRoomsUseCase.getAllMeetingRooms()).willReturn(List.of());

    // when & then
    mockMvc
        .perform(get("/meeting-rooms"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.httpStatus").value(200))
        .andExpect(jsonPath("$.code").value("MEETINGROOM-001"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("회의실 단건 조회 성공")
  void getMeetingRoom_success() throws Exception {
    // given
    Long id = ROOM_ID_1;
    MeetingRoomResponse meetingRoom =
        new MeetingRoomResponse(id, ROOM_NAME_A, CAPACITY_10, FEE_10000, ROOM_DESCRIPTION);

    given(getMeetingRoomUseCase.getMeetingRoom(id)).willReturn(meetingRoom);

    // when & then
    mockMvc
        .perform(get("/meeting-rooms/{id}", id))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.httpStatus").value(200))
        .andExpect(jsonPath("$.code").value("MEETINGROOM-002"))
        .andExpect(jsonPath("$.message").value("회의실 조회에 성공했습니다."))
        .andExpect(jsonPath("$.data.id").value(ROOM_ID_1))
        .andExpect(jsonPath("$.data.name").value(ROOM_NAME_A))
        .andExpect(jsonPath("$.data.capacity").value(CAPACITY_10))
        .andExpect(jsonPath("$.data.hourlyFee").value(JSON_FEE_10000))
        .andExpect(jsonPath("$.data.description").value(ROOM_DESCRIPTION));
  }

  @Test
  @DisplayName("회의실 단건 조회 실패 - 존재하지 않는 ID")
  void getMeetingRoom_notFound() throws Exception {
    // given
    Long id = ROOM_ID_NOT_FOUND;
    given(getMeetingRoomUseCase.getMeetingRoom(id))
        .willThrow(new MeetingRoomException(MeetingRoomErrorStatus.NOT_FOUND));

    // when & then
    mockMvc
        .perform(get("/meeting-rooms/{id}", id))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.httpStatus").value(404))
        .andExpect(jsonPath("$.code").value("MEETINGROOM-004"))
        .andExpect(jsonPath("$.message").value("회의실을 찾을 수 없습니다."));
  }

  @Test
  @DisplayName("회의실 단건 조회 실패 - 잘못된 ID (0)")
  void getMeetingRoom_invalidId_zero() throws Exception {
    // when & then
    mockMvc
        .perform(get("/meeting-rooms/{id}", 0))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("회의실 단건 조회 실패 - 잘못된 ID (음수)")
  void getMeetingRoom_invalidId_negative() throws Exception {
    // when & then
    mockMvc
        .perform(get("/meeting-rooms/{id}", -1))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }
}
