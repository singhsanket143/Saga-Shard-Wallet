package com.example.shardedsagawallet.services.saga;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shardedsagawallet.entities.SagaInstance;
import com.example.shardedsagawallet.entities.SagaStatus;
import com.example.shardedsagawallet.entities.SagaStep;
import com.example.shardedsagawallet.entities.StepStatus;
import com.example.shardedsagawallet.repositories.SagaInstanceRepository;
import com.example.shardedsagawallet.repositories.SagaStepRepository;
import com.example.shardedsagawallet.services.saga.steps.SagaStepFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorImpl implements SagaOrchestrator {

    private final ObjectMapper objectMapper;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepRepository sagaStepRepository;
    private final SagaStepFactory sagaStepFactory;

    @Override
    @Transactional
    public Long startSaga(SagaContext context) {
        try {
            String contextJson = objectMapper.writeValueAsString(context); // convert the context to a json as a string
            SagaInstance sagaInstance = SagaInstance
            .builder()
            .context(contextJson)
            .status(SagaStatus.STARTED)
            .build();

            sagaInstance = sagaInstanceRepository.save(sagaInstance);

            log.info("Started saga with id {}", sagaInstance.getId());

            return sagaInstance.getId();

        } catch (Exception e) {
            log.error("Error starting saga", e);
            throw new RuntimeException("Error starting saga", e);
        }

    }

    @Override
    @Transactional
    public boolean executeStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga instance not found"));

        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);
        if(step == null) {
            log.error("Saga step not found for step name {}", stepName);
            throw new RuntimeException("Saga step not found");
        }

        SagaStep sagaStepDB = sagaStepRepository
        .findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.PENDING)
        .orElse(
            SagaStep.builder().sagaInstanceId(sagaInstanceId).stepName(stepName).status(StepStatus.PENDING).build()
        );

        if(sagaStepDB.getId() == null) {
            sagaStepDB = sagaStepRepository.save(sagaStepDB);
        }
        
        try {
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class); 
            sagaStepDB.markAsRunning();
            sagaStepRepository.save(sagaStepDB); // updating the status to running in db
            
            boolean success = step.execute(sagaContext);

            if(success) {
                sagaStepDB.markAsCompleted();
                sagaStepRepository.save(sagaStepDB); // updating the status to completed in db

                sagaInstance.setCurrentStep(stepName); // step we just completed
                sagaInstance.markAsRunning();
                sagaInstanceRepository.save(sagaInstance); // updating the status to running in db

                log.info("Step {} executed successfully", stepName);
                return true;
            } else {
                sagaStepDB.markAsFailed();
                sagaStepRepository.save(sagaStepDB); // updating the status to failed in db
                log.error("Step {} failed", stepName);
                return false;
            }

        } catch (Exception e) {
            sagaStepDB.markAsFailed();
            sagaStepRepository.save(sagaStepDB);
            log.error("Failed to execute step {}", stepName);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga instance not found"));

        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);
        if(step == null) {
            log.error("Saga step not found for step name {}", stepName);
            throw new RuntimeException("Saga step not found");
        }

        SagaStep sagaStepDB = sagaStepRepository
        .findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.COMPLETED)
        .orElse(
            null // no such step found in the db
        );

        if(sagaStepDB.getId() == null) {
            log.info("Step {} not found in the db for saga instance {}, so it is already compensated or not executed", stepName, sagaInstanceId);
            return true;
        }
        
        try {
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class); 
            sagaStepDB.markAsCompensating();
            sagaStepRepository.save(sagaStepDB); // updating the status to running in db
            
            boolean success = step.compensate(sagaContext);

            if(success) {
                sagaStepDB.markAsCompensated();
                sagaStepRepository.save(sagaStepDB); // updating the status to completed in db
                log.info("Step {} compensated successfully", stepName);
                return true;
            } else {
                sagaStepDB.markAsFailed();
                sagaStepRepository.save(sagaStepDB); // updating the status to failed in db
                log.error("Step {} failed", stepName);
                return false;
            }

        } catch (Exception e) {
            sagaStepDB.markAsFailed();
            sagaStepRepository.save(sagaStepDB);
            log.error("Failed to execute step {}", stepName);
            return false;
        }
    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga instance not found"));
    }

    @Override
    @Transactional
    public void compensateSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga instance not found"));

        // mark the saga status as compensating in db
        sagaInstance.markAsCompensating();
        sagaInstanceRepository.save(sagaInstance);

        // get all the completed steps
        List<SagaStep> completedSteps = sagaStepRepository.findCompletedStepsBySagaInstanceId(sagaInstanceId);

        boolean allCompensated = true;
        for(SagaStep completedStep : completedSteps) {
            boolean compensated = this.compensateStep(sagaInstanceId, completedStep.getStepName());
            if(!compensated) {
                allCompensated = false;
            }
        }
        // Todo: make the compensations go in parallel

        if(allCompensated) {
            sagaInstance.markAsCompensated();
            sagaInstanceRepository.save(sagaInstance);
            log.info("Saga {} compensated successfully", sagaInstanceId);
        } else {
            log.error("Saga {} compensation failed", sagaInstanceId);
        }


    }

    @Override
    @Transactional
    public void failSaga(Long sagaInstanceId) { 
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga instance not found"));
        sagaInstance.markAsFailed();
        sagaInstanceRepository.save(sagaInstance);

        compensateSaga(sagaInstanceId);

        log.info("Saga {} failed", sagaInstanceId);
    }

    @Override
    @Transactional
    public void completeSaga(Long sagaInstanceId){
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga instance not found"));
        sagaInstance.markAsCompleted();
        sagaInstanceRepository.save(sagaInstance);
    }

}
