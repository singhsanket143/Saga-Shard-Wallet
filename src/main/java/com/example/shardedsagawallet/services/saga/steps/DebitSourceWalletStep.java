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
public class DebitSourceWalletStep implements SagaStepInterface {

    private final WalletRepository walletRepository;
    
    @Override
    @Transactional
    public boolean execute(SagaContext context) {
        Long fromWalletId = context.getLong("fromWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Debiting source wallet {} with amount {}", fromWalletId, amount);

        Wallet wallet = walletRepository.findByIdWithLock(fromWalletId)
        .orElseThrow(() -> new RuntimeException("Wallet not found"));

        log.info("Wallet fetched with balance {}", wallet.getBalance());
        context.put("originalSourceWalletBalance", wallet.getBalance());
        // TODO: Once the context is updated in memory, we need to update the context in the database

        walletRepository.updateBalanceByUserId(fromWalletId, wallet.getBalance().subtract(amount));

        log.info("Wallet saved with balance {}", wallet.getBalance());
        context.put("sourceWalletBalanceAfterDebit", wallet.getBalance());
        // TODO: Once the context is updated in memory, we need to update the context in the database

        log.info("Debit source wallet step executed successfully");

        
        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context) {
        Long fromWalletId = context.getLong("fromWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Compensating source wallet {} with amount {}", fromWalletId, amount);

        Wallet wallet = walletRepository.findByIdWithLock(fromWalletId)
        .orElseThrow(() -> new RuntimeException("Wallet not found"));

        log.info("Wallet fetched with balance {}", wallet.getBalance());
        context.put("sourceWalletBalanceBeforeCreditCompensation", wallet.getBalance());
        // TODO: Once the context is updated in memory, we need to update the context in the database


        walletRepository.updateBalanceByUserId(fromWalletId, wallet.getBalance().add(amount));
        log.info("Wallet saved with balance {}", wallet.getBalance());
        context.put("sourceWalletBalanceAfterCreditCompensation", wallet.getBalance());
        // TODO: Once the context is updated in memory, we need to update the context in the database

        log.info("Compensating source wallet step executed successfully");

        
        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString();
    }
}
