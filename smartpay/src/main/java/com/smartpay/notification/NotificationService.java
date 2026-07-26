package com.smartpay.notification;

import com.smartpay.payment.PaymentEntity;
import org.springframework.stereotype.Service;


@Service
public class NotificationService {

    public void notifyPayment(PaymentEntity paymentEntity){
        System.out.println("Payment of " + paymentEntity.getAmount() + paymentEntity.getCurrency() +"sent from your wallet"+
                paymentEntity.getSenderWalletId());
        System.out.println("Payment of " + paymentEntity.getAmount() + paymentEntity.getCurrency() +"received in your wallet"+
                paymentEntity.getReceiverWalletId());
    }

}
