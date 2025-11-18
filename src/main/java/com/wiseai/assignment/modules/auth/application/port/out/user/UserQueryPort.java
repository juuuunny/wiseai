package com.wiseai.assignment.modules.auth.application.port.out.user;

import com.wiseai.assignment.modules.user.domain.model.vo.UserInfo;

/** 사용자 조회를 위한 Port (auth 모듈에서 user 모듈 호출용) */
public interface UserQueryPort {
  /**
   * 이메일과 비밀번호로 로그인 가능 여부를 확인하고 사용자 정보를 반환합니다.
   *
   * @param email 이메일
   * @param rawPassword 평문 비밀번호
   * @return 사용자 정보
   */
  UserInfo checkLoginPossibleAndGetUserInfo(String email, String rawPassword);
}
