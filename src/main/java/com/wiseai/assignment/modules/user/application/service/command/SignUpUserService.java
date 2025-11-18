package com.wiseai.assignment.modules.user.application.service.command;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wiseai.assignment.modules.auth.application.dto.response.ReIssueTokenResponse;
import com.wiseai.assignment.modules.user.application.dto.request.SelfSignUpRequest;
import com.wiseai.assignment.modules.user.application.port.in.command.SelfSignUpUseCase;
import com.wiseai.assignment.modules.user.application.port.out.auth.JwtGeneratePort;
import com.wiseai.assignment.modules.user.application.port.out.auth.JwtValidatePort;
import com.wiseai.assignment.modules.user.application.port.out.auth.ManageRefreshTokenPort;
import com.wiseai.assignment.modules.user.application.port.out.command.UserCommandPort;
import com.wiseai.assignment.modules.user.domain.model.User;
import com.wiseai.assignment.modules.user.domain.service.UserDomainService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignUpUserService implements SelfSignUpUseCase {

  private final UserCommandPort userCommandPort;
  private final PasswordEncoder passwordEncoder;
  private final JwtGeneratePort jwtGeneratePort;
  private final JwtValidatePort jwtValidatePort;
  private final ManageRefreshTokenPort manageRefreshTokenPort;

  @Override
  @Transactional
  public ReIssueTokenResponse signUpSelf(SelfSignUpRequest request) {
    request.validatePasswordMatch();

    // 도메인 서비스를 통한 중복 검증
    UserDomainService.validateEmailUniqueness(userCommandPort.existsByEmail(request.email()));

    User user =
        User.create(request.email(), passwordEncoder.encode(request.password()), request.name());

    User savedUser = userCommandPort.save(user);

    // 회원가입 시 즉시 액세스 토큰과 리프레시 토큰 발급
    String accessToken =
        jwtGeneratePort.generateAccessToken(savedUser.getId(), savedUser.getRole());
    String refreshToken =
        jwtGeneratePort.generateRefreshToken(savedUser.getId(), savedUser.getRole());
    manageRefreshTokenPort.saveRefreshToken(savedUser.getId().toString(), refreshToken);

    log.info("회원가입 성공: userId={}, email={}", savedUser.getId(), savedUser.getEmail());
    return new ReIssueTokenResponse(
        accessToken,
        refreshToken,
        jwtValidatePort.getAccessTokenExpirationTime(),
        jwtValidatePort.getRefreshTokenExpirationTime());
  }
}
