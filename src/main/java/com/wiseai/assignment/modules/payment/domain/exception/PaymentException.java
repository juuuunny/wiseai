package com.wiseai.assignment.modules.payment.domain.exception;

import com.wiseai.assignment.modules.common.exception.BusinessException;
import com.wiseai.assignment.modules.payment.domain.status.PaymentErrorStatus;

public class PaymentException extends BusinessException {
  public PaymentException(PaymentErrorStatus errorStatus) {
    super(errorStatus);
  }
}

