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
import com.antenapro.hotelcheck.hospitality.HospitalityObservationCategory;
import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;
import com.antenapro.hotelcheck.input.HotelEvaluationInput;
import com.antenapro.hotelcheck.input.HotelEvaluationInputValidator;
import com.antenapro.hotelcheck.input.ValidationResult;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HospitalityAnalysisServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-04T00:00:00Z"), ZoneOffset.UTC);
    private static final String WEBSITE = "https://hotel.example.com";
    private final HotelEvaluationInputValidator inputValidator = new HotelEvaluationInputValidator();
    private final HospitalityAnalysisService service = new HospitalityAnalysisService();

    @Test
    void successfulHomepageFlowsFromEvidenceToObservationSignalFindingAndResult() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.SUCCESS,
                "<title>Grand Hotel</title>"
                        + "<h1>Grand Hotel</h1>"
                        + "<a href=\"/rooms\">Rooms & Suites</a>"
                        + "<a href=\"/amenities\">Pool and Wi-Fi</a>"
                        + "<a href=\"mailto:stay@hotel.example.com\">Contact Us</a>"
                        + "<a href=\"/booking\">Book Now</a>"
                        + "<a href=\"/dining\">Restaurant & Breakfast</a>", null);

        HospitalityAnalysisResult result = service.analyze(
                evidence.evaluationId(), List.of(evidence), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED);

        assertEquals(evidence.evaluationId(), result.evaluationId());
        assertEquals(7, result.findings().size());
        assertTrue(result.findings().stream().anyMatch(f -> f.category() == HospitalityObservationCategory.HOTEL_IDENTITY));
        assertTrue(result.findings().stream().anyMatch(f -> f.category() == HospitalityObservationCategory.ROOMS));
        assertTrue(result.findings().stream().anyMatch(f -> f.category() == HospitalityObservationCategory.AMENITIES));
        assertTrue(result.findings().stream().anyMatch(f -> f.category() == HospitalityObservationCategory.CONTACT));
        assertTrue(result.findings().stream().anyMatch(f -> f.category() == HospitalityObservationCategory.BOOKING));
        assertTrue(result.findings().stream().anyMatch(f -> f.category() == HospitalityObservationCategory.DINING));
        assertTrue(result.limitations().isEmpty());
        assertEquals(Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND,
                GuestJourneyStage.EXPLORE, GuestJourneyStage.BOOK), result.coverage().assessableJourneyStages());
        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, result.coverage().state());
    }

    @Test
    void intendedDimensionsContainTheCompleteGovernedHospitalityScope() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.SUCCESS,
                "<h1>Grand Hotel</h1><a href=\"/rooms\">Rooms</a>", null);

        HospitalityAnalysisCoverage coverage = service.analyze(
                evidence.evaluationId(), List.of(evidence), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED)
                .coverage();

        assertEquals(Set.of(
                HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING,
                HospitalityAnalysisDimension.DISCOVERABILITY_AND_NAVIGATION,
                HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION,
                HospitalityAnalysisDimension.AMENITIES_AND_GUEST_FACING_INFORMATION,
                HospitalityAnalysisDimension.CONTACT_AND_LOCATION,
                HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS,
                HospitalityAnalysisDimension.TRUST_AND_CLARITY,
                HospitalityAnalysisDimension.MOBILE_AND_TECHNICAL_GUEST_EXPERIENCE,
                HospitalityAnalysisDimension.SEO_AND_STRUCTURED_DATA_SUPPORTING_SIGNALS
        ), coverage.intendedDimensions());
    }

    @Test
    void currentAssessableDimensionsAreOnlyThoseSupportedByCurrentFindings() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.SUCCESS,
                "<h1>Grand Hotel</h1>"
                        + "<a href=\"/rooms\">Rooms</a>"
                        + "<a href=\"/amenities\">Pool and Wi-Fi</a>"
                        + "<a href=\"mailto:stay@hotel.example.com\">Contact Us</a>"
                        + "<a href=\"/booking\">Book Now</a>", null);

        HospitalityAnalysisCoverage coverage = service.analyze(
                evidence.evaluationId(), List.of(evidence), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED)
                .coverage();

        assertEquals(Set.of(
                HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING,
                HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION,
                HospitalityAnalysisDimension.AMENITIES_AND_GUEST_FACING_INFORMATION,
                HospitalityAnalysisDimension.CONTACT_AND_LOCATION,
                HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS
        ), coverage.assessableDimensions());
        assertTrue(coverage.intendedDimensions().containsAll(coverage.assessableDimensions()));
    }

    @Test
    void unsupportedDimensionsRemainIntendedButAreNotFalselyAssessable() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.SUCCESS,
                "<h1>Grand Hotel</h1>"
                        + "<a href=\"/rooms\">Rooms</a>"
                        + "<a href=\"/amenities\">Amenities</a>"
                        + "<a href=\"/contact\">Contact</a>"
                        + "<a href=\"/booking\">Book Now</a>", null);

        HospitalityAnalysisCoverage coverage = service.analyze(
                evidence.evaluationId(), List.of(evidence), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED)
                .coverage();

        Set<HospitalityAnalysisDimension> unsupported = Set.of(
                HospitalityAnalysisDimension.DISCOVERABILITY_AND_NAVIGATION,
                HospitalityAnalysisDimension.TRUST_AND_CLARITY,
                HospitalityAnalysisDimension.MOBILE_AND_TECHNICAL_GUEST_EXPERIENCE,
                HospitalityAnalysisDimension.SEO_AND_STRUCTURED_DATA_SUPPORTING_SIGNALS
        );

        assertTrue(coverage.intendedDimensions().containsAll(unsupported));
        assertTrue(unsupported.stream().noneMatch(coverage.assessableDimensions()::contains));
        assertFalse(coverage.assessableDimensions().containsAll(unsupported));
    }

    @Test
    void diningContributesToAmenitiesDimensionWithoutCreatingAnInventedDimension() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.SUCCESS,
                "<a href=\"/dining\">Restaurant & Breakfast</a>", null);

        HospitalityAnalysisCoverage coverage = service.analyze(
                evidence.evaluationId(), List.of(evidence), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED)
                .coverage();

        assertEquals(Set.of(HospitalityAnalysisDimension.AMENITIES_AND_GUEST_FACING_INFORMATION),
                coverage.assessableDimensions());
        assertEquals(9, coverage.intendedDimensions().size());
    }

    @Test
    void multipleEvidenceItemsRemainTraceableAndDistinct() {
        Evaluation evaluation = evaluation();
        StructuredEvidence homepage = evidence(evaluation, AcquisitionOutcome.SUCCESS,
                "<h1>Grand Hotel</h1><a href=\"/rooms\">Rooms</a>", null);
        StructuredEvidence booking = evidence(evaluation, AcquisitionOutcome.SUCCESS,
                "<a href=\"/booking\">Reservation</a>", null);

        HospitalityAnalysisResult result = service.analyze(
                homepage.evaluationId(), List.of(homepage, booking), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED);

        assertEquals(3, result.findings().size());
        assertTrue(result.findings().stream().anyMatch(f -> f.category() == HospitalityObservationCategory.HOTEL_IDENTITY));
        assertTrue(result.findings().stream().anyMatch(f -> f.category() == HospitalityObservationCategory.ROOMS));
        HospitalityFinding bookingFinding = result.findings().stream()
                .filter(f -> f.category() == HospitalityObservationCategory.BOOKING).findFirst().orElseThrow();
        assertSame(booking, bookingFinding.supportingEvidence());
    }

    @Test
    void successfulAndTimeoutEvidenceProduceFindingsAndUnableToVerifyLimitation() {
        Evaluation evaluation = evaluation();
        StructuredEvidence success = evidence(evaluation, AcquisitionOutcome.SUCCESS,
                "<a href=\"/rooms\">Rooms</a>", null);
        StructuredEvidence timeout = evidence(evaluation, AcquisitionOutcome.TIMEOUT, null, "request timed out");

        HospitalityAnalysisResult result = service.analyze(
                success.evaluationId(), List.of(success, timeout), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED);

        assertEquals(1, result.findings().size());
        assertEquals(1, result.limitations().size());
        HospitalityAnalysisLimitation limitation = result.limitations().iterator().next();
        assertEquals(HospitalityAnalysisLimitationType.UNABLE_TO_VERIFY, limitation.type());
        assertEquals(AcquisitionOutcome.TIMEOUT, limitation.sourceCondition());
        assertSame(timeout, limitation.supportingEvidence());
        assertFalse(result.findings().stream().anyMatch(f -> f.category() == HospitalityObservationCategory.BOOKING));
    }

    @Test
    void unsupportedAcquisitionOutcomeDoesNotBecomeHotelDeficiencyOrLimitation() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.UNSUPPORTED_SCHEME, null, "scheme not supported");

        HospitalityAnalysisResult result = service.analyze(
                evidence.evaluationId(), List.of(evidence), HospitalityAnalysisCoverageState.INSUFFICIENT_COVERAGE);

        assertTrue(result.findings().isEmpty());
        assertTrue(result.limitations().isEmpty());
    }

    @Test
    void absenceOfPositiveSignalDoesNotCreateNegativeFinding() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.SUCCESS,
                "<title>Welcome to our website</title><p>General information</p>", null);

        HospitalityAnalysisResult result = service.analyze(
                evidence.evaluationId(), List.of(evidence), HospitalityAnalysisCoverageState.INSUFFICIENT_COVERAGE);

        assertTrue(result.findings().isEmpty());
        assertTrue(result.limitations().isEmpty());
        assertTrue(result.coverage().assessableJourneyStages().isEmpty());
    }

    @Test
    void duplicateEquivalentEvidenceIsDeduplicatedDeterministically() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.SUCCESS,
                "<h1>Grand Hotel</h1><a href=\"/booking\">Book Now</a>", null);

        HospitalityAnalysisResult result = service.analyze(
                evidence.evaluationId(), List.of(evidence, evidence), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED);

        assertEquals(2, result.findings().size());
        assertEquals(result, service.analyze(
                evidence.evaluationId(), List.of(evidence, evidence), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED));
    }

    @Test
    void conflictingEvidenceIsRetainedRatherThanOverwritten() {
        Evaluation evaluation = evaluation();
        StructuredEvidence first = evidence(evaluation, AcquisitionOutcome.SUCCESS,
                "<h1>Grand Hotel</h1><a href=\"/rooms\">Rooms</a>", null);
        StructuredEvidence second = evidence(evaluation, AcquisitionOutcome.SUCCESS,
                "<h1>Grand Hotel</h1><a href=\"/suites\">Suites</a>", null);

        HospitalityAnalysisResult result = service.analyze(
                first.evaluationId(), List.of(first, second), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED);

        Set<HospitalityFinding> roomFindings = result.findings().stream()
                .filter(f -> f.category() == HospitalityObservationCategory.ROOMS)
                .collect(java.util.stream.Collectors.toSet());
        assertEquals(2, roomFindings.size());
        assertEquals(Set.of(first, second), roomFindings.stream()
                .map(HospitalityFinding::supportingEvidence)
                .collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void crossEvaluationEvidenceIsRejectedBeforeAnalysis() {
        StructuredEvidence first = evidence(AcquisitionOutcome.SUCCESS, "<h1>Grand Hotel</h1>", null);
        StructuredEvidence second = evidence(AcquisitionOutcome.SUCCESS, "<h1>Another Hotel</h1>", null);

        assertThrows(IllegalArgumentException.class, () -> service.analyze(
                first.evaluationId(), List.of(first, second), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED));
    }

    @Test
    void outputArtifactsBelongToTheRequestedEvaluation() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.SUCCESS,
                "<h1>Grand Hotel</h1><a href=\"/book\">Book Now</a>", null);

        HospitalityAnalysisResult result = service.analyze(
                evidence.evaluationId(), List.of(evidence), HospitalityAnalysisCoverageState.SUBSTANTIALLY_ASSESSED);

        assertEquals(evidence.evaluationId(), result.coverage().evaluationId());
        assertTrue(result.findings().stream().allMatch(f -> evidence.evaluationId().equals(f.evaluationId())));
        assertTrue(result.limitations().stream().allMatch(l -> evidence.evaluationId().equals(l.evaluationId())));
    }

    @Test
    void coverageStateIsCallerSuppliedAndFactualScopeComesFromArtifacts() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.SUCCESS,
                "<a href=\"/rooms\">Rooms</a>", null);

        HospitalityAnalysisResult result = service.analyze(
                evidence.evaluationId(), List.of(evidence), HospitalityAnalysisCoverageState.SUBSTANTIALLY_ASSESSED);

        assertEquals(HospitalityAnalysisCoverageState.SUBSTANTIALLY_ASSESSED, result.coverage().state());
        assertEquals(Set.of(GuestJourneyStage.EXPLORE), result.coverage().assessableJourneyStages());
        assertEquals(Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION), result.coverage().assessableDimensions());
        assertFalse(result.coverage().rationale().contains("%"));
        assertFalse(result.coverage().rationale().contains("threshold"));
    }

    @Test
    void identicalEvidenceProducesEquivalentResultsAcrossRuns() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.SUCCESS,
                "<title>Grand Hotel</title><a href=\"/rooms\">Rooms</a><a href=\"/book\">Book Now</a>", null);

        HospitalityAnalysisResult first = service.analyze(
                evidence.evaluationId(), List.of(evidence), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED);
        HospitalityAnalysisResult second = service.analyze(
                evidence.evaluationId(), List.of(evidence), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED);

        assertEquals(first, second);
        assertNotNull(first.coverage());
    }

    private StructuredEvidence evidence(AcquisitionOutcome outcome, String body, String limitation) {
        return evidence(evaluation(), outcome, body, limitation);
    }

    private StructuredEvidence evidence(Evaluation evaluation, AcquisitionOutcome outcome, String body, String limitation) {
        Integer statusCode = outcome == AcquisitionOutcome.SUCCESS ? 200 : null;
        AcquisitionResult acquisition = new AcquisitionResult(
                outcome, WEBSITE, WEBSITE + "/", statusCode, body == null ? null : "text/html",
                CLOCK.instant(),
                outcome == AcquisitionOutcome.TIMEOUT || outcome == AcquisitionOutcome.NETWORK_ERROR
                        ? AcquisitionMethod.UNAVAILABLE : AcquisitionMethod.HTTP_PUBLIC,
                body, Map.of(), List.of(), limitation);
        EvaluationAttempt attempt = evaluation.currentAttempt();
        CapabilityOutcome capabilityOutcome = CapabilityOutcome.record(
                "public-web-acquisition",
                outcome == AcquisitionOutcome.SUCCESS ? CapabilityOutcomeStatus.SUCCEEDED : CapabilityOutcomeStatus.FAILED,
                outcome.name(), CLOCK.instant());
        EvaluationAcquisitionResult source = new EvaluationAcquisitionResult(evaluation, attempt, acquisition, capabilityOutcome);
        return new StructuredEvidence(
                evaluation.evaluationId(), attempt.attemptId(), attempt.attemptNumber(), source, EvidenceProvenance.DISCOVERED,
                outcome, acquisition.requestedUrl(), acquisition.finalUrl(), acquisition.retrievalTimestamp(),
                acquisition.acquisitionMethod(), acquisition.statusCode(), acquisition.contentType(), acquisition.body(), acquisition.errorMessage());
    }

    private Evaluation evaluation() {
        CanonicalEvaluationRequest request = ((ValidationResult.Accepted)
                inputValidator.validate(new HotelEvaluationInput(null, null, WEBSITE))).request();
        return Evaluation.create(request, CLOCK);
    }
}
