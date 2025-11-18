package com.wiseai.assignment.modules.user.application.port.out.auth;

/** JWT 토큰 검증을 위한 Port (user 모듈에서 auth 모듈 호출용) */
public interface JwtValidatePort {
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
