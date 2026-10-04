package com.antenapro.hotelcheck.analysis;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Applies the governed REQ-033 coverage calibration to one evaluation's coverage.
 *
 * <p>This classifier consumes only the existing coverage representation. It does
 * not inspect evidence, findings, pages, recommendations, scores, or hotel
 * content, and it has no external or mutable state.</p>
 */
public final class HospitalityAnalysisCoverageClassifier {

    private static final int SUBSTANTIAL_DIMENSION_THRESHOLD = 6;
    private static final int SUBSTANTIAL_ASSESSABLE_STAGE_THRESHOLD = 3;

    private static final Set<GuestJourneyStage> ALL_JOURNEY_STAGES =
            Set.of(GuestJourneyStage.values());
    private static final Set<HospitalityAnalysisDimension> ALL_DIMENSIONS =
            Set.of(HospitalityAnalysisDimension.values());

    public HospitalityAnalysisCoverageState classify(HospitalityAnalysisCoverage coverage) {
        Objects.requireNonNull(coverage, "coverage must not be null");
        validateEvaluationIsolation(coverage);

        Set<GuestJourneyStage> coveredJourneyStages = EnumSet.noneOf(GuestJourneyStage.class);
        coveredJourneyStages.addAll(coverage.assessableJourneyStages());
        coveredJourneyStages.addAll(coverage.limitedJourneyStages());

        Set<HospitalityAnalysisDimension> coveredDimensions =
                EnumSet.noneOf(HospitalityAnalysisDimension.class);
        coveredDimensions.addAll(coverage.assessableDimensions());
        coveredDimensions.addAll(coverage.limitedDimensions());

        boolean substantial = coveredJourneyStages.containsAll(ALL_JOURNEY_STAGES)
                && coveredDimensions.stream().filter(ALL_DIMENSIONS::contains).count()
                >= SUBSTANTIAL_DIMENSION_THRESHOLD
                && coverage.assessableJourneyStages().stream()
                .filter(ALL_JOURNEY_STAGES::contains)
                .count() >= SUBSTANTIAL_ASSESSABLE_STAGE_THRESHOLD;

        if (substantial) {
            return HospitalityAnalysisCoverageState.SUBSTANTIALLY_ASSESSED;
        }

        if (!coveredJourneyStages.isEmpty() || !coveredDimensions.isEmpty()) {
            return HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED;
        }

        return HospitalityAnalysisCoverageState.INSUFFICIENT_COVERAGE;
    }

    private static void validateEvaluationIsolation(HospitalityAnalysisCoverage coverage) {
        UUID evaluationId = coverage.evaluationId();

        if (coverage.supportingFindings().stream()
                .anyMatch(finding -> !evaluationId.equals(finding.evaluationId()))) {
            throw new IllegalArgumentException("supportingFindings must belong to evaluationId");
        }
        if (coverage.supportingLimitations().stream()
                .anyMatch(limitation -> !evaluationId.equals(limitation.evaluationId()))) {
            throw new IllegalArgumentException("supportingLimitations must belong to evaluationId");
        }
    }
}
