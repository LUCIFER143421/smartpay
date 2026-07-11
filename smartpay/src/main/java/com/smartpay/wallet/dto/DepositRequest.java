package com.smartpay.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequest {
    @DecimalMin(value = "0.01", message = "The amount should be greater than ZERO")
    private BigDecimal amount;
}
