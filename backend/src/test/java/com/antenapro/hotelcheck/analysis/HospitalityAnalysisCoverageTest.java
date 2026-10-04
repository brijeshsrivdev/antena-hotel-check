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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HospitalityAnalysisCoverageTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-04T00:00:00Z"), ZoneOffset.UTC);
    private static final String WEBSITE = "https://hotel.example.com";
    private final HotelEvaluationInputValidator inputValidator = new HotelEvaluationInputValidator();
    private final HospitalityAnalysisLimitationService limitationService = new HospitalityAnalysisLimitationService();

    @Test
    void coverageStateVocabularyIsExactlyTheThreeSpecificationStates() {
        assertEquals(
                Set.of(
                        HospitalityAnalysisCoverageState.SUBSTANTIALLY_ASSESSED,
                        HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED,
                        HospitalityAnalysisCoverageState.INSUFFICIENT_COVERAGE
                ),
                Set.of(HospitalityAnalysisCoverageState.values())
        );
    }

    @Test
    void hospitalityDimensionsRepresentTheSpecificationScope() {
        assertEquals(9, HospitalityAnalysisDimension.values().length);
        assertTrue(Set.of(HospitalityAnalysisDimension.values()).containsAll(Set.of(
                HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING,
                HospitalityAnalysisDimension.DISCOVERABILITY_AND_NAVIGATION,
                HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION,
                HospitalityAnalysisDimension.AMENITIES_AND_GUEST_FACING_INFORMATION,
                HospitalityAnalysisDimension.CONTACT_AND_LOCATION,
                HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS,
                HospitalityAnalysisDimension.TRUST_AND_CLARITY,
                HospitalityAnalysisDimension.MOBILE_AND_TECHNICAL_GUEST_EXPERIENCE,
                HospitalityAnalysisDimension.SEO_AND_STRUCTURED_DATA_SUPPORTING_SIGNALS
        )));
    }

    @Test
    void coverageRetainsGuestJourneyAndIntendedHospitalityScope() {
        UUID evaluationId = UUID.randomUUID();
        HospitalityAnalysisCoverage coverage = coverage(
                evaluationId,
                HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED,
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE, GuestJourneyStage.TRUST, GuestJourneyStage.BOOK),
                Set.of(HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING, HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION),
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND),
                Set.of(HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING),
                Set.of(GuestJourneyStage.EXPLORE, GuestJourneyStage.BOOK),
                Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION),
                "explicitly assessed from intended hospitality scope"
        );

        assertEquals(evaluationId, coverage.evaluationId());
        assertEquals(Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE, GuestJourneyStage.TRUST, GuestJourneyStage.BOOK), coverage.intendedJourneyStages());
        assertEquals(Set.of(HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING, HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION), coverage.intendedDimensions());
        assertEquals(Set.of(GuestJourneyStage.EXPLORE, GuestJourneyStage.BOOK), coverage.limitedJourneyStages());
        assertEquals(Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION), coverage.limitedDimensions());
    }

    @Test
    void coverageCanRepresentEachQualitativeStateWithoutAThreshold() {
        UUID evaluationId = UUID.randomUUID();
        for (HospitalityAnalysisCoverageState state : HospitalityAnalysisCoverageState.values()) {
            HospitalityAnalysisCoverage coverage = coverage(
                    evaluationId,
                    state,
                    Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND),
                    Set.of(HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING),
                    Set.of(GuestJourneyStage.DISCOVER),
                    Set.of(HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING),
                    Set.of(GuestJourneyStage.UNDERSTAND),
                    Set.of(HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING),
                    "explicit state supplied without a numerical threshold"
            );

            assertEquals(state, coverage.state());
            assertTrue(coverage.rationale().contains("explicit"));
        }
    }

    @Test
    void inabilityToVerifyIsRepresentedAsLimitationContextNotHotelDeficiency() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.TIMEOUT, null, "request timed out");
        HospitalityAnalysisLimitation limitation = limitationService.create(
                evidence,
                Set.of(HospitalityObservationCategory.BOOKING),
                Set.of(GuestJourneyStage.BOOK)
        );

        HospitalityAnalysisCoverage coverage = coverage(
                limitation.evaluationId(),
                HospitalityAnalysisCoverageState.INSUFFICIENT_COVERAGE,
                Set.of(GuestJourneyStage.BOOK),
                Set.of(HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS),
                Set.of(),
                Set.of(),
                Set.of(GuestJourneyStage.BOOK),
                Set.of(HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS),
                Set.of(),
                Set.of(limitation),
                "explicitly limited because the booking source could not be verified; this does not establish that booking is absent"
        );

        assertEquals(HospitalityAnalysisLimitationType.UNABLE_TO_VERIFY, coverage.supportingLimitations().iterator().next().type());
        assertEquals(AcquisitionOutcome.TIMEOUT, coverage.supportingLimitations().iterator().next().sourceCondition());
        assertEquals(Set.of(HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS), coverage.limitedDimensions());
        assertTrue(coverage.rationale().contains("does not establish"));
    }

    @Test
    void findingAndLimitationReferencesPreserveEvaluationTraceability() {
        StructuredEvidence evidence = evidence(AcquisitionOutcome.SUCCESS, "<a href=\"/book\">Book Now</a>", null);
        HospitalityObservation observation = new HospitalityObservation(
                HospitalityObservationCategory.BOOKING,
                "Book Now -> /book",
                WEBSITE + "/",
                evidence,
                evidence.evaluationId(),
                evidence.attemptId(),
                evidence.attemptNumber(),
                EvidenceProvenance.DISCOVERED
        );
        HospitalityAnalysisSignal signal = new HospitalityAnalysisSignal(
                observation,
                Set.of(GuestJourneyStage.BOOK),
                "A booking entry point was observed.",
                HospitalityAnalysisSignalStatus.QUALIFIED
        );
        HospitalityFinding finding = new HospitalityFinding(signal, HospitalityFindingStatus.VERIFIED_OBSERVED);
        HospitalityAnalysisCoverage coverage = coverage(
                evidence.evaluationId(),
                HospitalityAnalysisCoverageState.SUBSTANTIALLY_ASSESSED,
                Set.of(GuestJourneyStage.BOOK),
                Set.of(HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS),
                Set.of(GuestJourneyStage.BOOK),
                Set.of(HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS),
                Set.of(),
                Set.of(),
                Set.of(finding),
                Set.of(),
                "explicitly assessed from traceable hospitality evidence"
        );

        assertSame(finding, coverage.supportingFindings().iterator().next());
        assertEquals(evidence.evaluationId(), coverage.evaluationId());
        assertSame(evidence, coverage.supportingFindings().iterator().next().supportingEvidence());
    }

    @Test
    void crossEvaluationTraceabilityIsRejected() {
        UUID evaluationId = UUID.randomUUID();
        StructuredEvidence evidence = evidence(AcquisitionOutcome.SUCCESS, "<h1>Hotel</h1>", null);
        HospitalityObservation observation = new HospitalityObservation(
                HospitalityObservationCategory.HOTEL_IDENTITY,
                "Hotel",
                WEBSITE + "/",
                evidence,
                evidence.evaluationId(),
                evidence.attemptId(),
                evidence.attemptNumber(),
                EvidenceProvenance.DISCOVERED
        );
        HospitalityAnalysisSignal signal = new HospitalityAnalysisSignal(
                observation,
                Set.of(GuestJourneyStage.DISCOVER),
                "Hotel identity was observed.",
                HospitalityAnalysisSignalStatus.QUALIFIED
        );
        HospitalityFinding finding = new HospitalityFinding(signal, HospitalityFindingStatus.VERIFIED_OBSERVED);

        assertThrows(IllegalArgumentException.class, () -> coverage(
                evaluationId,
                HospitalityAnalysisCoverageState.SUBSTANTIALLY_ASSESSED,
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of(HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING),
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of(HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING),
                Set.of(),
                Set.of(),
                Set.of(finding),
                Set.of(),
                "explicitly assessed from traceable hospitality evidence"
        ));
    }

    @Test
    void coverageIsNotDerivedFromFindingCountOrPageCount() {
        UUID evaluationId = UUID.randomUUID();
        HospitalityAnalysisCoverage withNoFindings = coverage(
                evaluationId,
                HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED,
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of(HospitalityAnalysisDimension.DISCOVERABILITY_AND_NAVIGATION),
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of(HospitalityAnalysisDimension.DISCOVERABILITY_AND_NAVIGATION),
                Set.of(),
                Set.of(),
                "state is explicitly supplied from intended hospitality scope; finding count and page count are not calibration inputs"
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, withNoFindings.state());
        assertTrue(withNoFindings.supportingFindings().isEmpty());
    }

    @Test
    void coverageDefensivelyCopiesCollections() {
        Set<GuestJourneyStage> intendedJourney = new java.util.HashSet<>(Set.of(GuestJourneyStage.DISCOVER));
        Set<HospitalityAnalysisDimension> intendedDimensions = new java.util.HashSet<>(Set.of(HospitalityAnalysisDimension.DISCOVERABILITY_AND_NAVIGATION));
        HospitalityAnalysisCoverage coverage = coverage(
                UUID.randomUUID(),
                HospitalityAnalysisCoverageState.SUBSTANTIALLY_ASSESSED,
                intendedJourney,
                intendedDimensions,
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of(HospitalityAnalysisDimension.DISCOVERABILITY_AND_NAVIGATION),
                Set.of(),
                Set.of(),
                "explicitly assessed"
        );

        intendedJourney.clear();
        intendedDimensions.clear();

        assertEquals(Set.of(GuestJourneyStage.DISCOVER), coverage.intendedJourneyStages());
        assertEquals(Set.of(HospitalityAnalysisDimension.DISCOVERABILITY_AND_NAVIGATION), coverage.intendedDimensions());
    }

    private HospitalityAnalysisCoverage coverage(
            UUID evaluationId,
            HospitalityAnalysisCoverageState state,
            Set<GuestJourneyStage> intendedJourneyStages,
            Set<HospitalityAnalysisDimension> intendedDimensions,
            Set<GuestJourneyStage> assessableJourneyStages,
            Set<HospitalityAnalysisDimension> assessableDimensions,
            Set<GuestJourneyStage> limitedJourneyStages,
            Set<HospitalityAnalysisDimension> limitedDimensions,
            String rationale
    ) {
        return coverage(
                evaluationId,
                state,
                intendedJourneyStages,
                intendedDimensions,
                assessableJourneyStages,
                assessableDimensions,
                limitedJourneyStages,
                limitedDimensions,
                Set.of(),
                Set.of(),
                rationale
        );
    }

    private HospitalityAnalysisCoverage coverage(
            UUID evaluationId,
            HospitalityAnalysisCoverageState state,
            Set<GuestJourneyStage> intendedJourneyStages,
            Set<HospitalityAnalysisDimension> intendedDimensions,
            Set<GuestJourneyStage> assessableJourneyStages,
            Set<HospitalityAnalysisDimension> assessableDimensions,
            Set<GuestJourneyStage> limitedJourneyStages,
            Set<HospitalityAnalysisDimension> limitedDimensions,
            Set<HospitalityFinding> findings,
            Set<HospitalityAnalysisLimitation> limitations,
            String rationale
    ) {
        return new HospitalityAnalysisCoverage(
                evaluationId,
                intendedJourneyStages,
                intendedDimensions,
                assessableJourneyStages,
                assessableDimensions,
                limitedJourneyStages,
                limitedDimensions,
                findings,
                limitations,
                state,
                rationale
        );
    }

    private StructuredEvidence evidence(AcquisitionOutcome outcome, String body, String limitation) {
        Integer statusCode = outcome == AcquisitionOutcome.HTTP_ERROR
                ? 503
                : outcome == AcquisitionOutcome.SUCCESS ? 200 : null;
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
                limitation
        );
        CanonicalEvaluationRequest request = ((ValidationResult.Accepted) inputValidator.validate(
                new HotelEvaluationInput(null, null, WEBSITE)
        )).request();
        Evaluation evaluation = Evaluation.create(request, CLOCK);
        EvaluationAttempt attempt = evaluation.currentAttempt();
        CapabilityOutcome capabilityOutcome = CapabilityOutcome.record(
                "public-web-acquisition",
                outcome == AcquisitionOutcome.SUCCESS ? CapabilityOutcomeStatus.SUCCEEDED : CapabilityOutcomeStatus.FAILED,
                outcome.name(),
                CLOCK.instant()
        );
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
                acquisition.errorMessage()
        );
    }
}
