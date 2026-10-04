package com.antenapro.hotelcheck.analysis;

import com.antenapro.hotelcheck.evidence.StructuredEvidence;
import com.antenapro.hotelcheck.hospitality.HospitalityObservation;
import com.antenapro.hotelcheck.hospitality.HospitalityObservationCategory;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record HospitalityFinding(
        HospitalityAnalysisSignal originatingSignal,
        HospitalityFindingStatus status
) {
    public HospitalityFinding {
        Objects.requireNonNull(originatingSignal, "originatingSignal must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }

    public HospitalityObservationCategory category() {
        return observation().category();
    }

    public Set<GuestJourneyStage> journeyStages() {
        return originatingSignal.journeyStages();
    }

    public String findingText() {
        return originatingSignal.interpretation();
    }

    public HospitalityObservation observation() {
        return originatingSignal.originatingObservation();
    }

    public StructuredEvidence supportingEvidence() {
        return observation().supportingEvidence();
    }

    public UUID evaluationId() {
        return observation().evaluationId();
    }

    public UUID attemptId() {
        return observation().attemptId();
    }

    public int attemptNumber() {
        return observation().attemptNumber();
    }
}
