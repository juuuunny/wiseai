package com.wiseai.assignment.modules.payment.adapter.jpa.mapper;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.payment.adapter.jpa.entity.PaymentProviderEntity;
import com.wiseai.assignment.modules.payment.domain.model.PaymentProvider;

@Component
public class PaymentProviderEntityMapper {

  public PaymentProvider toDomain(PaymentProviderEntity entity) {
    if (entity == null) {
      return null;
    }

    PaymentProvider domain =
        PaymentProvider.create(
            entity.getName(),
            entity.getApiEndpoint(),
            entity.getApiKey(),
            entity.getApiSecret(),
            entity.getPaymentMethod());

    if (entity.getId() != null) {
      domain = domain.withId(entity.getId());
    }

    if (!entity.isActive()) {
      domain = domain.deactivate();
    }

    return domain;
  }

  public PaymentProviderEntity toEntity(PaymentProvider domain) {
    if (domain == null) {
      return null;
    }

    PaymentProviderEntity entity =
        new PaymentProviderEntity(
            domain.getName(),
            domain.getApiEndpoint(),
            domain.getApiKey(),
            domain.getApiSecret(),
            domain.getPaymentMethod(),
            domain.isActive());

    if (domain.getId() != null) {
      entity =
          new PaymentProviderEntity(
              domain.getName(),
              domain.getApiEndpoint(),
              domain.getApiKey(),
              domain.getApiSecret(),
              domain.getPaymentMethod(),
              domain.isActive());
      // ID는 JPA가 관리하므로 별도 설정 불필요
    }

    return entity;
  }
}
