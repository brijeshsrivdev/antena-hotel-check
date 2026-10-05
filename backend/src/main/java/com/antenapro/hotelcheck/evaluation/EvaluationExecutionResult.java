package com.antenapro.hotelcheck.evaluation;

import com.antenapro.hotelcheck.analysis.GuestJourneyAnalysis;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisReport;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisResult;
import com.antenapro.hotelcheck.analysis.HospitalityRecommendation;
import com.antenapro.hotelcheck.evidence.StructuredEvidence;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable result of one governed evaluation execution.
 *
 * <p>The result retains the existing evaluation lifecycle objects and downstream
 * governed artifacts without introducing new analysis semantics.</p>
 */
public record EvaluationExecutionResult(
        Evaluation evaluation,
        EvaluationAttempt attempt,
        EvaluationAcquisitionResult acquisition,
        StructuredEvidence evidence,
        HospitalityAnalysisResult analysisResult,
        GuestJourneyAnalysis guestJourneyAnalysis,
        Set<HospitalityRecommendation> recommendations,
        HospitalityAnalysisReport report
) {
    public EvaluationExecutionResult {
        Objects.requireNonNull(evaluation, "evaluation must not be null");
        Objects.requireNonNull(attempt, "attempt must not be null");
        Objects.requireNonNull(acquisition, "acquisition must not be null");
        Objects.requireNonNull(evidence, "evidence must not be null");
        Objects.requireNonNull(analysisResult, "analysisResult must not be null");
        Objects.requireNonNull(guestJourneyAnalysis, "guestJourneyAnalysis must not be null");
        Objects.requireNonNull(recommendations, "recommendations must not be null");
        Objects.requireNonNull(report, "report must not be null");

        UUID evaluationId = evaluation.evaluationId();
        if (!attempt.request().equals(evaluation.request())) {
            throw new IllegalArgumentException("attempt must belong to evaluation request");
        }
        if (acquisition.evaluation() != evaluation || acquisition.attempt() != attempt) {
            throw new IllegalArgumentException("acquisition must retain the execution evaluation and attempt");
        }
        if (!evaluationId.equals(evidence.evaluationId())) {
            throw new IllegalArgumentException("evidence must belong to evaluation");
        }
        if (!attempt.attemptId().equals(evidence.attemptId())
                || attempt.attemptNumber() != evidence.attemptNumber()) {
            throw new IllegalArgumentException("evidence must belong to execution attempt");
        }
        if (!evaluationId.equals(analysisResult.evaluationId())) {
            throw new IllegalArgumentException("analysisResult must belong to evaluation");
        }
        if (!evaluationId.equals(guestJourneyAnalysis.evaluationId())) {
            throw new IllegalArgumentException("guestJourneyAnalysis must belong to evaluation");
        }
        if (recommendations.stream().anyMatch(recommendation ->
                !evaluationId.equals(recommendation.evaluationId()))) {
            throw new IllegalArgumentException("recommendations must belong to evaluation");
        }
        if (!evaluationId.equals(report.evaluationId())) {
            throw new IllegalArgumentException("report must belong to evaluation");
        }

        recommendations = Set.copyOf(recommendations);
    }
}
