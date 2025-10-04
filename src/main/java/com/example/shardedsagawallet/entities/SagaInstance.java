package com.example.shardedsagawallet.entities;

import org.apache.calcite.model.JsonType;

import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "saga_instance")
public class SagaInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SagaStatus status = SagaStatus.STARTED;

    @Type(JsonType.class)
    @Column(name = "context", columnDefinition = "json")
    private String context;

    @Column(name = "current_step")
    private String currentStep;

    public void markAsStarted() {
        this.status = SagaStatus.STARTED;
    }

    public void markAsRunning() {
        this.status = SagaStatus.RUNNING;
    }

    public void markAsCompleted() {
        this.status = SagaStatus.COMPLETED;
    }

    public void markAsFailed() {
        this.status = SagaStatus.FAILED;
    }

    public void markAsCompensating() {
        this.status = SagaStatus.COMPENSATING;
    }

    public void markAsCompensated() {
        this.status = SagaStatus.COMPENSATED;
    }
}
