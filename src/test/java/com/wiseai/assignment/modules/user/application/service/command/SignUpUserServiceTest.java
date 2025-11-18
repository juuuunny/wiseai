package com.wiseai.assignment.modules.user.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.wiseai.assignment.modules.auth.application.dto.response.ReIssueTokenResponse;
import com.wiseai.assignment.modules.user.application.dto.request.SelfSignUpRequest;
import com.wiseai.assignment.modules.user.application.port.out.auth.JwtGeneratePort;
import com.wiseai.assignment.modules.user.application.port.out.auth.JwtValidatePort;
import com.wiseai.assignment.modules.user.application.port.out.auth.ManageRefreshTokenPort;
import com.wiseai.assignment.modules.user.application.port.out.command.UserCommandPort;
import com.wiseai.assignment.modules.user.domain.enums.RoleType;
import com.wiseai.assignment.modules.user.domain.exception.UserException;
import com.wiseai.assignment.modules.user.domain.model.User;
import com.wiseai.assignment.modules.user.domain.status.UserErrorStatus;

@ExtendWith(MockitoExtension.class)
class SignUpUserServiceTest {

  @Mock private UserCommandPort userCommandPort;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtGeneratePort jwtGeneratePort;
  @Mock private JwtValidatePort jwtValidatePort;
  @Mock private ManageRefreshTokenPort manageRefreshTokenPort;

  @InjectMocks private SignUpUserService signUpUserService;

  @Test
  @DisplayName("회원가입 성공 시 토큰이 발급되고 저장된다")
  void signUpSelf_success() {
    // given
    SelfSignUpRequest request =
        new SelfSignUpRequest("user@example.com", "Password1!", "Password1!", "tester");

    given(userCommandPort.existsByEmail(request.email())).willReturn(false);
    given(passwordEncoder.encode(request.password())).willReturn("encoded");

    User savedUser =
        User.builder()
            .id(1L)
            .email(request.email())
            .password("encoded")
            .name(request.name())
            .role(RoleType.ROLE_USER)
            .build();
    given(userCommandPort.save(any(User.class))).willReturn(savedUser);

    given(jwtGeneratePort.generateAccessToken(savedUser.getId(), savedUser.getRole()))
        .willReturn("access-token");
    given(jwtGeneratePort.generateRefreshToken(savedUser.getId(), savedUser.getRole()))
        .willReturn("refresh-token");
    given(jwtValidatePort.getAccessTokenExpirationTime()).willReturn(900_000L);
    given(jwtValidatePort.getRefreshTokenExpirationTime()).willReturn(604_800_000L);

    ArgumentCaptor<String> refreshTokenCaptor = ArgumentCaptor.forClass(String.class);
    doAnswer(invocation -> null).when(manageRefreshTokenPort).saveRefreshToken(any(), any());

    // when
    ReIssueTokenResponse response = signUpUserService.signUpSelf(request);

    // then
    assertThat(response.accessToken()).isEqualTo("access-token");
    assertThat(response.refreshToken()).isEqualTo("refresh-token");
    assertThat(response.accessTokenExpiration()).isEqualTo(900_000L);
    assertThat(response.refreshTokenExpiration()).isEqualTo(604_800_000L);

    verify(manageRefreshTokenPort).saveRefreshToken(any(), refreshTokenCaptor.capture());
    assertThat(refreshTokenCaptor.getValue()).isEqualTo("refresh-token");
  }

  @Test
  @DisplayName("이미 존재하는 이메일이면 예외가 발생한다")
  void signUpSelf_fail_duplicateEmail() {
    // given
    SelfSignUpRequest request =
        new SelfSignUpRequest("user@example.com", "Password1!", "Password1!", "tester");

    given(userCommandPort.existsByEmail(request.email())).willReturn(true);

    // expect
    assertThatThrownBy(() -> signUpUserService.signUpSelf(request))
        .isInstanceOf(UserException.class)
        .hasMessage(UserErrorStatus.DUPLICATED_EMAIL.getMessage());
  }

  @Test
  @DisplayName("비밀번호와 확인값이 다르면 예외가 발생한다")
  void signUpSelf_fail_passwordMismatch() {
    SelfSignUpRequest request =
        new SelfSignUpRequest("user@example.com", "Password1!", "Password2@", "tester");

    assertThatThrownBy(() -> signUpUserService.signUpSelf(request))
        .isInstanceOf(UserException.class)
        .hasMessage(UserErrorStatus.PASSWORD_CONFIRM_NOT_MATCH.getMessage());
  }
}
