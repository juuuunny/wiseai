package com.wiseai.assignment.modules.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.wiseai.assignment.modules.security.application.port.out.auth.JwtValidatePort;
import com.wiseai.assignment.modules.security.filter.JwtFilter;
import com.wiseai.assignment.modules.security.handler.CustomAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final CorsConfigurationSource corsConfigurationSource;

  private final JwtValidatePort jwtValidatePort;
  private final SecurityPathConfig securityPathConfig;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  /**
   * HTTP 요청에 대한 Spring Security 필터 체인을 구성하여 인증·인가 정책을 적용합니다.
   *
   * <p>구성 내용: - 지정된 CorsConfigurationSource로 CORS 활성화 - CSRF, 폼 로그인, HTTP Basic 비활성화 - 세션을
   * STATELESS로 설정 - 커스텀 AuthenticationEntryPoint로 인증 예외 처리 - JwtValidateUseCase 기반의 JwtFilter를
   * UsernamePasswordAuthenticationFilter 앞에 추가 - 엔드포인트별 접근 제어: - Swagger, 정적 리소스, 헬스체크/액추에이터, 웹훅 등
   * 공개 허용 - 회원가입·비밀번호 재설정·인증·이메일·레퍼런스 등 공개 허용 - GET /api/v1/projects/me, GET /api/v1/projects/like
   * 및 GET /api/v1/datasets/me는 인증 필요 - 그 외 GET /api/v1/projects/**, GET /api/v1/datasets/**,
   * /api/v1/files/**, /api/v1/users/** 등은 공개 허용 - /api/v1/user/** 는 USER 또는 ADMIN 역할 필요 -
   * /api/v1/admin/** 는 ADMIN 역할 필요 - 위에 해당하지 않는 모든 요청은 인증 필요
   *
   * <p>CSRF 보호 비활성화 이유: - 이 애플리케이션은 JWT 기반의 Stateless REST API입니다 - 세션 쿠키를 사용하지 않으므로 CSRF 공격에 취약하지
   * 않습니다 - 모든 인증은 JWT 토큰을 통해 처리되며, 토큰은 Authorization 헤더에 포함됩니다 - CORS 정책이 적절히 설정되어 있어 Cross-Origin
   * 요청이 제어됩니다
   *
   * @return 구성된 SecurityFilterChain 인스턴스
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        // CSRF 보호 비활성화: JWT 기반 Stateless API이므로 세션 쿠키를 사용하지 않아 CSRF 공격에 취약하지 않음
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthenticationEntryPoint))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/swagger",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/swagger-config/**",
                        "/webjars/**",
                        "/.well-known/**",
                        "/favicon.ico",
                        "/health",
                        "/actuator/**",
                        "/static/**",
                        "/docs/**")
                    .permitAll()
                    .requestMatchers("/", "/api/v1/users/signup", "/api/v1/auth/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(
            new JwtFilter(jwtValidatePort, securityPathConfig),
            UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  /** actuator 요청은 security filter 자체에서 완전히 제외시킴 (필터 자체 적용 안 됨) */
  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return web -> web.ignoring().requestMatchers("/actuator/**");
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
