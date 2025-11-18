package com.wiseai.assignment.modules.meetingroom.domain.status;

import org.springframework.http.HttpStatus;

import com.wiseai.assignment.modules.common.status.BaseSuccessCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeetingRoomSuccessStatus implements BaseSuccessCode {
  OK_GET_MEETING_ROOMS(HttpStatus.OK, "MEETINGROOM-001", "회의실 목록 조회에 성공했습니다."),
  OK_GET_MEETING_ROOM(HttpStatus.OK, "MEETINGROOM-002", "회의실 조회에 성공했습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
