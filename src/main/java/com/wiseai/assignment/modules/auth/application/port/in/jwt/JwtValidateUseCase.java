package com.wiseai.assignment.modules.auth.application.port.in.jwt;

import com.wiseai.assignment.modules.user.domain.enums.RoleType;

public interface JwtValidateUseCase {
  /**
   * 주어진 JWT 토큰의 유효성을 검사합니다.
   *
   * @param token 검사할 JWT 토큰 문자열
   */
  void validateToken(String token);

  /**
   * JWT 토큰에서 유저의 ID를 추출하여 반환합니다.
   *
   * @param token 유저 정보를 포함한 JWT 토큰
   * @return 토큰에 인코딩된 유저의 ID
   */
  Long getUserIdFromToken(String token);

  /**
   * JWT 토큰에서 사용자의 역할(RoleType)을 추출합니다.
   *
   * @param token 역할 정보를 포함한 JWT 토큰
   * @return 토큰에 인코딩된 사용자의 역할
   */
  RoleType getRoleFromToken(String token);

  /**
   * 액세스 토큰의 만료 시간을 반환합니다.
   *
   * @return 액세스 토큰의 만료 시간(밀리초 단위)
   */
  long getAccessTokenExpirationTime();

  /**
   * 리프레시 토큰의 만료 시간을 반환합니다.
   *
   * @return 리프레시 토큰의 만료 시간(밀리초 단위)
   */
  long getRefreshTokenExpirationTime();
}
