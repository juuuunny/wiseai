package com.wiseai.assignment.modules.payment.application.port.out.command;

import com.wiseai.assignment.modules.payment.domain.model.Payment;

public interface PaymentCommandPort {
  Payment save(Payment payment);

  Payment update(Payment payment);

  void delete(Long id);
}
