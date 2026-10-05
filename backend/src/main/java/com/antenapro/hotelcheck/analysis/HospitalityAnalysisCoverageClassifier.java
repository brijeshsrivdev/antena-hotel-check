package com.antenapro.hotelcheck.analysis;

import java.util.Objects;
import java.util.Set;

/**
 * Applies the governed REQ-033 coverage calibration to one evaluation's
 * pre-classification coverage assessment.
 *
 * <p>This classifier consumes only governed coverage facts. It does not inspect
 * evidence, findings, pages, recommendations, scores, or hotel content, and it
 * has no external or mutable state.</p>
 */
public final class HospitalityAnalysisCoverageClassifier {

    private static final int SUBSTANTIAL_DIMENSION_THRESHOLD = 6;
    private static final int SUBSTANTIAL_ASSESSABLE_STAGE_THRESHOLD = 3;

    private static final Set<GuestJourneyStage> ALL_JOURNEY_STAGES =
            Set.of(GuestJourneyStage.values());

    public HospitalityAnalysisCoverageState classify(HospitalityCoverageAssessment assessment) {
        Objects.requireNonNull(assessment, "assessment must not be null");

        Set<GuestJourneyStage> coveredJourneyStages = assessment.coveredJourneyStages();
        Set<HospitalityAnalysisDimension> coveredDimensions = assessment.coveredDimensions();

        boolean substantial = coveredJourneyStages.containsAll(ALL_JOURNEY_STAGES)
                && coveredDimensions.size() >= SUBSTANTIAL_DIMENSION_THRESHOLD
                && assessment.assessableJourneyStages().size() >= SUBSTANTIAL_ASSESSABLE_STAGE_THRESHOLD;

        if (substantial) {
            return HospitalityAnalysisCoverageState.SUBSTANTIALLY_ASSESSED;
        }

        if (!coveredJourneyStages.isEmpty() || !coveredDimensions.isEmpty()) {
            return HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED;
        }

        return HospitalityAnalysisCoverageState.INSUFFICIENT_COVERAGE;
    }
}
