package com.wiseai.assignment.modules.auth.adapter.user;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.auth.application.port.out.user.UserQueryPort;
import com.wiseai.assignment.modules.user.application.port.in.query.IsLoginPossibleUseCase;
import com.wiseai.assignment.modules.user.domain.model.vo.UserInfo;

import lombok.RequiredArgsConstructor;

/** 사용자 조회 Adapter (auth 모듈 -> user 모듈) */
@Component
@RequiredArgsConstructor
public class UserQueryAdapter implements UserQueryPort {
  private final IsLoginPossibleUseCase isLoginPossibleUseCase;

  @Override
  public UserInfo checkLoginPossibleAndGetUserInfo(String email, String rawPassword) {
    return isLoginPossibleUseCase.checkLoginPossibleAndGetUserInfo(email, rawPassword);
  }
}
