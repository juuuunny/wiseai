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
   * UsernamePasswordAuthenticationFilter 앞에 추가 - 엔드포인트별 접근 제어: - Swagger, 정적 리소스, 헬스체크/액추에이터 등 공개
   * 허용 - 회원가입·인증 API 공개 허용 - 회의실, 예약, 결제, 웹훅 API 공개 허용 (과제 요구사항) - 그 외 모든 요청은 인증 필요
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
        .authorizeHttpRequests(
            auth ->
                auth
                    // Swagger 및 정적 리소스
                    .requestMatchers(
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
                        "/docs/**",
                        "/error")
                    .permitAll()
                    // 인증 관련 API (회원가입, 로그인, 토큰 재발급)
                    .requestMatchers("/", "/api/v1/users/signup", "/api/v1/auth/**")
                    .permitAll()
                    // 회의실 API (공개 - 과제 요구사항)
                    .requestMatchers("/meeting-rooms", "/meeting-rooms/**")
                    .permitAll()
                    // 예약 API (공개 - 과제 요구사항)
                    .requestMatchers("/reservations", "/reservations/**")
                    .permitAll()
                    // 결제 API (공개 - 과제 요구사항)
                    .requestMatchers("/payments", "/payments/**")
                    .permitAll()
                    // 웹훅 API (공개 - 과제 요구사항)
                    .requestMatchers("/webhooks", "/webhooks/**")
                    .permitAll()
                    // 그 외 모든 요청은 인증 필요
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(
            new JwtFilter(jwtValidatePort, securityPathConfig),
            UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthenticationEntryPoint));
    return http.build();
  }

  /**
   * Security 필터 체인에서 완전히 제외할 경로 설정 (필터 자체 적용 안 됨) - actuator: 헬스체크 및 모니터링만 제외 - 공개 API는
   * permitAll()로 처리하되, JWT 필터는 실행하여 토큰이 있으면 인증 정보 설정
   */
  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return web -> web.ignoring().requestMatchers("/actuator/**");
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
