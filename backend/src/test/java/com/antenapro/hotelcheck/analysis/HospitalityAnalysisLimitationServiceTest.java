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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

class HospitalityAnalysisLimitationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-04T00:00:00Z"), ZoneOffset.UTC);
    private static final String WEBSITE = "https://hotel.example.com";

    private final HotelEvaluationInputValidator inputValidator = new HotelEvaluationInputValidator();
    private final HospitalityAnalysisLimitationService service = new HospitalityAnalysisLimitationService();

    @Test
    void supportedTimeoutProducesUnableToVerifyLimitation() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.TIMEOUT, null, "request timed out");

        HospitalityAnalysisLimitation limitation = service.create(
                evidence,
                Set.of(HospitalityObservationCategory.BOOKING),
                Set.of(GuestJourneyStage.BOOK)
        );

        assertEquals(HospitalityAnalysisLimitationType.UNABLE_TO_VERIFY, limitation.type());
        assertEquals(AcquisitionOutcome.TIMEOUT, limitation.sourceCondition());
        assertEquals("request timed out", limitation.explanation());
        assertEquals(Set.of(HospitalityObservationCategory.BOOKING), limitation.categories());
        assertEquals(Set.of(GuestJourneyStage.BOOK), limitation.journeyStages());
    }

    @Test
    void sourceConditionIsDerivedFromSupportingEvidence() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.HTTP_ERROR, null, "HTTP request failed with status 503");

        HospitalityAnalysisLimitation limitation = service.create(evidence);

        assertSame(limitation.supportingEvidence().acquisitionOutcome(), limitation.sourceCondition());
        assertEquals(evidence.acquisitionOutcome(), limitation.sourceCondition());
    }

    @Test
    void limitationPreservesEvaluationAttemptAndEvidenceTraceability() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.HTTP_ERROR, null, "HTTP request failed with status 503");

        HospitalityAnalysisLimitation limitation = service.create(evidence);

        assertSame(evidence, limitation.supportingEvidence());
        assertSame(evidence.sourceObservation(), limitation.supportingEvidence().sourceObservation());
        assertEquals(evidence.evaluationId(), limitation.evaluationId());
        assertEquals(evidence.attemptId(), limitation.attemptId());
        assertEquals(evidence.attemptNumber(), limitation.attemptNumber());
        assertEquals(evidence.requestedUrl(), limitation.requestedUrl());
        assertEquals(evidence.finalUrl(), limitation.finalUrl());
        assertEquals(evidence.limitation(), limitation.sourceLimitation());
    }

    @Test
    void sameInputProducesSameLimitationSemantics() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.NETWORK_ERROR, null, "network unavailable");

        HospitalityAnalysisLimitation first = service.create(
                evidence,
                Set.of(HospitalityObservationCategory.BOOKING),
                Set.of(GuestJourneyStage.BOOK)
        );
        HospitalityAnalysisLimitation second = service.create(
                evidence,
                Set.of(HospitalityObservationCategory.BOOKING),
                Set.of(GuestJourneyStage.BOOK)
        );

        assertEquals(first, second);
    }

    @Test
    void successfulEvidenceDoesNotBecomeLimitation() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.SUCCESS, "<a href=\"/book\">Book Now</a>", null);

        assertThrows(IllegalArgumentException.class, () -> service.create(evidence));
    }

    @Test
    void notAttemptedStateCannotBeInventedFromMissingEvidence() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.SUCCESS, null, null);

        assertThrows(IllegalArgumentException.class, () -> service.create(evidence));
    }

    @Test
    void missingEvidenceDoesNotBecomeHotelCapabilityAbsence() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.SUCCESS, null, null);

        assertThrows(IllegalArgumentException.class, () -> service.create(
                evidence,
                Set.of(HospitalityObservationCategory.ROOMS),
                Set.of(GuestJourneyStage.EXPLORE)
        ));
    }

    @Test
    void invalidTargetIsNotInterpretedAsInabilityToVerify() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.INVALID_TARGET, null, "invalid target");

        assertThrows(IllegalArgumentException.class, () -> service.create(evidence));
    }

    @Test
    void unsupportedSchemeIsNotInterpretedAsInabilityToVerify() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.UNSUPPORTED_SCHEME, null, "unsupported scheme");

        assertThrows(IllegalArgumentException.class, () -> service.create(evidence));
    }

    @Test
    void nonDiscoveredEvidenceCannotCreateLimitation() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.TIMEOUT, null, "request timed out");
        StructuredEvidence inferred = new StructuredEvidence(
                evidence.evaluationId(),
                evidence.attemptId(),
                evidence.attemptNumber(),
                evidence.sourceObservation(),
                EvidenceProvenance.INFERRED,
                evidence.acquisitionOutcome(),
                evidence.requestedUrl(),
                evidence.finalUrl(),
                evidence.retrievalTimestamp(),
                evidence.acquisitionMethod(),
                evidence.statusCode(),
                evidence.contentType(),
                evidence.observedContent(),
                evidence.limitation()
        );

        assertThrows(IllegalArgumentException.class, () -> service.create(inferred));
    }

    @Test
    void sourceEvidenceIsNotMutated() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.HTTP_ERROR, null, "HTTP 503");
        String originalLimitation = evidence.limitation();
        AcquisitionOutcome originalOutcome = evidence.acquisitionOutcome();

        HospitalityAnalysisLimitation limitation = service.create(evidence);

        assertSame(evidence, limitation.supportingEvidence());
        assertEquals(originalLimitation, evidence.limitation());
        assertEquals(originalOutcome, evidence.acquisitionOutcome());
        assertFalse(evidence.hasObservedContent());
    }

    @Test
    void nullInputFollowsExistingValidationConvention() {
        assertThrows(NullPointerException.class, () -> service.create(null));
    }

    private StructuredEvidence evidence(AcquisitionOutcome outcome, String body, String limitation) {
        AcquisitionResult acquisition = new AcquisitionResult(
                outcome,
                WEBSITE,
                WEBSITE,
                outcome == AcquisitionOutcome.HTTP_ERROR ? 503 : null,
                body == null ? null : "text/html",
                CLOCK.instant(),
                outcome == AcquisitionOutcome.TIMEOUT || outcome == AcquisitionOutcome.NETWORK_ERROR
                        ? AcquisitionMethod.UNAVAILABLE
                        : AcquisitionMethod.HTTP_PUBLIC,
                body,
                Map.of("source", "first-party"),
                List.of(),
                limitation
        );

        CanonicalEvaluationRequest request = request(WEBSITE);
        Evaluation evaluation = Evaluation.create(request, CLOCK);
        EvaluationAttempt attempt = evaluation.currentAttempt();
        CapabilityOutcome capabilityOutcome = CapabilityOutcome.record(
                "public-web-acquisition",
                outcome == AcquisitionOutcome.SUCCESS
                        ? CapabilityOutcomeStatus.SUCCEEDED
                        : CapabilityOutcomeStatus.FAILED,
                outcome.name(),
                CLOCK.instant()
        );
        EvaluationAcquisitionResult source = new EvaluationAcquisitionResult(
                evaluation,
                attempt,
                acquisition,
                capabilityOutcome
        );

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
                acquisition.errorMessage()
        );
    }

    private CanonicalEvaluationRequest request(String websiteUrl) {
        ValidationResult result = inputValidator.validate(new HotelEvaluationInput(null, null, websiteUrl));
        return ((ValidationResult.Accepted) result).request();
    }
}
