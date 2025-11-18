package com.wiseai.assignment.modules.user.application.port.in.command;

import com.wiseai.assignment.modules.auth.application.dto.response.ReIssueTokenResponse;
import com.wiseai.assignment.modules.user.application.dto.request.SelfSignUpRequest;

public interface SelfSignUpUseCase {
  ReIssueTokenResponse signUpSelf(SelfSignUpRequest request);
}
