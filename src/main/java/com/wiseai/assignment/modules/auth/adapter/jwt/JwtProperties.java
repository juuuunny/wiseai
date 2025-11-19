package com.wiseai.assignment.modules.auth.adapter.jwt;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

/** application.yml로부터 주입받은 jwt 설정 값 */
@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "spring.jwt")
public class JwtProperties {
  @NotBlank(message = "시크릿 키는 빈 값 또는 null이 허용되지 않습니다.")
  private String secret;

  @Min(1)
  private long accessTokenExpirationTime;

  @Min(1)
  private long refreshTokenExpirationTime;
}
