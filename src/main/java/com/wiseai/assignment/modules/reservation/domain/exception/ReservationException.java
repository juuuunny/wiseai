package com.wiseai.assignment.modules.reservation.domain.exception;

import com.wiseai.assignment.modules.common.exception.BusinessException;
import com.wiseai.assignment.modules.common.status.BaseErrorCode;

public class ReservationException extends BusinessException {
  public ReservationException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
