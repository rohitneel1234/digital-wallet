package com.wallet.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionFilterRequest {
    private BigDecimal minAmount;
    private LocalDateTime from;
    private LocalDateTime to;
}
