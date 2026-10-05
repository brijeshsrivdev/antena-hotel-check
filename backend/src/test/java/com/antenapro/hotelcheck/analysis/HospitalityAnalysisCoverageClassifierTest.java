package com.antenapro.hotelcheck.analysis;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HospitalityAnalysisCoverageClassifierTest {

    private static final Set<GuestJourneyStage> ALL_STAGES = Set.of(GuestJourneyStage.values());
    private static final Set<HospitalityAnalysisDimension> ALL_DIMENSIONS = Set.of(HospitalityAnalysisDimension.values());

    private final HospitalityAnalysisCoverageClassifier classifier = new HospitalityAnalysisCoverageClassifier();

    @Test
    void classifiesFiveStagesSixDimensionsThreeAssessableStagesAsSubstantial() {
        HospitalityCoverageAssessment assessment = assessment(
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE),
                firstDimensions(6),
                Set.of(GuestJourneyStage.TRUST, GuestJourneyStage.BOOK),
                Set.of()
        );

        assertEquals(HospitalityAnalysisCoverageState.SUBSTANTIALLY_ASSESSED, classifier.classify(assessment));
    }

    @Test
    void fiveStagesSixDimensionsButOnlyTwoAssessableStagesIsNotSubstantial() {
        HospitalityCoverageAssessment assessment = assessment(
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND),
                firstDimensions(6),
                Set.of(GuestJourneyStage.EXPLORE, GuestJourneyStage.TRUST, GuestJourneyStage.BOOK),
                Set.of()
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, classifier.classify(assessment));
    }

    @Test
    void fiveStagesFiveDimensionsAndThreeAssessableStagesIsNotSubstantial() {
        HospitalityCoverageAssessment assessment = assessment(
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE),
                firstDimensions(5),
                Set.of(GuestJourneyStage.TRUST, GuestJourneyStage.BOOK),
                Set.of()
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, classifier.classify(assessment));
    }

    @Test
    void fewerThanFiveCoveredJourneyStagesIsNotSubstantial() {
        HospitalityCoverageAssessment assessment = assessment(
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE),
                firstDimensions(6),
                Set.of(GuestJourneyStage.TRUST),
                Set.of()
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, classifier.classify(assessment));
    }

    @Test
    void oneCoveredJourneyStageProducesPartialCoverage() {
        HospitalityCoverageAssessment assessment = assessment(
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of(),
                Set.of(),
                Set.of()
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, classifier.classify(assessment));
    }

    @Test
    void oneCoveredDimensionWithoutJourneyStagesProducesPartialCoverage() {
        HospitalityCoverageAssessment assessment = assessment(
                Set.of(),
                Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION),
                Set.of(),
                Set.of()
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, classifier.classify(assessment));
    }

    @Test
    void zeroCoveredJourneyStagesAndDimensionsProducesInsufficientCoverage() {
        HospitalityCoverageAssessment assessment = assessment(Set.of(), Set.of(), Set.of(), Set.of());

        assertEquals(HospitalityAnalysisCoverageState.INSUFFICIENT_COVERAGE, classifier.classify(assessment));
    }

    @Test
    void limitedScopeCountsAsCoveredButNotAsAssessableEvidence() {
        HospitalityCoverageAssessment assessment = new HospitalityCoverageAssessment(
                UUID.randomUUID(),
                ALL_STAGES,
                firstDimensions(6),
                Set.of(),
                Set.of(),
                ALL_STAGES,
                firstDimensions(6)
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, classifier.classify(assessment));
    }

    @Test
    void limitedScopeCanCompleteCoveredScopeButNotSubstantialAssessableStageThreshold() {
        HospitalityCoverageAssessment assessment = new HospitalityCoverageAssessment(
                UUID.randomUUID(),
                ALL_STAGES,
                ALL_DIMENSIONS,
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND),
                firstDimensions(6),
                Set.of(GuestJourneyStage.EXPLORE, GuestJourneyStage.TRUST, GuestJourneyStage.BOOK),
                Set.of()
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, classifier.classify(assessment));
    }

    @Test
    void unsupportedDimensionsRemainUncovered() {
        HospitalityCoverageAssessment assessment = assessment(
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE),
                Set.of(
                        HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING,
                        HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION,
                        HospitalityAnalysisDimension.AMENITIES_AND_GUEST_FACING_INFORMATION,
                        HospitalityAnalysisDimension.CONTACT_AND_LOCATION,
                        HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS
                ),
                Set.of(GuestJourneyStage.TRUST),
                Set.of()
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, classifier.classify(assessment));
    }

    @Test
    void absenceOfBookingEvidenceDoesNotCreateBookingFailure() {
        HospitalityCoverageAssessment assessment = assessment(
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION),
                Set.of(),
                Set.of()
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, classifier.classify(assessment));
    }

    @Test
    void classificationDoesNotMutateAssessmentInput() {
        HospitalityCoverageAssessment assessment = assessment(
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE),
                firstDimensions(6),
                Set.of(GuestJourneyStage.TRUST, GuestJourneyStage.BOOK),
                Set.of()
        );

        HospitalityAnalysisCoverageState state = classifier.classify(assessment);

        assertEquals(HospitalityAnalysisCoverageState.SUBSTANTIALLY_ASSESSED, state);
        assertEquals(
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE),
                assessment.assessableJourneyStages()
        );
        assertEquals(firstDimensions(6), assessment.assessableDimensions());
    }

    @Test
    void identicalAssessmentProducesIdenticalClassification() {
        HospitalityCoverageAssessment assessment = assessment(
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE),
                firstDimensions(6),
                Set.of(GuestJourneyStage.TRUST, GuestJourneyStage.BOOK),
                Set.of()
        );

        HospitalityAnalysisCoverageState first = classifier.classify(assessment);
        HospitalityAnalysisCoverageState second = classifier.classify(assessment);

        assertSame(first, second);
    }

    @Test
    void nullAssessmentIsRejected() {
        assertThrows(NullPointerException.class, () -> classifier.classify(null));
    }

    private static HospitalityCoverageAssessment assessment(
            Set<GuestJourneyStage> assessableJourneyStages,
            Set<HospitalityAnalysisDimension> assessableDimensions,
            Set<GuestJourneyStage> limitedJourneyStages,
            Set<HospitalityAnalysisDimension> limitedDimensions
    ) {
        return new HospitalityCoverageAssessment(
                UUID.randomUUID(),
                ALL_STAGES,
                ALL_DIMENSIONS,
                assessableJourneyStages,
                assessableDimensions,
                limitedJourneyStages,
                limitedDimensions
        );
    }

    private static Set<HospitalityAnalysisDimension> firstDimensions(int count) {
        return Set.of(HospitalityAnalysisDimension.values()).stream()
                .limit(count)
                .collect(java.util.stream.Collectors.toSet());
    }
}
