package com.wiseai.assignment.modules.common.util;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

/** HTTP 요청 헤더에서 정보를 추출하는 유틸리티 클래스 */
public class ExtractHeaderUtil {

  private ExtractHeaderUtil() {
    // Utility class
  }

  /**
   * HTTP 요청의 Authorization 헤더에서 Bearer 타입의 액세스 토큰을 추출합니다.
   *
   * @param request 액세스 토큰을 추출할 HttpServletRequest 객체
   * @return Bearer 토큰이 존재하면 해당 토큰을, 없거나 형식이 올바르지 않으면 빈 Optional을 반환합니다.
   */
  public static Optional<String> extractAccessToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");

    if (header == null || !header.startsWith("Bearer ")) {
      return Optional.empty();
    }

    return Optional.of(header.substring(7));
  }
}
