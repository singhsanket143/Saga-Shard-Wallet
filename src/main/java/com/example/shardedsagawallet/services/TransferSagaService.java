package com.example.shardedsagawallet.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.shardedsagawallet.entities.Transaction;
import com.example.shardedsagawallet.services.saga.SagaContext;
import com.example.shardedsagawallet.services.saga.SagaOrchestrator;
import com.example.shardedsagawallet.services.saga.steps.SagaStepFactory;
import com.example.shardedsagawallet.services.saga.steps.SagaStepFactory.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferSagaService {
    
    private final TransactionService transactionService;
    private final SagaOrchestrator sagaOrchestrator;


    @Transactional
    public Long initiateTransfer(
        Long fromWalletId,
        Long toWalletId,
        BigDecimal amount,
        String description
    ) {
        log.info("Initiating transfer from wallet {} to wallet {} with amount {} and description {}", fromWalletId, toWalletId, amount, description);

        Transaction transaction = transactionService.createTransaction(fromWalletId, toWalletId, amount, description);

        SagaContext sagaContext = SagaContext.builder()
            .data(Map.ofEntries(
                Map.entry("transactionId", transaction.getId()),
                Map.entry("fromWalletId", fromWalletId),
                Map.entry("toWalletId", toWalletId),
                Map.entry("amount", amount),
                Map.entry("description", description)
            ))
            .build();

        log.info("Saga context created with id {}", sagaContext.get("description"));

        Long sagaInstanceId = sagaOrchestrator.startSaga(sagaContext);
        log.info("Saga instance created with id {}", sagaInstanceId);

        transactionService.updateTransactionWithSagaInstanceId(transaction.getId(), sagaInstanceId);

        executeTransferSaga(sagaInstanceId);

        return sagaInstanceId;
    }

    public void executeTransferSaga(Long sagaInstanceId) {
        log.info("Executing transfer saga with id {}", sagaInstanceId);


        try {
            for(SagaStepType step : SagaStepFactory.TransferMoneySagaSteps) {
                boolean success  = sagaOrchestrator.executeStep(sagaInstanceId, step.toString() );
                if(!success) {
                    log.error("Failed to execute step {}", step.toString());
                    sagaOrchestrator.failSaga(sagaInstanceId);
                    return;
                }

            }
            sagaOrchestrator.completeSaga(sagaInstanceId);
            log.info("Transfer saga completed with id {}", sagaInstanceId);
        } catch (Exception e) {
            log.error("Failed to execute transfer saga with id {}", sagaInstanceId, e);
            sagaOrchestrator.failSaga(sagaInstanceId);
            
        }
    }
}
