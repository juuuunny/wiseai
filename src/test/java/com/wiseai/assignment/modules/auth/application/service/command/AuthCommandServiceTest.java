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

  // 공통 테스트 데이터
  private static final Long TEST_USER_ID = 1L;
  private static final String TEST_EMAIL = "user@example.com";
  private static final String TEST_PASSWORD = "Password1!";
  private static final String TEST_NAME = "tester";
  private static final String ACCESS_TOKEN = "access";
  private static final String REFRESH_TOKEN = "refresh-token";
  private static final String NEW_ACCESS_TOKEN = "new-access";
  private static final String NEW_REFRESH_TOKEN = "new-refresh";
  private static final String OTHER_TOKEN = "other-token";
  private static final String USER_ID_STRING = "1";
  private static final long ACCESS_TOKEN_EXPIRATION = 900_000L;
  private static final long REFRESH_TOKEN_EXPIRATION = 604_800_000L;

  @Test
  @DisplayName("자체 로그인 성공 시 토큰 응답을 반환한다")
  void login_success() {
    // given
    SelfLoginRequest request = new SelfLoginRequest(TEST_EMAIL, TEST_PASSWORD);
    UserInfo userInfo = new UserInfo(TEST_USER_ID, RoleType.ROLE_USER, TEST_EMAIL, TEST_NAME);

    given(userQueryPort.checkLoginPossibleAndGetUserInfo(request.email(), request.password()))
        .willReturn(userInfo);
    given(jwtGeneratorPort.generateAccessToken(TEST_USER_ID, RoleType.ROLE_USER))
        .willReturn(ACCESS_TOKEN);
    given(jwtGeneratorPort.generateRefreshToken(TEST_USER_ID, RoleType.ROLE_USER))
        .willReturn(REFRESH_TOKEN);
    given(jwtValidatorPort.getAccessTokenExpirationTime()).willReturn(ACCESS_TOKEN_EXPIRATION);
    given(jwtValidatorPort.getRefreshTokenExpirationTime()).willReturn(REFRESH_TOKEN_EXPIRATION);

    // when
    ReIssueTokenResponse response = authCommandService.login(request);

    // then
    assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
    assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
    verify(manageRefreshTokenPort).saveRefreshToken(USER_ID_STRING, REFRESH_TOKEN);
  }

  @Test
  @DisplayName("리프레시 토큰을 검증하고 토큰을 재발급한다")
  void reIssueToken_success() {
    // given
    doNothing().when(jwtValidatorPort).validateToken(REFRESH_TOKEN);
    given(jwtValidatorPort.getUserIdFromToken(REFRESH_TOKEN)).willReturn(TEST_USER_ID);
    given(manageRefreshTokenPort.getRefreshToken(USER_ID_STRING)).willReturn(REFRESH_TOKEN);
    given(jwtValidatorPort.getRoleFromToken(REFRESH_TOKEN)).willReturn(RoleType.ROLE_USER);
    given(jwtGeneratorPort.generateAccessToken(TEST_USER_ID, RoleType.ROLE_USER))
        .willReturn(NEW_ACCESS_TOKEN);
    given(jwtGeneratorPort.generateRefreshToken(TEST_USER_ID, RoleType.ROLE_USER))
        .willReturn(NEW_REFRESH_TOKEN);
    given(jwtValidatorPort.getAccessTokenExpirationTime()).willReturn(ACCESS_TOKEN_EXPIRATION);
    given(jwtValidatorPort.getRefreshTokenExpirationTime()).willReturn(REFRESH_TOKEN_EXPIRATION);

    // when
    ReIssueTokenResponse response = authCommandService.reIssueToken(REFRESH_TOKEN);

    // then
    assertThat(response.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
    assertThat(response.refreshToken()).isEqualTo(NEW_REFRESH_TOKEN);
    verify(manageRefreshTokenPort).saveRefreshToken(USER_ID_STRING, NEW_REFRESH_TOKEN);
  }

  @Test
  @DisplayName("Redis에 저장된 리프레시 토큰이 없으면 예외가 발생한다")
  void reIssueToken_fail_missingRedisToken() {
    doNothing().when(jwtValidatorPort).validateToken(REFRESH_TOKEN);
    given(jwtValidatorPort.getUserIdFromToken(REFRESH_TOKEN)).willReturn(TEST_USER_ID);
    given(manageRefreshTokenPort.getRefreshToken(USER_ID_STRING)).willReturn(null);

    assertThatThrownBy(() -> authCommandService.reIssueToken(REFRESH_TOKEN))
        .isInstanceOf(AuthException.class)
        .hasMessage(AuthErrorStatus.EXPIRED_REFRESH_TOKEN.getMessage());
  }

  @Test
  @DisplayName("Redis에 저장된 토큰과 다르면 예외가 발생한다")
  void reIssueToken_fail_mismatchedToken() {
    doNothing().when(jwtValidatorPort).validateToken(REFRESH_TOKEN);
    given(jwtValidatorPort.getUserIdFromToken(REFRESH_TOKEN)).willReturn(TEST_USER_ID);
    given(manageRefreshTokenPort.getRefreshToken(USER_ID_STRING)).willReturn(OTHER_TOKEN);

    assertThatThrownBy(() -> authCommandService.reIssueToken(REFRESH_TOKEN))
        .isInstanceOf(AuthException.class)
        .hasMessage(AuthErrorStatus.REFRESH_TOKEN_USER_MISMATCH_IN_REDIS.getMessage());
  }

  @Test
  @DisplayName("토큰 검증 단계에서 예외가 발생하면 바로 전파된다")
  void reIssueToken_fail_invalidToken() {
    doNothing().when(jwtValidatorPort).validateToken(REFRESH_TOKEN);
    given(jwtValidatorPort.getUserIdFromToken(REFRESH_TOKEN)).willReturn(TEST_USER_ID);
    given(manageRefreshTokenPort.getRefreshToken(USER_ID_STRING)).willReturn(REFRESH_TOKEN);
    given(jwtValidatorPort.getRoleFromToken(REFRESH_TOKEN)).willReturn(RoleType.ROLE_USER);
    given(jwtGeneratorPort.generateAccessToken(TEST_USER_ID, RoleType.ROLE_USER))
        .willThrow(new AuthException(AuthErrorStatus.FAILED_GENERATE_ACCESS_TOKEN));

    assertThatThrownBy(() -> authCommandService.reIssueToken(REFRESH_TOKEN))
        .isInstanceOf(AuthException.class)
        .hasMessage(AuthErrorStatus.FAILED_GENERATE_ACCESS_TOKEN.getMessage());
  }

  @Test
  @DisplayName("로그인 시 사용자 정보 조회에 실패하면 예외가 전파된다")
  void login_fail_invalidCredential() {
    SelfLoginRequest request = new SelfLoginRequest(TEST_EMAIL, TEST_PASSWORD);
    given(userQueryPort.checkLoginPossibleAndGetUserInfo(request.email(), request.password()))
        .willThrow(new AuthException(AuthErrorStatus.BAD_REQUEST_LOGIN));

    assertThatThrownBy(() -> authCommandService.login(request))
        .isInstanceOf(AuthException.class)
        .hasMessage(AuthErrorStatus.BAD_REQUEST_LOGIN.getMessage());
  }
}
