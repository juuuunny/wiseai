package com.wiseai.assignment.modules.user.adapter.auth;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.auth.application.port.in.token.ManageRefreshTokenUseCase;
import com.wiseai.assignment.modules.user.application.port.out.auth.ManageRefreshTokenPort;

import lombok.RequiredArgsConstructor;

/** 리프레시 토큰 관리 Adapter (user 모듈 -> auth 모듈) */
@Component
@RequiredArgsConstructor
public class ManageRefreshTokenAdapter implements ManageRefreshTokenPort {
  private final ManageRefreshTokenUseCase manageRefreshTokenUseCase;

  @Override
  public void saveRefreshToken(String userId, String refreshToken) {
    manageRefreshTokenUseCase.saveRefreshToken(userId, refreshToken);
  }
}
