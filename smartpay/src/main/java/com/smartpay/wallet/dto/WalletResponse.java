package com.smartpay.wallet.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class WalletResponse {
    private UUID id;
    private BigDecimal balance;
    private String currency;
    private LocalDateTime createdAt;
}
