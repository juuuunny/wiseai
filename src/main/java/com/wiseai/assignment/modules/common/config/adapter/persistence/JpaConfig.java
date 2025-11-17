package com.wiseai.assignment.modules.common.config.adapter.persistence;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
  /**
   * JPA 감사(Auditing) 기능을 위한 AuditorAware입니다. 현재는 인증 기능이 없으므로 빈 Optional을 반환합니다. 나중에 인증 기능이 추가되면 실제
   * 사용자 ID를 반환하도록 수정할 수 있습니다.
   *
   * @return 현재는 항상 빈 Optional을 반환
   */
  @Bean
  public AuditorAware<Long> auditorProvider() {
    return Optional::empty;
  }
}
