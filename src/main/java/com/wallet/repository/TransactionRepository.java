package com.wallet.repository;

import com.wallet.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // All transactions for a wallet (sent or received)
    @Query("SELECT t FROM Transaction t WHERE t.senderWallet.id = :walletId OR t.receiverWallet.id = :walletId ORDER BY t.createdAt DESC")
    List<Transaction> findAllByWalletId(@Param("walletId") Long walletId);

    // Filter by minimum amount (JPQL)
    @Query("SELECT t FROM Transaction t WHERE (t.senderWallet.id = :walletId OR t.receiverWallet.id = :walletId) AND t.amount >= :minAmount ORDER BY t.createdAt DESC")
    List<Transaction> findByWalletIdAndMinAmount(@Param("walletId") Long walletId,
                                                  @Param("minAmount") BigDecimal minAmount);

    // Filter by date range (JPQL)
    @Query("SELECT t FROM Transaction t WHERE (t.senderWallet.id = :walletId OR t.receiverWallet.id = :walletId) AND t.createdAt BETWEEN :from AND :to ORDER BY t.createdAt DESC")
    List<Transaction> findByWalletIdAndDateRange(@Param("walletId") Long walletId,
                                                  @Param("from") LocalDateTime from,
                                                  @Param("to") LocalDateTime to);

    // System-wide stats for scheduler
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.createdAt >= :since")
    Long countTransactionsSince(@Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.createdAt >= :since")
    BigDecimal sumTransactionAmountSince(@Param("since") LocalDateTime since);
}
