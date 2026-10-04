package com.antenapro.hotelcheck.hospitality;

import com.antenapro.hotelcheck.evidence.EvidenceProvenance;
import com.antenapro.hotelcheck.evidence.StructuredEvidence;

import java.util.Objects;
import java.util.UUID;

public record HospitalityObservation(
        HospitalityObservationCategory category,
        String observedValue,
        String sourceReference,
        StructuredEvidence supportingEvidence,
        UUID evaluationId,
        UUID attemptId,
        int attemptNumber,
        EvidenceProvenance provenance
) {
    public HospitalityObservation {
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(observedValue, "observedValue must not be null");
        if (observedValue.isBlank()) {
            throw new IllegalArgumentException("observedValue must not be blank");
        }
        Objects.requireNonNull(sourceReference, "sourceReference must not be null");
        if (sourceReference.isBlank()) {
            throw new IllegalArgumentException("sourceReference must not be blank");
        }
        Objects.requireNonNull(supportingEvidence, "supportingEvidence must not be null");
        Objects.requireNonNull(evaluationId, "evaluationId must not be null");
        Objects.requireNonNull(attemptId, "attemptId must not be null");
        if (attemptNumber < 1) {
            throw new IllegalArgumentException("attemptNumber must be positive");
        }
        Objects.requireNonNull(provenance, "provenance must not be null");
    }
}
