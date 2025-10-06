package com.example.shardedsagawallet.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;

import com.example.shardedsagawallet.entities.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromWalletId(Long fromWalletId); // all the debit transactions

    List<Transaction> findByToWalletId(Long toWalletId); // all the credit transactions

    @Query("SELECT t FROM Transaction t WHERE t.fromWalletId = :walletId OR t.toWalletId = :walletId")
    List<Transaction> findByWalletId(@Param("walletId") Long walletId); // all the transactions for a wallet

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findBySagaInstanceId(Long sagaInstanceId);
    
}
