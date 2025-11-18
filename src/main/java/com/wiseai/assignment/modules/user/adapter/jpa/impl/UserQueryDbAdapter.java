package com.wiseai.assignment.modules.user.adapter.jpa.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.user.adapter.jpa.mapper.UserEntityMapper;
import com.wiseai.assignment.modules.user.adapter.jpa.repository.UserJpaRepository;
import com.wiseai.assignment.modules.user.application.port.out.query.UserQueryPort;
import com.wiseai.assignment.modules.user.domain.model.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserQueryDbAdapter implements UserQueryPort {

  private final UserJpaRepository userJpaRepository;
  private final UserEntityMapper userEntityMapper;

  @Override
  public Optional<User> findByEmail(String email) {
    return userJpaRepository.findByEmail(email).map(userEntityMapper::toDomain);
  }
}
