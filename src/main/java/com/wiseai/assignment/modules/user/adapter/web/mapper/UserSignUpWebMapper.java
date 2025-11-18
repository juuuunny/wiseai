package com.wiseai.assignment.modules.user.adapter.web.mapper;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.user.adapter.web.request.SelfSignUpWebRequest;
import com.wiseai.assignment.modules.user.application.dto.request.SelfSignUpRequest;

@Component
public class UserSignUpWebMapper {

  public SelfSignUpRequest toApplicationDto(SelfSignUpWebRequest webRequest) {
    return new SelfSignUpRequest(
        webRequest.email(), webRequest.password(), webRequest.passwordConfirm(), webRequest.name());
  }
}
