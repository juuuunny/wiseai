package com.wiseai.assignment.modules.user.application.port.out.query;

import java.util.Optional;

import com.wiseai.assignment.modules.user.domain.model.User;

public interface UserQueryPort {
  Optional<User> findByEmail(String email);
}
