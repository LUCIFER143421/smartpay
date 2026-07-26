package com.smartpay.fraud.engine;

import com.smartpay.payment.dto.PaymentRequest;

public interface FraudRule {
    int evaluate(PaymentRequest paymentRequest,String senderEmail);
}
