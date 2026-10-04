package com.antenapro.hotelcheck.analysis;

import java.util.Objects;
import java.util.Set;

/**
 * Deterministic executive-level report summary derived only from existing report components.
 * Counts are descriptive representations, not scores, priorities, or coverage measures.
 */
public record HospitalityAnalysisReportSummary(
        int deficiencyCount,
        int limitationCount,
        int recommendationCount,
        Set<GuestJourneyStage> stagesWithObservedImpact
) {
    public HospitalityAnalysisReportSummary {
        if (deficiencyCount < 0) {
            throw new IllegalArgumentException("deficiencyCount must not be negative");
        }
        if (limitationCount < 0) {
            throw new IllegalArgumentException("limitationCount must not be negative");
        }
        if (recommendationCount < 0) {
            throw new IllegalArgumentException("recommendationCount must not be negative");
        }
        Objects.requireNonNull(stagesWithObservedImpact, "stagesWithObservedImpact must not be null");
        stagesWithObservedImpact = Set.copyOf(stagesWithObservedImpact);
    }
}
