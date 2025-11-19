package com.wiseai.assignment.modules.payment.application.port.out.query;

import com.wiseai.assignment.modules.payment.domain.model.Payment;
import java.util.Optional;

public interface PaymentQueryPort {
  Optional<Payment> findById(Long id);
}

