package com.antenapro.hotelcheck.evaluation;

import com.antenapro.hotelcheck.acquisition.AcquisitionMethod;
import com.antenapro.hotelcheck.acquisition.AcquisitionOutcome;
import com.antenapro.hotelcheck.acquisition.AcquisitionResult;
import com.antenapro.hotelcheck.analysis.GuestJourneyAnalysis;
import com.antenapro.hotelcheck.analysis.GuestJourneyAnalysisService;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisCoverageClassifier;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisCoverageState;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisReport;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisReportService;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisResult;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisService;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisSignalService;
import com.antenapro.hotelcheck.analysis.HospitalityCoverageAssessment;
import com.antenapro.hotelcheck.analysis.HospitalityCoverageAssessmentDerivationService;
import com.antenapro.hotelcheck.analysis.HospitalityFindingService;
import com.antenapro.hotelcheck.analysis.HospitalityRecommendationService;
import com.antenapro.hotelcheck.evidence.EvidenceNormalizationService;
import com.antenapro.hotelcheck.evidence.StructuredEvidence;
import com.antenapro.hotelcheck.hospitality.HospitalityObservationService;
import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;
import com.antenapro.hotelcheck.input.HotelEvaluationInput;
import com.antenapro.hotelcheck.input.HotelEvaluationInputValidator;
import com.antenapro.hotelcheck.input.ValidationResult;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.InOrder;

class EvaluationExecutionOrchestratorTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-05T00:00:00Z"), ZoneOffset.UTC);
    private static final String WEBSITE = "https://hotel.example.com";

    private final HotelEvaluationInputValidator inputValidator = new HotelEvaluationInputValidator();

    @Test
    void executesTheGovernedPipelineInRequiredOrderAndReturnsOneEvaluationAttributedResult() {
        CanonicalEvaluationRequest request = request();
        Evaluation evaluation = Evaluation.create(request, CLOCK);
        evaluation.start();
        EvaluationAttempt attempt = evaluation.currentAttempt();

        EvaluationAcquisitionIntegrationService acquisitionIntegration = mock(EvaluationAcquisitionIntegrationService.class);
        EvidenceNormalizationService evidenceNormalization = mock(EvidenceNormalizationService.class);
        HospitalityObservationService observationService = mock(HospitalityObservationService.class);
        HospitalityAnalysisSignalService signalService = mock(HospitalityAnalysisSignalService.class);
        HospitalityFindingService findingService = mock(HospitalityFindingService.class);
        HospitalityCoverageAssessmentDerivationService derivationService = mock(HospitalityCoverageAssessmentDerivationService.class);
        HospitalityAnalysisCoverageClassifier classifier = mock(HospitalityAnalysisCoverageClassifier.class);
        HospitalityAnalysisService analysisService = mock(HospitalityAnalysisService.class);
        GuestJourneyAnalysisService journeyService = mock(GuestJourneyAnalysisService.class);
        HospitalityRecommendationService recommendationService = mock(HospitalityRecommendationService.class);
        HospitalityAnalysisReportService reportService = mock(HospitalityAnalysisReportService.class);

        EvaluationAcquisitionResult acquisition = new EvaluationAcquisitionResult(
                evaluation,
                attempt,
                acquisitionResult(),
                CapabilityOutcome.record(
                        "public-web-acquisition",
                        CapabilityOutcomeStatus.SUCCEEDED,
                        "SUCCESS",
                        CLOCK.instant())
        );
        StructuredEvidence evidence = mock(StructuredEvidence.class);
        HospitalityCoverageAssessment assessment = mock(HospitalityCoverageAssessment.class);
        HospitalityAnalysisResult analysisResult = mock(HospitalityAnalysisResult.class);
        GuestJourneyAnalysis journeyAnalysis = mock(GuestJourneyAnalysis.class);
        HospitalityAnalysisReport report = mock(HospitalityAnalysisReport.class);

        when(acquisitionIntegration.execute(request)).thenReturn(acquisition);
        when(evidenceNormalization.normalize(acquisition)).thenReturn(evidence);
        when(observationService.observe(evidence)).thenReturn(List.of());
        when(derivationService.derive(eq(evaluation.evaluationId()), any(), any(), any())).thenReturn(assessment);
        when(classifier.classify(assessment)).thenReturn(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED);
        when(analysisService.analyze(eq(evaluation.evaluationId()), eq(List.of(evidence)),
                eq(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED))).thenReturn(analysisResult);
        when(journeyService.analyze(analysisResult)).thenReturn(journeyAnalysis);
        when(recommendationService.recommend(journeyAnalysis)).thenReturn(Set.of());
        when(reportService.assemble(analysisResult, journeyAnalysis, Set.of(), request.evaluationTarget()))
                .thenReturn(report);
        when(evidence.evaluationId()).thenReturn(evaluation.evaluationId());
        when(evidence.attemptId()).thenReturn(attempt.attemptId());
        when(evidence.attemptNumber()).thenReturn(attempt.attemptNumber());
        when(analysisResult.evaluationId()).thenReturn(evaluation.evaluationId());
        when(journeyAnalysis.evaluationId()).thenReturn(evaluation.evaluationId());
        when(report.evaluationId()).thenReturn(evaluation.evaluationId());

        EvaluationExecutionResult result = orchestrator(
                acquisitionIntegration,
                evidenceNormalization,
                observationService,
                signalService,
                findingService,
                derivationService,
                classifier,
                analysisService,
                journeyService,
                recommendationService,
                reportService
        ).execute(request);

        assertSame(evaluation, result.evaluation());
        assertSame(attempt, result.attempt());
        assertSame(report, result.report());
        assertEquals(EvaluationLifecycleState.INCOMPLETE, result.attempt().state());
        assertEquals(true, result.attempt().ownerFacingOutcome().analysisReportAvailable());
        assertEquals(false, result.attempt().ownerFacingOutcome().interactivePreviewAvailable());

        InOrder order = inOrder(
                acquisitionIntegration,
                evidenceNormalization,
                observationService,
                derivationService,
                classifier,
                analysisService,
                journeyService,
                recommendationService,
                reportService
        );
        order.verify(acquisitionIntegration).execute(request);
        order.verify(evidenceNormalization).normalize(acquisition);
        order.verify(observationService).observe(evidence);
        order.verify(derivationService).derive(eq(evaluation.evaluationId()), any(), any(), any());
        order.verify(classifier).classify(assessment);
        order.verify(analysisService).analyze(
                eq(evaluation.evaluationId()),
                eq(List.of(evidence)),
                eq(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED));
        order.verify(journeyService).analyze(analysisResult);
        order.verify(recommendationService).recommend(journeyAnalysis);
        order.verify(reportService).assemble(analysisResult, journeyAnalysis, Set.of(), request.evaluationTarget());
    }

    @Test
    void passesTheExactAssessmentToTheClassifierAndExactClassifierStateToAnalysis() {
        CanonicalEvaluationRequest request = request();
        Fixture fixture = fixture(request);
        HospitalityCoverageAssessment assessment = mock(HospitalityCoverageAssessment.class);
        HospitalityAnalysisResult analysisResult = mock(HospitalityAnalysisResult.class);
        GuestJourneyAnalysis journeyAnalysis = mock(GuestJourneyAnalysis.class);
        HospitalityAnalysisReport report = mock(HospitalityAnalysisReport.class);

        when(fixture.derivationService.derive(eq(fixture.evaluation.evaluationId()), any(), any(), any()))
                .thenReturn(assessment);
        when(fixture.classifier.classify(assessment)).thenReturn(HospitalityAnalysisCoverageState.SUBSTANTIALLY_ASSESSED);
        when(fixture.analysisService.analyze(eq(fixture.evaluation.evaluationId()), any(),
                eq(HospitalityAnalysisCoverageState.SUBSTANTIALLY_ASSESSED))).thenReturn(analysisResult);
        when(fixture.journeyService.analyze(analysisResult)).thenReturn(journeyAnalysis);
        when(fixture.recommendationService.recommend(journeyAnalysis)).thenReturn(Set.of());
        when(fixture.reportService.assemble(any(), any(), eq(Set.of()), eq(request.evaluationTarget()))).thenReturn(report);
        stubResultIdentity(fixture, analysisResult, journeyAnalysis, report);

        fixture.orchestrator.execute(request);

        verify(fixture.classifier).classify(assessment);
        verify(fixture.analysisService).analyze(
                eq(fixture.evaluation.evaluationId()),
                eq(List.of(fixture.evidence)),
                eq(HospitalityAnalysisCoverageState.SUBSTANTIALLY_ASSESSED));
    }

    @Test
    void propagatesTheSameEvaluationIdentityAcrossEvidenceDerivationAnalysisAndFinalReport() {
        CanonicalEvaluationRequest request = request();
        Fixture fixture = fixture(request);
        HospitalityCoverageAssessment assessment = mock(HospitalityCoverageAssessment.class);
        HospitalityAnalysisResult analysisResult = mock(HospitalityAnalysisResult.class);
        GuestJourneyAnalysis journeyAnalysis = mock(GuestJourneyAnalysis.class);
        HospitalityAnalysisReport report = mock(HospitalityAnalysisReport.class);

        when(fixture.derivationService.derive(eq(fixture.evaluation.evaluationId()), any(), any(), any()))
                .thenReturn(assessment);
        when(fixture.classifier.classify(assessment)).thenReturn(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED);
        when(fixture.analysisService.analyze(eq(fixture.evaluation.evaluationId()), any(), any())).thenReturn(analysisResult);
        when(fixture.journeyService.analyze(analysisResult)).thenReturn(journeyAnalysis);
        when(fixture.recommendationService.recommend(journeyAnalysis)).thenReturn(Set.of());
        when(fixture.reportService.assemble(any(), any(), any(), any())).thenReturn(report);
        stubResultIdentity(fixture, analysisResult, journeyAnalysis, report);

        fixture.orchestrator.execute(request);

        verify(fixture.derivationService).derive(eq(fixture.evaluation.evaluationId()), any(), any(), any());
        verify(fixture.analysisService).analyze(eq(fixture.evaluation.evaluationId()), any(), any());
        verify(fixture.reportService).assemble(eq(analysisResult), eq(journeyAnalysis), eq(Set.of()), eq(request.evaluationTarget()));
    }

    @Test
    void marksMandatoryDownstreamFailureAsFailedAndDoesNotRunLaterStages() {
        CanonicalEvaluationRequest request = request();
        Fixture fixture = fixture(request);
        HospitalityCoverageAssessment assessment = mock(HospitalityCoverageAssessment.class);
        RuntimeException failure = new IllegalStateException("analysis failed");

        when(fixture.derivationService.derive(eq(fixture.evaluation.evaluationId()), any(), any(), any()))
                .thenReturn(assessment);
        when(fixture.classifier.classify(assessment)).thenReturn(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED);
        when(fixture.analysisService.analyze(eq(fixture.evaluation.evaluationId()), any(),
                eq(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED))).thenThrow(failure);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> fixture.orchestrator.execute(request));

        assertSame(failure, thrown);
        assertEquals(EvaluationLifecycleState.FAILED, fixture.attempt.state());
        assertEquals("deterministic hospitality analysis", fixture.attempt.terminalOutcome().metadata().get("stage"));
        verify(fixture.journeyService, never()).analyze(any());
        verify(fixture.recommendationService, never()).recommend(any());
        verify(fixture.reportService, never()).assemble(any(), any(), any(), any());
    }

    @Test
    void doesNotInventAcoverageStateWhenClassificationHasNotRun() {
        CanonicalEvaluationRequest request = request();
        Fixture fixture = fixture(request);
        HospitalityCoverageAssessment assessment = mock(HospitalityCoverageAssessment.class);
        when(fixture.derivationService.derive(eq(fixture.evaluation.evaluationId()), any(), any(), any()))
                .thenReturn(assessment);
        when(fixture.classifier.classify(assessment)).thenThrow(new IllegalStateException("classification unavailable"));

        assertThrows(IllegalStateException.class, () -> fixture.orchestrator.execute(request));

        verify(fixture.analysisService, never()).analyze(any(), any(), any());
    }

    private void stubResultIdentity(
            Fixture fixture,
            HospitalityAnalysisResult analysisResult,
            GuestJourneyAnalysis journeyAnalysis,
            HospitalityAnalysisReport report
    ) {
        when(fixture.evidence.evaluationId()).thenReturn(fixture.evaluation.evaluationId());
        when(fixture.evidence.attemptId()).thenReturn(fixture.attempt.attemptId());
        when(fixture.evidence.attemptNumber()).thenReturn(fixture.attempt.attemptNumber());
        when(analysisResult.evaluationId()).thenReturn(fixture.evaluation.evaluationId());
        when(journeyAnalysis.evaluationId()).thenReturn(fixture.evaluation.evaluationId());
        when(report.evaluationId()).thenReturn(fixture.evaluation.evaluationId());
    }

    private Fixture fixture(CanonicalEvaluationRequest request) {
        Evaluation evaluation = Evaluation.create(request, CLOCK);
        evaluation.start();
        EvaluationAttempt attempt = evaluation.currentAttempt();

        EvaluationAcquisitionIntegrationService acquisitionIntegration = mock(EvaluationAcquisitionIntegrationService.class);
        EvidenceNormalizationService evidenceNormalization = mock(EvidenceNormalizationService.class);
        HospitalityObservationService observationService = mock(HospitalityObservationService.class);
        HospitalityAnalysisSignalService signalService = mock(HospitalityAnalysisSignalService.class);
        HospitalityFindingService findingService = mock(HospitalityFindingService.class);
        HospitalityCoverageAssessmentDerivationService derivationService = mock(HospitalityCoverageAssessmentDerivationService.class);
        HospitalityAnalysisCoverageClassifier classifier = mock(HospitalityAnalysisCoverageClassifier.class);
        HospitalityAnalysisService analysisService = mock(HospitalityAnalysisService.class);
        GuestJourneyAnalysisService journeyService = mock(GuestJourneyAnalysisService.class);
        HospitalityRecommendationService recommendationService = mock(HospitalityRecommendationService.class);
        HospitalityAnalysisReportService reportService = mock(HospitalityAnalysisReportService.class);

        EvaluationAcquisitionResult acquisition = new EvaluationAcquisitionResult(
                evaluation,
                attempt,
                acquisitionResult(),
                CapabilityOutcome.record("public-web-acquisition", CapabilityOutcomeStatus.SUCCEEDED, "SUCCESS", CLOCK.instant())
        );
        StructuredEvidence evidence = mock(StructuredEvidence.class);

        when(acquisitionIntegration.execute(request)).thenReturn(acquisition);
        when(evidenceNormalization.normalize(acquisition)).thenReturn(evidence);
        when(observationService.observe(evidence)).thenReturn(List.of());
        when(recommendationService.recommend(any())).thenReturn(Set.of());

        return new Fixture(
                new EvaluationExecutionOrchestrator(
                        acquisitionIntegration,
                        evidenceNormalization,
                        observationService,
                        signalService,
                        findingService,
                        derivationService,
                        classifier,
                        analysisService,
                        journeyService,
                        recommendationService,
                        reportService
                ),
                evaluation,
                attempt,
                evidence,
                derivationService,
                classifier,
                analysisService,
                journeyService,
                recommendationService,
                reportService
        );
    }

    private EvaluationExecutionOrchestrator orchestrator(
            EvaluationAcquisitionIntegrationService acquisitionIntegration,
            EvidenceNormalizationService evidenceNormalization,
            HospitalityObservationService observationService,
            HospitalityAnalysisSignalService signalService,
            HospitalityFindingService findingService,
            HospitalityCoverageAssessmentDerivationService derivationService,
            HospitalityAnalysisCoverageClassifier classifier,
            HospitalityAnalysisService analysisService,
            GuestJourneyAnalysisService journeyService,
            HospitalityRecommendationService recommendationService,
            HospitalityAnalysisReportService reportService
    ) {
        return new EvaluationExecutionOrchestrator(
                acquisitionIntegration,
                evidenceNormalization,
                observationService,
                signalService,
                findingService,
                derivationService,
                classifier,
                analysisService,
                journeyService,
                recommendationService,
                reportService
        );
    }

    private CanonicalEvaluationRequest request() {
        ValidationResult result = inputValidator.validate(new HotelEvaluationInput(null, null, WEBSITE));
        return ((ValidationResult.Accepted) result).request();
    }

    private AcquisitionResult acquisitionResult() {
        return new AcquisitionResult(
                AcquisitionOutcome.SUCCESS,
                WEBSITE,
                WEBSITE,
                200,
                "text/html",
                CLOCK.instant(),
                AcquisitionMethod.HTTP_PUBLIC,
                "<html>hotel</html>",
                java.util.Map.of(),
                List.of(),
                null
        );
    }

    private record Fixture(
            EvaluationExecutionOrchestrator orchestrator,
            Evaluation evaluation,
            EvaluationAttempt attempt,
            StructuredEvidence evidence,
            HospitalityCoverageAssessmentDerivationService derivationService,
            HospitalityAnalysisCoverageClassifier classifier,
            HospitalityAnalysisService analysisService,
            GuestJourneyAnalysisService journeyService,
            HospitalityRecommendationService recommendationService,
            HospitalityAnalysisReportService reportService
    ) {
    }
}
