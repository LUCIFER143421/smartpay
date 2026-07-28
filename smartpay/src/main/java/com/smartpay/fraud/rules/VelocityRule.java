package com.smartpay.fraud.rules;

import com.smartpay.common.exception.ResourceNotFoundException;
import com.smartpay.fraud.engine.FraudRule;
import com.smartpay.payment.PaymentRepository;
import com.smartpay.payment.dto.PaymentRequest;
import com.smartpay.user.UserEntity;
import com.smartpay.user.UserRepository;
import com.smartpay.wallet.WalletEntity;
import com.smartpay.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VelocityRule implements FraudRule {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public int evaluate(PaymentRequest paymentRequest, String senderEmail) {
        UserEntity user=userRepository.findByEmail(senderEmail).orElseThrow(
                ()-> new ResourceNotFoundException("user not found")
        );
        List<WalletEntity> wallets = walletRepository.findByUserId(user.getId());
        if (wallets.isEmpty()) {
            throw new ResourceNotFoundException("No wallet found for this user");
        }
        WalletEntity wallet=wallets.get(0);
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusSeconds(60);
        if(paymentRepository.countBySenderWalletIdAndCreatedAtAfter(wallet.getId(), oneMinuteAgo)>=3){
            return 80;
        }
        return 0;
    }
}
