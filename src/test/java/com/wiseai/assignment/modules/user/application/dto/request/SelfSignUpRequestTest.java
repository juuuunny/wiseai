package com.wiseai.assignment.modules.user.application.dto.request;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wiseai.assignment.modules.user.domain.exception.UserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SelfSignUpRequestTest {

  @Test
  @DisplayName("비밀번호와 확인값이 동일하면 검증을 통과한다")
  void validatePasswordMatch_success() {
    SelfSignUpRequest request =
        new SelfSignUpRequest("user@example.com", "Password1!", "Password1!", "테스터");

    assertThatCode(request::validatePasswordMatch).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("비밀번호와 확인값이 다르면 예외가 발생한다")
  void validatePasswordMatch_fail() {
    SelfSignUpRequest request =
        new SelfSignUpRequest("user@example.com", "Password1!", "Password2@", "테스터");

    assertThatThrownBy(request::validatePasswordMatch).isInstanceOf(UserException.class);
  }
}

