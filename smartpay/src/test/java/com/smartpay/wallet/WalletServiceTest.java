package com.smartpay.wallet;

import com.smartpay.common.exception.ForbiddenException;
import com.smartpay.user.UserEntity;
import com.smartpay.user.UserRepository;
import com.smartpay.wallet.dto.DepositRequest;
import com.smartpay.wallet.dto.WalletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock private WalletRepository walletRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private WalletService walletService;

    // ─── Test 4: Happy path deposit ────────────────────────────────────────────
    @Test
    void deposit_shouldIncreaseBalance_whenWalletBelongsToUser() {
        // ARRANGE
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        String email = "priya@test.com";

        UserEntity user = UserEntity.builder()
                .id(userId)
                .email(email)
                .build();

        WalletEntity wallet = WalletEntity.builder()
                .id(walletId)
                .userId(userId)
                .balance(new BigDecimal("1000"))
                .currency("INR")
                .build();

        DepositRequest request = new DepositRequest();
        request.setAmount(new BigDecimal("500"));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(WalletEntity.class))).thenAnswer(i -> i.getArgument(0));

        // ACT
        WalletResponse response = walletService.deposit(walletId, email, request);

        // ASSERT
        assertNotNull(response);
        assertEquals(new BigDecimal("1500"), response.getBalance());
        verify(walletRepository).save(any(WalletEntity.class));
    }

    // ─── Test 5: Deposit to wallet not owned by user ───────────────────────────
    @Test
    void deposit_shouldThrowForbiddenException_whenWalletBelongsToDifferentUser() {
        // ARRANGE
        UUID userId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID(); // wallet belongs to someone else
        UUID walletId = UUID.randomUUID();
        String email = "priya@test.com";

        UserEntity user = UserEntity.builder()
                .id(userId)
                .email(email)
                .build();

        WalletEntity wallet = WalletEntity.builder()
                .id(walletId)
                .userId(differentUserId) // owned by a DIFFERENT user
                .balance(new BigDecimal("1000"))
                .build();

        DepositRequest request = new DepositRequest();
        request.setAmount(new BigDecimal("500"));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        // ACT + ASSERT
        assertThrows(ForbiddenException.class, () ->
                walletService.deposit(walletId, email, request));

        // save() should NEVER be called if wallet doesn't belong to user
        verify(walletRepository, never()).save(any());
    }

    // ─── Test 6: Debit with insufficient balance ───────────────────────────────
    @Test
    void debit_shouldThrowException_whenBalanceIsInsufficient() {
        // ARRANGE
        UUID walletId = UUID.randomUUID();

        WalletEntity wallet = WalletEntity.builder()
                .id(walletId)
                .balance(new BigDecimal("100")) // only 100 in wallet
                .build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        // ACT + ASSERT — trying to debit 500 from a wallet with only 100
        assertThrows(IllegalArgumentException.class, () ->
                walletService.debit(walletId, new BigDecimal("500")));

        // save() should NEVER be called if balance is insufficient
        verify(walletRepository, never()).save(any());
    }
}