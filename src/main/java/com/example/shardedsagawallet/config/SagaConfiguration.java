package com.example.shardedsagawallet.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.shardedsagawallet.services.saga.SagaStepInterface;
import com.example.shardedsagawallet.services.saga.steps.CreditDestinationWalletStep;
import com.example.shardedsagawallet.services.saga.steps.DebitSourceWalletStep;
import com.example.shardedsagawallet.services.saga.steps.UpdateTransactionStatus;
import com.example.shardedsagawallet.services.saga.steps.SagaStepFactory.SagaStepType;

@Configuration
public class SagaConfiguration {

    @Bean
    public Map<String, SagaStepInterface> sagaStepMap(
        DebitSourceWalletStep debitSourceWalletStep,
        CreditDestinationWalletStep creditDestinationWalletStep,
        UpdateTransactionStatus updateTransactionStatus
    ) {
        Map<String, SagaStepInterface> map = new HashMap<>();
        map.put(SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString(), debitSourceWalletStep);
        map.put(SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString(), creditDestinationWalletStep);
        map.put(SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString(), updateTransactionStatus);
        return map;
    }
    
}
