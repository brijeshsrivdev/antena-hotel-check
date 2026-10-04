package com.antenapro.hotelcheck.evidence;

import com.antenapro.hotelcheck.acquisition.AcquisitionMethod;
import com.antenapro.hotelcheck.acquisition.AcquisitionOutcome;
import com.antenapro.hotelcheck.acquisition.AcquisitionResult;
import com.antenapro.hotelcheck.evaluation.CapabilityOutcome;
import com.antenapro.hotelcheck.evaluation.CapabilityOutcomeStatus;
import com.antenapro.hotelcheck.evaluation.Evaluation;
import com.antenapro.hotelcheck.evaluation.EvaluationAcquisitionResult;
import com.antenapro.hotelcheck.evaluation.EvaluationAttempt;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvidenceNormalizationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-04T00:00:00Z"), ZoneOffset.UTC);
    private static final String WEBSITE = "https://hotel.example.com";
    private final HotelEvaluationInputValidator inputValidator = new HotelEvaluationInputValidator();
    private final EvidenceNormalizationService service = new EvidenceNormalizationService();

    @Test
    void successfulAcquisitionProducesStructuredDiscoveredEvidence() {
        EvaluationAcquisitionResult source = source(successfulAcquisition("<html><body>24-hour front desk</body></html>"));

        StructuredEvidence evidence = service.normalize(source);

        assertEquals(source.evaluation().evaluationId(), evidence.evaluationId());
        assertEquals(source.attempt().attemptId(), evidence.attemptId());
        assertEquals(source.attempt().attemptNumber(), evidence.attemptNumber());
        assertSame(source, evidence.sourceObservation());
        assertEquals(EvidenceProvenance.DISCOVERED, evidence.sourceProvenance());
        assertEquals(AcquisitionOutcome.SUCCESS, evidence.acquisitionOutcome());
        assertTrue(evidence.representsSuccessfulObservation());
    }

    @Test
    void sourceMetadataAndObservedContentArePreservedWithoutSemanticExtraction() {
        Instant retrievedAt = Instant.parse("2026-10-04T00:00:01Z");
        String body = "<html><body>24-hour front desk</body></html>";
        AcquisitionResult acquisition = new AcquisitionResult(
                AcquisitionOutcome.SUCCESS,
                WEBSITE,
                "https://hotel.example.com/rooms",
                200,
                "text/html; charset=UTF-8",
                retrievedAt,
                AcquisitionMethod.HTTP_PUBLIC,
                body,
                Map.of("source", "first-party"),
                List.of(WEBSITE, "https://hotel.example.com/rooms"),
                null
        );

        StructuredEvidence evidence = service.normalize(source(acquisition));

        assertEquals(WEBSITE, evidence.requestedUrl());
        assertEquals("https://hotel.example.com/rooms", evidence.finalUrl());
        assertEquals(retrievedAt, evidence.retrievalTimestamp());
        assertEquals(AcquisitionMethod.HTTP_PUBLIC, evidence.acquisitionMethod());
        assertEquals(200, evidence.statusCode());
        assertEquals("text/html; charset=UTF-8", evidence.contentType());
        assertEquals(body, evidence.observedContent());
        assertTrue(evidence.hasObservedContent());
        assertNull(evidence.limitation());
        assertFalse(evidence.toString().contains("frontDesk24Hours"));
    }

    @Test
    void failureProducesLimitationWithoutObservedContentOrFeatureAbsence() {
        AcquisitionResult acquisition = new AcquisitionResult(
                AcquisitionOutcome.TIMEOUT,
                WEBSITE,
                WEBSITE,
                null,
                null,
                Instant.parse("2026-10-04T00:00:02Z"),
                AcquisitionMethod.UNAVAILABLE,
                null,
                Map.of("reason", "timeout"),
                List.of(),
                "request timed out"
        );

        StructuredEvidence evidence = service.normalize(source(acquisition));

        assertEquals(AcquisitionOutcome.TIMEOUT, evidence.acquisitionOutcome());
        assertEquals("request timed out", evidence.limitation());
        assertNull(evidence.observedContent());
        assertFalse(evidence.hasObservedContent());
        assertFalse(evidence.representsSuccessfulObservation());
        assertEquals(EvidenceProvenance.DISCOVERED, evidence.sourceProvenance());
    }

    @Test
    void unavailableHttpErrorRemainsDistinguishableFromSuccess() {
        AcquisitionResult acquisition = new AcquisitionResult(
                AcquisitionOutcome.HTTP_ERROR,
                WEBSITE,
                WEBSITE,
                503,
                "text/html",
                Instant.parse("2026-10-04T00:00:03Z"),
                AcquisitionMethod.HTTP_PUBLIC,
                null,
                Map.of(),
                List.of(),
                "HTTP request failed with status 503"
        );

        StructuredEvidence evidence = service.normalize(source(acquisition));

        assertEquals(AcquisitionOutcome.HTTP_ERROR, evidence.acquisitionOutcome());
        assertEquals(503, evidence.statusCode());
        assertNull(evidence.observedContent());
        assertEquals("HTTP request failed with status 503", evidence.limitation());
    }

    @Test
    void normalizationDoesNotMutateOrReplaceAcquisitionResult() {
        AcquisitionResult acquisition = successfulAcquisition("body");
        EvaluationAcquisitionResult source = source(acquisition);

        StructuredEvidence evidence = service.normalize(source);

        assertSame(acquisition, source.acquisitionResult());
        assertSame(source, evidence.sourceObservation());
        assertEquals(AcquisitionOutcome.SUCCESS, acquisition.outcome());
        assertEquals("body", acquisition.body());
        assertEquals(WEBSITE, acquisition.requestedUrl());
    }

    private EvaluationAcquisitionResult source(AcquisitionResult acquisition) {
        CanonicalEvaluationRequest request = request(WEBSITE);
        Evaluation evaluation = Evaluation.create(request, CLOCK);
        EvaluationAttempt attempt = evaluation.currentAttempt();
        CapabilityOutcome outcome = CapabilityOutcome.record(
                "public-web-acquisition",
                acquisition.outcome() == AcquisitionOutcome.SUCCESS
                        ? CapabilityOutcomeStatus.SUCCEEDED
                        : CapabilityOutcomeStatus.FAILED,
                acquisition.outcome().name(),
                CLOCK.instant()
        );
        return new EvaluationAcquisitionResult(evaluation, attempt, acquisition, outcome);
    }

    private AcquisitionResult successfulAcquisition(String body) {
        return new AcquisitionResult(
                AcquisitionOutcome.SUCCESS,
                WEBSITE,
                WEBSITE,
                200,
                "text/html",
                CLOCK.instant(),
                AcquisitionMethod.HTTP_PUBLIC,
                body,
                Map.of(),
                List.of(),
                null
        );
    }

    private CanonicalEvaluationRequest request(String websiteUrl) {
        ValidationResult result = inputValidator.validate(new HotelEvaluationInput(null, null, websiteUrl));
        return ((ValidationResult.Accepted) result).request();
    }
}
