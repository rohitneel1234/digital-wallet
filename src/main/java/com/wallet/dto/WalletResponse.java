package com.wallet.dto;

import com.wallet.entity.enums.WalletStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WalletResponse {
    private Long id;
    private String walletNumber;
    private BigDecimal balance;
    private WalletStatus status;
    private String ownerEmail;
    private LocalDateTime createdAt;
}
