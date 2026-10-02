package com.antenapro.hotelcheck.persistence.jpa;

import com.antenapro.hotelcheck.evaluation.CapabilityOutcome;
import com.antenapro.hotelcheck.evaluation.CapabilityOutcomeStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "evaluation_capability_outcomes")
public class CapabilityOutcomeJpaEntity {
    @Id
    private UUID id;
    private String capability;

    @Enumerated(EnumType.STRING)
    private CapabilityOutcomeStatus status;

    private String detail;
    private Instant recordedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private EvaluationAttemptJpaEntity attempt;

    protected CapabilityOutcomeJpaEntity() {
    }

    static CapabilityOutcomeJpaEntity fromDomain(EvaluationAttemptJpaEntity attempt, CapabilityOutcome outcome) {
        CapabilityOutcomeJpaEntity entity = new CapabilityOutcomeJpaEntity();
        entity.id = UUID.randomUUID();
        entity.capability = outcome.capability();
        entity.status = outcome.status();
        entity.detail = outcome.detail();
        entity.recordedAt = outcome.recordedAt();
        entity.attempt = attempt;
        return entity;
    }

    CapabilityOutcome toDomain() {
        return new CapabilityOutcome(capability, status, detail, recordedAt);
    }
}
