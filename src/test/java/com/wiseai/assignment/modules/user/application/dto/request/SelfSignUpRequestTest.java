package com.wiseai.assignment.modules.user.application.dto.request;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.wiseai.assignment.modules.user.domain.exception.UserException;

class SelfSignUpRequestTest {

  // 공통 테스트 데이터
  private static final String TEST_EMAIL = "user@example.com";
  private static final String TEST_PASSWORD = "Password1!";
  private static final String TEST_PASSWORD_CONFIRM = "Password1!";
  private static final String TEST_PASSWORD_MISMATCH = "Password2@";
  private static final String TEST_NAME = "테스터";

  @Test
  @DisplayName("비밀번호와 확인값이 동일하면 검증을 통과한다")
  void validatePasswordMatch_success() {
    SelfSignUpRequest request =
        new SelfSignUpRequest(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD_CONFIRM, TEST_NAME);

    assertThatCode(request::validatePasswordMatch).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("비밀번호와 확인값이 다르면 예외가 발생한다")
  void validatePasswordMatch_fail() {
    SelfSignUpRequest request =
        new SelfSignUpRequest(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD_MISMATCH, TEST_NAME);

    assertThatThrownBy(request::validatePasswordMatch).isInstanceOf(UserException.class);
  }
}
