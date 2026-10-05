package com.antenapro.hotelcheck.api;

import com.antenapro.hotelcheck.analysis.GuestJourneyAnalysis;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisReport;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisResult;
import com.antenapro.hotelcheck.analysis.HospitalityRecommendation;
import com.antenapro.hotelcheck.evidence.StructuredEvidence;
import com.antenapro.hotelcheck.evaluation.Evaluation;
import com.antenapro.hotelcheck.evaluation.EvaluationAcquisitionResult;
import com.antenapro.hotelcheck.evaluation.EvaluationAttempt;
import com.antenapro.hotelcheck.evaluation.EvaluationExecutionResult;

import java.util.Set;

/**
 * Thin transport representation of the governed execution result.
 * Domain semantics remain owned by EvaluationExecutionResult and its artifacts.
 */
public record EvaluationExecutionResponse(
        Evaluation evaluation,
        EvaluationAttempt attempt,
        EvaluationAcquisitionResult acquisition,
        StructuredEvidence evidence,
        HospitalityAnalysisResult analysisResult,
        GuestJourneyAnalysis guestJourneyAnalysis,
        Set<HospitalityRecommendation> recommendations,
        HospitalityAnalysisReport report
) {
    public static EvaluationExecutionResponse from(EvaluationExecutionResult result) {
        return new EvaluationExecutionResponse(
                result.evaluation(),
                result.attempt(),
                result.acquisition(),
                result.evidence(),
                result.analysisResult(),
                result.guestJourneyAnalysis(),
                result.recommendations(),
                result.report()
        );
    }
}
