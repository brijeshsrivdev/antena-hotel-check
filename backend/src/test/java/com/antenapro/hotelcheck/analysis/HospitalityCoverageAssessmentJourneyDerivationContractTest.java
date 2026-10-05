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

class HospitalityCoverageAssessmentJourneyDerivationContractTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-04T00:00:00Z"), ZoneOffset.UTC);
    private static final String WEBSITE = "https://hotel.example.com";

    private final HotelEvaluationInputValidator inputValidator = new HotelEvaluationInputValidator();
    private final HospitalityCoverageAssessmentDerivationService service =
            new HospitalityCoverageAssessmentDerivationService();

    @Test
    void originatingObservationCategoryOverridesInconsistentSuppliedJourneyStages() {
        Evaluation evaluation = evaluation();
        HospitalityAnalysisSignal inconsistentRoomsSignal = signal(
                evaluation,
                HospitalityObservationCategory.ROOMS,
                Set.of(GuestJourneyStage.BOOK)
        );

        HospitalityCoverageAssessment assessment = service.derive(
                evaluation.evaluationId(),
                List.of(inconsistentRoomsSignal),
                List.of(),
                List.of()
        );

        assertEquals(Set.of(GuestJourneyStage.EXPLORE), assessment.assessableJourneyStages());
        assertFalse(assessment.assessableJourneyStages().contains(GuestJourneyStage.BOOK));
    }

    @Test
    void authoritativeMappingIsAppliedForAllSixObservationCategories() {
        Evaluation evaluation = evaluation();
        List<HospitalityAnalysisSignal> signals = List.of(
                signal(evaluation, HospitalityObservationCategory.HOTEL_IDENTITY, Set.of(GuestJourneyStage.BOOK)),
                signal(evaluation, HospitalityObservationCategory.ROOMS, Set.of(GuestJourneyStage.DISCOVER)),
                signal(evaluation, HospitalityObservationCategory.AMENITIES, Set.of(GuestJourneyStage.BOOK)),
                signal(evaluation, HospitalityObservationCategory.CONTACT, Set.of(GuestJourneyStage.EXPLORE)),
                signal(evaluation, HospitalityObservationCategory.BOOKING, Set.of(GuestJourneyStage.DISCOVER)),
                signal(evaluation, HospitalityObservationCategory.DINING, Set.of(GuestJourneyStage.TRUST))
        );

        HospitalityCoverageAssessment assessment = service.derive(
                evaluation.evaluationId(), signals, List.of(), List.of());

        assertEquals(Set.of(
                GuestJourneyStage.DISCOVER,
                GuestJourneyStage.UNDERSTAND,
                GuestJourneyStage.EXPLORE,
                GuestJourneyStage.BOOK
        ), assessment.assessableJourneyStages());
        assertFalse(assessment.assessableJourneyStages().contains(GuestJourneyStage.TRUST));
    }

    private HospitalityAnalysisSignal signal(
            Evaluation evaluation,
            HospitalityObservationCategory category,
            Set<GuestJourneyStage> suppliedJourneyStages
    ) {
        StructuredEvidence evidence = evidence(evaluation);
        HospitalityObservation observation = new HospitalityObservation(
                category,
                category.name(),
                WEBSITE + "/" + category.name().toLowerCase(),
                evidence,
                evaluation.evaluationId(),
                evaluation.currentAttempt().attemptId(),
                evaluation.currentAttempt().attemptNumber(),
                EvidenceProvenance.DISCOVERED
        );
        return new HospitalityAnalysisSignal(
                observation,
                suppliedJourneyStages,
                "deliberately supplied journey stages",
                HospitalityAnalysisSignalStatus.QUALIFIED
        );
    }

    private StructuredEvidence evidence(Evaluation evaluation) {
        AcquisitionResult acquisition = new AcquisitionResult(
                AcquisitionOutcome.SUCCESS,
                WEBSITE,
                WEBSITE,
                200,
                "text/html",
                CLOCK.instant(),
                AcquisitionMethod.HTTP_PUBLIC,
                "<h1>Hotel</h1>",
                Map.of(),
                List.of(),
                null
        );
        EvaluationAttempt attempt = evaluation.currentAttempt();
        CapabilityOutcome capabilityOutcome = CapabilityOutcome.record(
                "public-web-acquisition",
                CapabilityOutcomeStatus.SUCCEEDED,
                AcquisitionOutcome.SUCCESS.name(),
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
                AcquisitionOutcome.SUCCESS,
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
