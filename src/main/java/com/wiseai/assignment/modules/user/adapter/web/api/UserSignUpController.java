package com.wiseai.assignment.modules.user.adapter.web.api;

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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserSignUpController implements UserSignUpApi {

  private final UserSignUpWebMapper userSignUpWebMapper;
  private final SelfSignUpUseCase selfSignUpUseCase;
  private final CookieUtil cookieUtil;

  @Override
  public ResponseEntity<SuccessResponse<Void>> signUpUserSelf(
      SelfSignUpWebRequest webRequest, HttpServletRequest request, HttpServletResponse response) {
    SelfSignUpRequest requestDto = userSignUpWebMapper.toApplicationDto(webRequest);
    ReIssueTokenResponse tokenResponse = selfSignUpUseCase.signUpSelf(requestDto);
    setTokenCookies(request, response, tokenResponse);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(SuccessResponse.of(UserSuccessStatus.CREATED_USER));
  }

  private void setTokenCookies(
      HttpServletRequest request,
      HttpServletResponse response,
      ReIssueTokenResponse tokenResponse) {
    // 액세스 토큰 쿠키 저장
    long accessTokenExpirationSeconds = tokenResponse.accessTokenExpiration() / 1000;
    int accessTokenMaxAge =
        accessTokenExpirationSeconds > Integer.MAX_VALUE
            ? Integer.MAX_VALUE
            : (int) accessTokenExpirationSeconds;
    cookieUtil.setCookie(
        request, response, "accessToken", tokenResponse.accessToken(), accessTokenMaxAge);

    // 리프레시 토큰 쿠키 저장
    long refreshTokenExpirationSeconds = tokenResponse.refreshTokenExpiration() / 1000;
    int refreshTokenMaxAge =
        refreshTokenExpirationSeconds > Integer.MAX_VALUE
            ? Integer.MAX_VALUE
            : (int) refreshTokenExpirationSeconds;
    cookieUtil.setCookie(
        request, response, "refreshToken", tokenResponse.refreshToken(), refreshTokenMaxAge);
  }
}
