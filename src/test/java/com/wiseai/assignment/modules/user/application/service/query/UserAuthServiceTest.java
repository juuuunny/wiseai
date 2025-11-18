package com.wiseai.assignment.modules.user.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.wiseai.assignment.modules.user.application.port.out.query.UserQueryPort;
import com.wiseai.assignment.modules.user.domain.enums.RoleType;
import com.wiseai.assignment.modules.user.domain.exception.UserException;
import com.wiseai.assignment.modules.user.domain.model.User;
import com.wiseai.assignment.modules.user.domain.model.vo.UserInfo;
import com.wiseai.assignment.modules.user.domain.status.UserErrorStatus;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

  @Mock private UserQueryPort userQueryPort;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserAuthService userAuthService;

  // 공통 테스트 데이터
  private static final Long TEST_USER_ID = 1L;
  private static final String TEST_EMAIL = "user@example.com";
  private static final String TEST_PASSWORD = "Password1!";
  private static final String TEST_PASSWORD_WRONG = "wrong";
  private static final String ENCODED_PASSWORD = "encoded";
  private static final String TEST_NAME = "tester";

  @Test
  @DisplayName("이메일과 비밀번호가 일치하면 사용자 정보를 반환한다")
  void checkLoginPossible_success() {
    // given
    User storedUser =
        User.builder()
            .id(TEST_USER_ID)
            .email(TEST_EMAIL)
            .password(ENCODED_PASSWORD)
            .name(TEST_NAME)
            .role(RoleType.ROLE_USER)
            .build();

    given(userQueryPort.findByEmail(storedUser.getEmail())).willReturn(Optional.of(storedUser));
    given(passwordEncoder.matches(TEST_PASSWORD, storedUser.getPassword())).willReturn(true);

    // when
    UserInfo userInfo = userAuthService.checkLoginPossibleAndGetUserInfo(TEST_EMAIL, TEST_PASSWORD);

    // then
    assertThat(userInfo.id()).isEqualTo(TEST_USER_ID);
    assertThat(userInfo.role()).isEqualTo(RoleType.ROLE_USER);
  }

  @Test
  @DisplayName("존재하지 않는 이메일이면 예외가 발생한다")
  void checkLoginPossible_fail_notFound() {
    given(userQueryPort.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());

    assertThatThrownBy(() -> userAuthService.checkLoginPossibleAndGetUserInfo(TEST_EMAIL, "pw"))
        .isInstanceOf(UserException.class)
        .hasMessage(UserErrorStatus.INVALID_CREDENTIAL.getMessage());
  }

  @Test
  @DisplayName("비밀번호가 일치하지 않으면 예외가 발생한다")
  void checkLoginPossible_fail_wrongPassword() {
    User storedUser =
        User.builder()
            .id(TEST_USER_ID)
            .email(TEST_EMAIL)
            .password(ENCODED_PASSWORD)
            .name(TEST_NAME)
            .role(RoleType.ROLE_USER)
            .build();

    given(userQueryPort.findByEmail(storedUser.getEmail())).willReturn(Optional.of(storedUser));
    given(passwordEncoder.matches(TEST_PASSWORD_WRONG, storedUser.getPassword())).willReturn(false);

    assertThatThrownBy(
            () -> userAuthService.checkLoginPossibleAndGetUserInfo(TEST_EMAIL, TEST_PASSWORD_WRONG))
        .isInstanceOf(UserException.class)
        .hasMessage(UserErrorStatus.INVALID_CREDENTIAL.getMessage());
  }
}
