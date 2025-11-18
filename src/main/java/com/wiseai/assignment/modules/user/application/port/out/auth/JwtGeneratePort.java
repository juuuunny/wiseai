package com.wiseai.assignment.modules.user.application.port.out.auth;

import com.wiseai.assignment.modules.user.domain.enums.RoleType;

/** JWT 토큰 생성을 위한 Port (user 모듈에서 auth 모듈 호출용) */
public interface JwtGeneratePort {
  /**
   * 액세스 토큰을 생성합니다.
   *
   * @param userId 사용자 ID
   * @param role 사용자 역할
   * @return 생성된 액세스 토큰
   */
  String generateAccessToken(Long userId, RoleType role);

  /**
   * 리프레시 토큰을 생성합니다.
   *
   * @param userId 사용자 ID
   * @param role 사용자 역할
   * @return 생성된 리프레시 토큰
   */
  String generateRefreshToken(Long userId, RoleType role);
}
