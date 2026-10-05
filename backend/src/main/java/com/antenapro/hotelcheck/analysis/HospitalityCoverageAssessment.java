package com.antenapro.hotelcheck.analysis;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable pre-classification coverage facts for one evaluation.
 *
 * <p>This value object deliberately contains no coverage classification state.
 * Covered scope is derived from assessable and limited scope.</p>
 */
public record HospitalityCoverageAssessment(
        UUID evaluationId,
        Set<GuestJourneyStage> intendedJourneyStages,
        Set<HospitalityAnalysisDimension> intendedDimensions,
        Set<GuestJourneyStage> assessableJourneyStages,
        Set<HospitalityAnalysisDimension> assessableDimensions,
        Set<GuestJourneyStage> limitedJourneyStages,
        Set<HospitalityAnalysisDimension> limitedDimensions
) {
    public HospitalityCoverageAssessment {
        Objects.requireNonNull(evaluationId, "evaluationId must not be null");
        Objects.requireNonNull(intendedJourneyStages, "intendedJourneyStages must not be null");
        Objects.requireNonNull(intendedDimensions, "intendedDimensions must not be null");
        Objects.requireNonNull(assessableJourneyStages, "assessableJourneyStages must not be null");
        Objects.requireNonNull(assessableDimensions, "assessableDimensions must not be null");
        Objects.requireNonNull(limitedJourneyStages, "limitedJourneyStages must not be null");
        Objects.requireNonNull(limitedDimensions, "limitedDimensions must not be null");

        validateDisjoint(
                assessableJourneyStages,
                limitedJourneyStages,
                "assessableJourneyStages and limitedJourneyStages must not overlap"
        );
        validateDisjoint(
                assessableDimensions,
                limitedDimensions,
                "assessableDimensions and limitedDimensions must not overlap"
        );
        validateContained(
                assessableJourneyStages,
                intendedJourneyStages,
                "assessableJourneyStages must be within intendedJourneyStages"
        );
        validateContained(
                limitedJourneyStages,
                intendedJourneyStages,
                "limitedJourneyStages must be within intendedJourneyStages"
        );
        validateContained(
                assessableDimensions,
                intendedDimensions,
                "assessableDimensions must be within intendedDimensions"
        );
        validateContained(
                limitedDimensions,
                intendedDimensions,
                "limitedDimensions must be within intendedDimensions"
        );

        intendedJourneyStages = Set.copyOf(intendedJourneyStages);
        intendedDimensions = Set.copyOf(intendedDimensions);
        assessableJourneyStages = Set.copyOf(assessableJourneyStages);
        assessableDimensions = Set.copyOf(assessableDimensions);
        limitedJourneyStages = Set.copyOf(limitedJourneyStages);
        limitedDimensions = Set.copyOf(limitedDimensions);
    }

    public Set<GuestJourneyStage> coveredJourneyStages() {
        EnumSet<GuestJourneyStage> covered = EnumSet.noneOf(GuestJourneyStage.class);
        covered.addAll(assessableJourneyStages);
        covered.addAll(limitedJourneyStages);
        return Set.copyOf(covered);
    }

    public Set<HospitalityAnalysisDimension> coveredDimensions() {
        EnumSet<HospitalityAnalysisDimension> covered = EnumSet.noneOf(HospitalityAnalysisDimension.class);
        covered.addAll(assessableDimensions);
        covered.addAll(limitedDimensions);
        return Set.copyOf(covered);
    }

    private static <T> void validateDisjoint(Set<T> first, Set<T> second, String message) {
        for (T value : first) {
            if (second.contains(value)) {
                throw new IllegalArgumentException(message);
            }
        }
    }

    private static <T> void validateContained(Set<T> scope, Set<T> intended, String message) {
        if (!intended.containsAll(scope)) {
            throw new IllegalArgumentException(message);
        }
    }
}
