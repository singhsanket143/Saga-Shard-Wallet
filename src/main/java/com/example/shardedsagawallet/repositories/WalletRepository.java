package com.example.shardedsagawallet.repositories;

import java.util.List;
import java.util.Optional;
import java.math.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.shardedsagawallet.entities.Wallet;

import jakarta.persistence.LockModeType;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    List<Wallet> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.userId = :id")
    Optional<Wallet> findByIdWithLock(@Param("id")  Long id);

    @Modifying
    @Query("UPDATE Wallet w SET w.balance = :balance WHERE w.userId = :userId")
    void updateBalanceByUserId(@Param("userId") Long userId, @Param("balance") BigDecimal balance);

}
