package com.smartpay.payment;

import com.smartpay.audit.AuditService;
import com.smartpay.common.exception.ResourceNotFoundException;
import com.smartpay.fraud.engine.FraudEngine;
import com.smartpay.ledger.LedgerService;
import com.smartpay.notification.NotificationService;
import com.smartpay.payment.dto.PaymentRequest;
import com.smartpay.payment.dto.PaymentResponse;
import com.smartpay.user.UserEntity;
import com.smartpay.user.UserRepository;
import com.smartpay.wallet.WalletEntity;
import com.smartpay.wallet.WalletRepository;
import com.smartpay.wallet.WalletService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private WalletService walletService;
    @Mock private LedgerService ledgerService;
    @Mock private FraudEngine fraudEngine;
    @Mock private NotificationService notificationService;
    @Mock private AuditService auditService;

    @InjectMocks
    private PaymentService paymentService;

    // ─── Test 7: Happy path payment ────────────────────────────────────────────
    @Test
    void processPayment_shouldReturnSuccess_whenEverythingIsValid() {
        // ARRANGE
        String email = "priya@test.com";
        String idempotencyKey = "unique-key-001";
        UUID senderWalletId = UUID.randomUUID();
        UUID receiverWalletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        PaymentRequest request = new PaymentRequest(
                new BigDecimal("500"), receiverWalletId, senderWalletId);

        UserEntity user = UserEntity.builder()
                .id(userId).email(email).build();

        WalletEntity senderWallet = WalletEntity.builder()
                .id(senderWalletId).userId(userId)
                .balance(new BigDecimal("5000")).currency("INR").build();

        // No existing payment with this idempotency key
        when(paymentRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.empty());
        // Fraud check passes (no exception thrown)
        doNothing().when(fraudEngine).evaluate(any(), anyString());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(walletRepository.findById(senderWalletId)).thenReturn(Optional.of(senderWallet));
        when(paymentRepository.save(any(PaymentEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        // ACT
        PaymentResponse response = paymentService.processPayment(idempotencyKey, email, request);

        // ASSERT
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        verify(walletService).debit(senderWalletId, new BigDecimal("500"));
        verify(walletService).credit(receiverWalletId, new BigDecimal("500"));
        verify(ledgerService).recordEntries(any(), any(), any(), any());
    }

    // ─── Test 8: Duplicate idempotency key ─────────────────────────────────────
    @Test
    void processPayment_shouldReturnCachedResult_whenIdempotencyKeyAlreadyUsed() {
        // ARRANGE
        String idempotencyKey = "duplicate-key";
        UUID senderWalletId = UUID.randomUUID();
        UUID receiverWalletId = UUID.randomUUID();

        PaymentEntity existingPayment = PaymentEntity.builder()
                .id(UUID.randomUUID())
                .senderWalletId(senderWalletId)
                .receiverWalletId(receiverWalletId)
                .amount(new BigDecimal("500"))
                .currency("INR")
                .status("SUCCESS")
                .build();

        PaymentRequest request = new PaymentRequest(
                new BigDecimal("500"), receiverWalletId, senderWalletId);

        // This idempotency key ALREADY EXISTS
        when(paymentRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.of(existingPayment));

        // ACT
        PaymentResponse response = paymentService.processPayment(
                idempotencyKey, "priya@test.com", request);

        // ASSERT — should return cached result immediately
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());

        // debit/credit should NEVER be called — no double charge
        verify(walletService, never()).debit(any(), any());
        verify(walletService, never()).credit(any(), any());
    }

    // ─── Test 9: Fraud detected ─────────────────────────────────────────────────
    @Test
    void processPayment_shouldThrowFraudException_whenFraudDetected() {
        // ARRANGE
        String idempotencyKey = "fraud-key-001";
        UUID senderWalletId = UUID.randomUUID();
        UUID receiverWalletId = UUID.randomUUID();

        PaymentRequest request = new PaymentRequest(
                new BigDecimal("60000"), receiverWalletId, senderWalletId);

        when(paymentRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.empty());

        // Fraud engine throws exception (amount > 50000)
        doThrow(new com.smartpay.common.exception.FraudDetectedException("High amount detected"))
                .when(fraudEngine).evaluate(any(), anyString());

        // ACT + ASSERT
        assertThrows(com.smartpay.common.exception.FraudDetectedException.class, () ->
                paymentService.processPayment(idempotencyKey, "priya@test.com", request));

        // No money should move
        verify(walletService, never()).debit(any(), any());
        verify(walletService, never()).credit(any(), any());
    }

    // ─── Test 10: Insufficient balance ─────────────────────────────────────────
    @Test
    void processPayment_shouldThrowException_whenBalanceInsufficient() {
        // ARRANGE
        String email = "priya@test.com";
        String idempotencyKey = "insuf-key-001";
        UUID senderWalletId = UUID.randomUUID();
        UUID receiverWalletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        PaymentRequest request = new PaymentRequest(
                new BigDecimal("99999"), receiverWalletId, senderWalletId);

        UserEntity user = UserEntity.builder()
                .id(userId).email(email).build();

        WalletEntity senderWallet = WalletEntity.builder()
                .id(senderWalletId).userId(userId)
                .balance(new BigDecimal("100")) // not enough
                .currency("INR").build();

        when(paymentRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.empty());
        doNothing().when(fraudEngine).evaluate(any(), anyString());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(walletRepository.findById(senderWalletId)).thenReturn(Optional.of(senderWallet));
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // debit() throws because balance is insufficient
        doThrow(new IllegalArgumentException("Insufficient balance"))
                .when(walletService).debit(senderWalletId, new BigDecimal("99999"));

        // ACT + ASSERT
        assertThrows(IllegalArgumentException.class, () ->
                paymentService.processPayment(idempotencyKey, email, request));
    }
}