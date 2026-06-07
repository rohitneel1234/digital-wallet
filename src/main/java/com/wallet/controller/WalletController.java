package com.wallet.controller;

import com.wallet.dto.ApiResponse;
import com.wallet.dto.WalletResponse;
import com.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // Get my wallet
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<WalletResponse>> getMyWallet(
            @AuthenticationPrincipal UserDetails userDetails) {
        WalletResponse response = walletService.getMyWallet(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Wallet retrieved", response));
    }

    // Get wallet by ID (owner or admin)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        WalletResponse response = walletService.getWalletById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Wallet retrieved", response));
    }

    // Admin: get all wallets
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getAllWallets() {
        List<WalletResponse> wallets = walletService.getAllWallets();
        return ResponseEntity.ok(ApiResponse.success("All wallets retrieved", wallets));
    }

    // Close wallet (owner closes own, admin can close any)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> closeWallet(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        walletService.closeWallet(id, userDetails.getUsername(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Wallet closed successfully", null));
    }
}
