package com.smartpay.ledger;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {
    private final LedgerRepository ledgerRepository;

    public void recordEntries(UUID senderWalletId, UUID receiverWalletId, BigDecimal amount,UUID paymentId){
        LedgerEntry debitEntry= LedgerEntry.builder()
                .paymentId(paymentId)
                .walletId(senderWalletId)
                .amount(amount)
                .entryType("DEBIT")
                .build();

        LedgerEntry creditEntry= LedgerEntry.builder()
                .paymentId(paymentId)
                .walletId(receiverWalletId)
                .amount(amount)
                .entryType("CREDIT")
                .build();

        ledgerRepository.saveAll(List.of(debitEntry,creditEntry));
    }
}
