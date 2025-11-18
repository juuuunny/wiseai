package com.wiseai.assignment.modules.user.domain.service;

import com.wiseai.assignment.modules.user.domain.exception.UserException;
import com.wiseai.assignment.modules.user.domain.status.UserErrorStatus;

/**
 * 사용자 도메인 서비스
 *
 * <p>여러 엔티티에 걸친 비즈니스 로직이나 도메인 규칙을 처리합니다.
 */
public class UserDomainService {
  private UserDomainService() {
    // Utility class
  }

  /**
   * 이메일 중복 여부를 확인합니다.
   *
   * @param existsByEmail 이메일 존재 여부를 확인하는 함수
   * @throws UserException 이메일이 이미 존재하는 경우
   */
  public static void validateEmailUniqueness(boolean existsByEmail) {
    if (existsByEmail) {
      throw new UserException(UserErrorStatus.DUPLICATED_EMAIL);
    }
  }
}
