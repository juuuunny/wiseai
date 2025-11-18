package com.wiseai.assignment.modules.auth.application.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wiseai.assignment.modules.auth.application.dto.request.SelfLoginRequest;
import com.wiseai.assignment.modules.auth.application.dto.response.ReIssueTokenResponse;
import com.wiseai.assignment.modules.auth.application.port.in.auth.ReIssueTokenUseCase;
import com.wiseai.assignment.modules.auth.application.port.in.auth.SelfLoginUseCase;
import com.wiseai.assignment.modules.auth.application.port.out.jwt.JwtGeneratorPort;
import com.wiseai.assignment.modules.auth.application.port.out.jwt.JwtValidatorPort;
import com.wiseai.assignment.modules.auth.application.port.out.token.ManageRefreshTokenPort;
import com.wiseai.assignment.modules.auth.application.port.out.user.UserQueryPort;
import com.wiseai.assignment.modules.auth.domain.exception.AuthException;
import com.wiseai.assignment.modules.auth.domain.status.AuthErrorStatus;
import com.wiseai.assignment.modules.common.support.lock.DistributedLock;
import com.wiseai.assignment.modules.user.domain.enums.RoleType;
import com.wiseai.assignment.modules.user.domain.model.vo.UserInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthCommandService implements SelfLoginUseCase, ReIssueTokenUseCase {
  private final JwtGeneratorPort jwtGeneratorPort;
  private final JwtValidatorPort jwtValidatorPort;
  private final ManageRefreshTokenPort manageRefreshTokenPort;
  private final UserQueryPort userQueryPort;

  /**
   * 사용자의 이메일과 비밀번호를 검증하여 로그인한 후, 새로운 액세스 토큰과 리프레시 토큰을 발급한다.
   *
   * @param requestDto 로그인 요청 정보(이메일, 비밀번호 등)
   * @return 발급된 액세스 토큰과 리프레시 토큰, 각 만료 시간이 포함된 응답 객체
   */
  @Override
  @Transactional
  public ReIssueTokenResponse login(SelfLoginRequest requestDto) {
    UserInfo userInfo = authenticateUser(requestDto);
    return generateTokenResponse(userInfo);
  }

  private UserInfo authenticateUser(SelfLoginRequest requestDto) {
    return userQueryPort.checkLoginPossibleAndGetUserInfo(
        requestDto.email(), requestDto.password());
  }

  private ReIssueTokenResponse generateTokenResponse(UserInfo userInfo) {
    String accessToken = jwtGeneratorPort.generateAccessToken(userInfo.id(), userInfo.role());
    String refreshToken = jwtGeneratorPort.generateRefreshToken(userInfo.id(), userInfo.role());
    manageRefreshTokenPort.saveRefreshToken(userInfo.id().toString(), refreshToken);

    return new ReIssueTokenResponse(
        accessToken,
        refreshToken,
        jwtValidatorPort.getAccessTokenExpirationTime(),
        jwtValidatorPort.getRefreshTokenExpirationTime());
  }

  /**
   * 리프레시 토큰을 검증하고 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.
   *
   * <p>분산 락을 적용하여 동일한 리프레시 토큰으로의 동시 재발급을 방지하며, 저장된 리프레시 토큰과 입력된 토큰이 일치할 때만 새로운 토큰을 생성합니다. 토큰이
   * 만료되었거나 일치하지 않을 경우 인증 예외가 발생합니다.
   *
   * @param refreshToken 클라이언트가 제공한 리프레시 토큰
   * @return 새로 발급된 액세스 토큰과 리프레시 토큰, 각 만료 시간이 포함된 응답 객체
   */
  @Override
  @DistributedLock(
      key = "'lock:refresh-reissue:' + #refreshToken",
      waitTime = 200L,
      leaseTime = 1000L,
      retry = 1)
  @Transactional
  public ReIssueTokenResponse reIssueToken(String refreshToken) {
    // 리프레시 토큰 검증 (이미 JWT 파싱에서 검증됨)
    jwtValidatorPort.validateToken(refreshToken);

    // 쿠키의 리프레시 토큰으로 유저 아이디를 반환한다.
    Long userId = jwtValidatorPort.getUserIdFromToken(refreshToken);

    // 레디스의 리프레시 토큰과 입력받은 리프레시 토큰을 비교한다.
    String savedRefreshToken = manageRefreshTokenPort.getRefreshToken(userId.toString());
    if (savedRefreshToken == null) {
      log.warn("토큰 재발급 실패: 저장된 리프레시 토큰 없음 - userId={}", userId);
      throw new AuthException(AuthErrorStatus.EXPIRED_REFRESH_TOKEN);
    }
    if (!savedRefreshToken.equals(refreshToken)) {
      log.warn("토큰 재발급 실패: 리프레시 토큰 불일치 - userId={}", userId);
      throw new AuthException(AuthErrorStatus.REFRESH_TOKEN_USER_MISMATCH_IN_REDIS);
    }

    // 어세스 토큰과 리프레시 토큰을 발급 후 반환한다.
    RoleType userRole = jwtValidatorPort.getRoleFromToken(refreshToken);
    String newAccessToken = jwtGeneratorPort.generateAccessToken(userId, userRole);
    String newRefreshToken = jwtGeneratorPort.generateRefreshToken(userId, userRole);

    // 레디스에 리프레시 토큰 저장
    manageRefreshTokenPort.saveRefreshToken(userId.toString(), newRefreshToken);
    return new ReIssueTokenResponse(
        newAccessToken,
        newRefreshToken,
        jwtValidatorPort.getAccessTokenExpirationTime(),
        jwtValidatorPort.getRefreshTokenExpirationTime());
  }
}
