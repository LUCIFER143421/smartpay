package com.smartpay.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    Optional<PaymentEntity> findByIdempotencyKey(String key);
    List<PaymentEntity> findBySenderWalletIdOrReceiverWalletId(UUID senderWalletId, UUID receiverWalletId);
    int countBySenderWalletIdAndCreatedAtAfter(UUID senderWalletId, LocalDateTime after);
}
