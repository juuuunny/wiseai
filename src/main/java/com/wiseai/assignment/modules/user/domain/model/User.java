package com.wiseai.assignment.modules.user.domain.model;

import com.wiseai.assignment.modules.user.domain.enums.RoleType;
import com.wiseai.assignment.modules.user.domain.exception.UserException;
import com.wiseai.assignment.modules.user.domain.status.UserErrorStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {
  private final Long id;
  private final String email;
  private final String password;
  private final String name;
  private final RoleType role;

  /**
   * 사용자 생성 팩토리 메서드
   *
   * @param email 이메일
   * @param encodedPassword 암호화된 비밀번호
   * @param name 이름
   * @return 생성된 User 객체
   */
  public static User create(String email, String encodedPassword, String name) {
    validateEmail(email);
    validateName(name);

    return User.builder()
        .email(email)
        .password(encodedPassword)
        .name(name)
        .role(RoleType.ROLE_USER)
        .build();
  }

  /**
   * ID를 포함한 User 객체 생성
   *
   * @param id 사용자 ID
   * @return ID가 포함된 User 객체
   */
  public User withId(Long id) {
    return User.builder().id(id).email(email).password(password).name(name).role(role).build();
  }

  /** 이메일 형식 검증 */
  private static void validateEmail(String email) {
    if (email == null || email.isBlank()) {
      throw new UserException(UserErrorStatus.INVALID_CREDENTIAL);
    }
    if (!email.contains("@")) {
      throw new UserException(UserErrorStatus.INVALID_CREDENTIAL);
    }
  }

  /** 이름 검증 */
  private static void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new UserException(UserErrorStatus.INVALID_CREDENTIAL);
    }
  }
}
