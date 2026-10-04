package com.antenapro.hotelcheck.evidence;

import com.antenapro.hotelcheck.acquisition.AcquisitionOutcome;
import com.antenapro.hotelcheck.evaluation.EvaluationAcquisitionResult;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record StructuredEvidence(
        UUID evaluationId,
        UUID attemptId,
        int attemptNumber,
        EvaluationAcquisitionResult sourceObservation,
        EvidenceProvenance provenance,
        AcquisitionOutcome acquisitionOutcome,
        String requestedUrl,
        String finalUrl,
        Instant retrievalTimestamp,
        String acquisitionMethod,
        Integer statusCode,
        String contentType,
        String observedContent,
        String limitation
) {
    public StructuredEvidence {
        Objects.requireNonNull(evaluationId, "evaluationId must not be null");
        Objects.requireNonNull(attemptId, "attemptId must not be null");
        if (attemptNumber < 1) {
            throw new IllegalArgumentException("attemptNumber must be positive");
        }
        Objects.requireNonNull(sourceObservation, "sourceObservation must not be null");
        Objects.requireNonNull(provenance, "provenance must not be null");
        Objects.requireNonNull(acquisitionOutcome, "acquisitionOutcome must not be null");
    }

    public boolean hasObservedContent() {
        return observedContent != null;
    }

    public boolean representsSuccessfulObservation() {
        return acquisitionOutcome == AcquisitionOutcome.SUCCESS;
    }
}
