package com.wiseai.assignment.modules.auth.application.service.query;

import org.springframework.stereotype.Service;

import com.wiseai.assignment.modules.auth.application.port.in.jwt.JwtValidateUseCase;
import com.wiseai.assignment.modules.auth.application.port.out.jwt.JwtValidatorPort;
import com.wiseai.assignment.modules.user.domain.enums.RoleType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtQueryService implements JwtValidateUseCase {
  private final JwtValidatorPort jwtValidatorPort;

  /**
   * 주어진 JWT 토큰의 유효성을 검사합니다.
   *
   * @param token 검사할 JWT 토큰 문자열
   */
  @Override
  public void validateToken(String token) {
    jwtValidatorPort.validateToken(token);
  }

  /**
   * JWT 토큰에서 유저 ID를 추출하여 반환합니다.
   *
   * @param token 유저 정보를 포함한 JWT 토큰 문자열
   * @return 토큰에 포함된 유저 ID
   */
  @Override
  public Long getUserIdFromToken(String token) {
    return jwtValidatorPort.getUserIdFromToken(token);
  }

  /**
   * 주어진 토큰에서 유저의 역할(RoleType)을 추출합니다.
   *
   * @param token 역할 정보를 추출할 JWT 토큰 문자열
   * @return 토큰에 포함된 유저의 역할(RoleType)
   */
  @Override
  public RoleType getRoleFromToken(String token) {
    return jwtValidatorPort.getRoleFromToken(token);
  }

  /**
   * Access Token의 만료 시간을 밀리초 단위로 반환합니다.
   *
   * @return Access Token의 만료 시간(밀리초)
   */
  @Override
  public long getAccessTokenExpirationTime() {
    return jwtValidatorPort.getAccessTokenExpirationTime();
  }

  /**
   * Refresh Token의 만료 시간을 밀리초 단위로 반환합니다.
   *
   * @return Refresh Token의 만료 시간(밀리초)
   */
  @Override
  public long getRefreshTokenExpirationTime() {
    return jwtValidatorPort.getRefreshTokenExpirationTime();
  }
}
