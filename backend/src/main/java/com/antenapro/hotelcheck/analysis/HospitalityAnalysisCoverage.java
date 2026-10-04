package com.antenapro.hotelcheck.analysis;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record HospitalityAnalysisCoverage(
        UUID evaluationId,
        Set<GuestJourneyStage> intendedJourneyStages,
        Set<HospitalityAnalysisDimension> intendedDimensions,
        Set<GuestJourneyStage> assessableJourneyStages,
        Set<HospitalityAnalysisDimension> assessableDimensions,
        Set<GuestJourneyStage> limitedJourneyStages,
        Set<HospitalityAnalysisDimension> limitedDimensions,
        Set<HospitalityFinding> supportingFindings,
        Set<HospitalityAnalysisLimitation> supportingLimitations,
        HospitalityAnalysisCoverageState state,
        String rationale
) {
    public HospitalityAnalysisCoverage {
        Objects.requireNonNull(evaluationId, "evaluationId must not be null");
        Objects.requireNonNull(intendedJourneyStages, "intendedJourneyStages must not be null");
        Objects.requireNonNull(intendedDimensions, "intendedDimensions must not be null");
        Objects.requireNonNull(assessableJourneyStages, "assessableJourneyStages must not be null");
        Objects.requireNonNull(assessableDimensions, "assessableDimensions must not be null");
        Objects.requireNonNull(limitedJourneyStages, "limitedJourneyStages must not be null");
        Objects.requireNonNull(limitedDimensions, "limitedDimensions must not be null");
        Objects.requireNonNull(supportingFindings, "supportingFindings must not be null");
        Objects.requireNonNull(supportingLimitations, "supportingLimitations must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(rationale, "rationale must not be null");
        if (intendedJourneyStages.isEmpty()) {
            throw new IllegalArgumentException("intendedJourneyStages must not be empty");
        }
        if (intendedDimensions.isEmpty()) {
            throw new IllegalArgumentException("intendedDimensions must not be empty");
        }
        if (!intendedJourneyStages.containsAll(assessableJourneyStages)) {
            throw new IllegalArgumentException("assessableJourneyStages must be within intendedJourneyStages");
        }
        if (!intendedJourneyStages.containsAll(limitedJourneyStages)) {
            throw new IllegalArgumentException("limitedJourneyStages must be within intendedJourneyStages");
        }
        if (!intendedDimensions.containsAll(assessableDimensions)) {
            throw new IllegalArgumentException("assessableDimensions must be within intendedDimensions");
        }
        if (!intendedDimensions.containsAll(limitedDimensions)) {
            throw new IllegalArgumentException("limitedDimensions must be within intendedDimensions");
        }
        if (rationale.isBlank()) {
            throw new IllegalArgumentException("rationale must not be blank");
        }
        validateEvaluationTraceability(supportingFindings, supportingLimitations, evaluationId);
        intendedJourneyStages = Set.copyOf(intendedJourneyStages);
        intendedDimensions = Set.copyOf(intendedDimensions);
        assessableJourneyStages = Set.copyOf(assessableJourneyStages);
        assessableDimensions = Set.copyOf(assessableDimensions);
        limitedJourneyStages = Set.copyOf(limitedJourneyStages);
        limitedDimensions = Set.copyOf(limitedDimensions);
        supportingFindings = Set.copyOf(supportingFindings);
        supportingLimitations = Set.copyOf(supportingLimitations);
    }

    private static void validateEvaluationTraceability(
            Set<HospitalityFinding> findings,
            Set<HospitalityAnalysisLimitation> limitations,
            UUID evaluationId
    ) {
        if (findings.stream().anyMatch(finding -> !evaluationId.equals(finding.evaluationId()))) {
            throw new IllegalArgumentException("supportingFindings must belong to evaluationId");
        }
        if (limitations.stream().anyMatch(limitation -> !evaluationId.equals(limitation.evaluationId()))) {
            throw new IllegalArgumentException("supportingLimitations must belong to evaluationId");
        }
    }
}
