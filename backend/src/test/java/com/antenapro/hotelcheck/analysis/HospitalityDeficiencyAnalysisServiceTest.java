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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HospitalityDeficiencyAnalysisServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-04T00:00:00Z"), ZoneOffset.UTC);
    private static final String WEBSITE = "https://hotel.example.com";
    private final HotelEvaluationInputValidator inputValidator = new HotelEvaluationInputValidator();
    private final HospitalityAnalysisService service = new HospitalityAnalysisService();

    @Test
    void observedRoomPageWithoutBookingActionProducesBookingAndRoomInformationDeficiencies() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/rooms",
                AcquisitionOutcome.SUCCESS,
                "<title>Grand Hotel Rooms</title>"
                        + "<h1>Rooms</h1>"
                        + "<p>Our rooms.</p>");

        HospitalityAnalysisResult result = analyze(evidence);

        assertTrue(result.findings().stream().anyMatch(f ->
                f.category() == HospitalityObservationCategory.ROOMS
                        && f.findingText().contains("does not expose a usable booking action")));
        assertTrue(result.findings().stream().anyMatch(f ->
                f.category() == HospitalityObservationCategory.ROOMS
                        && f.findingText().contains("does not contain meaningful room decision-support information")));
        assertTrue(result.limitations().isEmpty());
    }

    @Test
    void observedContactPageWithoutContactDetailProducesContactDeficiency() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/contact",
                AcquisitionOutcome.SUCCESS,
                "<title>Contact Grand Hotel</title>"
                        + "<h1>Contact Us</h1>"
                        + "<p>Send us a message.</p>");

        HospitalityAnalysisResult result = analyze(evidence);

        assertTrue(result.findings().stream().anyMatch(f ->
                f.category() == HospitalityObservationCategory.CONTACT
                        && f.findingText().contains("does not expose a usable guest-facing contact or location detail")));
    }

    @Test
    void observedAmenitiesContextWithoutGuestInformationProducesGuestInformationDeficiency() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/amenities",
                AcquisitionOutcome.SUCCESS,
                "<title>Grand Hotel Amenities</title>"
                        + "<h1>Amenities</h1>"
                        + "<p>Explore the property.</p>");

        HospitalityAnalysisResult result = analyze(evidence);

        assertTrue(result.findings().stream().anyMatch(f ->
                f.category() == HospitalityObservationCategory.AMENITIES
                        && f.findingText().contains("does not contain meaningful stay-related decision-support information")));
    }

    @Test
    void materialIdentityConflictProducesTrustFindingsAndPreservesBothSources() {
        Evaluation evaluation = evaluation();
        StructuredEvidence first = evidence(evaluation, "https://hotel.example.com/about", AcquisitionOutcome.SUCCESS,
                "<h1>Grand Hotel</h1>");
        StructuredEvidence second = evidence(evaluation, "https://directory.example.com/hotel", AcquisitionOutcome.SUCCESS,
                "<h1>Grand Resort Hotel</h1>");

        HospitalityAnalysisResult result = service.analyze(
                evaluation.evaluationId(), List.of(first, second), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED);

        List<HospitalityFinding> conflicts = result.findings().stream()
                .filter(f -> f.findingText().contains("conflicting hotel identity information"))
                .toList();

        assertEquals(2, conflicts.size());
        assertEquals(
                java.util.Set.of(first, second),
                conflicts.stream().map(HospitalityFinding::supportingEvidence).collect(java.util.stream.Collectors.toSet()));
        assertTrue(conflicts.stream().allMatch(f -> f.evaluationId().equals(evaluation.evaluationId())));
    }

    @Test
    void genericSuccessfulPageWithKeywordAbsenceDoesNotCreateDeficiency() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/",
                AcquisitionOutcome.SUCCESS,
                "<title>Welcome</title><p>General information.</p>");

        HospitalityAnalysisResult result = analyze(evidence);

        assertTrue(result.findings().isEmpty());
        assertTrue(result.limitations().isEmpty());
    }

    @Test
    void failedAcquisitionRemainsLimitationAndNeverBecomesDeficiency() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/rooms",
                AcquisitionOutcome.TIMEOUT,
                null);

        HospitalityAnalysisResult result = analyze(evidence);

        assertTrue(result.findings().isEmpty());
        assertEquals(1, result.limitations().size());
        assertFalse(result.limitations().isEmpty());
    }

    @Test
    void unsupportedAcquisitionDoesNotBecomeDeficiency() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/rooms",
                AcquisitionOutcome.UNSUPPORTED_SCHEME,
                null);

        HospitalityAnalysisResult result = analyze(evidence);

        assertTrue(result.findings().isEmpty());
        assertTrue(result.limitations().isEmpty());
    }

    @Test
    void repeatedExecutionProducesEquivalentDeficiencyResults() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/rooms",
                AcquisitionOutcome.SUCCESS,
                "<h1>Rooms</h1><p>Our rooms.</p>");

        HospitalityAnalysisResult first = analyze(evidence);
        HospitalityAnalysisResult second = analyze(evidence);

        assertEquals(first, second);
    }

    private HospitalityAnalysisResult analyze(StructuredEvidence evidence) {
        return service.analyze(
                evidence.evaluationId(), List.of(evidence), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED);
    }

    private StructuredEvidence evidence(String url, AcquisitionOutcome outcome, String body) {
        return evidence(evaluation(), url, outcome, body);
    }

    private StructuredEvidence evidence(Evaluation evaluation, String url, AcquisitionOutcome outcome, String body) {
        Integer statusCode = outcome == AcquisitionOutcome.SUCCESS ? 200 : null;
        AcquisitionResult acquisition = new AcquisitionResult(
                outcome,
                url,
                url,
                statusCode,
                body == null ? null : "text/html",
                CLOCK.instant(),
                outcome == AcquisitionOutcome.TIMEOUT || outcome == AcquisitionOutcome.NETWORK_ERROR
                        ? AcquisitionMethod.UNAVAILABLE : AcquisitionMethod.HTTP_PUBLIC,
                body,
                Map.of(),
                List.of(),
                outcome == AcquisitionOutcome.TIMEOUT ? "request timed out" : null);
        EvaluationAttempt attempt = evaluation.currentAttempt();
        CapabilityOutcome capabilityOutcome = CapabilityOutcome.record(
                "public-web-acquisition",
                outcome == AcquisitionOutcome.SUCCESS ? CapabilityOutcomeStatus.SUCCEEDED : CapabilityOutcomeStatus.FAILED,
                outcome.name(),
                CLOCK.instant());
        EvaluationAcquisitionResult source = new EvaluationAcquisitionResult(evaluation, attempt, acquisition, capabilityOutcome);
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

    private Evaluation evaluation() {
        CanonicalEvaluationRequest request = ((ValidationResult.Accepted)
                inputValidator.validate(new HotelEvaluationInput(null, null, WEBSITE))).request();
        return Evaluation.create(request, CLOCK);
    }
}
