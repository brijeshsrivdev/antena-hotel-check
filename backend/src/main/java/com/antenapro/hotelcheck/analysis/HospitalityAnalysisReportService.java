package com.antenapro.hotelcheck.analysis;

import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Assembles a structured report from already-produced governed analysis outputs.
 * This service performs no analysis, discovery, recommendation generation, or network access.
 */
public final class HospitalityAnalysisReportService {

    public HospitalityAnalysisReport assemble(
            HospitalityAnalysisResult analysisResult,
            GuestJourneyAnalysis guestJourneyAnalysis,
            Set<HospitalityRecommendation> recommendations,
            CanonicalEvaluationRequest.EvaluationTarget hotelTarget
    ) {
        Objects.requireNonNull(analysisResult, "analysisResult must not be null");
        Objects.requireNonNull(guestJourneyAnalysis, "guestJourneyAnalysis must not be null");
        Objects.requireNonNull(recommendations, "recommendations must not be null");

        UUID evaluationId = analysisResult.evaluationId();
        if (!evaluationId.equals(guestJourneyAnalysis.evaluationId())) {
            throw new IllegalArgumentException("guestJourneyAnalysis must belong to analysisResult evaluationId");
        }
        if (recommendations.stream().anyMatch(recommendation ->
                !evaluationId.equals(recommendation.evaluationId()))) {
            throw new IllegalArgumentException("recommendations must belong to analysisResult evaluationId");
        }

        Set<HospitalityFinding> deficiencies = analysisResult.findings().stream()
                .filter(finding -> finding.kind() == HospitalityFindingKind.DEFICIENCY)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<GuestJourneyStage> stagesWithObservedImpact = guestJourneyAnalysis.stages().stream()
                .filter(stage -> stage.state() == GuestJourneyImpactState.OBSERVED_IMPACT)
                .map(GuestJourneyStageAnalysis::stage)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        HospitalityAnalysisReportSummary summary = new HospitalityAnalysisReportSummary(
                deficiencies.size(),
                analysisResult.limitations().size(),
                recommendations.size(),
                stagesWithObservedImpact
        );

        return new HospitalityAnalysisReport(
                evaluationId,
                hotelTarget,
                analysisResult,
                guestJourneyAnalysis,
                recommendations,
                summary
        );
    }
}
