package com.wallet.repository;

import com.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);

    Optional<Wallet> findByWalletNumber(String walletNumber);

    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.status = com.wallet.entity.enums.WalletStatus.ACTIVE")
    Optional<Wallet> findActiveWalletByUserId(@Param("userId") Long userId);
}
