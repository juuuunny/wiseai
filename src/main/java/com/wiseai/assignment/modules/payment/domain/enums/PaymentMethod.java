package com.wiseai.assignment.modules.payment.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
  TOSS("토스페이먼츠"),
  KAKAO("카카오페이");

  private final String name;
}
