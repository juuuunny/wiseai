package com.wiseai.assignment.modules.user.domain.enums;

public enum RoleType {
  ROLE_USER("ROLE_USER"),
  ROLE_ADMIN("ROLE_ADMIN");

  private final String value;

  RoleType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static RoleType of(String value) {
    for (RoleType type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    return ROLE_USER;
  }
}
