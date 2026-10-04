package com.antenapro.hotelcheck.analysis;

import com.antenapro.hotelcheck.evidence.StructuredEvidence;
import com.antenapro.hotelcheck.hospitality.HospitalityObservation;
import com.antenapro.hotelcheck.hospitality.HospitalityObservationCategory;

import java.util.Locale;
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

    /**
     * Identifies the deterministic cross-source hotel-identity conflict represented by
     * two retained identity findings. This uses typed observation/category data rather
     * than the human-readable finding interpretation.
     */
    public boolean isIdentityConflictWith(HospitalityFinding other) {
        Objects.requireNonNull(other, "other must not be null");
        if (category() != HospitalityObservationCategory.HOTEL_IDENTITY
                || other.category() != HospitalityObservationCategory.HOTEL_IDENTITY
                || !evaluationId().equals(other.evaluationId())
                || observation().sourceReference().equals(other.observation().sourceReference())) {
            return false;
        }

        String left = normalize(observation().observedValue());
        String right = normalize(other.observation().observedValue());
        return !left.equals(right) && !left.contains(right) && !right.contains(left);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
