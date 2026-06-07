package com.wallet.health;

import com.wallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TransactionEngineHealthIndicator implements HealthIndicator {

    private final TransactionRepository transactionRepository;

    @Override
    public Health health() {
        try {
            long count = transactionRepository.countTransactionsSince(LocalDateTime.now().minusHours(1));
            return Health.up()
                    .withDetail("status", "OPERATIONAL")
                    .withDetail("transactions_last_hour", count)
                    .withDetail("engine", "Transaction Engine")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "DOWN")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
