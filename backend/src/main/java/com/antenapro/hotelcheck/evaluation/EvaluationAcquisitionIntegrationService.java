package com.antenapro.hotelcheck.evaluation;

import com.antenapro.hotelcheck.acquisition.AcquisitionResult;
import com.antenapro.hotelcheck.acquisition.PublicWebAcquisitionService;
import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class EvaluationAcquisitionIntegrationService {
    private static final String CAPABILITY = "public-web-acquisition";

    private final PublicWebAcquisitionService acquisitionService;
    private final Clock clock;

    public EvaluationAcquisitionIntegrationService(PublicWebAcquisitionService acquisitionService) {
        this(acquisitionService, Clock.systemUTC());
    }

    public EvaluationAcquisitionIntegrationService(PublicWebAcquisitionService acquisitionService, Clock clock) {
        this.acquisitionService = Objects.requireNonNull(acquisitionService, "acquisitionService must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public EvaluationAcquisitionResult execute(CanonicalEvaluationRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        Evaluation evaluation = Evaluation.create(request, clock);
        evaluation.start();
        EvaluationAttempt attempt = evaluation.currentAttempt();

        AcquisitionResult acquisitionResult = acquisitionService.acquire(request);
        CapabilityOutcome capabilityOutcome = toCapabilityOutcome(acquisitionResult);
        attempt.recordCapabilityOutcome(capabilityOutcome);

        return new EvaluationAcquisitionResult(evaluation, attempt, acquisitionResult, capabilityOutcome);
    }

    private CapabilityOutcome toCapabilityOutcome(AcquisitionResult acquisitionResult) {
        Instant recordedAt = acquisitionResult.retrievalTimestamp() != null
                ? acquisitionResult.retrievalTimestamp()
                : clock.instant();

        CapabilityOutcomeStatus status = acquisitionResult.isSuccess()
                ? CapabilityOutcomeStatus.SUCCEEDED
                : CapabilityOutcomeStatus.FAILED;

        String detail = acquisitionResult.outcome().name();
        if (acquisitionResult.errorMessage() != null && !acquisitionResult.errorMessage().isBlank()) {
            detail += ": " + acquisitionResult.errorMessage();
        }

        return CapabilityOutcome.record(CAPABILITY, status, detail, recordedAt);
    }
}
