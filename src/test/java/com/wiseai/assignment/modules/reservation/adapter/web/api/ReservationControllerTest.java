package com.wiseai.assignment.modules.reservation.adapter.web.api;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.web.servlet.MockMvc;

import com.wiseai.assignment.modules.common.exception.GlobalExceptionHandler;
import com.wiseai.assignment.modules.reservation.application.dto.request.CreateReservationRequest;
import com.wiseai.assignment.modules.reservation.application.dto.response.ReservationResponse;
import com.wiseai.assignment.modules.reservation.application.port.in.command.CancelReservationUseCase;
import com.wiseai.assignment.modules.reservation.application.port.in.command.CreateReservationUseCase;
import com.wiseai.assignment.modules.reservation.application.port.in.query.GetReservationUseCase;
import com.wiseai.assignment.modules.reservation.application.port.in.query.GetReservationsUseCase;
import com.wiseai.assignment.modules.reservation.domain.enums.ReservationStatus;
import com.wiseai.assignment.modules.reservation.domain.exception.ReservationException;
import com.wiseai.assignment.modules.reservation.domain.status.ReservationErrorStatus;
import com.wiseai.assignment.modules.security.config.SecurityConfig;
import com.wiseai.assignment.modules.security.filter.JwtFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
    controllers = ReservationController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = {
      @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = EnableWebSecurity.class),
      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
    })
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("ReservationController 테스트")
class ReservationControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private CreateReservationUseCase createReservationUseCase;
  @MockBean private GetReservationUseCase getReservationUseCase;
  @MockBean private GetReservationsUseCase getReservationsUseCase;
  @MockBean private CancelReservationUseCase cancelReservationUseCase;

  // 공통 테스트 데이터
  private static final int TEST_YEAR = 2024;
  private static final int TEST_MONTH = 1;
  private static final int TEST_DAY = 1;
  private static final Long DEFAULT_MEETING_ROOM_ID = 1L;
  private static final Long DEFAULT_USER_ID = 1L;
  private static final Long DEFAULT_RESERVATION_ID = 1L;
  private static final Long RESERVATION_ID_NOT_FOUND = 999L;
  private static final LocalDateTime DEFAULT_START_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 10, 0);
  private static final LocalDateTime DEFAULT_END_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 11, 0);
  private static final BigDecimal DEFAULT_TOTAL_AMOUNT = new BigDecimal("10000");

  @Test
  @DisplayName("예약 생성 성공")
  void createReservation_success() throws Exception {
    // given
    CreateReservationRequest request =
        new CreateReservationRequest(
            DEFAULT_MEETING_ROOM_ID, DEFAULT_START_TIME, DEFAULT_END_TIME, DEFAULT_TOTAL_AMOUNT);

    ReservationResponse response =
        new ReservationResponse(
            DEFAULT_RESERVATION_ID,
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            ReservationStatus.PENDING,
            DEFAULT_TOTAL_AMOUNT);

    given(
            createReservationUseCase.createReservation(
                DEFAULT_MEETING_ROOM_ID,
                DEFAULT_USER_ID,
                DEFAULT_START_TIME,
                DEFAULT_END_TIME,
                DEFAULT_TOTAL_AMOUNT))
        .willReturn(response);

    // when & then
    mockMvc
        .perform(
            post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.httpStatus").value(201))
        .andExpect(jsonPath("$.code").value("RESERVATION-001"))
        .andExpect(jsonPath("$.message").value("예약 생성에 성공했습니다."))
        .andExpect(jsonPath("$.data.id").value(DEFAULT_RESERVATION_ID))
        .andExpect(jsonPath("$.data.meetingRoomId").value(DEFAULT_MEETING_ROOM_ID))
        .andExpect(jsonPath("$.data.userId").value(DEFAULT_USER_ID))
        .andExpect(jsonPath("$.data.status").value("PENDING"));
  }

  @Test
  @DisplayName("예약 생성 실패 - 겹치는 예약 존재")
  void createReservation_fail_duplicate() throws Exception {
    // given
    CreateReservationRequest request =
        new CreateReservationRequest(
            DEFAULT_MEETING_ROOM_ID, DEFAULT_START_TIME, DEFAULT_END_TIME, DEFAULT_TOTAL_AMOUNT);

    given(
            createReservationUseCase.createReservation(
                DEFAULT_MEETING_ROOM_ID,
                DEFAULT_USER_ID,
                DEFAULT_START_TIME,
                DEFAULT_END_TIME,
                DEFAULT_TOTAL_AMOUNT))
        .willThrow(new ReservationException(ReservationErrorStatus.DUPLICATE_RESERVATION));

    // when & then
    mockMvc
        .perform(
            post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.httpStatus").value(409))
        .andExpect(jsonPath("$.code").value("RESERVATION-003"))
        .andExpect(jsonPath("$.message").value("해당 시간대에 이미 예약이 존재합니다."));
  }

  @Test
  @DisplayName("예약 단건 조회 성공")
  void getReservation_success() throws Exception {
    // given
    ReservationResponse response =
        new ReservationResponse(
            DEFAULT_RESERVATION_ID,
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            ReservationStatus.PENDING,
            DEFAULT_TOTAL_AMOUNT);

    given(getReservationUseCase.getReservation(DEFAULT_RESERVATION_ID)).willReturn(response);

    // when & then
    mockMvc
        .perform(get("/reservations/{id}", DEFAULT_RESERVATION_ID))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.httpStatus").value(200))
        .andExpect(jsonPath("$.code").value("RESERVATION-002"))
        .andExpect(jsonPath("$.message").value("예약 조회에 성공했습니다."))
        .andExpect(jsonPath("$.data.id").value(DEFAULT_RESERVATION_ID));
  }

  @Test
  @DisplayName("예약 단건 조회 실패 - 존재하지 않는 ID")
  void getReservation_notFound() throws Exception {
    // given
    given(getReservationUseCase.getReservation(RESERVATION_ID_NOT_FOUND))
        .willThrow(new ReservationException(ReservationErrorStatus.NOT_FOUND));

    // when & then
    mockMvc
        .perform(get("/reservations/{id}", RESERVATION_ID_NOT_FOUND))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.httpStatus").value(404))
        .andExpect(jsonPath("$.code").value("RESERVATION-006"));
  }

  @Test
  @DisplayName("사용자별 예약 목록 조회 성공")
  void getReservationsByUserId_success() throws Exception {
    // given
    ReservationResponse response1 =
        new ReservationResponse(
            DEFAULT_RESERVATION_ID,
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            ReservationStatus.PENDING,
            DEFAULT_TOTAL_AMOUNT);

    given(getReservationsUseCase.getReservationsByUserId(DEFAULT_USER_ID))
        .willReturn(List.of(response1));

    // when & then
    mockMvc
        .perform(get("/reservations/users/{userId}", DEFAULT_USER_ID))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.httpStatus").value(200))
        .andExpect(jsonPath("$.code").value("RESERVATION-003"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].id").value(DEFAULT_RESERVATION_ID));
  }

  @Test
  @DisplayName("예약 취소 성공")
  void cancelReservation_success() throws Exception {
    // given
    ReservationResponse response =
        new ReservationResponse(
            DEFAULT_RESERVATION_ID,
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            ReservationStatus.CANCELLED,
            DEFAULT_TOTAL_AMOUNT);

    given(cancelReservationUseCase.cancelReservation(DEFAULT_RESERVATION_ID, DEFAULT_USER_ID))
        .willReturn(response);

    // when & then
    mockMvc
        .perform(
            delete("/reservations/{id}/users/{userId}", DEFAULT_RESERVATION_ID, DEFAULT_USER_ID))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.httpStatus").value(200))
        .andExpect(jsonPath("$.code").value("RESERVATION-004"))
        .andExpect(jsonPath("$.message").value("예약 취소에 성공했습니다."))
        .andExpect(jsonPath("$.data.status").value("CANCELLED"));
  }

  @Test
  @DisplayName("예약 취소 실패 - 권한 없음")
  void cancelReservation_fail_unauthorized() throws Exception {
    // given
    given(cancelReservationUseCase.cancelReservation(DEFAULT_RESERVATION_ID, DEFAULT_USER_ID))
        .willThrow(new ReservationException(ReservationErrorStatus.UNAUTHORIZED));

    // when & then
    mockMvc
        .perform(
            delete("/reservations/{id}/users/{userId}", DEFAULT_RESERVATION_ID, DEFAULT_USER_ID))
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.httpStatus").value(403))
        .andExpect(jsonPath("$.code").value("RESERVATION-009"));
  }
}
