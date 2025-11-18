package com.wiseai.assignment.modules.meetingroom.domain.status;

import org.springframework.http.HttpStatus;

import com.wiseai.assignment.modules.common.status.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 회의실 도메인에서 공통으로 사용하는 에러 코드. */
@Getter
@RequiredArgsConstructor
public enum MeetingRoomErrorStatus implements BaseErrorCode {
  INVALID_NAME(HttpStatus.BAD_REQUEST, "MEETINGROOM-001", "회의실 이름을 입력해주세요."),
  INVALID_CAPACITY(HttpStatus.BAD_REQUEST, "MEETINGROOM-002", "수용 인원은 1명 이상이어야 합니다."),
  INVALID_HOURLY_FEE(HttpStatus.BAD_REQUEST, "MEETINGROOM-003", "시간당 요금은 0 이상이어야 합니다."),
  INVALID_MINUTES(HttpStatus.BAD_REQUEST, "MEETINGROOM-005", "예약 시간은 1분 이상이어야 합니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "MEETINGROOM-004", "회의실을 찾을 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
