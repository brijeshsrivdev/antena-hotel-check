package com.antenapro.hotelcheck.analysis;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record HospitalityRecommendation(
        UUID evaluationId,
        RecommendationCategory category,
        String action,
        Set<GuestJourneyStage> journeyStages,
        HospitalityFinding sourceFinding,
        HospitalityAnalysisLimitation sourceLimitation
) {
    public HospitalityRecommendation {
        Objects.requireNonNull(evaluationId, "evaluationId must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(action, "action must not be null");
        if (action.isBlank()) {
            throw new IllegalArgumentException("action must not be blank");
        }
        Objects.requireNonNull(journeyStages, "journeyStages must not be null");
        journeyStages = Set.copyOf(journeyStages);
        if (journeyStages.isEmpty()) {
            throw new IllegalArgumentException("journeyStages must not be empty");
        }
        if ((sourceFinding == null) == (sourceLimitation == null)) {
            throw new IllegalArgumentException("exactly one source finding or source limitation is required");
        }
        if (sourceFinding != null && !evaluationId.equals(sourceFinding.evaluationId())) {
            throw new IllegalArgumentException("source finding must belong to evaluationId");
        }
        if (sourceLimitation != null && !evaluationId.equals(sourceLimitation.evaluationId())) {
            throw new IllegalArgumentException("source limitation must belong to evaluationId");
        }
    }

    public static HospitalityRecommendation fromFinding(
            HospitalityFinding finding,
            RecommendationCategory category,
            String action,
            Set<GuestJourneyStage> journeyStages
    ) {
        Objects.requireNonNull(finding, "finding must not be null");
        return new HospitalityRecommendation(
                finding.evaluationId(), category, action, journeyStages, finding, null);
    }
}
