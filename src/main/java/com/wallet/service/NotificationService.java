package com.wallet.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class NotificationService {

    @Async("notificationExecutor")
    public void sendTransactionSuccessNotification(String senderEmail,
                                                    String receiverEmail,
                                                    BigDecimal amount,
                                                    String referenceId) {
        try {
            // Simulate async notification (email/push in production)
            Thread.sleep(100);
            log.info("[NOTIFICATION] Transaction Successful - Ref: {} | From: {} | To: {} | Amount: {}",
                    referenceId, senderEmail, receiverEmail, amount);
            // In production: integrate with email service (SendGrid, SES) or push notifications
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Notification interrupted for ref: {}", referenceId);
        }
    }

    @Async("notificationExecutor")
    public void sendWalletCreatedNotification(String email, String walletNumber) {
        log.info("[NOTIFICATION] Wallet Created - Email: {} | Wallet: {}", email, walletNumber);
    }
}
