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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HospitalityCoverageAssessmentDerivationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-04T00:00:00Z"), ZoneOffset.UTC);
    private static final String WEBSITE = "https://hotel.example.com";
    private final HotelEvaluationInputValidator inputValidator = new HotelEvaluationInputValidator();
    private final HospitalityCoverageAssessmentDerivationService service =
            new HospitalityCoverageAssessmentDerivationService();

    @Test
    void derivesAllGovernedJourneyMappings() {
        Evaluation evaluation = evaluation();
        HospitalityAnalysisSignal hotel = signal(evaluation, HospitalityObservationCategory.HOTEL_IDENTITY,
                "Grand Hotel", WEBSITE + "/identity");
        HospitalityAnalysisSignal rooms = signal(evaluation, HospitalityObservationCategory.ROOMS,
                "Rooms", WEBSITE + "/rooms");
        HospitalityAnalysisSignal amenities = signal(evaluation, HospitalityObservationCategory.AMENITIES,
                "Pool and Wi-Fi", WEBSITE + "/amenities");
        HospitalityAnalysisSignal contact = signal(evaluation, HospitalityObservationCategory.CONTACT,
                "Contact", WEBSITE + "/contact");
        HospitalityAnalysisSignal booking = signal(evaluation, HospitalityObservationCategory.BOOKING,
                "Book Now", WEBSITE + "/booking");
        HospitalityAnalysisSignal dining = signal(evaluation, HospitalityObservationCategory.DINING,
                "Restaurant", WEBSITE + "/dining");

        HospitalityCoverageAssessment assessment = service.derive(
                evaluation.evaluationId(), List.of(hotel, rooms, amenities, contact, booking, dining), List.of(), List.of());

        assertEquals(Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND,
                GuestJourneyStage.EXPLORE, GuestJourneyStage.BOOK), assessment.assessableJourneyStages());
        assertFalse(assessment.assessableJourneyStages().contains(GuestJourneyStage.TRUST));
    }

    @Test
    void derivesAllSupportedDimensionsAndPreservesUnsupportedDimensions() {
        Evaluation evaluation = evaluation();
        List<HospitalityAnalysisSignal> signals = List.of(
                signal(evaluation, HospitalityObservationCategory.HOTEL_IDENTITY, "Grand Hotel", WEBSITE + "/identity"),
                signal(evaluation, HospitalityObservationCategory.ROOMS, "Rooms", WEBSITE + "/rooms"),
                signal(evaluation, HospitalityObservationCategory.AMENITIES, "Pool", WEBSITE + "/amenities"),
                signal(evaluation, HospitalityObservationCategory.CONTACT, "Contact", WEBSITE + "/contact"),
                signal(evaluation, HospitalityObservationCategory.BOOKING, "Book", WEBSITE + "/booking"),
                signal(evaluation, HospitalityObservationCategory.DINING, "Restaurant", WEBSITE + "/dining")
        );

        HospitalityCoverageAssessment assessment = service.derive(
                evaluation.evaluationId(), signals, List.of(), List.of());

        assertEquals(Set.of(
                HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING,
                HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION,
                HospitalityAnalysisDimension.AMENITIES_AND_GUEST_FACING_INFORMATION,
                HospitalityAnalysisDimension.CONTACT_AND_LOCATION,
                HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS
        ), assessment.assessableDimensions());
        assertTrue(assessment.intendedDimensions().contains(HospitalityAnalysisDimension.DISCOVERABILITY_AND_NAVIGATION));
        assertTrue(assessment.intendedDimensions().contains(HospitalityAnalysisDimension.MOBILE_AND_TECHNICAL_GUEST_EXPERIENCE));
        assertTrue(assessment.intendedDimensions().contains(HospitalityAnalysisDimension.SEO_AND_STRUCTURED_DATA_SUPPORTING_SIGNALS));
        assertFalse(assessment.assessableDimensions().contains(HospitalityAnalysisDimension.DISCOVERABILITY_AND_NAVIGATION));
        assertFalse(assessment.assessableDimensions().contains(HospitalityAnalysisDimension.MOBILE_AND_TECHNICAL_GUEST_EXPERIENCE));
        assertFalse(assessment.assessableDimensions().contains(HospitalityAnalysisDimension.SEO_AND_STRUCTURED_DATA_SUPPORTING_SIGNALS));
    }

    @Test
    void diningMapsOnlyToAmenitiesAndGuestFacingInformation() {
        Evaluation evaluation = evaluation();
        HospitalityAnalysisSignal dining = signal(evaluation, HospitalityObservationCategory.DINING,
                "Restaurant", WEBSITE + "/dining");

        HospitalityCoverageAssessment assessment = service.derive(
                evaluation.evaluationId(), List.of(dining), List.of(), List.of());

        assertEquals(Set.of(HospitalityAnalysisDimension.AMENITIES_AND_GUEST_FACING_INFORMATION),
                assessment.assessableDimensions());
        assertFalse(assessment.assessableDimensions().contains(HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS));
    }

    @Test
    void missingBookingDoesNotCreateBookingCoverageOrFailure() {
        Evaluation evaluation = evaluation();
        HospitalityAnalysisSignal rooms = signal(evaluation, HospitalityObservationCategory.ROOMS,
                "Rooms", WEBSITE + "/rooms");

        HospitalityCoverageAssessment assessment = service.derive(
                evaluation.evaluationId(), List.of(rooms), List.of(), List.of());

        assertFalse(assessment.assessableJourneyStages().contains(GuestJourneyStage.BOOK));
        assertFalse(assessment.limitedJourneyStages().contains(GuestJourneyStage.BOOK));
        assertFalse(assessment.assessableDimensions().contains(
                HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS));
        assertFalse(assessment.limitedDimensions().contains(
                HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS));
    }

    @Test
    void explicitLimitationCreatesLimitedScopeWithoutMakingItAssessable() {
        Evaluation evaluation = evaluation();
        StructuredEvidence evidence = evidence(evaluation, AcquisitionOutcome.TIMEOUT, null, "request timed out");
        HospitalityAnalysisLimitation limitation = new HospitalityAnalysisLimitation(
                Set.of(HospitalityObservationCategory.ROOMS),
                Set.of(GuestJourneyStage.EXPLORE),
                HospitalityAnalysisLimitationType.UNABLE_TO_VERIFY,
                evidence,
                "Room information could not be verified.");

        HospitalityCoverageAssessment assessment = service.derive(
                evaluation.evaluationId(), List.of(), List.of(), List.of(limitation));

        assertEquals(Set.of(GuestJourneyStage.EXPLORE), assessment.limitedJourneyStages());
        assertEquals(Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION),
                assessment.limitedDimensions());
        assertTrue(assessment.coveredJourneyStages().contains(GuestJourneyStage.EXPLORE));
        assertTrue(assessment.coveredDimensions().contains(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION));
        assertFalse(assessment.assessableJourneyStages().contains(GuestJourneyStage.EXPLORE));
        assertFalse(assessment.assessableDimensions().contains(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION));
    }

    @Test
    void unscopedLimitationDoesNotBecomeArbitraryJourneyOrDimensionCoverage() {
        Evaluation evaluation = evaluation();
        StructuredEvidence evidence = evidence(evaluation, AcquisitionOutcome.TIMEOUT, null, "request timed out");
        HospitalityAnalysisLimitation limitation = new HospitalityAnalysisLimitation(
                Set.of(), Set.of(), HospitalityAnalysisLimitationType.UNABLE_TO_VERIFY, evidence, "Unable to verify.");

        HospitalityCoverageAssessment assessment = service.derive(
                evaluation.evaluationId(), List.of(), List.of(), List.of(limitation));

        assertTrue(assessment.limitedJourneyStages().isEmpty());
        assertTrue(assessment.limitedDimensions().isEmpty());
        assertTrue(assessment.coveredJourneyStages().isEmpty());
        assertTrue(assessment.coveredDimensions().isEmpty());
    }

    @Test
    void typedMaterialIdentityConflictEstablishesTrustAndClarity() {
        Evaluation evaluation = evaluation();
        HospitalityAnalysisSignal first = signal(evaluation, HospitalityObservationCategory.HOTEL_IDENTITY,
                "Grand Hotel Mumbai", WEBSITE + "/source-a");
        HospitalityAnalysisSignal second = signal(evaluation, HospitalityObservationCategory.HOTEL_IDENTITY,
                "Sunrise Resort Pune", WEBSITE + "/source-b");

        HospitalityFinding firstFinding = new HospitalityFinding(first, HospitalityFindingStatus.VERIFIED_OBSERVED);
        HospitalityFinding secondFinding = new HospitalityFinding(second, HospitalityFindingStatus.VERIFIED_OBSERVED);

        HospitalityCoverageAssessment assessment = service.derive(
                evaluation.evaluationId(), List.of(first, second), List.of(firstFinding, secondFinding), List.of());

        assertTrue(assessment.assessableDimensions().contains(HospitalityAnalysisDimension.TRUST_AND_CLARITY));
    }

    @Test
    void nonConflictingIdentityObservationsDoNotEstablishTrust() {
        Evaluation evaluation = evaluation();
        HospitalityAnalysisSignal first = signal(evaluation, HospitalityObservationCategory.HOTEL_IDENTITY,
                "Grand Hotel", WEBSITE + "/source-a");
        HospitalityAnalysisSignal second = signal(evaluation, HospitalityObservationCategory.HOTEL_IDENTITY,
                "Grand Hotel", WEBSITE + "/source-b");

        HospitalityFinding firstFinding = new HospitalityFinding(first, HospitalityFindingStatus.VERIFIED_OBSERVED);
        HospitalityFinding secondFinding = new HospitalityFinding(second, HospitalityFindingStatus.VERIFIED_OBSERVED);

        HospitalityCoverageAssessment assessment = service.derive(
                evaluation.evaluationId(), List.of(first, second), List.of(firstFinding, secondFinding), List.of());

        assertFalse(assessment.assessableDimensions().contains(HospitalityAnalysisDimension.TRUST_AND_CLARITY));
    }

    @Test
    void mixedEvaluationInputIsRejectedInsteadOfSilentlyFiltered() {
        Evaluation first = evaluation();
        Evaluation second = evaluation();
        HospitalityAnalysisSignal signal = signal(second, HospitalityObservationCategory.ROOMS,
                "Rooms", WEBSITE + "/rooms");

        assertThrows(IllegalArgumentException.class, () -> service.derive(
                first.evaluationId(), List.of(signal), List.of(), List.of()));
    }

    @Test
    void assessmentRetainsEvaluationIdentityAndSatisfiesStructuralInvariants() {
        Evaluation evaluation = evaluation();
        HospitalityAnalysisSignal rooms = signal(evaluation, HospitalityObservationCategory.ROOMS,
                "Rooms", WEBSITE + "/rooms");
        StructuredEvidence evidence = evidence(evaluation, AcquisitionOutcome.TIMEOUT, null, "request timed out");
        HospitalityAnalysisLimitation limitation = new HospitalityAnalysisLimitation(
                Set.of(HospitalityObservationCategory.BOOKING),
                Set.of(GuestJourneyStage.BOOK),
                HospitalityAnalysisLimitationType.UNABLE_TO_VERIFY,
                evidence,
                "Booking could not be verified.");

        HospitalityCoverageAssessment assessment = service.derive(
                evaluation.evaluationId(), List.of(rooms), List.of(), List.of(limitation));

        assertEquals(evaluation.evaluationId(), assessment.evaluationId());
        assertTrue(assessment.intendedJourneyStages().containsAll(assessment.assessableJourneyStages()));
        assertTrue(assessment.intendedJourneyStages().containsAll(assessment.limitedJourneyStages()));
        assertTrue(assessment.intendedDimensions().containsAll(assessment.assessableDimensions()));
        assertTrue(assessment.intendedDimensions().containsAll(assessment.limitedDimensions()));
        assertTrue(Set.copyOf(assessment.assessableJourneyStages()).stream()
                .noneMatch(assessment.limitedJourneyStages()::contains));
        assertTrue(Set.copyOf(assessment.assessableDimensions()).stream()
                .noneMatch(assessment.limitedDimensions()::contains));
        assertEquals(Set.of(GuestJourneyStage.EXPLORE, GuestJourneyStage.BOOK), assessment.coveredJourneyStages());
        assertEquals(Set.of(
                HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION,
                HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS
        ), assessment.coveredDimensions());
    }

    @Test
    void identicalGovernedInputProducesIdenticalAssessment() {
        Evaluation evaluation = evaluation();
        HospitalityAnalysisSignal rooms = signal(evaluation, HospitalityObservationCategory.ROOMS,
                "Rooms", WEBSITE + "/rooms");

        HospitalityCoverageAssessment first = service.derive(
                evaluation.evaluationId(), List.of(rooms), List.of(), List.of());
        HospitalityCoverageAssessment second = service.derive(
                evaluation.evaluationId(), List.of(rooms), List.of(), List.of());

        assertEquals(first, second);
    }

    @Test
    void returnedAssessmentCollectionsAreImmutable() {
        Evaluation evaluation = evaluation();
        HospitalityAnalysisSignal rooms = signal(evaluation, HospitalityObservationCategory.ROOMS,
                "Rooms", WEBSITE + "/rooms");
        HospitalityCoverageAssessment assessment = service.derive(
                evaluation.evaluationId(), List.of(rooms), List.of(), List.of());

        assertThrows(UnsupportedOperationException.class,
                () -> assessment.assessableJourneyStages().add(GuestJourneyStage.BOOK));
        assertThrows(UnsupportedOperationException.class,
                () -> assessment.coveredDimensions().clear());
    }

    private HospitalityAnalysisSignal signal(
            Evaluation evaluation,
            HospitalityObservationCategory category,
            String observedValue,
            String sourceReference
    ) {
        StructuredEvidence evidence = evidence(evaluation, AcquisitionOutcome.SUCCESS,
                "<h1>Hotel</h1>", null);
        HospitalityObservation observation = new HospitalityObservation(
                category,
                observedValue,
                sourceReference,
                evidence,
                evaluation.evaluationId(),
                evaluation.currentAttempt().attemptId(),
                evaluation.currentAttempt().attemptNumber(),
                EvidenceProvenance.DISCOVERED
        );

        Set<GuestJourneyStage> journeyStages = switch (category) {
            case HOTEL_IDENTITY -> Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND);
            case ROOMS -> Set.of(GuestJourneyStage.EXPLORE);
            case AMENITIES -> Set.of(GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE);
            case CONTACT -> Set.of(GuestJourneyStage.DISCOVER);
            case BOOKING -> Set.of(GuestJourneyStage.BOOK);
            case DINING -> Set.of(GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE);
        };

        return new HospitalityAnalysisSignal(
                observation,
                journeyStages,
                "governed test signal",
                HospitalityAnalysisSignalStatus.QUALIFIED
        );
    }

    private StructuredEvidence evidence(
            Evaluation evaluation,
            AcquisitionOutcome outcome,
            String body,
            String limitation
    ) {
        AcquisitionResult acquisition = new AcquisitionResult(
                outcome,
                WEBSITE,
                WEBSITE,
                outcome == AcquisitionOutcome.SUCCESS ? 200 : null,
                outcome == AcquisitionOutcome.SUCCESS ? "text/html" : null,
                CLOCK.instant(),
                outcome == AcquisitionOutcome.SUCCESS
                        ? AcquisitionMethod.HTTP_PUBLIC
                        : AcquisitionMethod.UNAVAILABLE,
                body,
                Map.of(),
                List.of(),
                limitation
        );
        EvaluationAttempt attempt = evaluation.currentAttempt();
        CapabilityOutcome capabilityOutcome = CapabilityOutcome.record(
                "public-web-acquisition",
                outcome == AcquisitionOutcome.SUCCESS ? CapabilityOutcomeStatus.SUCCEEDED : CapabilityOutcomeStatus.FAILED,
                outcome.name(),
                CLOCK.instant()
        );
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
                acquisition.errorMessage()
        );
    }

    private Evaluation evaluation() {
        ValidationResult result = inputValidator.validate(new HotelEvaluationInput(null, null, WEBSITE));
        CanonicalEvaluationRequest request = ((ValidationResult.Accepted) result).request();
        return Evaluation.create(request, CLOCK);
    }
}
