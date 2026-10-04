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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HospitalityAnalysisCompletenessFoundationTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-04T00:00:00Z"), ZoneOffset.UTC);
    private static final String WEBSITE = "https://hotel.example.com";
    private final HotelEvaluationInputValidator inputValidator = new HotelEvaluationInputValidator();
    private final HospitalityAnalysisService service = new HospitalityAnalysisService();

    @Test
    void crossSourceIdentityConflictMakesTrustClarityAssessable() {
        Evaluation evaluation = evaluation();
        StructuredEvidence first = evidence(evaluation, "https://hotel.example.com/about", "<h1>Grand Hotel</h1>");
        StructuredEvidence second = evidence(evaluation, "https://hotel.example.com/contact", "<h1>Grand Resort</h1>");

        HospitalityAnalysisResult result = service.analyze(
                evaluation.evaluationId(), List.of(first, second), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED);

        assertTrue(result.findings().stream().anyMatch(f -> f.category() == HospitalityObservationCategory.HOTEL_IDENTITY
                && f.findingText().startsWith("Retained public sources expose conflicting hotel identity information:")));
        assertTrue(result.coverage().assessableDimensions().contains(HospitalityAnalysisDimension.TRUST_AND_CLARITY));
    }

    @Test
    void trustRemainsUnsupportedWhenNoGovernedConflictEvidenceExists() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/rooms", "<h1>Grand Hotel</h1><a href=\"/rooms\">Rooms</a>");

        Set<HospitalityAnalysisDimension> assessable = service.analyze(
                evidence.evaluationId(), List.of(evidence), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED)
                .coverage().assessableDimensions();

        assertFalse(assessable.contains(HospitalityAnalysisDimension.TRUST_AND_CLARITY));
        assertFalse(assessable.contains(HospitalityAnalysisDimension.MOBILE_AND_TECHNICAL_GUEST_EXPERIENCE));
        assertFalse(assessable.contains(HospitalityAnalysisDimension.SEO_AND_STRUCTURED_DATA_SUPPORTING_SIGNALS));
    }

    @Test
    void bookingAbsenceRemainsNonDeficiencyWhileExistingDimensionsRemainAssessable() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/rooms", "<h1>Grand Hotel</h1><a href=\"/rooms\">Rooms</a>");

        HospitalityAnalysisResult result = service.analyze(
                evidence.evaluationId(), List.of(evidence), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED);

        assertTrue(result.coverage().assessableDimensions().contains(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION));
        assertFalse(result.findings().stream().anyMatch(f -> f.category() == HospitalityObservationCategory.BOOKING));
        assertFalse(result.findings().stream().anyMatch(f -> f.findingText().toLowerCase().contains("booking")
                && f.findingText().toLowerCase().contains("deficien")));
    }

    private StructuredEvidence evidence(String finalUrl, String body) {
        return evidence(evaluation(), finalUrl, body);
    }

    private StructuredEvidence evidence(Evaluation evaluation, String finalUrl, String body) {
        AcquisitionResult acquisition = new AcquisitionResult(
                AcquisitionOutcome.SUCCESS, WEBSITE, finalUrl, 200, "text/html",
                CLOCK.instant(), AcquisitionMethod.HTTP_PUBLIC, body, Map.of(), List.of(), null);
        EvaluationAttempt attempt = evaluation.currentAttempt();
        CapabilityOutcome capabilityOutcome = CapabilityOutcome.record(
                "public-web-acquisition", CapabilityOutcomeStatus.SUCCEEDED,
                AcquisitionOutcome.SUCCESS.name(), CLOCK.instant());
        EvaluationAcquisitionResult source = new EvaluationAcquisitionResult(evaluation, attempt, acquisition, capabilityOutcome);
        return new StructuredEvidence(
                evaluation.evaluationId(), attempt.attemptId(), attempt.attemptNumber(), source, EvidenceProvenance.DISCOVERED,
                AcquisitionOutcome.SUCCESS, acquisition.requestedUrl(), acquisition.finalUrl(), acquisition.retrievalTimestamp(),
                acquisition.acquisitionMethod(), acquisition.statusCode(), acquisition.contentType(), acquisition.body(), acquisition.errorMessage());
    }

    private Evaluation evaluation() {
        CanonicalEvaluationRequest request = ((ValidationResult.Accepted)
                inputValidator.validate(new HotelEvaluationInput(null, null, WEBSITE))).request();
        return Evaluation.create(request, CLOCK);
    }
}
