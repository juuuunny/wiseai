package com.wiseai.assignment.modules.user.adapter.jpa.mapper;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.user.adapter.jpa.entity.UserEntity;
import com.wiseai.assignment.modules.user.domain.enums.RoleType;
import com.wiseai.assignment.modules.user.domain.model.User;

@Component
public class UserEntityMapper {

  public User toDomain(UserEntity entity) {
    return User.builder()
        .id(entity.getId())
        .email(entity.getEmail())
        .password(entity.getPassword())
        .name(entity.getName())
        .role(RoleType.of(entity.getRole()))
        .build();
  }

  public UserEntity toEntity(User user) {
    return new UserEntity(
        user.getEmail(), user.getPassword(), user.getName(), user.getRole().getValue());
  }
}
