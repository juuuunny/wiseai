package com.wiseai.assignment.modules.user.domain.exception;

import com.wiseai.assignment.modules.common.exception.BusinessException;
import com.wiseai.assignment.modules.common.status.BaseErrorCode;

public class UserException extends BusinessException {
  public UserException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
