package com.wiseai.assignment.modules.auth.application.service.token;

import org.springframework.stereotype.Service;

import com.wiseai.assignment.modules.auth.application.port.in.token.ManageRefreshTokenUseCase;
import com.wiseai.assignment.modules.auth.application.port.out.token.ManageRefreshTokenPort;
import com.wiseai.assignment.modules.auth.domain.exception.AuthException;
import com.wiseai.assignment.modules.auth.domain.status.AuthErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ManageRefreshTokenService implements ManageRefreshTokenUseCase {
  private final ManageRefreshTokenPort manageRefreshTokenPort;

  /**
   * 지정한 사용자 ID의 리프레시 토큰을 Redis에 저장합니다.
   *
   * <p>실제 저장은 ManageRefreshTokenPort에 위임됩니다.
   *
   * @param userId 리프레시 토큰을 저장할 대상 사용자 ID
   * @param refreshToken 저장할 리프레시 토큰 값
   */
  @Override
  public void saveRefreshToken(String userId, String refreshToken) {
    manageRefreshTokenPort.saveRefreshToken(userId, refreshToken);
  }

  /**
   * 주어진 사용자 ID로 Redis에서 리프레시 토큰을 조회하여 반환한다.
   *
   * <p>조회된 토큰이 없으면 인증 예외를 발생시킨다.
   *
   * @param userId 조회 대상 사용자 ID
   * @return 조회된 리프레시 토큰 문자열
   * @throws AuthException 토큰이 존재하지 않거나 만료된 경우 (AuthErrorStatus.EXPIRED_REFRESH_TOKEN)
   */
  @Override
  public String getRefreshToken(String userId) {
    String refreshToken = manageRefreshTokenPort.getRefreshToken(userId);
    if (refreshToken == null) {
      throw new AuthException(AuthErrorStatus.EXPIRED_REFRESH_TOKEN);
    }
    return refreshToken;
  }

  /**
   * 지정한 사용자 ID의 리프레시 토큰을 Redis에서 삭제한다.
   *
   * <p>실제 삭제는 ManageRefreshTokenPort에 위임한다.
   *
   * @param userId 삭제할 대상의 사용자 ID
   */
  @Override
  public void deleteRefreshToken(String userId) {
    manageRefreshTokenPort.deleteRefreshToken(userId);
  }
}
