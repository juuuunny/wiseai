package com.wiseai.assignment.modules.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.wiseai.assignment.modules.user.domain.enums.RoleType;
import com.wiseai.assignment.modules.user.domain.exception.UserException;
import com.wiseai.assignment.modules.user.domain.model.User;
import com.wiseai.assignment.modules.user.domain.status.UserErrorStatus;

class UserTest {

  // 공통 테스트 데이터
  private static final String TEST_EMAIL = "tester@example.com";
  private static final String TEST_PASSWORD = "encodedPW";
  private static final String TEST_PASSWORD_ENCODED = "encoded";
  private static final String TEST_NAME = "테스터";
  private static final String EMPTY_EMAIL = "  ";
  private static final String EMPTY_NAME = "";

  @Test
  @DisplayName("유효한 값으로 User 생성 시 ROLE_USER 권한으로 생성된다")
  void createUser_success() {
    // when
    User user = User.create(TEST_EMAIL, TEST_PASSWORD, TEST_NAME);

    // then
    assertThat(user.getEmail()).isEqualTo(TEST_EMAIL);
    assertThat(user.getPassword()).isEqualTo(TEST_PASSWORD);
    assertThat(user.getName()).isEqualTo(TEST_NAME);
    assertThat(user.getRole()).isEqualTo(RoleType.ROLE_USER);
  }

  @Test
  @DisplayName("이메일이 비어있으면 예외가 발생한다")
  void createUser_fail_invalidEmail() {
    assertThatThrownBy(() -> User.create(EMPTY_EMAIL, TEST_PASSWORD_ENCODED, TEST_NAME))
        .isInstanceOf(UserException.class)
        .hasMessage(UserErrorStatus.INVALID_CREDENTIAL.getMessage());
  }

  @Test
  @DisplayName("이름이 비어있으면 예외가 발생한다")
  void createUser_fail_invalidName() {
    assertThatThrownBy(() -> User.create(TEST_EMAIL, TEST_PASSWORD_ENCODED, EMPTY_NAME))
        .isInstanceOf(UserException.class)
        .hasMessage(UserErrorStatus.INVALID_CREDENTIAL.getMessage());
  }
}
