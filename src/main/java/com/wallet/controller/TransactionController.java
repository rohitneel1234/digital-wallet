package com.wallet.controller;

import com.wallet.dto.*;
import com.wallet.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // Perform a fund transfer
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TransactionResponse response = transactionService.transfer(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Transfer successful", response));
    }

    // Get my transaction history
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getMyTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TransactionResponse> transactions = transactionService.getMyTransactions(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved", transactions));
    }

    // Filter transactions by amount or date (JPQL)
    @PostMapping("/me/filter")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> filterTransactions(
            @RequestBody TransactionFilterRequest filter,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TransactionResponse> transactions = transactionService.getFilteredTransactions(
                userDetails.getUsername(), filter);
        return ResponseEntity.ok(ApiResponse.success("Filtered transactions retrieved", transactions));
    }

    // Admin: view all transactions
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions() {
        List<TransactionResponse> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(ApiResponse.success("All transactions retrieved", transactions));
    }
}
