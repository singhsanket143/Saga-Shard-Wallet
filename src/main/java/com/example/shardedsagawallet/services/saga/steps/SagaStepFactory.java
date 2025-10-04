package com.example.shardedsagawallet.services.saga.steps;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.shardedsagawallet.services.saga.SagaStepInterface;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SagaStepFactory {

    private final Map<String, SagaStepInterface> sagaStepMap;

    public static enum SagaStepType {
        DEBIT_SOURCE_WALLET_STEP,
        CREDIT_DESTINATION_WALLET_STEP,
        UPDATE_TRANSACTION_STATUS_STEP
    }

    public static final List<SagaStepType> TransferMoneySagaSteps = List.of(
        SagaStepFactory.SagaStepType.DEBIT_SOURCE_WALLET_STEP,
        SagaStepFactory.SagaStepType.CREDIT_DESTINATION_WALLET_STEP,
        SagaStepFactory.SagaStepType.UPDATE_TRANSACTION_STATUS_STEP
    );


    public SagaStepInterface getSagaStep(String stepName) {
        return sagaStepMap.get(stepName);
    }
    
}
