package com.antenapro.hotelcheck.analysis;

import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Immutable structured hospitality analysis report over one existing evaluation boundary.
 *
 * <p>The report retains existing analysis and journey objects rather than copying their
 * evidence or provenance. It contains no presentation, scoring, prioritization, or
 * recommendation-generation semantics.</p>
 */
public record HospitalityAnalysisReport(
        UUID evaluationId,
        CanonicalEvaluationRequest.EvaluationTarget hotelTarget,
        HospitalityAnalysisResult analysisResult,
        GuestJourneyAnalysis guestJourneyAnalysis,
        Set<HospitalityRecommendation> recommendations,
        HospitalityAnalysisReportSummary executiveSummary
) {
    public HospitalityAnalysisReport {
        Objects.requireNonNull(evaluationId, "evaluationId must not be null");
        Objects.requireNonNull(analysisResult, "analysisResult must not be null");
        Objects.requireNonNull(guestJourneyAnalysis, "guestJourneyAnalysis must not be null");
        Objects.requireNonNull(recommendations, "recommendations must not be null");
        Objects.requireNonNull(executiveSummary, "executiveSummary must not be null");

        if (!evaluationId.equals(analysisResult.evaluationId())) {
            throw new IllegalArgumentException("analysisResult must belong to evaluationId");
        }
        if (!evaluationId.equals(guestJourneyAnalysis.evaluationId())) {
            throw new IllegalArgumentException("guestJourneyAnalysis must belong to evaluationId");
        }
        if (recommendations.stream().anyMatch(recommendation ->
                !evaluationId.equals(recommendation.evaluationId()))) {
            throw new IllegalArgumentException("recommendations must belong to evaluationId");
        }

        recommendations = Set.copyOf(recommendations);
    }

    /**
     * Returns only governed deficiency findings. Ordinary observations are not silently
     * promoted to strengths or problems.
     */
    public Set<HospitalityFinding> deficiencies() {
        return analysisResult.findings().stream()
                .filter(finding -> finding.kind() == HospitalityFindingKind.DEFICIENCY)
                .collect(Collectors.toUnmodifiableSet());
    }

    public HospitalityAnalysisCoverage coverage() {
        return analysisResult.coverage();
    }

    public Set<HospitalityAnalysisLimitation> limitations() {
        return analysisResult.limitations();
    }

    /**
     * No positive/verified strength contract currently exists in the governed analysis
     * model, so strengths are intentionally not synthesized by REQ-031.
     */
    public Set<HospitalityFinding> strengths() {
        return Set.of();
    }
}
