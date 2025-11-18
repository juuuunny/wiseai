package com.wiseai.assignment.modules.user.domain.model.vo;

import com.wiseai.assignment.modules.user.domain.enums.RoleType;

public record UserInfo(Long id, RoleType role, String email, String name) {}
