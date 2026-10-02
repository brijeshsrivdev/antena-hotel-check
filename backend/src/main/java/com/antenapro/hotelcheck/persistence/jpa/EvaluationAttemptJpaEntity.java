package com.antenapro.hotelcheck.persistence.jpa;

import com.antenapro.hotelcheck.evaluation.CapabilityOutcome;
import com.antenapro.hotelcheck.evaluation.EvaluationAttempt;
import com.antenapro.hotelcheck.evaluation.EvaluationLifecycleState;
import com.antenapro.hotelcheck.evaluation.OwnerFacingOutcome;
import com.antenapro.hotelcheck.evaluation.TerminalOutcome;
import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "evaluation_attempts")
public class EvaluationAttemptJpaEntity {
    @Id
    private UUID id;
    private int attemptNumber;
    private Instant createdAt;
    private Instant startedAt;

    @Enumerated(EnumType.STRING)
    private EvaluationLifecycleState state;

    private boolean analysisReportAvailable;
    private boolean interactivePreviewAvailable;
    private boolean ownerFacingOutcomePresent;

    private String terminalReason;
    private Instant terminalRecordedAt;
    private boolean terminalOutcomePresent;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "evaluation_attempt_terminal_metadata", joinColumns = @JoinColumn(name = "attempt_id"))
    @Column(name = "metadata_value")
    private Map<String, String> terminalMetadata = new LinkedHashMap<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private EvaluationJpaEntity evaluation;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("capability ASC")
    private List<CapabilityOutcomeJpaEntity> capabilityOutcomes = new ArrayList<>();

    protected EvaluationAttemptJpaEntity() {
    }

    static EvaluationAttemptJpaEntity fromDomain(EvaluationJpaEntity evaluation, EvaluationAttempt attempt) {
        EvaluationAttemptJpaEntity entity = new EvaluationAttemptJpaEntity();
        entity.id = attempt.attemptId();
        entity.attemptNumber = attempt.attemptNumber();
        entity.createdAt = attempt.createdAt();
        entity.startedAt = attempt.startedAt();
        entity.state = attempt.state();
        if (attempt.ownerFacingOutcome() != null) {
            entity.ownerFacingOutcomePresent = true;
            entity.analysisReportAvailable = attempt.ownerFacingOutcome().analysisReportAvailable();
            entity.interactivePreviewAvailable = attempt.ownerFacingOutcome().interactivePreviewAvailable();
        }
        if (attempt.terminalOutcome() != null) {
            entity.terminalOutcomePresent = true;
            entity.terminalReason = attempt.terminalOutcome().reason();
            entity.terminalRecordedAt = attempt.terminalOutcome().recordedAt();
            entity.terminalMetadata = new LinkedHashMap<>(attempt.terminalOutcome().metadata());
        }
        entity.evaluation = evaluation;
        for (CapabilityOutcome outcome : attempt.capabilityOutcomes().values()) {
            entity.capabilityOutcomes.add(CapabilityOutcomeJpaEntity.fromDomain(entity, outcome));
        }
        return entity;
    }

    EvaluationAttempt toDomain(CanonicalEvaluationRequest request) {
        TerminalOutcome terminalOutcome = terminalOutcomePresent
                ? new TerminalOutcome(state, terminalReason, terminalMetadata, terminalRecordedAt)
                : null;
        OwnerFacingOutcome ownerFacingOutcome = ownerFacingOutcomePresent
                ? new OwnerFacingOutcome(analysisReportAvailable, interactivePreviewAvailable)
                : null;
        Map<String, CapabilityOutcome> outcomes = new LinkedHashMap<>();
        for (CapabilityOutcomeJpaEntity outcome : capabilityOutcomes) {
            CapabilityOutcome domainOutcome = outcome.toDomain();
            outcomes.put(domainOutcome.capability(), domainOutcome);
        }
        return EvaluationAttempt.rehydrate(
                id,
                attemptNumber,
                request,
                createdAt,
                startedAt,
                state,
                terminalOutcome,
                ownerFacingOutcome,
                outcomes,
                Clock.systemUTC()
        );
    }
}
