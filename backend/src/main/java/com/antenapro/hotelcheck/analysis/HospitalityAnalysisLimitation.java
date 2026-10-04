package com.antenapro.hotelcheck.analysis;

import com.antenapro.hotelcheck.acquisition.AcquisitionOutcome;
import com.antenapro.hotelcheck.evidence.StructuredEvidence;
import com.antenapro.hotelcheck.hospitality.HospitalityObservationCategory;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record HospitalityAnalysisLimitation(
        Set<HospitalityObservationCategory> categories,
        Set<GuestJourneyStage> journeyStages,
        HospitalityAnalysisLimitationType type,
        StructuredEvidence supportingEvidence,
        String explanation
) {
    public HospitalityAnalysisLimitation {
        Objects.requireNonNull(categories, "categories must not be null");
        Objects.requireNonNull(journeyStages, "journeyStages must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(supportingEvidence, "supportingEvidence must not be null");
        Objects.requireNonNull(explanation, "explanation must not be null");
        if (explanation.isBlank()) {
            throw new IllegalArgumentException("explanation must not be blank");
        }
        categories = Set.copyOf(categories);
        journeyStages = Set.copyOf(journeyStages);
    }

    public AcquisitionOutcome sourceCondition() {
        return supportingEvidence.acquisitionOutcome();
    }

    public UUID evaluationId() {
        return supportingEvidence.evaluationId();
    }

    public UUID attemptId() {
        return supportingEvidence.attemptId();
    }

    public int attemptNumber() {
        return supportingEvidence.attemptNumber();
    }

    public String requestedUrl() {
        return supportingEvidence.requestedUrl();
    }

    public String finalUrl() {
        return supportingEvidence.finalUrl();
    }

    public String sourceLimitation() {
        return supportingEvidence.limitation();
    }
}
