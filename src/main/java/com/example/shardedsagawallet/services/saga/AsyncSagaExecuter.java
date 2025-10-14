package com.example.shardedsagawallet.services.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncSagaExecuter {

    private final SagaOrchestrator sagaOrchestrator;

    @Async("compensationExecutor")
    public CompletableFuture<Boolean> compensateStep(Long sagaInstanceId, String stepName) {
        log.info("Entering into async compensateStep");
        boolean result = sagaOrchestrator.compensateStep(sagaInstanceId, stepName);
        return CompletableFuture.completedFuture(result);
    }

}
