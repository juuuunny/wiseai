package com.wiseai.assignment.modules.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wiseai.assignment.modules.user.domain.enums.RoleType;
import com.wiseai.assignment.modules.user.domain.exception.UserException;
import com.wiseai.assignment.modules.user.domain.model.User;
import com.wiseai.assignment.modules.user.domain.status.UserErrorStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  @DisplayName("유효한 값으로 User 생성 시 ROLE_USER 권한으로 생성된다")
  void createUser_success() {
    // when
    User user = User.create("tester@example.com", "encodedPW", "테스터");

    // then
    assertThat(user.getEmail()).isEqualTo("tester@example.com");
    assertThat(user.getPassword()).isEqualTo("encodedPW");
    assertThat(user.getName()).isEqualTo("테스터");
    assertThat(user.getRole()).isEqualTo(RoleType.ROLE_USER);
  }

  @Test
  @DisplayName("이메일이 비어있으면 예외가 발생한다")
  void createUser_fail_invalidEmail() {
    assertThatThrownBy(() -> User.create("  ", "encoded", "테스터"))
        .isInstanceOf(UserException.class)
        .hasMessage(UserErrorStatus.INVALID_CREDENTIAL.getMessage());
  }

  @Test
  @DisplayName("이름이 비어있으면 예외가 발생한다")
  void createUser_fail_invalidName() {
    assertThatThrownBy(() -> User.create("tester@example.com", "encoded", ""))
        .isInstanceOf(UserException.class)
        .hasMessage(UserErrorStatus.INVALID_CREDENTIAL.getMessage());
  }
}

