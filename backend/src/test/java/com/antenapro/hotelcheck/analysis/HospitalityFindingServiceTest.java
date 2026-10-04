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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HospitalityFindingServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-04T00:00:00Z"), ZoneOffset.UTC);
    private static final String WEBSITE = "https://hotel.example.com";

    private final HotelEvaluationInputValidator inputValidator = new HotelEvaluationInputValidator();
    private final HospitalityFindingService service = new HospitalityFindingService();

    @Test
    void createsVerifiedObservedFindingFromSupportedSignal() {
        StructuredEvidence evidence = evidence(successfulAcquisition("<a href=\"/book\">Book Now</a>"));
        HospitalityObservation observation = observation(HospitalityObservationCategory.BOOKING, "Book Now -> /book", evidence);
        HospitalityAnalysisSignal signal = signal(observation, Set.of(GuestJourneyStage.BOOK), "A booking entry point was observed.");

        HospitalityFinding finding = service.create(signal).orElseThrow();

        assertEquals(HospitalityFindingStatus.VERIFIED_OBSERVED, finding.status());
        assertSame(signal, finding.originatingSignal());
        assertEquals(HospitalityObservationCategory.BOOKING, finding.category());
        assertEquals(Set.of(GuestJourneyStage.BOOK), finding.journeyStages());
        assertEquals("A booking entry point was observed.", finding.findingText());
    }

    @Test
    void preservesJourneyStageTraceabilityAndEvaluationAttemptAttribution() {
        StructuredEvidence evidence = evidence(successfulAcquisition("<h1>Sunrise Hotel</h1>"));
        HospitalityObservation observation = observation(HospitalityObservationCategory.HOTEL_IDENTITY, "Sunrise Hotel", evidence);
        HospitalityAnalysisSignal signal = signal(
                observation,
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND),
                "Hotel/property identity information was observed."
        );

        HospitalityFinding finding = service.create(signal).orElseThrow();

        assertSame(signal, finding.originatingSignal());
        assertSame(observation, finding.observation());
        assertSame(evidence, finding.supportingEvidence());
        assertEquals(evidence.evaluationId(), finding.evaluationId());
        assertEquals(evidence.attemptId(), finding.attemptId());
        assertEquals(evidence.attemptNumber(), finding.attemptNumber());
    }

    @Test
    void bookingEntryPointDoesNotBecomeBookingSuccess() {
        StructuredEvidence evidence = evidence(successfulAcquisition("<a href=\"/book\">Book Now</a>"));
        HospitalityObservation observation = observation(HospitalityObservationCategory.BOOKING, "Book Now -> /book", evidence);
        HospitalityAnalysisSignal signal = signal(observation, Set.of(GuestJourneyStage.BOOK), "A booking entry point was observed.");

        HospitalityFinding finding = service.create(signal).orElseThrow();

        assertEquals("A booking entry point was observed.", finding.findingText());
        assertFalse(finding.findingText().toLowerCase().contains("booking succeeds"));
        assertFalse(finding.findingText().toLowerCase().contains("accepts reservations"));
        assertFalse(finding.findingText().toLowerCase().contains("completed"));
    }

    @Test
    void roomInformationDoesNotBecomeAvailability() {
        StructuredEvidence evidence = evidence(successfulAcquisition("<a href=\"/rooms\">Rooms</a>"));
        HospitalityObservation observation = observation(HospitalityObservationCategory.ROOMS, "Rooms -> /rooms", evidence);
        HospitalityAnalysisSignal signal = signal(observation, Set.of(GuestJourneyStage.EXPLORE), "A room-related entry point or room information was observed.");

        HospitalityFinding finding = service.create(signal).orElseThrow();

        assertEquals("A room-related entry point or room information was observed.", finding.findingText());
        assertFalse(finding.findingText().toLowerCase().contains("available"));
        assertFalse(finding.findingText().toLowerCase().contains("availability"));
        assertFalse(finding.findingText().toLowerCase().contains("inventory"));
    }

    @Test
    void failedEvidenceCannotBecomeObservedDeficiencyOrVerifiedFinding() {
        StructuredEvidence evidence = evidence(failedAcquisition());
        HospitalityObservation observation = observation(HospitalityObservationCategory.BOOKING, "Book Now", evidence);
        HospitalityAnalysisSignal signal = signal(observation, Set.of(GuestJourneyStage.BOOK), "A booking entry point was observed.");

        assertTrue(service.create(signal).isEmpty());
    }

    @Test
    void inferredObservationCannotBecomeVerifiedFinding() {
        StructuredEvidence evidence = evidence(successfulAcquisition("<h1>Sunrise Hotel</h1>"));
        HospitalityObservation observation = new HospitalityObservation(
                HospitalityObservationCategory.HOTEL_IDENTITY,
                "Sunrise Hotel",
                WEBSITE + " [inferred]",
                evidence,
                evidence.evaluationId(),
                evidence.attemptId(),
                evidence.attemptNumber(),
                EvidenceProvenance.INFERRED
        );
        HospitalityAnalysisSignal signal = signal(observation, Set.of(GuestJourneyStage.DISCOVER), "Hotel/property identity information was observed.");

        assertTrue(service.create(signal).isEmpty());
    }

    @Test
    void demonstrationObservationCannotBecomeVerifiedFinding() {
        StructuredEvidence evidence = evidence(successfulAcquisition("<h1>Sunrise Hotel</h1>"));
        HospitalityObservation observation = new HospitalityObservation(
                HospitalityObservationCategory.HOTEL_IDENTITY,
                "Sunrise Hotel",
                WEBSITE + " [demo]",
                evidence,
                evidence.evaluationId(),
                evidence.attemptId(),
                evidence.attemptNumber(),
                EvidenceProvenance.DEMONSTRATION
        );
        HospitalityAnalysisSignal signal = signal(observation, Set.of(GuestJourneyStage.DISCOVER), "Hotel/property identity information was observed.");

        assertTrue(service.create(signal).isEmpty());
    }

    @Test
    void sourceSignalAndObservationAreNotMutated() {
        StructuredEvidence evidence = evidence(successfulAcquisition("<a href=\"/book\">Book Now</a>"));
        HospitalityObservation observation = observation(HospitalityObservationCategory.BOOKING, "Book Now -> /book", evidence);
        HospitalityAnalysisSignal signal = signal(observation, Set.of(GuestJourneyStage.BOOK), "A booking entry point was observed.");

        service.create(signal).orElseThrow();

        assertEquals(HospitalityAnalysisSignalStatus.QUALIFIED, signal.status());
        assertSame(observation, signal.originatingObservation());
        assertEquals("Book Now -> /book", observation.observedValue());
        assertEquals(EvidenceProvenance.DISCOVERED, observation.provenance());
        assertSame(evidence, observation.supportingEvidence());
    }

    @Test
    void repeatedCreationIsDeterministic() {
        StructuredEvidence evidence = evidence(successfulAcquisition("<a href=\"/rooms\">Rooms</a>"));
        HospitalityObservation observation = observation(HospitalityObservationCategory.ROOMS, "Rooms -> /rooms", evidence);
        HospitalityAnalysisSignal signal = signal(observation, Set.of(GuestJourneyStage.EXPLORE), "A room-related entry point or room information was observed.");

        HospitalityFinding first = service.create(signal).orElseThrow();
        HospitalityFinding second = service.create(signal).orElseThrow();

        assertEquals(first, second);
        assertEquals(first.findingText(), second.findingText());
        assertEquals(first.status(), second.status());
    }

    @Test
    void findingStatusVocabularyMatchesSpecificationWithoutAddingStates() {
        assertEquals(
                Set.of(
                        HospitalityFindingStatus.VERIFIED_OBSERVED,
                        HospitalityFindingStatus.LIMITATION,
                        HospitalityFindingStatus.INSUFFICIENT_EVIDENCE,
                        HospitalityFindingStatus.NOT_APPLICABLE
                ),
                Set.of(HospitalityFindingStatus.values())
        );
    }

    @Test
    void nullSignalIsRejectedConsistently() {
        assertThrows(NullPointerException.class, () -> service.create(null));
    }

    private HospitalityAnalysisSignal signal(
            HospitalityObservation observation,
            Set<GuestJourneyStage> journeyStages,
            String interpretation
    ) {
        return new HospitalityAnalysisSignal(
                observation,
                journeyStages,
                interpretation,
                HospitalityAnalysisSignalStatus.QUALIFIED
        );
    }

    private HospitalityObservation observation(
            HospitalityObservationCategory category,
            String observedValue,
            StructuredEvidence evidence
    ) {
        return new HospitalityObservation(
                category,
                observedValue,
                evidence.finalUrl() + " [finding]",
                evidence,
                evidence.evaluationId(),
                evidence.attemptId(),
                evidence.attemptNumber(),
                EvidenceProvenance.DISCOVERED
        );
    }

    private StructuredEvidence evidence(AcquisitionResult acquisition) {
        Evaluation evaluation = Evaluation.create(request(), CLOCK);
        EvaluationAttempt attempt = evaluation.currentAttempt();
        CapabilityOutcome outcome = CapabilityOutcome.record(
                "public-web-acquisition",
                acquisition.outcome() == AcquisitionOutcome.SUCCESS
                        ? CapabilityOutcomeStatus.SUCCEEDED
                        : CapabilityOutcomeStatus.FAILED,
                acquisition.outcome().name(),
                CLOCK.instant()
        );
        EvaluationAcquisitionResult source = new EvaluationAcquisitionResult(evaluation, attempt, acquisition, outcome);

        return new StructuredEvidence(
                evaluation.evaluationId(),
                attempt.attemptId(),
                attempt.attemptNumber(),
                source,
                EvidenceProvenance.DISCOVERED,
                acquisition.outcome(),
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

    private AcquisitionResult failedAcquisition() {
        return new AcquisitionResult(
                AcquisitionOutcome.TIMEOUT,
                WEBSITE,
                WEBSITE,
                null,
                null,
                CLOCK.instant(),
                AcquisitionMethod.UNAVAILABLE,
                null,
                Map.of(),
                List.of(),
                "request timed out"
        );
    }

    private CanonicalEvaluationRequest request() {
        ValidationResult result = inputValidator.validate(new HotelEvaluationInput(null, null, WEBSITE));
        return ((ValidationResult.Accepted) result).request();
    }
}
