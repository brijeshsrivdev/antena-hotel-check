package com.antenapro.hotelcheck.analysis;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HospitalityCoverageAssessmentTest {

    private static final Set<GuestJourneyStage> ALL_STAGES = Set.of(GuestJourneyStage.values());
    private static final Set<HospitalityAnalysisDimension> ALL_DIMENSIONS = Set.of(HospitalityAnalysisDimension.values());

    @Test
    void validAssessmentIsCreated() {
        UUID evaluationId = UUID.randomUUID();

        HospitalityCoverageAssessment assessment = new HospitalityCoverageAssessment(
                evaluationId,
                ALL_STAGES,
                ALL_DIMENSIONS,
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION),
                Set.of(GuestJourneyStage.TRUST),
                Set.of(HospitalityAnalysisDimension.CONTACT_AND_LOCATION)
        );

        assertEquals(evaluationId, assessment.evaluationId());
        assertEquals(Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.TRUST), assessment.coveredJourneyStages());
        assertEquals(
                Set.of(
                        HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION,
                        HospitalityAnalysisDimension.CONTACT_AND_LOCATION
                ),
                assessment.coveredDimensions()
        );
    }

    @Test
    void emptyScopeIsValid() {
        HospitalityCoverageAssessment assessment = new HospitalityCoverageAssessment(
                UUID.randomUUID(), Set.of(), Set.of(), Set.of(), Set.of(), Set.of(), Set.of()
        );

        assertEquals(Set.of(), assessment.coveredJourneyStages());
        assertEquals(Set.of(), assessment.coveredDimensions());
    }

    @Test
    void coveredScopeIsUnionOfAssessableAndLimitedScope() {
        HospitalityCoverageAssessment assessment = new HospitalityCoverageAssessment(
                UUID.randomUUID(),
                ALL_STAGES,
                ALL_DIMENSIONS,
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION),
                Set.of(GuestJourneyStage.BOOK),
                Set.of(HospitalityAnalysisDimension.CONTACT_AND_LOCATION)
        );

        assertEquals(
                Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.BOOK),
                assessment.coveredJourneyStages()
        );
        assertEquals(
                Set.of(
                        HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION,
                        HospitalityAnalysisDimension.CONTACT_AND_LOCATION
                ),
                assessment.coveredDimensions()
        );
    }

    @Test
    void overlappingJourneyScopeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new HospitalityCoverageAssessment(
                UUID.randomUUID(),
                ALL_STAGES,
                ALL_DIMENSIONS,
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of(),
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of()
        ));
    }

    @Test
    void overlappingDimensionScopeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new HospitalityCoverageAssessment(
                UUID.randomUUID(),
                ALL_STAGES,
                ALL_DIMENSIONS,
                Set.of(),
                Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION),
                Set.of(),
                Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION)
        ));
    }

    @Test
    void journeyAssessableScopeOutsideIntendedScopeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new HospitalityCoverageAssessment(
                UUID.randomUUID(),
                Set.of(GuestJourneyStage.DISCOVER),
                ALL_DIMENSIONS,
                Set.of(GuestJourneyStage.BOOK),
                Set.of(),
                Set.of(),
                Set.of()
        ));
    }

    @Test
    void journeyLimitedScopeOutsideIntendedScopeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new HospitalityCoverageAssessment(
                UUID.randomUUID(),
                Set.of(GuestJourneyStage.DISCOVER),
                ALL_DIMENSIONS,
                Set.of(),
                Set.of(),
                Set.of(GuestJourneyStage.BOOK),
                Set.of()
        ));
    }

    @Test
    void dimensionAssessableScopeOutsideIntendedScopeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new HospitalityCoverageAssessment(
                UUID.randomUUID(),
                ALL_STAGES,
                Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION),
                Set.of(),
                Set.of(HospitalityAnalysisDimension.CONTACT_AND_LOCATION),
                Set.of(),
                Set.of()
        ));
    }

    @Test
    void dimensionLimitedScopeOutsideIntendedScopeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new HospitalityCoverageAssessment(
                UUID.randomUUID(),
                ALL_STAGES,
                Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(HospitalityAnalysisDimension.CONTACT_AND_LOCATION)
        ));
    }

    @Test
    void nullRequiredInputsAreRejected() {
        UUID evaluationId = UUID.randomUUID();

        assertThrows(NullPointerException.class, () -> new HospitalityCoverageAssessment(
                null, ALL_STAGES, ALL_DIMENSIONS, Set.of(), Set.of(), Set.of(), Set.of()
        ));
        assertThrows(NullPointerException.class, () -> new HospitalityCoverageAssessment(
                evaluationId, null, ALL_DIMENSIONS, Set.of(), Set.of(), Set.of(), Set.of()
        ));
        assertThrows(NullPointerException.class, () -> new HospitalityCoverageAssessment(
                evaluationId, ALL_STAGES, null, Set.of(), Set.of(), Set.of(), Set.of()
        ));
        assertThrows(NullPointerException.class, () -> new HospitalityCoverageAssessment(
                evaluationId, ALL_STAGES, ALL_DIMENSIONS, null, Set.of(), Set.of(), Set.of()
        ));
        assertThrows(NullPointerException.class, () -> new HospitalityCoverageAssessment(
                evaluationId, ALL_STAGES, ALL_DIMENSIONS, Set.of(), null, Set.of(), Set.of()
        ));
        assertThrows(NullPointerException.class, () -> new HospitalityCoverageAssessment(
                evaluationId, ALL_STAGES, ALL_DIMENSIONS, Set.of(), Set.of(), null, Set.of()
        ));
        assertThrows(NullPointerException.class, () -> new HospitalityCoverageAssessment(
                evaluationId, ALL_STAGES, ALL_DIMENSIONS, Set.of(), Set.of(), Set.of(), null
        ));
    }

    @Test
    void constructorCollectionsAreDefensivelyCopied() {
        EnumSet<GuestJourneyStage> intendedStages = EnumSet.allOf(GuestJourneyStage.class);
        EnumSet<HospitalityAnalysisDimension> intendedDimensions = EnumSet.allOf(HospitalityAnalysisDimension.class);
        EnumSet<GuestJourneyStage> assessableStages = EnumSet.of(GuestJourneyStage.DISCOVER);
        EnumSet<HospitalityAnalysisDimension> assessableDimensions =
                EnumSet.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION);

        HospitalityCoverageAssessment assessment = new HospitalityCoverageAssessment(
                UUID.randomUUID(),
                intendedStages,
                intendedDimensions,
                assessableStages,
                assessableDimensions,
                Set.of(),
                Set.of()
        );

        intendedStages.clear();
        intendedDimensions.clear();
        assessableStages.clear();
        assessableDimensions.clear();

        assertEquals(ALL_STAGES, assessment.intendedJourneyStages());
        assertEquals(ALL_DIMENSIONS, assessment.intendedDimensions());
        assertEquals(Set.of(GuestJourneyStage.DISCOVER), assessment.assessableJourneyStages());
        assertEquals(
                Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION),
                assessment.assessableDimensions()
        );
    }

    @Test
    void returnedCollectionsAreImmutable() {
        HospitalityCoverageAssessment assessment = new HospitalityCoverageAssessment(
                UUID.randomUUID(),
                ALL_STAGES,
                ALL_DIMENSIONS,
                Set.of(GuestJourneyStage.DISCOVER),
                Set.of(HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION),
                Set.of(),
                Set.of()
        );

        assertThrows(UnsupportedOperationException.class,
                () -> assessment.assessableJourneyStages().add(GuestJourneyStage.BOOK));
        assertThrows(UnsupportedOperationException.class,
                () -> assessment.coveredJourneyStages().add(GuestJourneyStage.BOOK));
        assertThrows(UnsupportedOperationException.class,
                () -> assessment.assessableDimensions().add(HospitalityAnalysisDimension.CONTACT_AND_LOCATION));
        assertThrows(UnsupportedOperationException.class,
                () -> assessment.coveredDimensions().add(HospitalityAnalysisDimension.CONTACT_AND_LOCATION));
    }

    @Test
    void assessmentsWithDifferentEvaluationIdsRemainIndependent() {
        HospitalityCoverageAssessment first = new HospitalityCoverageAssessment(
                UUID.randomUUID(), ALL_STAGES, ALL_DIMENSIONS,
                Set.of(GuestJourneyStage.DISCOVER), Set.of(), Set.of(), Set.of()
        );
        HospitalityCoverageAssessment second = new HospitalityCoverageAssessment(
                UUID.randomUUID(), ALL_STAGES, ALL_DIMENSIONS,
                Set.of(), Set.of(), Set.of(GuestJourneyStage.BOOK), Set.of()
        );

        assertEquals(Set.of(GuestJourneyStage.DISCOVER), first.coveredJourneyStages());
        assertEquals(Set.of(GuestJourneyStage.BOOK), second.coveredJourneyStages());
    }
}
