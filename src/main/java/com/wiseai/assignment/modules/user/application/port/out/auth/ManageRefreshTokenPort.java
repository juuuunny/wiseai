package com.wiseai.assignment.modules.user.application.port.out.auth;

/** 리프레시 토큰 관리를 위한 Port (user 모듈에서 auth 모듈 호출용) */
public interface ManageRefreshTokenPort {
  /**
   * 리프레시 토큰을 저장합니다.
   *
   * @param userId 사용자 ID (문자열)
   * @param refreshToken 리프레시 토큰
   */
  void saveRefreshToken(String userId, String refreshToken);
}
