package com.wiseai.assignment.modules.auth.application.port.in.auth;

import com.wiseai.assignment.modules.auth.application.dto.request.SelfLoginRequest;
import com.wiseai.assignment.modules.auth.application.dto.response.ReIssueTokenResponse;

public interface SelfLoginUseCase {
  /**
   * 사용자의 자체 로그인 요청을 처리하고 새로운 액세스 토큰과 리프레시 토큰을 반환합니다.
   *
   * @param requestDto 자체 로그인에 필요한 사용자 인증 정보가 포함된 요청 객체
   * @return 인증에 성공한 사용자를 위한 액세스 토큰과 리프레시 토큰 응답
   */
  ReIssueTokenResponse login(SelfLoginRequest requestDto);
}
