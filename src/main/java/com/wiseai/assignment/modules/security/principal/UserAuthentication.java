package com.wiseai.assignment.modules.security.principal;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import com.wiseai.assignment.modules.security.exception.SecurityException;
import com.wiseai.assignment.modules.security.status.SecurityErrorStatus;
import com.wiseai.assignment.modules.user.domain.enums.RoleType;

public class UserAuthentication extends UsernamePasswordAuthenticationToken {
  public UserAuthentication(
      Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
    super(principal, credentials, authorities);
  }

  /** 현재 로그인한 유저의 식별자 반환 */
  public Long getUserId() {
    if (getPrincipal() instanceof CustomUserDetails userDetails) {
      return userDetails.getUserId();
    }
    throw new SecurityException(SecurityErrorStatus.UNEXPECTED_PRINCIPAL_TYPE_NOT_USER_DETAILS);
  }

  /** 유저 역할 반환 */
  public RoleType getRole() {
    if (getPrincipal() instanceof CustomUserDetails userDetails) {
      return userDetails.getRole();
    }
    throw new SecurityException(SecurityErrorStatus.UNEXPECTED_PRINCIPAL_TYPE_NOT_USER_DETAILS);
  }

  /** 어드민 여부 확인 (권한 기반) */
  public boolean isAdmin() {
    return getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
  }
}
