package com.wiseai.assignment.modules.security.config;

import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.Getter;

/** 보안 필터 예외 경로들을 관리하는 설정 클래스 */
@Component
@Getter
public class SecurityPathConfig {

  private final Set<String> jwtExcludedPaths =
      Set.of(
          "/swagger",
          "/swagger-ui",
          "/v3/api-docs",
          "/swagger-resources",
          "/webjars",
          "/.well-known",
          "/swagger-ui.html",
          "/favicon.ico",
          "/health",
          "/actuator",
          "/static",
          "/docs",
          "/",
          "/error",
          "/api/v1/users/signup",
          "/api/v1/auth");

  public boolean isJwtExcludedPath(String path) {
    return jwtExcludedPaths.stream().anyMatch(path::startsWith);
  }
}
