package com.wiseai.assignment.modules.user.adapter.jpa.impl;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.user.adapter.jpa.mapper.UserEntityMapper;
import com.wiseai.assignment.modules.user.adapter.jpa.repository.UserJpaRepository;
import com.wiseai.assignment.modules.user.application.port.out.command.UserCommandPort;
import com.wiseai.assignment.modules.user.domain.model.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserCommandDbAdapter implements UserCommandPort {

  private final UserJpaRepository userJpaRepository;
  private final UserEntityMapper userEntityMapper;

  @Override
  public User save(User user) {
    var saved = userJpaRepository.save(userEntityMapper.toEntity(user));
    return userEntityMapper.toDomain(saved);
  }

  @Override
  public boolean existsByEmail(String email) {
    return userJpaRepository.existsByEmail(email);
  }
}
