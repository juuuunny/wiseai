package com.wiseai.assignment.modules.meetingroom.domain.exception;

import com.wiseai.assignment.modules.common.exception.BusinessException;
import com.wiseai.assignment.modules.common.status.BaseErrorCode;

/**
 * 회의실 도메인의 공통 예외.
 */
public class MeetingRoomException extends BusinessException {
  public MeetingRoomException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}

