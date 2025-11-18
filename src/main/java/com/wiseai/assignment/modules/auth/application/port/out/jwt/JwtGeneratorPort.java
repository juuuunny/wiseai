package com.wiseai.assignment.modules.auth.application.port.out.jwt;

import com.wiseai.assignment.modules.user.domain.enums.RoleType;

public interface JwtGeneratorPort {
  /**
   * 주어진 사용자 ID와 역할 정보를 기반으로 액세스 토큰을 생성합니다.
   *
   * @param userId 액세스 토큰을 생성할 사용자의 고유 식별자
   * @param role 사용자의 역할 유형
   * @return 생성된 액세스 토큰 문자열
   */
  String generateAccessToken(Long userId, RoleType role);

  /**
   * 주어진 사용자 ID와 역할 정보를 기반으로 리프레시 토큰을 생성합니다.
   *
   * @param userId 토큰을 생성할 사용자의 고유 식별자
   * @param role 사용자의 역할 유형
   * @return 생성된 리프레시 토큰 문자열
   */
  String generateRefreshToken(Long userId, RoleType role);
}
