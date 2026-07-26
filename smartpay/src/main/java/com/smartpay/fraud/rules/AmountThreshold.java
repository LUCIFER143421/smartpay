package com.smartpay.fraud.rules;

import com.smartpay.fraud.engine.FraudRule;
import com.smartpay.payment.dto.PaymentRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AmountThreshold implements FraudRule {
    BigDecimal threshold= new BigDecimal("50000");
    @Override
    public int evaluate(PaymentRequest paymentRequest, String senderEmail) {
        if(paymentRequest.getAmount().compareTo(threshold) >0){
            return 70;
        }
        return 0;
    }
}
