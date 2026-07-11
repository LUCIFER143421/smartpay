package com.smartpay.wallet;

import com.smartpay.common.exception.ForbiddenException;
import com.smartpay.common.exception.ResourceNotFoundException;
import com.smartpay.user.UserEntity;
import com.smartpay.user.UserRepository;
import com.smartpay.wallet.dto.DepositRequest;
import com.smartpay.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public WalletEntity createWallet( UUID userId,String currency){
        WalletEntity wallet= WalletEntity.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .currency(currency)
                .build();

       return walletRepository.save(wallet);
    }

    public List<WalletResponse> getWallets(String email){
        UserEntity user= userRepository.findByEmail(email).orElseThrow(
                ()-> new ResourceNotFoundException("User not found with email: "+ email)
        );
        List<WalletEntity> wallets=walletRepository.findByUserId(user.getId());
        return wallets.stream()
                .map(w-> new WalletResponse(w.getId(),w.getBalance(),w.getCurrency(),w.getCreatedAt()))
                .toList();
    }

    public WalletResponse deposit(UUID walletId, String email, DepositRequest request){
        UserEntity user= userRepository.findByEmail(email).orElseThrow(
                ()-> new ResourceNotFoundException("User not found with email: "+ email)
        );
        WalletEntity wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (!wallet.getUserId().equals(user.getId())) {
            throw new ForbiddenException("This wallet does not belong to you");
        }
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        walletRepository.save(wallet);
        return new WalletResponse(wallet.getId(),wallet.getBalance(),wallet.getCurrency(),wallet.getCreatedAt());
    }
}
