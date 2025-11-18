package com.wiseai.assignment.modules.auth.adapter.jwt;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.auth.application.port.out.jwt.JwtValidatorPort;
import com.wiseai.assignment.modules.user.domain.enums.RoleType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtValidatorAdapter implements JwtValidatorPort {
  private final JwtUtilInternal jwtUtilInternal;
  private final JwtProperties jwtProperties;

  /**
   * 토큰의 유효성 검사
   *
   * @param token 유효성 확인을 하고자 하는 토큰
   */
  @Override
  public void validateToken(String token) {
    jwtUtilInternal.parseToken(token);
  }

  /**
   * JWT 토큰에서 사용자 ID를 추출(어세스 토큰, 리프레시 토큰)
   *
   * @param token 토큰 문자열
   * @return 유저 아이디
   */
  @Override
  public Long getUserIdFromToken(String token) {
    return jwtUtilInternal.parseToken(token).get("userId", Long.class);
  }

  /**
   * JWT 토큰에서 사용자 역할(RoleType)을 추출
   *
   * @param token JWT 토큰 문자열
   * @return 토큰에 포함된 사용자 역할(RoleType)
   */
  @Override
  public RoleType getRoleFromToken(String token) {
    String role = jwtUtilInternal.parseToken(token).get("role", String.class);
    return RoleType.of(role);
  }

  /**
   * 액세스 토큰의 만료 시간을 반환합니다.
   *
   * @return 액세스 토큰의 만료 시간(밀리초 단위)
   */
  @Override
  public long getAccessTokenExpirationTime() {
    return jwtProperties.getAccessTokenExpirationTime();
  }

  /**
   * 리프레시 토큰의 만료 시간을 반환합니다.
   *
   * @return 리프레시 토큰의 만료 시간(밀리초 단위)
   */
  @Override
  public long getRefreshTokenExpirationTime() {
    return jwtProperties.getRefreshTokenExpirationTime();
  }
}
