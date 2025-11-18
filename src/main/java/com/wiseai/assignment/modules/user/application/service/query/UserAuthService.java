package com.wiseai.assignment.modules.user.application.service.query;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wiseai.assignment.modules.user.application.port.in.query.IsLoginPossibleUseCase;
import com.wiseai.assignment.modules.user.application.port.out.query.UserQueryPort;
import com.wiseai.assignment.modules.user.domain.exception.UserException;
import com.wiseai.assignment.modules.user.domain.model.User;
import com.wiseai.assignment.modules.user.domain.model.vo.UserInfo;
import com.wiseai.assignment.modules.user.domain.status.UserErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService implements IsLoginPossibleUseCase {

  private final UserQueryPort userQueryPort;
  private final PasswordEncoder passwordEncoder;

  @Override
  public UserInfo checkLoginPossibleAndGetUserInfo(String email, String rawPassword) {
    User user =
        userQueryPort
            .findByEmail(email)
            .orElseThrow(
                () -> {
                  log.warn("로그인 실패: 존재하지 않는 이메일 - email={}", email);
                  return new UserException(UserErrorStatus.INVALID_CREDENTIAL);
                });

    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
      log.warn("로그인 실패: 비밀번호 불일치 - userId={}, email={}", user.getId(), email);
      throw new UserException(UserErrorStatus.INVALID_CREDENTIAL);
    }

    return new UserInfo(user.getId(), user.getRole(), user.getEmail(), user.getName());
  }
}
