package com.wiseai.assignment.modules.user.adapter.auth;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.auth.application.port.in.jwt.JwtGenerateUseCase;
import com.wiseai.assignment.modules.user.application.port.out.auth.JwtGeneratePort;
import com.wiseai.assignment.modules.user.domain.enums.RoleType;

import lombok.RequiredArgsConstructor;

/** JWT 토큰 생성 Adapter (user 모듈 -> auth 모듈) */
@Component
@RequiredArgsConstructor
public class JwtGenerateAdapter implements JwtGeneratePort {
  private final JwtGenerateUseCase jwtGenerateUseCase;

  @Override
  public String generateAccessToken(Long userId, RoleType role) {
    return jwtGenerateUseCase.generateAccessToken(userId, role);
  }

  @Override
  public String generateRefreshToken(Long userId, RoleType role) {
    return jwtGenerateUseCase.generateRefreshToken(userId, role);
  }
}
