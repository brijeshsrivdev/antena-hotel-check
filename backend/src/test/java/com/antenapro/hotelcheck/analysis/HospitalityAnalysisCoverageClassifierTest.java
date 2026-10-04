package com.antenapro.hotelcheck.analysis;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
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
        HospitalityAnalysisCoverage coverage = coverage(
                ALL_STAGES,
                firstDimensions(6),
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE),
                Set.of(HospitalityAnalysisDimension.values()).stream().limit(6).collect(java.util.stream.Collectors.toSet())
        );

        assertEquals(HospitalityAnalysisCoverageState.SUBSTANTIALLY_ASSESSED, classifier.classify(coverage));
    }

    @Test
    void fiveStagesSixDimensionsButOnlyTwoAssessableStagesIsNotSubstantial() {
        HospitalityAnalysisCoverage coverage = coverage(
                ALL_STAGES,
                firstDimensions(6),
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND),
                firstDimensions(6)
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, classifier.classify(coverage));
    }

    @Test
    void fiveStagesFiveDimensionsAndThreeAssessableStagesIsNotSubstantial() {
        HospitalityAnalysisCoverage coverage = coverage(
                ALL_STAGES,
                firstDimensions(5),
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE),
                firstDimensions(5)
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, classifier.classify(coverage));
    }

    @Test
    void fewerThanFiveCoveredJourneyStagesIsNotSubstantial() {
        HospitalityAnalysisCoverage coverage = coverage(
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE, GuestJourneyStage.TRUST),
                firstDimensions(6),
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE),
                firstDimensions(6)
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, classifier.classify(coverage));
    }

    @Test
    void oneCoveredJourneyStageProducesPartialCoverage() {
        HospitalityAnalysisCoverage coverage = coverage(
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of(),
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of()
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, classifier.classify(coverage));
    }

    @Test
    void oneCoveredDimensionWithoutJourneyStagesProducesPartialCoverage() {
        HospitalityAnalysisCoverage coverage = coverage(
                Set.of(),
                Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION),
                Set.of(),
                Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION)
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, classifier.classify(coverage));
    }

    @Test
    void zeroCoveredJourneyStagesAndDimensionsProducesInsufficientCoverage() {
        HospitalityAnalysisCoverage coverage = coverage(Set.of(), Set.of(), Set.of(), Set.of());

        assertEquals(HospitalityAnalysisCoverageState.INSUFFICIENT_COVERAGE, classifier.classify(coverage));
    }

    @Test
    void limitedScopeCountsAsCoveredButNotAsAssessableEvidence() {
        HospitalityAnalysisCoverage coverage = new HospitalityAnalysisCoverage(
                UUID.randomUUID(),
                ALL_STAGES,
                firstDimensions(6),
                Set.of(),
                Set.of(),
                ALL_STAGES,
                firstDimensions(6),
                Set.of(),
                Set.of(),
                HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED,
                "limited scope is explicitly accounted for but not assessable evidence"
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, classifier.classify(coverage));
    }

    @Test
    void unsupportedDimensionsRemainUncovered() {
        HospitalityAnalysisCoverage coverage = coverage(
                ALL_STAGES,
                ALL_DIMENSIONS,
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE),
                Set.of(
                        HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING,
                        HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION,
                        HospitalityAnalysisDimension.AMENITIES_AND_GUEST_FACING_INFORMATION,
                        HospitalityAnalysisDimension.CONTACT_AND_LOCATION,
                        HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS
                )
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, classifier.classify(coverage));
    }

    @Test
    void absenceOfBookingEvidenceDoesNotCreateBookingFailure() {
        HospitalityAnalysisCoverage coverage = coverage(
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION),
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION)
        );

        assertEquals(HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED, classifier.classify(coverage));
    }

    @Test
    void crossEvaluationSupportingFindingIsRejectedByExistingCoverageBoundary() {
        UUID evaluationId = UUID.randomUUID();
        UUID foreignEvaluationId = UUID.randomUUID();
        HospitalityFinding foreignFinding = org.mockito.Mockito.mock(HospitalityFinding.class);
        org.mockito.Mockito.when(foreignFinding.evaluationId()).thenReturn(foreignEvaluationId);

        assertThrows(IllegalArgumentException.class, () -> new HospitalityAnalysisCoverage(
                evaluationId,
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of(HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING),
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of(HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING),
                Set.of(),
                Set.of(),
                Set.of(foreignFinding),
                Set.of(),
                HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED,
                "cross-evaluation supporting evidence must be rejected"
        ));
    }

    @Test
    void classificationDoesNotMutateCoverageInput() {
        Set<GuestJourneyStage> assessableStages = EnumSet.of(
                GuestJourneyStage.DISCOVER,
                GuestJourneyStage.UNDERSTAND,
                GuestJourneyStage.EXPLORE
        );
        Set<HospitalityAnalysisDimension> assessableDimensions = EnumSet.copyOf(firstDimensions(6));
        Set<GuestJourneyStage> originalStages = Set.copyOf(assessableStages);
        Set<HospitalityAnalysisDimension> originalDimensions = Set.copyOf(assessableDimensions);

        HospitalityAnalysisCoverage coverage = coverage(ALL_STAGES, ALL_DIMENSIONS, assessableStages, assessableDimensions);
        HospitalityAnalysisCoverageState state = classifier.classify(coverage);

        assertEquals(HospitalityAnalysisCoverageState.SUBSTANTIALLY_ASSESSED, state);
        assertEquals(originalStages, coverage.assessableJourneyStages());
        assertEquals(originalDimensions, coverage.assessableDimensions());
    }

    @Test
    void identicalCoverageProducesIdenticalClassification() {
        HospitalityAnalysisCoverage coverage = coverage(
                ALL_STAGES,
                firstDimensions(6),
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE),
                firstDimensions(6)
        );

        HospitalityAnalysisCoverageState first = classifier.classify(coverage);
        HospitalityAnalysisCoverageState second = classifier.classify(coverage);

        assertSame(first, second);
    }

    @Test
    void nullCoverageIsRejected() {
        assertThrows(NullPointerException.class, () -> classifier.classify(null));
    }

    private static HospitalityAnalysisCoverage coverage(
            Set<GuestJourneyStage> assessableJourneyStages,
            Set<HospitalityAnalysisDimension> assessableDimensions,
            Set<GuestJourneyStage> limitedJourneyStages,
            Set<HospitalityAnalysisDimension> limitedDimensions
    ) {
        Set<GuestJourneyStage> intendedJourneyStages = EnumSet.allOf(GuestJourneyStage.class);
        Set<HospitalityAnalysisDimension> intendedDimensions = EnumSet.allOf(HospitalityAnalysisDimension.class);
        return new HospitalityAnalysisCoverage(
                UUID.randomUUID(),
                intendedJourneyStages,
                intendedDimensions,
                assessableJourneyStages,
                assessableDimensions,
                limitedJourneyStages,
                limitedDimensions,
                Set.of(),
                Set.of(),
                HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED,
                "test coverage classification input"
        );
    }

    private static Set<HospitalityAnalysisDimension> firstDimensions(int count) {
        return Set.of(HospitalityAnalysisDimension.values()).stream()
                .limit(count)
                .collect(java.util.stream.Collectors.toSet());
    }
}
