package com.wiseai.assignment.modules.user.application.dto.request;

import com.wiseai.assignment.modules.user.domain.exception.UserException;
import com.wiseai.assignment.modules.user.domain.status.UserErrorStatus;

public record SelfSignUpRequest(
    String email, String password, String passwordConfirm, String name) {

  public void validatePasswordMatch() {
    if (!password.equals(passwordConfirm)) {
      throw new UserException(UserErrorStatus.PASSWORD_CONFIRM_NOT_MATCH);
    }
  }
}
