package com.wiseai.assignment.modules.user.application.port.out.command;

import com.wiseai.assignment.modules.user.domain.model.User;

public interface UserCommandPort {
  User save(User user);

  boolean existsByEmail(String email);
}
