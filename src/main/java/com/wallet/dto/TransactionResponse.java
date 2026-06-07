package com.wallet.dto;

import com.wallet.entity.enums.TransactionStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionResponse {
    private Long id;
    private String referenceId;
    private String senderWalletNumber;
    private String receiverWalletNumber;
    private BigDecimal amount;
    private TransactionStatus status;
    private String note;
    private LocalDateTime createdAt;
}
