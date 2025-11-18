package com.wiseai.assignment.modules.user.domain.status;

import org.springframework.http.HttpStatus;

import com.wiseai.assignment.modules.common.status.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorStatus implements BaseErrorCode {
  DUPLICATED_EMAIL(HttpStatus.CONFLICT, "USER-001", "이미 사용 중인 이메일입니다."),
  INVALID_CREDENTIAL(HttpStatus.BAD_REQUEST, "USER-002", "이메일 또는 비밀번호가 올바르지 않습니다."),
  PASSWORD_CONFIRM_NOT_MATCH(HttpStatus.BAD_REQUEST, "USER-003", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
