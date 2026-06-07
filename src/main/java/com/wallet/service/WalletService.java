package com.wallet.service;

import com.wallet.dto.WalletResponse;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.entity.enums.WalletStatus;
import com.wallet.exception.ResourceNotFoundException;
import com.wallet.exception.WalletClosedException;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "walletBalance", key = "#walletId")
    public WalletResponse getWalletById(Long walletId, String currentUserEmail) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + walletId));
        validateOwnership(wallet, currentUserEmail);
        return toResponse(wallet);
    }

    @Transactional(readOnly = true)
    public WalletResponse getMyWallet(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + email));
        return toResponse(wallet);
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> getAllWallets() {
        return walletRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    @CacheEvict(value = "walletBalance", key = "#walletId")
    public void closeWallet(Long walletId, String currentUserEmail, boolean isAdmin) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + walletId));
        if (!isAdmin) {
            validateOwnership(wallet, currentUserEmail);
        }
        if (wallet.getStatus() == WalletStatus.CLOSED) {
            throw new WalletClosedException("Wallet is already closed");
        }
        wallet.setStatus(WalletStatus.CLOSED);
        walletRepository.save(wallet);
        log.info("Wallet closed: {}", wallet.getWalletNumber());
    }

    @Transactional(readOnly = true)
    public Wallet findActiveWallet(Long userId) {
        return walletRepository.findActiveWalletByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Active wallet not found for user: " + userId));
    }

    @Transactional(readOnly = true)
    public Wallet findByWalletNumber(String walletNumber) {
        return walletRepository.findByWalletNumber(walletNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + walletNumber));
    }

    private void validateOwnership(Wallet wallet, String email) {
        if (!wallet.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("You do not own this wallet");
        }
    }

    public WalletResponse toResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .walletNumber(wallet.getWalletNumber())
                .balance(wallet.getBalance())
                .status(wallet.getStatus())
                .ownerEmail(wallet.getUser().getEmail())
                .createdAt(wallet.getCreatedAt())
                .build();
    }
}
