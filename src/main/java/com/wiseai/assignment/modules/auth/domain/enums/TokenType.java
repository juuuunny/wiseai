package com.wiseai.assignment.modules.auth.domain.enums;

import java.util.Arrays;

import com.wiseai.assignment.modules.auth.domain.exception.AuthException;
import com.wiseai.assignment.modules.auth.domain.status.AuthErrorStatus;

import lombok.Getter;

@Getter
public enum TokenType {
  ACCESS("ACCESS"),
  REFRESH("REFRESH");

  private final String value;

  TokenType(String value) {
    this.value = value;
  }

  /**
   * 주어진 문자열에 해당하는 TokenType 열거형 상수를 반환합니다.
   *
   * <p>입력값이 "ACCESS", "REFRESH" 중 하나와 일치하지 않으면 규칙 위반을 로그로 남기고 AuthException을 발생시킵니다.
   *
   * @param input 토큰 타입을 나타내는 문자열
   * @return 일치하는 TokenType 열거형 상수
   * @throws AuthException 입력값이 유효하지 않은 경우 발생
   */
  public static TokenType of(String input) {
    return Arrays.stream(TokenType.values())
        .filter(type -> type.value.equalsIgnoreCase(input) || type.name().equalsIgnoreCase(input))
        .findFirst()
        .orElseThrow(() -> new AuthException(AuthErrorStatus.BAD_REQUEST_TOKEN_TYPE));
  }
}
