package com.wiseai.assignment.modules.user.application.port.in.query;

import com.wiseai.assignment.modules.user.domain.model.vo.UserInfo;

public interface IsLoginPossibleUseCase {
  UserInfo checkLoginPossibleAndGetUserInfo(String email, String rawPassword);
}
