package com.wiseai.assignment.modules.reservation.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wiseai.assignment.modules.reservation.application.dto.response.ReservationResponse;
import com.wiseai.assignment.modules.reservation.application.port.out.query.ReservationQueryPort;
import com.wiseai.assignment.modules.reservation.domain.enums.ReservationStatus;
import com.wiseai.assignment.modules.reservation.domain.exception.ReservationException;
import com.wiseai.assignment.modules.reservation.domain.model.Reservation;
import com.wiseai.assignment.modules.reservation.domain.status.ReservationErrorStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationQueryService 테스트")
class ReservationQueryServiceTest {

  @Mock private ReservationQueryPort reservationQueryPort;

  @InjectMocks private ReservationQueryService reservationQueryService;

  // 공통 테스트 데이터
  private static final int TEST_YEAR = 2024;
  private static final int TEST_MONTH = 1;
  private static final int TEST_DAY = 1;
  private static final Long DEFAULT_MEETING_ROOM_ID = 1L;
  private static final Long DEFAULT_USER_ID = 1L;
  private static final Long DEFAULT_RESERVATION_ID = 1L;
  private static final Long SECOND_RESERVATION_ID = 2L;
  private static final LocalDateTime DEFAULT_START_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 10, 0);
  private static final LocalDateTime DEFAULT_END_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 11, 0);
  private static final LocalDateTime SECOND_START_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 11, 0);
  private static final LocalDateTime SECOND_END_TIME =
      LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 12, 0);
  private static final BigDecimal DEFAULT_TOTAL_AMOUNT = new BigDecimal("10000");
  private static final BigDecimal SECOND_TOTAL_AMOUNT = new BigDecimal("15000");

  @Test
  @DisplayName("예약 단건 조회 성공")
  void getReservation_success() {
    // given
    Reservation reservation =
        Reservation.create(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);
    Reservation reservationWithId = reservation.withId(DEFAULT_RESERVATION_ID);

    given(reservationQueryPort.findById(DEFAULT_RESERVATION_ID))
        .willReturn(Optional.of(reservationWithId));

    // when
    ReservationResponse result = reservationQueryService.getReservation(DEFAULT_RESERVATION_ID);

    // then
    assertThat(result.id()).isEqualTo(DEFAULT_RESERVATION_ID);
    assertThat(result.meetingRoomId()).isEqualTo(DEFAULT_MEETING_ROOM_ID);
    assertThat(result.userId()).isEqualTo(DEFAULT_USER_ID);
    assertThat(result.startTime()).isEqualTo(DEFAULT_START_TIME);
    assertThat(result.endTime()).isEqualTo(DEFAULT_END_TIME);
    assertThat(result.status()).isEqualTo(ReservationStatus.PENDING);
    assertThat(result.totalAmount()).isEqualByComparingTo(DEFAULT_TOTAL_AMOUNT);
  }

  @Test
  @DisplayName("예약 단건 조회 실패 - 존재하지 않는 ID")
  void getReservation_notFound() {
    // given
    Long notFoundId = 999L;
    given(reservationQueryPort.findById(notFoundId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> reservationQueryService.getReservation(notFoundId))
        .isInstanceOf(ReservationException.class)
        .satisfies(
            exception -> {
              ReservationException ex = (ReservationException) exception;
              assertThat(ex.getErrorCode()).isEqualTo(ReservationErrorStatus.NOT_FOUND);
            });
  }

  @Test
  @DisplayName("사용자별 예약 목록 조회 성공")
  void getReservationsByUserId_success() {
    // given
    Reservation reservation1 =
        Reservation.create(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME,
            DEFAULT_TOTAL_AMOUNT);
    reservation1 = reservation1.withId(DEFAULT_RESERVATION_ID);

    Reservation reservation2 =
        Reservation.create(
            DEFAULT_MEETING_ROOM_ID,
            DEFAULT_USER_ID,
            SECOND_START_TIME,
            SECOND_END_TIME,
            SECOND_TOTAL_AMOUNT);
    reservation2 = reservation2.withId(SECOND_RESERVATION_ID);

    given(reservationQueryPort.findByUserId(DEFAULT_USER_ID))
        .willReturn(List.of(reservation1, reservation2));

    // when
    List<ReservationResponse> result =
        reservationQueryService.getReservationsByUserId(DEFAULT_USER_ID);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).id()).isEqualTo(DEFAULT_RESERVATION_ID);
    assertThat(result.get(1).id()).isEqualTo(SECOND_RESERVATION_ID);
  }

  @Test
  @DisplayName("사용자별 예약 목록 조회 - 빈 리스트")
  void getReservationsByUserId_empty() {
    // given
    given(reservationQueryPort.findByUserId(DEFAULT_USER_ID)).willReturn(List.of());

    // when
    List<ReservationResponse> result =
        reservationQueryService.getReservationsByUserId(DEFAULT_USER_ID);

    // then
    assertThat(result).isEmpty();
  }
}
