package com.smartpay.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private UUID senderWalletId;
    private UUID receiverWalletId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
}
