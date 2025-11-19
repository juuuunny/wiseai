package com.wiseai.assignment.modules.user.adapter.web.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.wiseai.assignment.modules.auth.application.dto.response.ReIssueTokenResponse;
import com.wiseai.assignment.modules.common.dto.response.SuccessResponse;
import com.wiseai.assignment.modules.common.util.CookieUtil;
import com.wiseai.assignment.modules.user.adapter.web.mapper.UserSignUpWebMapper;
import com.wiseai.assignment.modules.user.adapter.web.request.SelfSignUpWebRequest;
import com.wiseai.assignment.modules.user.application.dto.request.SelfSignUpRequest;
import com.wiseai.assignment.modules.user.application.port.in.command.SelfSignUpUseCase;
import com.wiseai.assignment.modules.user.domain.status.UserSuccessStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserSignUpController implements UserSignUpApi {

  private final UserSignUpWebMapper userSignUpWebMapper;
  private final SelfSignUpUseCase selfSignUpUseCase;
  private final CookieUtil cookieUtil;

  @Override
  public ResponseEntity<SuccessResponse<Void>> signUpUserSelf(
      SelfSignUpWebRequest webRequest, HttpServletRequest request, HttpServletResponse response) {
    log.debug("회원가입 요청: email={}", webRequest.email());
    SelfSignUpRequest requestDto = userSignUpWebMapper.toApplicationDto(webRequest);
    ReIssueTokenResponse tokenResponse = selfSignUpUseCase.signUpSelf(requestDto);
    setTokenCookies(request, response, tokenResponse);
    log.info("회원가입 성공: email={}", webRequest.email());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(SuccessResponse.of(UserSuccessStatus.CREATED_USER));
  }

  private void setTokenCookies(
      HttpServletRequest request,
      HttpServletResponse response,
      ReIssueTokenResponse tokenResponse) {
    cookieUtil.setTokenCookiesWithoutExpiration(
        request,
        response,
        tokenResponse.accessToken(),
        tokenResponse.refreshToken(),
        tokenResponse.accessTokenExpiration(),
        tokenResponse.refreshTokenExpiration());
  }
}
