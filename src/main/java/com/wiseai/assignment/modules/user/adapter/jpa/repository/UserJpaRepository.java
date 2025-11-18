package com.wiseai.assignment.modules.user.adapter.jpa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wiseai.assignment.modules.user.adapter.jpa.entity.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
  boolean existsByEmail(String email);

  Optional<UserEntity> findByEmail(String email);
}
