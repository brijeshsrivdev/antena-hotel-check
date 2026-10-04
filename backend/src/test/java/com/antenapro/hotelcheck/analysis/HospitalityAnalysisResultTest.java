package com.antenapro.hotelcheck.analysis;

import com.antenapro.hotelcheck.acquisition.AcquisitionMethod;
import com.antenapro.hotelcheck.acquisition.AcquisitionOutcome;
import com.antenapro.hotelcheck.acquisition.AcquisitionResult;
import com.antenapro.hotelcheck.evidence.EvidenceProvenance;
import com.antenapro.hotelcheck.evidence.StructuredEvidence;
import com.antenapro.hotelcheck.evaluation.CapabilityOutcome;
import com.antenapro.hotelcheck.evaluation.CapabilityOutcomeStatus;
import com.antenapro.hotelcheck.evaluation.Evaluation;
import com.antenapro.hotelcheck.evaluation.EvaluationAcquisitionResult;
import com.antenapro.hotelcheck.evaluation.EvaluationAttempt;
import com.antenapro.hotelcheck.hospitality.HospitalityObservation;
import com.antenapro.hotelcheck.hospitality.HospitalityObservationCategory;
import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;
import com.antenapro.hotelcheck.input.HotelEvaluationInput;
import com.antenapro.hotelcheck.input.HotelEvaluationInputValidator;
import com.antenapro.hotelcheck.input.ValidationResult;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HospitalityAnalysisResultTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-04T00:00:00Z"), ZoneOffset.UTC);
    private static final String WEBSITE = "https://hotel.example.com";
    private final HotelEvaluationInputValidator inputValidator = new HotelEvaluationInputValidator();
    private final HospitalityAnalysisLimitationService limitationService = new HospitalityAnalysisLimitationService();

    @Test
    void aggregatesFindingsLimitationsAndCoverageWithoutDuplicatingDomainObjects() {
        Evaluation evaluation = evaluation();
        StructuredEvidence findingEvidence = evidence(evaluation, AcquisitionOutcome.SUCCESS, "<a href=\"/book\">Book Now</a>", null);
        StructuredEvidence limitationEvidence = evidence(evaluation, AcquisitionOutcome.TIMEOUT, null, "booking source timed out");
        HospitalityFinding finding = finding(findingEvidence);
        HospitalityAnalysisLimitation limitation = limitation(limitationEvidence);
        HospitalityAnalysisCoverage coverage = coverage(evaluation.evaluationId(), finding, limitation);

        HospitalityAnalysisResult result = new HospitalityAnalysisResult(
                evaluation.evaluationId(), Set.of(finding), Set.of(limitation), coverage);

        assertEquals(evaluation.evaluationId(), result.evaluationId());
        assertSame(finding, result.findings().iterator().next());
        assertSame(limitation, result.limitations().iterator().next());
        assertSame(coverage, result.coverage());
    }

    @Test
    void acceptsZeroFindingsWithoutCreatingAnInferredDeficiency() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.TIMEOUT, null, "booking source timed out");
        HospitalityAnalysisLimitation limitation = limitation(evidence);
        HospitalityAnalysisCoverage coverage = coverage(evidence.evaluationId(), null, limitation);

        HospitalityAnalysisResult result = new HospitalityAnalysisResult(
                evidence.evaluationId(), Set.of(), Set.of(limitation), coverage);

        assertTrue(result.findings().isEmpty());
        assertSame(limitation, result.limitations().iterator().next());
        assertEquals(HospitalityAnalysisLimitationType.UNABLE_TO_VERIFY, result.limitations().iterator().next().type());
        assertEquals(HospitalityAnalysisCoverageState.INSUFFICIENT_COVERAGE, result.coverage().state());
    }

    @Test
    void rejectsCrossEvaluationFinding() {
        StructuredEvidence first = evidence(AcquisitionOutcome.SUCCESS, "<h1>Hotel A</h1>", null);
        StructuredEvidence second = evidence(AcquisitionOutcome.SUCCESS, "<h1>Hotel B</h1>", null);
        HospitalityFinding foreignFinding = finding(second);
        HospitalityAnalysisCoverage coverage = coverage(first.evaluationId(), null, null);

        assertThrows(IllegalArgumentException.class, () ->
                new HospitalityAnalysisResult(first.evaluationId(), Set.of(foreignFinding), Set.of(), coverage));
    }

    @Test
    void rejectsCrossEvaluationLimitation() {
        StructuredEvidence first = evidence(AcquisitionOutcome.SUCCESS, "<h1>Hotel A</h1>", null);
        StructuredEvidence second = evidence(AcquisitionOutcome.TIMEOUT, null, "booking source timed out");
        HospitalityAnalysisLimitation foreignLimitation = limitation(second);
        HospitalityAnalysisCoverage coverage = coverage(first.evaluationId(), null, null);

        assertThrows(IllegalArgumentException.class, () ->
                new HospitalityAnalysisResult(first.evaluationId(), Set.of(), Set.of(foreignLimitation), coverage));
    }

    @Test
    void rejectsCrossEvaluationCoverage() {
        StructuredEvidence first = evidence(AcquisitionOutcome.SUCCESS, "<h1>Hotel A</h1>", null);
        StructuredEvidence second = evidence(AcquisitionOutcome.SUCCESS, "<h1>Hotel B</h1>", null);
        HospitalityAnalysisCoverage foreignCoverage = coverage(second.evaluationId(), null, null);

        assertThrows(IllegalArgumentException.class, () ->
                new HospitalityAnalysisResult(first.evaluationId(), Set.of(), Set.of(), foreignCoverage));
    }

    @Test
    void preservesLimitationSemanticsAndCoverageRepresentation() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.TIMEOUT, null, "booking source timed out");
        HospitalityAnalysisLimitation limitation = limitation(evidence);
        HospitalityAnalysisCoverage coverage = coverage(evidence.evaluationId(), null, limitation);

        HospitalityAnalysisResult result = new HospitalityAnalysisResult(
                evidence.evaluationId(), Set.of(), Set.of(limitation), coverage);

        assertEquals(HospitalityAnalysisLimitationType.UNABLE_TO_VERIFY, result.limitations().iterator().next().type());
        assertEquals("booking source timed out", result.limitations().iterator().next().explanation());
        assertSame(coverage, result.coverage());
        assertEquals(HospitalityAnalysisCoverageState.INSUFFICIENT_COVERAGE, result.coverage().state());
    }

    @Test
    void defensivelyCopiesInputCollectionsAndExposesImmutableCollections() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.SUCCESS, "<h1>Hotel</h1>", null);
        HospitalityFinding finding = finding(evidence);
        HospitalityAnalysisCoverage coverage = coverage(evidence.evaluationId(), finding, null);
        Set<HospitalityFinding> findings = new HashSet<>(Set.of(finding));
        Set<HospitalityAnalysisLimitation> limitations = new HashSet<>();

        HospitalityAnalysisResult result = new HospitalityAnalysisResult(
                evidence.evaluationId(), findings, limitations, coverage);

        findings.clear();
        limitations.add(limitation(evidence(AcquisitionOutcome.TIMEOUT, null, "booking source timed out")));

        assertEquals(Set.of(finding), result.findings());
        assertTrue(result.limitations().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> result.findings().clear());
        assertThrows(UnsupportedOperationException.class, () -> result.limitations().clear());
    }

    @Test
    void sameInputsProduceEquivalentResults() {
        Evaluation evaluation = evaluation();
        StructuredEvidence findingEvidence = evidence(evaluation, AcquisitionOutcome.SUCCESS, "<h1>Hotel</h1>", null);
        StructuredEvidence limitationEvidence = evidence(evaluation, AcquisitionOutcome.TIMEOUT, null, "booking source timed out");
        HospitalityFinding finding = finding(findingEvidence);
        HospitalityAnalysisLimitation limitation = limitation(limitationEvidence);
        HospitalityAnalysisCoverage coverage = coverage(evaluation.evaluationId(), finding, limitation);

        HospitalityAnalysisResult first = new HospitalityAnalysisResult(
                evaluation.evaluationId(), Set.of(finding), Set.of(limitation), coverage);
        HospitalityAnalysisResult second = new HospitalityAnalysisResult(
                evaluation.evaluationId(), Set.of(finding), Set.of(limitation), coverage);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    private Evaluation evaluation() {
        CanonicalEvaluationRequest request = ((ValidationResult.Accepted) inputValidator
                .validate(new HotelEvaluationInput(null, null, WEBSITE))).request();
        return Evaluation.create(request, CLOCK);
    }

    private HospitalityFinding finding(StructuredEvidence evidence) {
        HospitalityObservation observation = new HospitalityObservation(
                HospitalityObservationCategory.BOOKING,
                "Book Now -> /book",
                WEBSITE + "/",
                evidence,
                evidence.evaluationId(),
                evidence.attemptId(),
                evidence.attemptNumber(),
                EvidenceProvenance.DISCOVERED);
        HospitalityAnalysisSignal signal = new HospitalityAnalysisSignal(
                observation,
                Set.of(GuestJourneyStage.BOOK),
                "A booking entry point was observed.",
                HospitalityAnalysisSignalStatus.QUALIFIED);
        return new HospitalityFinding(signal, HospitalityFindingStatus.VERIFIED_OBSERVED);
    }

    private HospitalityAnalysisLimitation limitation(StructuredEvidence evidence) {
        return limitationService.create(
                evidence,
                Set.of(HospitalityObservationCategory.BOOKING),
                Set.of(GuestJourneyStage.BOOK));
    }

    private HospitalityAnalysisCoverage coverage(UUID evaluationId,
                                                   HospitalityFinding finding,
                                                   HospitalityAnalysisLimitation limitation) {
        return new HospitalityAnalysisCoverage(
                evaluationId,
                Set.of(GuestJourneyStage.BOOK),
                Set.of(HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS),
                finding == null ? Set.of() : Set.of(GuestJourneyStage.BOOK),
                finding == null ? Set.of() : Set.of(HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS),
                limitation == null ? Set.of(GuestJourneyStage.BOOK) : Set.of(),
                limitation == null ? Set.of(HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS) : Set.of(),
                finding == null ? Set.of() : Set.of(finding),
                limitation == null ? Set.of() : Set.of(limitation),
                limitation == null ? HospitalityAnalysisCoverageState.SUBSTANTIALLY_ASSESSED : HospitalityAnalysisCoverageState.INSUFFICIENT_COVERAGE,
                limitation == null
                        ? "explicitly assessed from traceable hospitality evidence"
                        : "booking could not be verified; this does not establish that booking is absent");
    }

    private StructuredEvidence evidence(AcquisitionOutcome outcome, String body, String limitation) {
        return evidence(evaluation(), outcome, body, limitation);
    }

    private StructuredEvidence evidence(Evaluation evaluation, AcquisitionOutcome outcome, String body, String limitation) {
        Integer statusCode;
        if (outcome == AcquisitionOutcome.HTTP_ERROR) {
            statusCode = 503;
        } else if (outcome == AcquisitionOutcome.SUCCESS) {
            statusCode = 200;
        } else {
            statusCode = null;
        }
        AcquisitionResult acquisition = new AcquisitionResult(
                outcome,
                WEBSITE,
                WEBSITE,
                statusCode,
                body == null ? null : "text/html",
                CLOCK.instant(),
                outcome == AcquisitionOutcome.TIMEOUT || outcome == AcquisitionOutcome.NETWORK_ERROR
                        ? AcquisitionMethod.UNAVAILABLE
                        : AcquisitionMethod.HTTP_PUBLIC,
                body,
                Map.of(),
                List.of(),
                limitation);
        EvaluationAttempt attempt = evaluation.currentAttempt();
        CapabilityOutcome capabilityOutcome = CapabilityOutcome.record(
                "public-web-acquisition",
                outcome == AcquisitionOutcome.SUCCESS
                        ? CapabilityOutcomeStatus.SUCCEEDED
                        : CapabilityOutcomeStatus.FAILED,
                outcome.name(),
                CLOCK.instant());
        EvaluationAcquisitionResult source = new EvaluationAcquisitionResult(
                evaluation, attempt, acquisition, capabilityOutcome);
        return new StructuredEvidence(
                evaluation.evaluationId(),
                attempt.attemptId(),
                attempt.attemptNumber(),
                source,
                EvidenceProvenance.DISCOVERED,
                outcome,
                acquisition.requestedUrl(),
                acquisition.finalUrl(),
                acquisition.retrievalTimestamp(),
                acquisition.acquisitionMethod(),
                acquisition.statusCode(),
                acquisition.contentType(),
                acquisition.body(),
                acquisition.errorMessage());
    }
}
