package com.wiseai.assignment.modules.user.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.wiseai.assignment.modules.user.application.port.out.query.UserQueryPort;
import com.wiseai.assignment.modules.user.domain.enums.RoleType;
import com.wiseai.assignment.modules.user.domain.exception.UserException;
import com.wiseai.assignment.modules.user.domain.model.User;
import com.wiseai.assignment.modules.user.domain.model.vo.UserInfo;
import com.wiseai.assignment.modules.user.domain.status.UserErrorStatus;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

  @Mock private UserQueryPort userQueryPort;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserAuthService userAuthService;

  @Test
  @DisplayName("이메일과 비밀번호가 일치하면 사용자 정보를 반환한다")
  void checkLoginPossible_success() {
    // given
    User storedUser =
        User.builder()
            .id(1L)
            .email("user@example.com")
            .password("encoded")
            .name("tester")
            .role(RoleType.ROLE_USER)
            .build();

    given(userQueryPort.findByEmail(storedUser.getEmail())).willReturn(Optional.of(storedUser));
    given(passwordEncoder.matches("Password1!", storedUser.getPassword())).willReturn(true);

    // when
    UserInfo userInfo =
        userAuthService.checkLoginPossibleAndGetUserInfo("user@example.com", "Password1!");

    // then
    assertThat(userInfo.id()).isEqualTo(1L);
    assertThat(userInfo.role()).isEqualTo(RoleType.ROLE_USER);
  }

  @Test
  @DisplayName("존재하지 않는 이메일이면 예외가 발생한다")
  void checkLoginPossible_fail_notFound() {
    given(userQueryPort.findByEmail("user@example.com")).willReturn(Optional.empty());

    assertThatThrownBy(
            () -> userAuthService.checkLoginPossibleAndGetUserInfo("user@example.com", "pw"))
        .isInstanceOf(UserException.class)
        .hasMessage(UserErrorStatus.INVALID_CREDENTIAL.getMessage());
  }

  @Test
  @DisplayName("비밀번호가 일치하지 않으면 예외가 발생한다")
  void checkLoginPossible_fail_wrongPassword() {
    User storedUser =
        User.builder()
            .id(1L)
            .email("user@example.com")
            .password("encoded")
            .name("tester")
            .role(RoleType.ROLE_USER)
            .build();

    given(userQueryPort.findByEmail(storedUser.getEmail())).willReturn(Optional.of(storedUser));
    given(passwordEncoder.matches("wrong", storedUser.getPassword())).willReturn(false);

    assertThatThrownBy(
            () -> userAuthService.checkLoginPossibleAndGetUserInfo("user@example.com", "wrong"))
        .isInstanceOf(UserException.class)
        .hasMessage(UserErrorStatus.INVALID_CREDENTIAL.getMessage());
  }
}

