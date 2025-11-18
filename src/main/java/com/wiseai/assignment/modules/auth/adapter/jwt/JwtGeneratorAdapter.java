package com.wiseai.assignment.modules.auth.adapter.jwt;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.auth.application.port.out.jwt.JwtGeneratorPort;
import com.wiseai.assignment.modules.auth.domain.enums.TokenType;
import com.wiseai.assignment.modules.user.domain.enums.RoleType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtGeneratorAdapter implements JwtGeneratorPort {
  private final JwtUtilInternal jwtUtilInternal;
  private final JwtProperties jwtProperties;

  /**
   * 인증에 사용될 어세스 토큰 발급
   *
   * @param userId 유저 아이디
   * @param role 유저 역할
   * @return 인증에 사용할 JWT 토큰 문자열
   */
  @Override
  public String generateAccessToken(Long userId, RoleType role) {
    return jwtUtilInternal.generateToken(
        Map.of("userId", userId, "role", role.getValue()),
        jwtProperties.getAccessTokenExpirationTime(),
        TokenType.ACCESS);
  }

  /**
   * 토큰 재발급에 사용할 리프레시 토큰 발급
   *
   * @param userId 유저 아이디
   * @param role 유저 역할
   * @return 토큰 재발급에 사용할 수 있는 리프레시 토큰
   */
  @Override
  public String generateRefreshToken(Long userId, RoleType role) {
    return jwtUtilInternal.generateToken(
        Map.of("userId", userId, "role", role.getValue()),
        jwtProperties.getRefreshTokenExpirationTime(),
        TokenType.REFRESH);
  }
}
