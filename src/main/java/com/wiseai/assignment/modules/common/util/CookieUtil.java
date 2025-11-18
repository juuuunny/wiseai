package com.wiseai.assignment.modules.common.util;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtil {
  @Value("${spring.profiles.active:local}")
  private String activeProfile;

  /**
   * 지정한 이름, 값, 만료 시간을 가진 HTTP-Only 쿠키를 HTTP 응답에 추가합니다. 실제 요청의 프로토콜을 감지하여 Secure 설정을 동적으로 적용합니다.
   *
   * @param request HTTP 요청 객체 (프로토콜 감지용)
   * @param response HTTP 응답 객체
   * @param name 쿠키의 이름
   * @param value 쿠키의 값
   * @param maxAge 쿠키의 만료 시간(초 단위)
   */
  public void setCookie(
      HttpServletRequest request,
      HttpServletResponse response,
      String name,
      String value,
      int maxAge) {
    boolean isSecure = isSecureEnvironment(request);
    String sameSite = getSameSitePolicy(request);
    String domain = getCookieDomain();

    ResponseCookie cookie =
        ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(isSecure)
            .sameSite(sameSite)
            .domain(domain)
            .path("/")
            .maxAge(Duration.ofSeconds(maxAge))
            .build();

    response.addHeader("Set-Cookie", cookie.toString());
  }

  /** 실제 요청의 프로토콜을 감지하여 HTTPS 사용 여부를 확인합니다. 개발 환경에서 HTTP/HTTPS 모두 지원할 때 사용합니다. */
  private boolean isSecureEnvironment(HttpServletRequest request) {
    if ("prod".equals(activeProfile)) {
      return true; // 운영 환경은 항상 HTTPS
    } else if ("dev".equals(activeProfile)) {
      // 개발 환경: 실제 요청 프로토콜 감지
      return isHttpsRequest(request);
    } else {
      // 로컬 환경: 실제 요청 프로토콜 감지
      return isHttpsRequest(request);
    }
  }

  /** HTTP 요청이 HTTPS인지 확인합니다. X-Forwarded-Proto 헤더와 isSecure() 메서드를 모두 확인합니다. */
  private boolean isHttpsRequest(HttpServletRequest request) {
    // 1. X-Forwarded-Proto 헤더 확인 (프록시/로드밸런서 뒤에서 사용)
    String forwardedProto = request.getHeader("X-Forwarded-Proto");
    if (forwardedProto != null) {
      return "https".equalsIgnoreCase(forwardedProto);
    }

    // 2. X-Forwarded-Ssl 헤더 확인 (일부 프록시에서 사용)
    String forwardedSsl = request.getHeader("X-Forwarded-Ssl");
    if (forwardedSsl != null) {
      return "on".equalsIgnoreCase(forwardedSsl);
    }

    // 3. 서블릿의 isSecure() 메서드 확인
    return request.isSecure();
  }

  /** 환경에 따른 SameSite 정책을 반환합니다. HTTPS에서는 None, HTTP에서는 Lax를 사용합니다. */
  private String getSameSitePolicy(HttpServletRequest request) {
    boolean isSecure = isSecureEnvironment(request);

    if ("prod".equals(activeProfile)) {
      return "None"; // 운영 환경: 항상 HTTPS이므로 None
    } else if ("dev".equals(activeProfile)) {
      return isSecure ? "None" : "Lax"; // 개발 환경: HTTPS면 None, HTTP면 Lax
    } else {
      return isSecure ? "None" : "Lax"; // 로컬 환경: HTTPS면 None, HTTP면 Lax
    }
  }

  /** 환경에 따른 쿠키 도메인을 반환합니다. wiseai.com의 서브 도메인 간 쿠키 공유를 위해 상위 도메인을 사용합니다. */
  private String getCookieDomain() {
    if ("prod".equals(activeProfile)) {
      return ".wiseai.com"; // 운영 환경: wiseai.com ↔ api.wiseai.com 쿠키 공유
    } else if ("dev".equals(activeProfile)) {
      return ".wiseai.com"; // 개발 환경: wiseai.com ↔ dev-api.wiseai.com 쿠키 공유
    } else {
      return null; // 로컬 환경: localhost에서는 도메인 설정 없음
    }
  }

  /**
   * HTTP 요청의 쿠키에서 "refreshToken" 값을 찾아 반환합니다. 요청에 "refreshToken" 쿠키가 없으면 빈 Optional을 반환합니다.
   *
   * @param request HTTP 요청 객체
   * @return "refreshToken" 쿠키 값이 존재하면 해당 값을 포함한 Optional, 없으면 빈 Optional
   */
  public Optional<String> getRefreshTokenFromCookies(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }
    return Arrays.stream(request.getCookies())
        .filter(cookie -> "refreshToken".equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }

  /**
   * 지정한 이름의 쿠키를 삭제합니다. 쿠키를 삭제하기 위해 maxAge를 0으로 설정하고 빈 값을 설정합니다.
   *
   * @param request HTTP 요청 객체 (프로토콜 감지용)
   * @param response HTTP 응답 객체
   * @param name 삭제할 쿠키의 이름
   */
  public void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
    boolean isSecure = isSecureEnvironment(request);
    String sameSite = getSameSitePolicy(request);
    String domain = getCookieDomain();

    ResponseCookie cookie =
        ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(isSecure)
            .sameSite(sameSite)
            .domain(domain)
            .path("/")
            .maxAge(Duration.ofSeconds(0))
            .build();

    response.addHeader("Set-Cookie", cookie.toString());
  }

  /**
   * 로그아웃 시 모든 인증 관련 쿠키를 삭제합니다. accessToken, refreshToken, accessTokenExpiration,
   * refreshTokenExpiration 쿠키를 모두 삭제합니다.
   *
   * @param request HTTP 요청 객체 (프로토콜 감지용)
   * @param response HTTP 응답 객체
   */
  public void deleteAllAuthCookies(HttpServletRequest request, HttpServletResponse response) {
    deleteCookie(request, response, "accessToken");
    deleteCookie(request, response, "refreshToken");
    deleteCookie(request, response, "accessTokenExpiration");
    deleteCookie(request, response, "refreshTokenExpiration");
  }

  /**
   * 토큰 응답 DTO를 기반으로 액세스 토큰과 리프레시 토큰을 쿠키에 저장합니다. 액세스 토큰 만료 시간도 함께 저장합니다.
   *
   * @param request HTTP 요청 객체 (프로토콜 감지용)
   * @param response HTTP 응답 객체
   * @param accessToken 액세스 토큰
   * @param refreshToken 리프레시 토큰
   * @param accessTokenExpiration 액세스 토큰 만료 시간 (밀리초)
   * @param refreshTokenExpiration 리프레시 토큰 만료 시간 (밀리초)
   */
  public void setTokenCookies(
      HttpServletRequest request,
      HttpServletResponse response,
      String accessToken,
      String refreshToken,
      long accessTokenExpiration,
      long refreshTokenExpiration) {
    // 액세스 토큰 쿠키 저장
    long accessTokenExpirationSeconds = accessTokenExpiration / 1000;
    int accessTokenMaxAge =
        accessTokenExpirationSeconds > Integer.MAX_VALUE
            ? Integer.MAX_VALUE
            : (int) accessTokenExpirationSeconds;
    setCookie(request, response, "accessToken", accessToken, accessTokenMaxAge);
    setCookie(
        request,
        response,
        "accessTokenExpiration",
        String.valueOf(((long) accessTokenMaxAge) * 1000),
        accessTokenMaxAge);

    // 리프레시 토큰 쿠키 저장
    long refreshTokenExpirationSeconds = refreshTokenExpiration / 1000;
    int refreshTokenMaxAge =
        refreshTokenExpirationSeconds > Integer.MAX_VALUE
            ? Integer.MAX_VALUE
            : (int) refreshTokenExpirationSeconds;
    setCookie(request, response, "refreshToken", refreshToken, refreshTokenMaxAge);
  }

  /**
   * 토큰 응답 DTO를 기반으로 액세스 토큰과 리프레시 토큰을 쿠키에 저장합니다. 액세스 토큰 만료 시간은 저장하지 않습니다.
   *
   * @param request HTTP 요청 객체 (프로토콜 감지용)
   * @param response HTTP 응답 객체
   * @param accessToken 액세스 토큰
   * @param refreshToken 리프레시 토큰
   * @param accessTokenExpiration 액세스 토큰 만료 시간 (밀리초)
   * @param refreshTokenExpiration 리프레시 토큰 만료 시간 (밀리초)
   */
  public void setTokenCookiesWithoutExpiration(
      HttpServletRequest request,
      HttpServletResponse response,
      String accessToken,
      String refreshToken,
      long accessTokenExpiration,
      long refreshTokenExpiration) {
    // 액세스 토큰 쿠키 저장
    long accessTokenExpirationSeconds = accessTokenExpiration / 1000;
    int accessTokenMaxAge =
        accessTokenExpirationSeconds > Integer.MAX_VALUE
            ? Integer.MAX_VALUE
            : (int) accessTokenExpirationSeconds;
    setCookie(request, response, "accessToken", accessToken, accessTokenMaxAge);

    // 리프레시 토큰 쿠키 저장
    long refreshTokenExpirationSeconds = refreshTokenExpiration / 1000;
    int refreshTokenMaxAge =
        refreshTokenExpirationSeconds > Integer.MAX_VALUE
            ? Integer.MAX_VALUE
            : (int) refreshTokenExpirationSeconds;
    setCookie(request, response, "refreshToken", refreshToken, refreshTokenMaxAge);
  }
}
