package com.wiseai.assignment.modules.auth.application.port.in.jwt;

import com.wiseai.assignment.modules.user.domain.enums.RoleType;

public interface JwtGenerateUseCase {
  /**
   * 주어진 사용자 ID와 역할을 기반으로 액세스 토큰을 생성합니다.
   *
   * @param userId 액세스 토큰을 생성할 사용자의 ID
   * @param role 사용자의 역할 정보
   * @return 생성된 액세스 토큰 문자열
   */
  String generateAccessToken(Long userId, RoleType role);

  /**
   * 주어진 사용자 ID와 역할을 기반해 토큰 재발급용 리프레시 JWT를 생성합니다.
   *
   * <p>리프레시 토큰은 액세스 토큰 만료 시 새로운 액세스 토큰을 발급받기 위해 사용됩니다.
   *
   * @param userId 리프레시 토큰을 발급할 대상 사용자의 식별자
   * @param role 발급 대상 사용자의 역할 (RoleType)
   * @return 생성된 리프레시 JWT 토큰 문자열
   */
  String generateRefreshToken(Long userId, RoleType role);
}
