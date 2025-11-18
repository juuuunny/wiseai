package com.wiseai.assignment.modules.auth.adapter.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.wiseai.assignment.modules.auth.adapter.web.mapper.AuthWebMapper;
import com.wiseai.assignment.modules.auth.adapter.web.request.SelfLoginWebRequest;
import com.wiseai.assignment.modules.auth.application.dto.request.SelfLoginRequest;
import com.wiseai.assignment.modules.auth.application.dto.response.ReIssueTokenResponse;
import com.wiseai.assignment.modules.auth.application.port.in.auth.ReIssueTokenUseCase;
import com.wiseai.assignment.modules.auth.application.port.in.auth.SelfLoginUseCase;
import com.wiseai.assignment.modules.auth.domain.status.AuthSuccessStatus;
import com.wiseai.assignment.modules.common.dto.response.SuccessResponse;
import com.wiseai.assignment.modules.common.util.CookieUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {
  private final AuthWebMapper authWebMapper;
  private final SelfLoginUseCase selfLoginUseCase;
  private final ReIssueTokenUseCase reIssueTokenUseCase;
  private final CookieUtil cookieUtil;

  /**
   * 자체 로그인 요청을 처리하고, 인증에 성공하면 액세스 토큰과 리프레시 토큰을 HTTP 쿠키에 저장한다.
   *
   * <p>사용자의 이메일과 비밀번호로 자체 로그인을 수행하며, 성공 시 액세스 토큰과 리프레시 토큰을 응답 쿠키로 설정한다.
   *
   * @param webRequest 자체 로그인에 필요한 사용자 정보가 포함된 요청 객체
   * @param response 토큰을 쿠키로 저장할 HTTP 응답 객체
   * @return 자체 로그인 성공 시 성공 상태를 포함한 응답
   */
  @Override
  public ResponseEntity<SuccessResponse<Void>> login(
      SelfLoginWebRequest webRequest, HttpServletRequest request, HttpServletResponse response) {
    log.debug("로그인 요청: email={}", webRequest.email());
    SelfLoginRequest requestDto = authWebMapper.toApplicationDto(webRequest);
    ReIssueTokenResponse responseDto = selfLoginUseCase.login(requestDto);
    setResponseHeaders(request, response, responseDto);
    log.info("로그인 성공: email={}", webRequest.email());
    return ResponseEntity.ok(SuccessResponse.of(AuthSuccessStatus.OK_SELF_LOGIN));
  }

  /**
   * 리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급하고, 해당 토큰 정보를 HTTP 쿠키에 저장합니다.
   *
   * @param refreshToken 클라이언트가 제공한 리프레시 토큰
   * @param response 토큰 정보를 쿠키로 설정할 HTTP 응답 객체
   * @return 토큰 재발급 성공 시 성공 상태의 응답을 반환합니다.
   */
  @Override
  public ResponseEntity<SuccessResponse<Void>> reIssueToken(
      String refreshToken, HttpServletRequest request, HttpServletResponse response) {
    log.debug("토큰 재발급 요청");
    // 토큰 재발급 진행
    ReIssueTokenResponse responseDto = reIssueTokenUseCase.reIssueToken(refreshToken);
    // 어세스 토큰, 어세스 토큰 만료기간, 리프레시 토큰 쿠키 저장
    setResponseHeaders(request, response, responseDto);
    log.info("토큰 재발급 성공");
    return ResponseEntity.status(HttpStatus.OK)
        .body(SuccessResponse.of(AuthSuccessStatus.OK_RE_ISSUE_TOKEN));
  }

  /**
   * 재발급된 토큰 정보를 기반으로 어세스 토큰, 어세스 토큰 만료기간, 리프레시 토큰을 HTTP 응답 쿠키에 저장합니다.
   *
   * @param request HTTP 요청 객체 (프로토콜 감지용)
   * @param response HTTP 응답 객체로, 쿠키가 설정됩니다.
   * @param responseDto 재발급된 토큰 및 만료 정보를 포함하는 DTO입니다.
   */
  private void setResponseHeaders(
      HttpServletRequest request, HttpServletResponse response, ReIssueTokenResponse responseDto) {
    cookieUtil.setTokenCookies(
        request,
        response,
        responseDto.accessToken(),
        responseDto.refreshToken(),
        responseDto.accessTokenExpiration(),
        responseDto.refreshTokenExpiration());
  }
}
