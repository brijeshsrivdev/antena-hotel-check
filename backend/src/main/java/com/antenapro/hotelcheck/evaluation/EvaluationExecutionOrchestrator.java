package com.antenapro.hotelcheck.evaluation;

import com.antenapro.hotelcheck.analysis.GuestJourneyAnalysis;
import com.antenapro.hotelcheck.analysis.GuestJourneyAnalysisService;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisCoverageClassifier;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisCoverageState;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisReport;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisReportService;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisResult;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisService;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisSignal;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisSignalService;
import com.antenapro.hotelcheck.analysis.HospitalityAnalysisLimitation;
import com.antenapro.hotelcheck.analysis.HospitalityCoverageAssessment;
import com.antenapro.hotelcheck.analysis.HospitalityCoverageAssessmentDerivationService;
import com.antenapro.hotelcheck.analysis.HospitalityFinding;
import com.antenapro.hotelcheck.analysis.HospitalityFindingService;
import com.antenapro.hotelcheck.analysis.HospitalityRecommendation;
import com.antenapro.hotelcheck.analysis.HospitalityRecommendationService;
import com.antenapro.hotelcheck.evidence.EvidenceNormalizationService;
import com.antenapro.hotelcheck.evidence.StructuredEvidence;
import com.antenapro.hotelcheck.hospitality.HospitalityObservation;
import com.antenapro.hotelcheck.hospitality.HospitalityObservationService;
import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Coordinates one canonical evaluation through the existing governed analysis pipeline.
 *
 * <p>This class owns sequencing and evaluation identity propagation only. Domain rules
 * remain in the existing acquisition, evidence, observation, signal, finding,
 * coverage, analysis, journey, recommendation, and report services.</p>
 */
public final class EvaluationExecutionOrchestrator {

    private final EvaluationAcquisitionIntegrationService acquisitionIntegrationService;
    private final EvidenceNormalizationService evidenceNormalizationService;
    private final HospitalityObservationService observationService;
    private final HospitalityAnalysisSignalService signalService;
    private final HospitalityFindingService findingService;
    private final HospitalityCoverageAssessmentDerivationService coverageAssessmentDerivationService;
    private final HospitalityAnalysisCoverageClassifier coverageClassifier;
    private final HospitalityAnalysisService analysisService;
    private final GuestJourneyAnalysisService guestJourneyAnalysisService;
    private final HospitalityRecommendationService recommendationService;
    private final HospitalityAnalysisReportService reportService;

    public EvaluationExecutionOrchestrator(
            EvaluationAcquisitionIntegrationService acquisitionIntegrationService,
            EvidenceNormalizationService evidenceNormalizationService,
            HospitalityObservationService observationService,
            HospitalityAnalysisSignalService signalService,
            HospitalityFindingService findingService,
            HospitalityCoverageAssessmentDerivationService coverageAssessmentDerivationService,
            HospitalityAnalysisCoverageClassifier coverageClassifier,
            HospitalityAnalysisService analysisService,
            GuestJourneyAnalysisService guestJourneyAnalysisService,
            HospitalityRecommendationService recommendationService,
            HospitalityAnalysisReportService reportService
    ) {
        this.acquisitionIntegrationService = Objects.requireNonNull(
                acquisitionIntegrationService, "acquisitionIntegrationService must not be null");
        this.evidenceNormalizationService = Objects.requireNonNull(
                evidenceNormalizationService, "evidenceNormalizationService must not be null");
        this.observationService = Objects.requireNonNull(observationService, "observationService must not be null");
        this.signalService = Objects.requireNonNull(signalService, "signalService must not be null");
        this.findingService = Objects.requireNonNull(findingService, "findingService must not be null");
        this.coverageAssessmentDerivationService = Objects.requireNonNull(
                coverageAssessmentDerivationService, "coverageAssessmentDerivationService must not be null");
        this.coverageClassifier = Objects.requireNonNull(coverageClassifier, "coverageClassifier must not be null");
        this.analysisService = Objects.requireNonNull(analysisService, "analysisService must not be null");
        this.guestJourneyAnalysisService = Objects.requireNonNull(
                guestJourneyAnalysisService, "guestJourneyAnalysisService must not be null");
        this.recommendationService = Objects.requireNonNull(
                recommendationService, "recommendationService must not be null");
        this.reportService = Objects.requireNonNull(reportService, "reportService must not be null");
    }

