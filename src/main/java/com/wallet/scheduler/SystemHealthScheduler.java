package com.wallet.scheduler;

import com.wallet.repository.TransactionRepository;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemHealthScheduler {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    // Runs every minute
    @Scheduled(fixedRate = 60000)
    public void logSystemHealth() {
        try {
            long totalUsers = userRepository.count();
            long totalWallets = walletRepository.count();
            long totalTransactions = transactionRepository.count();

            LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
            Long recentTxCount = transactionRepository.countTransactionsSince(oneMinuteAgo);
            BigDecimal recentTxVolume = transactionRepository.sumTransactionAmountSince(oneMinuteAgo);

            log.info("═══════════════ SYSTEM HEALTH REPORT ═══════════════");
            log.info("│ Users: {}  │  Wallets: {}  │  Total Transactions: {}", totalUsers, totalWallets, totalTransactions);
            log.info("│ Last 1 min → Transactions: {}  │  Volume: {}", recentTxCount, recentTxVolume);
            log.info("│ Transaction Engine: OPERATIONAL");
            log.info("═════════════════════════════════════════════════════");
        } catch (Exception e) {
            log.error("[SCHEDULER] Health check failed: {}", e.getMessage());
        }
    }
}
