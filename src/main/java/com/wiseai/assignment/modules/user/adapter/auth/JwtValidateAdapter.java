package com.wiseai.assignment.modules.user.adapter.auth;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.auth.application.port.in.jwt.JwtValidateUseCase;
import com.wiseai.assignment.modules.user.application.port.out.auth.JwtValidatePort;

import lombok.RequiredArgsConstructor;

/** JWT 토큰 검증 Adapter (user 모듈 -> auth 모듈) */
@Component("userJwtValidateAdapter")
@RequiredArgsConstructor
public class JwtValidateAdapter implements JwtValidatePort {
  private final JwtValidateUseCase jwtValidateUseCase;

  @Override
  public long getAccessTokenExpirationTime() {
    return jwtValidateUseCase.getAccessTokenExpirationTime();
  }

  @Override
  public long getRefreshTokenExpirationTime() {
    return jwtValidateUseCase.getRefreshTokenExpirationTime();
  }
}
