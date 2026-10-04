package com.antenapro.hotelcheck.analysis;

import com.antenapro.hotelcheck.hospitality.HospitalityObservation;

import java.util.Objects;
import java.util.Set;

public record HospitalityAnalysisSignal(
        HospitalityObservation originatingObservation,
        Set<GuestJourneyStage> journeyStages,
        String interpretation,
        HospitalityAnalysisSignalStatus status
) {
    public HospitalityAnalysisSignal {
        Objects.requireNonNull(originatingObservation, "originatingObservation must not be null");
        Objects.requireNonNull(journeyStages, "journeyStages must not be null");
        if (journeyStages.isEmpty()) {
            throw new IllegalArgumentException("journeyStages must not be empty");
        }
        journeyStages = Set.copyOf(journeyStages);
        Objects.requireNonNull(interpretation, "interpretation must not be null");
        if (interpretation.isBlank()) {
            throw new IllegalArgumentException("interpretation must not be blank");
        }
        Objects.requireNonNull(status, "status must not be null");
    }
}
