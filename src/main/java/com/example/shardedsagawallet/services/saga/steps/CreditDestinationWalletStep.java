package com.example.shardedsagawallet.services.saga.steps;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.example.shardedsagawallet.entities.Wallet;
import com.example.shardedsagawallet.repositories.WalletRepository;
import com.example.shardedsagawallet.services.saga.SagaContext;
import com.example.shardedsagawallet.services.saga.SagaStepInterface;
import com.example.shardedsagawallet.services.saga.steps.SagaStepFactory.SagaStepType;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditDestinationWalletStep implements SagaStepInterface {
    
    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public boolean execute(SagaContext context) {
        // Step 1: Get the destination wallet id from the context
        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Crediting destination wallet {} with amount {}", toWalletId, amount);

        // Step 2: Fetch the destination wallet from the database with a lock
        Wallet wallet = walletRepository.findByIdWithLock(toWalletId)
        .orElseThrow(() -> new RuntimeException("Wallet not found"));

        log.info("Wallet fetched with balance {}", wallet.getBalance());
        context.put("originalToWalletBalance", wallet.getBalance());
        // TODO: Once the context is updated in memory, we need to update the context in the database
        
        // Step 3: Credit the destination wallet
        walletRepository.updateBalanceByUserId(toWalletId, wallet.getBalance().add(amount));
        log.info("Wallet saved with balance {}", wallet.getBalance());
        context.put("toWalletBalanceAfterCredit", wallet.getBalance());
        // TODO: Once the context is updated in memory, we need to update the context in the database

        log.info("Credit destination wallet step executed successfully");
        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context) {
        // Step 1: Get the destination wallet id from the context
        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Compensation credit of destination wallet {} with amount {}", toWalletId, amount);

        // Step 2: Fetch the destination wallet from the database with a lock
        Wallet wallet = walletRepository.findByIdWithLock(toWalletId)
        .orElseThrow(() -> new RuntimeException("Wallet not found"));

        log.info("Wallet fetched with balance {}", wallet.getBalance());
        
        // Step 3: Credit the destination wallet
        
        walletRepository.updateBalanceByUserId(toWalletId, wallet.getBalance().subtract(amount));
        log.info("Wallet saved with balance {}", wallet.getBalance());
        context.put("toWalletBalanceAfterCreditCompensation", wallet.getBalance());
        // TODO: Once the context is updated in memory, we need to update the context in the database

        log.info("Credit compensation of destination wallet step executed successfully");
        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString();
    }
}
