package com.antenapro.hotelcheck.evaluation;

import com.antenapro.hotelcheck.acquisition.AcquisitionResult;

import java.util.Objects;

public record EvaluationAcquisitionResult(
        Evaluation evaluation,
        EvaluationAttempt attempt,
        AcquisitionResult acquisitionResult,
        CapabilityOutcome capabilityOutcome
) {
    public EvaluationAcquisitionResult {
        Objects.requireNonNull(evaluation, "evaluation must not be null");
        Objects.requireNonNull(attempt, "attempt must not be null");
        Objects.requireNonNull(acquisitionResult, "acquisitionResult must not be null");
        Objects.requireNonNull(capabilityOutcome, "capabilityOutcome must not be null");
    }
}
