package com.smartpay.payment;

import com.smartpay.common.exception.ResourceNotFoundException;
import com.smartpay.ledger.LedgerService;
import com.smartpay.payment.dto.PaymentRequest;
import com.smartpay.payment.dto.PaymentResponse;
import com.smartpay.user.UserEntity;
import com.smartpay.user.UserRepository;
import com.smartpay.wallet.WalletEntity;
import com.smartpay.wallet.WalletRepository;
import com.smartpay.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final LedgerService ledgerService;
    private final WalletRepository walletRepository;
    private final WalletService walletService;

    @Transactional
    public PaymentResponse processPayment(String idempotencyKey, String email, PaymentRequest paymentRequest) {
        Optional<PaymentEntity> result = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (result.isPresent()) {
            return new PaymentResponse(
                    result.get().getSenderWalletId(),
                    result.get().getReceiverWalletId(),
                    result.get().getAmount(),
                    result.get().getCurrency(),
                    result.get().getStatus(),
                    result.get().getCreatedAt());
        }
        UserEntity user=userRepository.findByEmail(email)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("User does't exist with email: "+ email)
                );

        List<WalletEntity> wallets=walletRepository.findByUserId(user.getId());
        WalletEntity wallet= wallets.get(0);
        PaymentEntity payment=PaymentEntity.builder()
                .senderWalletId(wallet.getId())
                .receiverWalletId(paymentRequest.getReceiverWalletId())
                .amount(paymentRequest.getAmount())
                .currency(wallet.getCurrency())
                .status("PROCESSING")
                .idempotencyKey(idempotencyKey)
                .build();
        paymentRepository.save(payment);
        walletService.debit(wallet.getId() ,paymentRequest.getAmount());
        walletService.credit(paymentRequest.getReceiverWalletId(),paymentRequest.getAmount());
        ledgerService.recordEntries(
                wallet.getId(),paymentRequest.getReceiverWalletId(),paymentRequest.getAmount(),payment.getId()
        );
        payment.setStatus("SUCCESS");
        paymentRepository.save(payment);

        return new PaymentResponse(payment.getSenderWalletId(),payment.getReceiverWalletId(),payment.getAmount(),
                payment.getCurrency(),payment.getStatus(),payment.getCreatedAt());
    }

    public PaymentResponse getPaymentById(UUID id){
        PaymentEntity payment=paymentRepository.findById(id)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("No payment with this payment id")
                );
        return new PaymentResponse(payment.getSenderWalletId(),payment.getReceiverWalletId(),
                payment.getAmount(),payment.getCurrency(),payment.getStatus(),payment.getCreatedAt());
    }

    public List<PaymentResponse> getMyPayments(String email){
        UserEntity user=userRepository.findByEmail(email)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("User does't exist with email: "+ email)
                );
        List<WalletEntity> wallets= walletRepository.findByUserId(user.getId());
        WalletEntity wallet=wallets.get(0);
        List<PaymentEntity> responses=paymentRepository.findBySenderWalletIdOrReceiverWalletId(wallet.getId(),wallet.getId());
        return responses.stream()
                .map(r-> new PaymentResponse(r.getSenderWalletId(),r.getReceiverWalletId(),r.getAmount()
                ,r.getCurrency(),r.getStatus(),r.getCreatedAt()))
                .toList();
    }
}
