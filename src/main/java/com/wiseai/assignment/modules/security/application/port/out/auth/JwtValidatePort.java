package com.wiseai.assignment.modules.security.application.port.out.auth;

import com.wiseai.assignment.modules.user.domain.enums.RoleType;

/** JWT 토큰 검증을 위한 Port (security 모듈에서 auth 모듈 호출용) */
public interface JwtValidatePort {
  /**
   * JWT 토큰의 유효성을 검사합니다.
   *
   * @param token 검사할 JWT 토큰 문자열
   */
  void validateToken(String token);

  /**
   * JWT 토큰에서 사용자 ID를 추출합니다.
   *
   * @param token JWT 토큰 문자열
   * @return 사용자 ID
   */
  Long getUserIdFromToken(String token);

  /**
   * JWT 토큰에서 사용자 역할을 추출합니다.
   *
   * @param token JWT 토큰 문자열
   * @return 사용자 역할
   */
  RoleType getRoleFromToken(String token);
}
