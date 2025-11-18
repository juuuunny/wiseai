package com.wiseai.assignment.modules.security.adapter.auth;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.auth.application.port.in.jwt.JwtValidateUseCase;
import com.wiseai.assignment.modules.security.application.port.out.auth.JwtValidatePort;
import com.wiseai.assignment.modules.user.domain.enums.RoleType;

import lombok.RequiredArgsConstructor;

/** JWT 토큰 검증 Adapter (security 모듈 -> auth 모듈) */
@Component("securityJwtValidateAdapter")
@RequiredArgsConstructor
public class JwtValidateAdapter implements JwtValidatePort {
  private final JwtValidateUseCase jwtValidateUseCase;

  @Override
  public void validateToken(String token) {
    jwtValidateUseCase.validateToken(token);
  }

  @Override
  public Long getUserIdFromToken(String token) {
    return jwtValidateUseCase.getUserIdFromToken(token);
  }

  @Override
  public RoleType getRoleFromToken(String token) {
    return jwtValidateUseCase.getRoleFromToken(token);
  }
}
