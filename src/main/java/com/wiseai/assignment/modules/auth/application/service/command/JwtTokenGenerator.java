package com.wiseai.assignment.modules.auth.application.service.command;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.auth.application.port.out.jwt.JwtGeneratorPort;
import com.wiseai.assignment.modules.auth.domain.exception.AuthException;
import com.wiseai.assignment.modules.auth.domain.status.AuthErrorStatus;
import com.wiseai.assignment.modules.user.domain.enums.RoleType;

import lombok.RequiredArgsConstructor;

/** JWT 토큰 생성 로직을 공통화하여 중복 제거 */
@Component
@RequiredArgsConstructor
public class JwtTokenGenerator {
  private final JwtGeneratorPort jwtGeneratorPort;

  /** 액세스 토큰 생성 */
  public String generateAccessToken(Long userId, RoleType role) {
    try {
      return jwtGeneratorPort.generateAccessToken(userId, role);
    } catch (Exception e) {
      throw new AuthException(AuthErrorStatus.FAILED_GENERATE_ACCESS_TOKEN);
    }
  }

  /** 리프레시 토큰 생성 */
  public String generateRefreshToken(Long userId, RoleType role) {
    try {
      return jwtGeneratorPort.generateRefreshToken(userId, role);
    } catch (Exception e) {
      throw new AuthException(AuthErrorStatus.FAILED_GENERATE_REFRESH_TOKEN);
    }
  }
}
