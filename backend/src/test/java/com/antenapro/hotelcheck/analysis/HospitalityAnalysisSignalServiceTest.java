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

class HospitalityAnalysisSignalServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-04T00:00:00Z"), ZoneOffset.UTC);
    private static final String WEBSITE = "https://hotel.example.com";

    private final HotelEvaluationInputValidator inputValidator = new HotelEvaluationInputValidator();
    private final HospitalityAnalysisSignalService service = new HospitalityAnalysisSignalService();

    @Test
    void qualifiesBookingEntryPointAsBookJourneySignalWithoutClaimingBookingSuccess() {
        StructuredEvidence evidence = evidence(successfulAcquisition("<a href=\"/book\">Book Now</a>"));
        HospitalityObservation observation = observation(HospitalityObservationCategory.BOOKING, "Book Now -> /book", evidence);

        HospitalityAnalysisSignal signal = service.qualify(observation).orElseThrow();

        assertEquals(Set.of(GuestJourneyStage.BOOK), signal.journeyStages());
        assertEquals("A booking entry point was observed.", signal.interpretation());
        assertEquals(HospitalityAnalysisSignalStatus.QUALIFIED, signal.status());
        assertSame(observation, signal.originatingObservation());
        assertSame(evidence, signal.originatingObservation().supportingEvidence());
        assertFalse(signal.interpretation().toLowerCase().contains("success"));
        assertFalse(signal.interpretation().toLowerCase().contains("usable"));
        assertFalse(signal.interpretation().toLowerCase().contains("accepts reservations"));
    }

    @Test
    void qualifiesRoomsWithoutInferringInventoryOrAvailability() {
        StructuredEvidence evidence = evidence(successfulAcquisition("<a href=\"/rooms\">Rooms</a>"));
        HospitalityObservation observation = observation(HospitalityObservationCategory.ROOMS, "Rooms -> /rooms", evidence);

        HospitalityAnalysisSignal signal = service.qualify(observation).orElseThrow();

        assertEquals(Set.of(GuestJourneyStage.EXPLORE), signal.journeyStages());
        assertEquals("A room-related entry point or room information was observed.", signal.interpretation());
        assertFalse(signal.interpretation().toLowerCase().contains("available"));
        assertFalse(signal.interpretation().toLowerCase().contains("inventory"));
    }

    @Test
    void contactObservationMapsOnlyToDiscoverNotTrust() {
        StructuredEvidence evidence = evidence(successfulAcquisition("<a href=\"/contact\">Contact Us</a>"));
        HospitalityObservation observation = observation(HospitalityObservationCategory.CONTACT, "Contact Us", evidence);

        HospitalityAnalysisSignal signal = service.qualify(observation).orElseThrow();

        assertEquals(Set.of(GuestJourneyStage.DISCOVER), signal.journeyStages());
        assertFalse(signal.journeyStages().contains(GuestJourneyStage.TRUST));
        assertEquals("A guest contact or location path was observed.", signal.interpretation());
    }

    @Test
    void mapsEachCurrentObservationCategoryOnlyToExplicitJourneyStages() {
        StructuredEvidence evidence = evidence(successfulAcquisition("<h1>Sunrise Hotel</h1>"));

        assertEquals(Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND),
                service.qualify(observation(HospitalityObservationCategory.HOTEL_IDENTITY, "Sunrise Hotel", evidence)).orElseThrow().journeyStages());
        assertEquals(Set.of(GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE),
                service.qualify(observation(HospitalityObservationCategory.AMENITIES, "Amenities", evidence)).orElseThrow().journeyStages());
        assertEquals(Set.of(GuestJourneyStage.DISCOVER),
                service.qualify(observation(HospitalityObservationCategory.CONTACT, "Contact Us", evidence)).orElseThrow().journeyStages());
        assertEquals(Set.of(GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE),
                service.qualify(observation(HospitalityObservationCategory.DINING, "Dining", evidence)).orElseThrow().journeyStages());
    }

    @Test
    void preservesObservationAttributionAndEvidenceWithoutCopyingEvidenceIntoSignal() {
        StructuredEvidence evidence = evidence(successfulAcquisition("<a href=\"/book\">Book Now</a>"));
        HospitalityObservation observation = observation(HospitalityObservationCategory.BOOKING, "Book Now -> /book", evidence);

        HospitalityAnalysisSignal signal = service.qualify(observation).orElseThrow();

        assertSame(observation, signal.originatingObservation());
        assertSame(evidence, signal.originatingObservation().supportingEvidence());
        assertEquals(evidence.evaluationId(), signal.originatingObservation().evaluationId());
        assertEquals(evidence.attemptId(), signal.originatingObservation().attemptId());
        assertEquals(evidence.attemptNumber(), signal.originatingObservation().attemptNumber());
        assertEquals(evidence.sourceProvenance(), signal.originatingObservation().provenance());
    }

    @Test
    void doesNotQualifyFailedEvidenceAsAnAnalysisSignal() {
        StructuredEvidence evidence = evidence(failedAcquisition());
        HospitalityObservation observation = observation(HospitalityObservationCategory.BOOKING, "Book Now", evidence);

        assertTrue(service.qualify(observation).isEmpty());
    }

    @Test
    void doesNotQualifyInferredObservationAsObservedAnalysisSignal() {
        StructuredEvidence evidence = evidence(successfulAcquisition("<h1>Sunrise Hotel</h1>"));
        HospitalityObservation observation = new HospitalityObservation(
                HospitalityObservationCategory.HOTEL_IDENTITY,
                "Sunrise Hotel",
                WEBSITE + " [h1]",
                evidence,
                evidence.evaluationId(),
                evidence.attemptId(),
                evidence.attemptNumber(),
                EvidenceProvenance.INFERRED
        );

        assertTrue(service.qualify(observation).isEmpty());
    }

    @Test
    void ambiguousRoomObservationIsNotStrengthenedIntoAvailabilityClaim() {
        StructuredEvidence evidence = evidence(successfulAcquisition("<h1>Sunrise Hotel</h1>"));
        HospitalityObservation observation = observation(HospitalityObservationCategory.ROOMS, "Rooms", evidence);

        HospitalityAnalysisSignal signal = service.qualify(observation).orElseThrow();

        assertEquals("A room-related entry point or room information was observed.", signal.interpretation());
        assertFalse(signal.interpretation().toLowerCase().contains("available"));
    }

    @Test
    void repeatedQualificationIsDeterministicAndDoesNotMutateObservation() {
        StructuredEvidence evidence = evidence(successfulAcquisition("<a href=\"/book\">Book Now</a>"));
        HospitalityObservation observation = observation(HospitalityObservationCategory.BOOKING, "Book Now -> /book", evidence);

        List<String> first = service.qualify(observation).stream().map(this::signature).toList();
        List<String> second = service.qualify(observation).stream().map(this::signature).toList();

        assertEquals(first, second);
        assertEquals(HospitalityObservationCategory.BOOKING, observation.category());
        assertEquals("Book Now -> /book", observation.observedValue());
        assertSame(evidence, observation.supportingEvidence());
        assertEquals(EvidenceProvenance.DISCOVERED, observation.provenance());
    }

    @Test
    void rejectsNullObservationInsteadOfProducingANegativeSignal() {
        assertThrows(NullPointerException.class, () -> service.qualify(null));
    }

    private String signature(HospitalityAnalysisSignal signal) {
        return signal.originatingObservation().category() + "|"
                + signal.journeyStages() + "|"
                + signal.interpretation() + "|"
                + signal.status();
    }

    private HospitalityObservation observation(
            HospitalityObservationCategory category,
            String observedValue,
            StructuredEvidence evidence
    ) {
        return new HospitalityObservation(
                category,
                observedValue,
                evidence.finalUrl() + " [signal]",
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
