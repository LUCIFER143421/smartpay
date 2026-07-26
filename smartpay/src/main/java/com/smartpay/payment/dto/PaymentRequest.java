package com.smartpay.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PaymentRequest {
    @DecimalMin(value = "0.01" , message = "Amount can't be zero")
    private BigDecimal amount;
    @NotNull(message = "Wallet id can't be null")
    private UUID receiverWalletId;
}
