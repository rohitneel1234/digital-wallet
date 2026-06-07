package com.wallet.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransferRequest {
    @NotBlank(message = "Receiver wallet number is required")
    private String receiverWalletNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Transfer amount must be positive")
    @DecimalMin(value = "0.01", message = "Minimum transfer amount is 0.01")
    private BigDecimal amount;

    private String note;
}
