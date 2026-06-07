package com.wallet.service;

import com.wallet.dto.TransactionFilterRequest;
import com.wallet.dto.TransactionResponse;
import com.wallet.dto.TransferRequest;
import com.wallet.entity.Transaction;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.entity.enums.TransactionStatus;
import com.wallet.entity.enums.WalletStatus;
import com.wallet.exception.InsufficientBalanceException;
import com.wallet.exception.ResourceNotFoundException;
import com.wallet.exception.WalletClosedException;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    @CacheEvict(value = "walletBalance", allEntries = true)
    public TransactionResponse transfer(TransferRequest request, String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        Wallet senderWallet = walletRepository.findActiveWalletByUserId(sender.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Active sender wallet not found"));

        Wallet receiverWallet = walletRepository.findByWalletNumber(request.getReceiverWalletNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver wallet not found: " + request.getReceiverWalletNumber()));

        if (senderWallet.getWalletNumber().equals(receiverWallet.getWalletNumber())) {
            throw new IllegalArgumentException("Cannot transfer to your own wallet");
        }
        if (receiverWallet.getStatus() == WalletStatus.CLOSED) {
            throw new WalletClosedException("Receiver wallet is closed");
        }
        if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance. Available: %.2f, Requested: %.2f",
                            senderWallet.getBalance(), request.getAmount()));
        }

        senderWallet.setBalance(senderWallet.getBalance().subtract(request.getAmount()));
        receiverWallet.setBalance(receiverWallet.getBalance().add(request.getAmount()));
        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        String referenceId = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        Transaction transaction = Transaction.builder()
                .referenceId(referenceId)
                .senderWallet(senderWallet)
                .receiverWallet(receiverWallet)
                .amount(request.getAmount())
                .status(TransactionStatus.SUCCESS)
                .note(request.getNote())
                .build();
        transaction = transactionRepository.save(transaction);

        log.info("Transfer completed: {} | {} -> {} | Amount: {}",
                referenceId, senderWallet.getWalletNumber(),
                receiverWallet.getWalletNumber(), request.getAmount());

        String receiverEmail = receiverWallet.getUser().getEmail();
        notificationService.sendTransactionSuccessNotification(
                senderEmail, receiverEmail, request.getAmount(), referenceId);

        return toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyTransactions(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        return transactionRepository.findAllByWalletId(wallet.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getFilteredTransactions(String email, TransactionFilterRequest filter) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        List<Transaction> transactions;
        if (filter.getMinAmount() != null) {
            transactions = transactionRepository.findByWalletIdAndMinAmount(wallet.getId(), filter.getMinAmount());
        } else if (filter.getFrom() != null && filter.getTo() != null) {
            transactions = transactionRepository.findByWalletIdAndDateRange(wallet.getId(), filter.getFrom(), filter.getTo());
        } else {
            transactions = transactionRepository.findAllByWalletId(wallet.getId());
        }
        return transactions.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream().map(this::toResponse).toList();
    }

    public TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .referenceId(t.getReferenceId())
                .senderWalletNumber(t.getSenderWallet().getWalletNumber())
                .receiverWalletNumber(t.getReceiverWallet().getWalletNumber())
                .amount(t.getAmount())
                .status(t.getStatus())
                .note(t.getNote())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
