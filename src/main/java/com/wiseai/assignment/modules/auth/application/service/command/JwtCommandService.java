package com.wiseai.assignment.modules.auth.application.service.command;

import org.springframework.stereotype.Service;

import com.wiseai.assignment.modules.auth.application.port.in.jwt.JwtGenerateUseCase;
import com.wiseai.assignment.modules.auth.domain.exception.AuthException;
import com.wiseai.assignment.modules.user.domain.enums.RoleType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtCommandService implements JwtGenerateUseCase {
  private final JwtTokenGenerator jwtTokenGenerator;

  /**
   * 주어진 사용자 ID와 역할을 기반으로 어세스 토큰을 생성합니다.
   *
   * @param userId 토큰을 발급할 사용자 ID
   * @param role 사용자의 역할 정보
   * @return 생성된 어세스 토큰 문자열
   * @throws AuthException 어세스 토큰 생성에 실패한 경우 발생
   */
  @Override
  public String generateAccessToken(Long userId, RoleType role) {
    return jwtTokenGenerator.generateAccessToken(userId, role);
  }

  /**
   * 지정된 사용자 ID와 역할을 기반으로 리프레시 토큰을 생성합니다.
   *
   * @param userId 토큰을 발급할 사용자 ID
   * @param role 토큰을 발급할 사용자의 역할
   * @return 생성된 리프레시 토큰 문자열
   * @throws AuthException 토큰 생성에 실패한 경우 발생
   */
  @Override
  public String generateRefreshToken(Long userId, RoleType role) {
    return jwtTokenGenerator.generateRefreshToken(userId, role);
  }
}
