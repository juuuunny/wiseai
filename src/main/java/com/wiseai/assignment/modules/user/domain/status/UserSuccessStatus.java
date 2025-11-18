package com.wiseai.assignment.modules.user.domain.status;

import org.springframework.http.HttpStatus;

import com.wiseai.assignment.modules.common.status.BaseSuccessCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserSuccessStatus implements BaseSuccessCode {
  CREATED_USER(HttpStatus.CREATED, "USER-201", "회원가입이 완료되었습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
