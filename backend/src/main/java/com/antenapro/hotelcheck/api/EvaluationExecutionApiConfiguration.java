package com.antenapro.hotelcheck.api;

import com.antenapro.hotelcheck.acquisition.HttpPublicWebAcquisitionService;
import com.antenapro.hotelcheck.analysis.GuestJourneyAnalysisService;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisCoverageClassifier;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisReportService;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisService;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisSignalService;
import com.antenapro.hotelcheck.analysis.HospitalityCoverageAssessmentDerivationService;
import com.antenapro.hotelcheck.analysis.HospitalityFindingService;
import com.antenapro.hotelcheck.analysis.HospitalityRecommendationService;
import com.antenapro.hotelcheck.evidence.EvidenceNormalizationService;
import com.antenapro.hotelcheck.evaluation.EvaluationAcquisitionIntegrationService;
import com.antenapro.hotelcheck.evaluation.EvaluationExecutionOrchestrator;
import com.antenapro.hotelcheck.hospitality.HospitalityObservationService;
import com.antenapro.hotelcheck.input.HotelEvaluationInputValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the existing evaluation pipeline into the HTTP boundary without moving
 * domain semantics into the API layer.
 */
@Configuration
public class EvaluationExecutionApiConfiguration {

    @Bean
    HotelEvaluationInputValidator hotelEvaluationInputValidator() {
        return new HotelEvaluationInputValidator();
    }

    @Bean
    EvaluationExecutionOrchestrator evaluationExecutionOrchestrator() {
        return new EvaluationExecutionOrchestrator(
                new EvaluationAcquisitionIntegrationService(new HttpPublicWebAcquisitionService()),
                new EvidenceNormalizationService(),
                new HospitalityObservationService(),
                new HospitalityAnalysisSignalService(),
                new HospitalityFindingService(),
                new HospitalityCoverageAssessmentDerivationService(),
                new HospitalityAnalysisCoverageClassifier(),
                new HospitalityAnalysisService(),
                new GuestJourneyAnalysisService(),
                new HospitalityRecommendationService(),
                new HospitalityAnalysisReportService()
        );
    }
}
