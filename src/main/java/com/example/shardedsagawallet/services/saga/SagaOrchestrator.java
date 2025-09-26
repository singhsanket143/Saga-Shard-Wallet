package com.example.shardedsagawallet.services.saga;

import com.example.shardedsagawallet.entities.SagaInstance;

public interface SagaOrchestrator {

   Long startSaga(SagaContext context);
   
   boolean executeStep(Long sagaInstanceId, String stepName);

   boolean compensateStep(Long sagaInstanceId, String stepName);

   SagaInstance getSagaInstance(Long sagaInstanceId);

   void compensateSaga(Long sagaInstanceId);

   void failSaga(Long sagaInstanceId);

   void completeSaga(Long sagaInstanceId);
    
} 