    public EvaluationExecutionResult execute(CanonicalEvaluationRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String stage = "acquisition";
        EvaluationAcquisitionResult acquisition = null;
        try {
            acquisition = acquisitionIntegrationService.execute(request);

            stage = "evidence normalization";
            StructuredEvidence evidence = evidenceNormalizationService.normalize(acquisition);
            UUID evaluationId = acquisition.evaluation().evaluationId();
            List<StructuredEvidence> evidenceItems = List.of(evidence);

            stage = "observation/signal preparation";
            PreparedFacts preparedFacts = prepareFacts(evidence);

            stage = "coverage assessment derivation";
            HospitalityCoverageAssessment assessment = coverageAssessmentDerivationService.derive(
                    evaluationId,
                    preparedFacts.qualifiedSignals(),
                    preparedFacts.findings(),
                    preparedFacts.limitations()
            );

            stage = "coverage classification";
            HospitalityAnalysisCoverageState coverageState = coverageClassifier.classify(assessment);

            stage = "deterministic hospitality analysis";
            HospitalityAnalysisResult analysisResult = analysisService.analyze(
                    evaluationId,
                    evidenceItems,
                    coverageState
            );

            stage = "guest journey analysis";
            GuestJourneyAnalysis guestJourneyAnalysis = guestJourneyAnalysisService.analyze(analysisResult);

            stage = "recommendations";
            Set<HospitalityRecommendation> recommendations = recommendationService.recommend(guestJourneyAnalysis);

            stage = "report";
            HospitalityAnalysisReport report = reportService.assemble(
                    analysisResult,
                    guestJourneyAnalysis,
                    recommendations,
                    request.evaluationTarget()
            );

            acquisition.attempt().incomplete(
                    "Evaluation analysis completed; interactive preview is not available in this execution slice.",
                    new OwnerFacingOutcome(true, false)
            );

            return new EvaluationExecutionResult(
                    acquisition.evaluation(),
                    acquisition.attempt(),
                    acquisition,
                    evidence,
                    analysisResult,
                    guestJourneyAnalysis,
                    recommendations,
                    report
            );
        } catch (RuntimeException failure) {
            if (acquisition != null && acquisition.attempt().state() == EvaluationLifecycleState.RUNNING) {
                acquisition.attempt().failed(
                        "Evaluation execution failed during " + stage,
                        Map.of("stage", stage, "exceptionType", failure.getClass().getName())
                );
            }
            throw failure;
        }
    }

    private PreparedFacts prepareFacts(StructuredEvidence evidence) {
        List<HospitalityAnalysisSignal> qualifiedSignals = new ArrayList<>();
        Set<HospitalityFinding> findings = new LinkedHashSet<>();

        for (HospitalityObservation observation : observationService.observe(evidence)) {
            signalService.qualify(observation)
                    .ifPresent(signal -> {
                        qualifiedSignals.add(signal);
                        findingService.create(signal).ifPresent(findings::add);
                    });
        }

        // The current acquisition limitation producer creates only unscoped limitations.
        // REQ-036 explicitly forbids assigning such failures to arbitrary scope, so no
        // limitation is synthesized here. The deterministic analysis service remains the
        // owner of the existing acquisition-limitation lifecycle.
        return new PreparedFacts(qualifiedSignals, findings, Set.of());
    }

    private record PreparedFacts(
            Collection<HospitalityAnalysisSignal> qualifiedSignals,
            Collection<HospitalityFinding> findings,
            Collection<HospitalityAnalysisLimitation> limitations
    ) {
        private PreparedFacts {
            qualifiedSignals = List.copyOf(qualifiedSignals);
            findings = Set.copyOf(findings);
            limitations = Set.copyOf(limitations);
        }
    }
}
