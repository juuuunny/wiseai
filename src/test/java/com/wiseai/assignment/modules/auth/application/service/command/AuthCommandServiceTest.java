package com.wiseai.assignment.modules.auth.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wiseai.assignment.modules.auth.application.dto.request.SelfLoginRequest;
import com.wiseai.assignment.modules.auth.application.dto.response.ReIssueTokenResponse;
import com.wiseai.assignment.modules.auth.application.port.out.jwt.JwtGeneratorPort;
import com.wiseai.assignment.modules.auth.application.port.out.jwt.JwtValidatorPort;
import com.wiseai.assignment.modules.auth.application.port.out.token.ManageRefreshTokenPort;
import com.wiseai.assignment.modules.auth.application.port.out.user.UserQueryPort;
import com.wiseai.assignment.modules.auth.domain.exception.AuthException;
import com.wiseai.assignment.modules.auth.domain.status.AuthErrorStatus;
import com.wiseai.assignment.modules.user.domain.enums.RoleType;
import com.wiseai.assignment.modules.user.domain.model.vo.UserInfo;

@ExtendWith(MockitoExtension.class)
class AuthCommandServiceTest {

  @Mock private JwtGeneratorPort jwtGeneratorPort;
  @Mock private JwtValidatorPort jwtValidatorPort;
  @Mock private ManageRefreshTokenPort manageRefreshTokenPort;
  @Mock private UserQueryPort userQueryPort;

  @InjectMocks private AuthCommandService authCommandService;

  @Test
  @DisplayName("자체 로그인 성공 시 토큰 응답을 반환한다")
  void login_success() {
    // given
    SelfLoginRequest request = new SelfLoginRequest("user@example.com", "Password1!");
    UserInfo userInfo = new UserInfo(1L, RoleType.ROLE_USER, "user@example.com", "tester");

    given(userQueryPort.checkLoginPossibleAndGetUserInfo(request.email(), request.password()))
        .willReturn(userInfo);
    given(jwtGeneratorPort.generateAccessToken(1L, RoleType.ROLE_USER)).willReturn("access");
    given(jwtGeneratorPort.generateRefreshToken(1L, RoleType.ROLE_USER)).willReturn("refresh");
    given(jwtValidatorPort.getAccessTokenExpirationTime()).willReturn(900_000L);
    given(jwtValidatorPort.getRefreshTokenExpirationTime()).willReturn(604_800_000L);

    // when
    ReIssueTokenResponse response = authCommandService.login(request);

    // then
    assertThat(response.accessToken()).isEqualTo("access");
    assertThat(response.refreshToken()).isEqualTo("refresh");
    verify(manageRefreshTokenPort).saveRefreshToken("1", "refresh");
  }

  @Test
  @DisplayName("리프레시 토큰을 검증하고 토큰을 재발급한다")
  void reIssueToken_success() {
    // given
    String refreshToken = "refresh-token";
    doNothing().when(jwtValidatorPort).validateToken(refreshToken);
    given(jwtValidatorPort.getUserIdFromToken(refreshToken)).willReturn(1L);
    given(manageRefreshTokenPort.getRefreshToken("1")).willReturn(refreshToken);
    given(jwtValidatorPort.getRoleFromToken(refreshToken)).willReturn(RoleType.ROLE_USER);
    given(jwtGeneratorPort.generateAccessToken(1L, RoleType.ROLE_USER)).willReturn("new-access");
    given(jwtGeneratorPort.generateRefreshToken(1L, RoleType.ROLE_USER)).willReturn("new-refresh");
    given(jwtValidatorPort.getAccessTokenExpirationTime()).willReturn(900_000L);
    given(jwtValidatorPort.getRefreshTokenExpirationTime()).willReturn(604_800_000L);

    // when
    ReIssueTokenResponse response = authCommandService.reIssueToken(refreshToken);

    // then
    assertThat(response.accessToken()).isEqualTo("new-access");
    assertThat(response.refreshToken()).isEqualTo("new-refresh");
    verify(manageRefreshTokenPort).saveRefreshToken("1", "new-refresh");
  }

  @Test
  @DisplayName("Redis에 저장된 리프레시 토큰이 없으면 예외가 발생한다")
  void reIssueToken_fail_missingRedisToken() {
    String refreshToken = "refresh-token";
    doNothing().when(jwtValidatorPort).validateToken(refreshToken);
    given(jwtValidatorPort.getUserIdFromToken(refreshToken)).willReturn(1L);
    given(manageRefreshTokenPort.getRefreshToken("1")).willReturn(null);

    assertThatThrownBy(() -> authCommandService.reIssueToken(refreshToken))
        .isInstanceOf(AuthException.class)
        .hasMessage(AuthErrorStatus.EXPIRED_REFRESH_TOKEN.getMessage());
  }

  @Test
  @DisplayName("Redis에 저장된 토큰과 다르면 예외가 발생한다")
  void reIssueToken_fail_mismatchedToken() {
    String refreshToken = "refresh-token";
    doNothing().when(jwtValidatorPort).validateToken(refreshToken);
    given(jwtValidatorPort.getUserIdFromToken(refreshToken)).willReturn(1L);
    given(manageRefreshTokenPort.getRefreshToken("1")).willReturn("other-token");

    assertThatThrownBy(() -> authCommandService.reIssueToken(refreshToken))
        .isInstanceOf(AuthException.class)
        .hasMessage(AuthErrorStatus.REFRESH_TOKEN_USER_MISMATCH_IN_REDIS.getMessage());
  }

  @Test
  @DisplayName("토큰 검증 단계에서 예외가 발생하면 바로 전파된다")
  void reIssueToken_fail_invalidToken() {
    String refreshToken = "refresh-token";
    doNothing().when(jwtValidatorPort).validateToken(refreshToken);
    given(jwtValidatorPort.getUserIdFromToken(refreshToken)).willReturn(1L);
    given(manageRefreshTokenPort.getRefreshToken("1")).willReturn(refreshToken);
    given(jwtValidatorPort.getRoleFromToken(refreshToken)).willReturn(RoleType.ROLE_USER);
    given(jwtGeneratorPort.generateAccessToken(1L, RoleType.ROLE_USER))
        .willThrow(new AuthException(AuthErrorStatus.FAILED_GENERATE_ACCESS_TOKEN));

    assertThatThrownBy(() -> authCommandService.reIssueToken(refreshToken))
        .isInstanceOf(AuthException.class)
        .hasMessage(AuthErrorStatus.FAILED_GENERATE_ACCESS_TOKEN.getMessage());
  }

  @Test
  @DisplayName("로그인 시 사용자 정보 조회에 실패하면 예외가 전파된다")
  void login_fail_invalidCredential() {
    SelfLoginRequest request = new SelfLoginRequest("user@example.com", "Password1!");
    given(userQueryPort.checkLoginPossibleAndGetUserInfo(request.email(), request.password()))
        .willThrow(new AuthException(AuthErrorStatus.BAD_REQUEST_LOGIN));

    assertThatThrownBy(() -> authCommandService.login(request))
        .isInstanceOf(AuthException.class)
        .hasMessage(AuthErrorStatus.BAD_REQUEST_LOGIN.getMessage());
  }
}
