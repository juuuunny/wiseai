package com.wiseai.assignment.modules.payment.domain.model;

import com.wiseai.assignment.modules.payment.domain.enums.PaymentMethod;

/**
 * 결제사 정보 도메인 모델
 *
 * <p>결제사명, API 엔드포인트, 인증정보를 관리합니다.
 */
public class PaymentProvider {

  private Long id;
  private String name;
  private String apiEndpoint;
  private String apiKey;
  private String apiSecret;
  private PaymentMethod paymentMethod;
  private boolean active;

  private PaymentProvider() {}

  private PaymentProvider(
      Long id,
      String name,
      String apiEndpoint,
      String apiKey,
      String apiSecret,
      PaymentMethod paymentMethod,
      boolean active) {
    this.id = id;
    this.name = name;
    this.apiEndpoint = apiEndpoint;
    this.apiKey = apiKey;
    this.apiSecret = apiSecret;
    this.paymentMethod = paymentMethod;
    this.active = active;
  }

  public static PaymentProvider create(
      String name,
      String apiEndpoint,
      String apiKey,
      String apiSecret,
      PaymentMethod paymentMethod) {
    validateName(name);
    validateApiEndpoint(apiEndpoint);
    validateApiKey(apiKey);
    validateApiSecret(apiSecret);
    validatePaymentMethod(paymentMethod);

    return new PaymentProvider(null, name, apiEndpoint, apiKey, apiSecret, paymentMethod, true);
  }

  public PaymentProvider withId(Long id) {
    return new PaymentProvider(id, name, apiEndpoint, apiKey, apiSecret, paymentMethod, active);
  }

  public PaymentProvider deactivate() {
    return new PaymentProvider(id, name, apiEndpoint, apiKey, apiSecret, paymentMethod, false);
  }

  public PaymentProvider activate() {
    return new PaymentProvider(id, name, apiEndpoint, apiKey, apiSecret, paymentMethod, true);
  }

  private static void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("결제사명은 필수입니다.");
    }
  }

  private static void validateApiEndpoint(String apiEndpoint) {
    if (apiEndpoint == null || apiEndpoint.isBlank()) {
      throw new IllegalArgumentException("API 엔드포인트는 필수입니다.");
    }
  }

  private static void validateApiKey(String apiKey) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalArgumentException("API 키는 필수입니다.");
    }
  }

  private static void validateApiSecret(String apiSecret) {
    if (apiSecret == null || apiSecret.isBlank()) {
      throw new IllegalArgumentException("API 시크릿은 필수입니다.");
    }
  }

  private static void validatePaymentMethod(PaymentMethod paymentMethod) {
    if (paymentMethod == null) {
      throw new IllegalArgumentException("결제 수단은 필수입니다.");
    }
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getApiEndpoint() {
    return apiEndpoint;
  }

  public String getApiKey() {
    return apiKey;
  }

  public String getApiSecret() {
    return apiSecret;
  }

  public PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  public boolean isActive() {
    return active;
  }
}
