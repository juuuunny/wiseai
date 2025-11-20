package com.wiseai.assignment.modules.payment.adapter.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_providers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentProviderEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(name = "api_endpoint", nullable = false, length = 500)
  private String apiEndpoint;

  @Column(name = "api_key", nullable = false, length = 255)
  private String apiKey;

  @Column(name = "api_secret", nullable = false, length = 255)
  private String apiSecret;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false, length = 20)
  private PaymentMethod paymentMethod;

  @Column(nullable = false)
  private boolean active;

  public PaymentProviderEntity(
      String name,
      String apiEndpoint,
      String apiKey,
      String apiSecret,
      PaymentMethod paymentMethod,
      boolean active) {
    this.name = name;
    this.apiEndpoint = apiEndpoint;
    this.apiKey = apiKey;
    this.apiSecret = apiSecret;
    this.paymentMethod = paymentMethod;
    this.active = active;
  }

  public void update(
      String name,
      String apiEndpoint,
      String apiKey,
      String apiSecret,
      PaymentMethod paymentMethod,
      boolean active) {
    this.name = name;
    this.apiEndpoint = apiEndpoint;
    this.apiKey = apiKey;
    this.apiSecret = apiSecret;
    this.paymentMethod = paymentMethod;
    this.active = active;
  }

  public void deactivate() {
    this.active = false;
  }

  public void activate() {
    this.active = true;
  }
}
