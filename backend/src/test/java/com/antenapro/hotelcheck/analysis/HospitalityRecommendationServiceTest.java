package com.antenapro.hotelcheck.analysis;

import com.antenapro.hotelcheck.evidence.StructuredEvidence;
import com.antenapro.hotelcheck.hospitality.HospitalityObservation;
import com.antenapro.hotelcheck.hospitality.HospitalityObservationCategory;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HospitalityRecommendationServiceTest {

    private final HospitalityRecommendationService service = new HospitalityRecommendationService();

    @Test
    void mapsSupportedDeficiencyFamiliesToBoundedCategories() {
        UUID evaluationId = UUID.randomUUID();

        assertMapping(evaluationId, HospitalityObservationCategory.BOOKING,
                RecommendationCategory.IMPROVE_BOOKING_DISCOVERABILITY);
        assertMapping(evaluationId, HospitalityObservationCategory.ROOMS,
                RecommendationCategory.IMPROVE_ROOM_INFORMATION);
        assertMapping(evaluationId, HospitalityObservationCategory.AMENITIES,
                RecommendationCategory.IMPROVE_GUEST_FACING_INFORMATION);
        assertMapping(evaluationId, HospitalityObservationCategory.DINING,
                RecommendationCategory.IMPROVE_GUEST_FACING_INFORMATION);
        assertMapping(evaluationId, HospitalityObservationCategory.CONTACT,
                RecommendationCategory.IMPROVE_CONTACT_LOCATION_INFORMATION);
    }

    @Test
    void identityConflictMapsToConflictResolutionWithoutParsingFindingText() {
        UUID evaluationId = UUID.randomUUID();
        HospitalityFinding left = finding(evaluationId, HospitalityObservationCategory.HOTEL_IDENTITY,
                "Hotel Alpha", "https://example.com/about", Set.of(GuestJourneyStage.DISCOVER));
        HospitalityFinding right = finding(evaluationId, HospitalityObservationCategory.HOTEL_IDENTITY,
                "Hotel Beta", "https://example.org/profile", Set.of(GuestJourneyStage.DISCOVER));

        GuestJourneyAnalysis journey = journey(evaluationId, Set.of(left, right), Set.of());

        Set<HospitalityRecommendation> recommendations = service.recommend(journey);

        assertThat(recommendations).hasSize(2);
        assertThat(recommendations)
                .extracting(HospitalityRecommendation::category)
                .containsOnly(RecommendationCategory.RESOLVE_INFORMATION_CONFLICT);
        assertThat(recommendations)
                .allSatisfy(recommendation -> assertThat(recommendation.action())
                        .isEqualTo("Resolve conflicting hotel identity information."));
    }

    @Test
    void unsupportedDeficiencyProducesNoSpeculativeRecommendation() {
        UUID evaluationId = UUID.randomUUID();
        HospitalityFinding finding = finding(evaluationId, HospitalityObservationCategory.HOTEL_IDENTITY,
                "Hotel Alpha", "https://example.com/about", Set.of(GuestJourneyStage.DISCOVER));

        assertThat(service.recommend(journey(evaluationId, Set.of(finding), Set.of()))).isEmpty();
    }

    @Test
    void bookingAbsenceDoesNotCreateBookingRecommendation() {
        UUID evaluationId = UUID.randomUUID();

        assertThat(service.recommend(journey(evaluationId, Set.of(), Set.of()))).isEmpty();
    }

    @Test
    void limitationDoesNotAutomaticallyBecomeRecommendation() {
        UUID evaluationId = UUID.randomUUID();
        HospitalityAnalysisLimitation limitation = mock(HospitalityAnalysisLimitation.class);
        when(limitation.evaluationId()).thenReturn(evaluationId);
        when(limitation.journeyStages()).thenReturn(Set.of(GuestJourneyStage.BOOK));

        assertThat(service.recommend(journey(evaluationId, Set.of(), Set.of(limitation)))).isEmpty();
    }

    @Test
    void journeyStagesComeFromExistingJourneyImpactSemantics() {
        UUID evaluationId = UUID.randomUUID();
        HospitalityFinding finding = finding(evaluationId, HospitalityObservationCategory.ROOMS,
                "Room information", "https://example.com/rooms", Set.of(GuestJourneyStage.EXPLORE));
        GuestJourneyAnalysis journey = journey(evaluationId, Set.of(finding), Set.of());

        HospitalityRecommendation recommendation = service.recommend(journey).iterator().next();

        assertThat(recommendation.journeyStages()).containsExactly(GuestJourneyStage.EXPLORE);
        assertThat(recommendation.sourceFinding()).isSameAs(finding);
        assertThat(recommendation.sourceLimitation()).isNull();
    }

    @Test
    void evaluationAIsolatedFromEvaluationB() {
        UUID evaluationA = UUID.randomUUID();
        UUID evaluationB = UUID.randomUUID();
        HospitalityFinding findingA = finding(evaluationA, HospitalityObservationCategory.ROOMS,
                "Room information", "https://a.example/rooms", Set.of(GuestJourneyStage.EXPLORE));
        HospitalityFinding findingB = finding(evaluationB, HospitalityObservationCategory.BOOKING,
                "Booking entry point", "https://b.example/book", Set.of(GuestJourneyStage.BOOK));

        Set<HospitalityRecommendation> recommendationsA = service.recommend(journey(evaluationA, Set.of(findingA), Set.of()));
        Set<HospitalityRecommendation> recommendationsB = service.recommend(journey(evaluationB, Set.of(findingB), Set.of()));

        assertThat(recommendationsA).hasSize(1);
        assertThat(recommendationsA.iterator().next().evaluationId()).isEqualTo(evaluationA);
        assertThat(recommendationsA.iterator().next().sourceFinding()).isSameAs(findingA);
        assertThat(recommendationsB).hasSize(1);
        assertThat(recommendationsB.iterator().next().evaluationId()).isEqualTo(evaluationB);
        assertThat(recommendationsB.iterator().next().sourceFinding()).isSameAs(findingB);
    }

    @Test
    void deterministicallyReturnsSameRecommendationsForSameInput() {
        UUID evaluationId = UUID.randomUUID();
        HospitalityFinding finding = finding(evaluationId, HospitalityObservationCategory.CONTACT,
                "Contact information", "https://example.com/contact", Set.of(GuestJourneyStage.DISCOVER));
        GuestJourneyAnalysis journey = journey(evaluationId, Set.of(finding), Set.of());

        assertThat(service.recommend(journey)).isEqualTo(service.recommend(journey));
    }

    @Test
    void nonDeficiencyFindingDoesNotProduceRecommendation() {
        UUID evaluationId = UUID.randomUUID();
        StructuredEvidence evidence = mock(StructuredEvidence.class);
        when(evidence.evaluationId()).thenReturn(evaluationId);
        when(evidence.attemptId()).thenReturn(UUID.randomUUID());
        HospitalityObservation observation = observation(evaluationId, HospitalityObservationCategory.ROOMS,
                "Room information", "https://example.com/rooms", evidence);
        HospitalityAnalysisSignal signal = new HospitalityAnalysisSignal(
                observation, Set.of(GuestJourneyStage.EXPLORE), "Room information observed",
                HospitalityAnalysisSignalStatus.QUALIFIED);
        HospitalityFinding finding = new HospitalityFinding(
                signal, HospitalityFindingStatus.VERIFIED_OBSERVED, HospitalityFindingKind.OBSERVATION);

        assertThat(service.recommend(journey(evaluationId, Set.of(finding), Set.of()))).isEmpty();
    }

    @Test
    void rejectsCrossEvaluationJourneyAtConstruction() {
        UUID evaluationA = UUID.randomUUID();
        UUID evaluationB = UUID.randomUUID();
        HospitalityFinding findingB = finding(evaluationB, HospitalityObservationCategory.ROOMS,
                "Room information", "https://b.example/rooms", Set.of(GuestJourneyStage.EXPLORE));

        assertThatThrownBy(() -> journey(evaluationA, Set.of(findingB), Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("journey stage impacts must belong to evaluationId");
    }

    private static void assertMapping(UUID evaluationId, HospitalityObservationCategory category,
                                      RecommendationCategory expectedCategory) {
        HospitalityFinding finding = finding(evaluationId, category, category.name(),
                "https://example.com/" + category.name().toLowerCase(), Set.of(defaultStage(category)));

        Set<HospitalityRecommendation> recommendations = new HospitalityRecommendationService()
                .recommend(journey(evaluationId, Set.of(finding), Set.of()));

        assertThat(recommendations).hasSize(1);
        HospitalityRecommendation recommendation = recommendations.iterator().next();
        assertThat(recommendation.category()).isEqualTo(expectedCategory);
        assertThat(recommendation.evaluationId()).isEqualTo(evaluationId);
        assertThat(recommendation.sourceFinding()).isSameAs(finding);
    }

    private static GuestJourneyStage defaultStage(HospitalityObservationCategory category) {
        return switch (category) {
            case BOOKING -> GuestJourneyStage.BOOK;
            case ROOMS -> GuestJourneyStage.EXPLORE;
            case AMENITIES, DINING -> GuestJourneyStage.UNDERSTAND;
            case CONTACT -> GuestJourneyStage.DISCOVER;
            case HOTEL_IDENTITY -> GuestJourneyStage.DISCOVER;
        };
    }

    private static HospitalityFinding finding(UUID evaluationId, HospitalityObservationCategory category,
                                              String observedValue, String sourceReference,
                                              Set<GuestJourneyStage> journeyStages) {
        StructuredEvidence evidence = mock(StructuredEvidence.class);
        when(evidence.evaluationId()).thenReturn(evaluationId);
        when(evidence.attemptId()).thenReturn(UUID.randomUUID());
        when(evidence.attemptNumber()).thenReturn(1);

        HospitalityObservation observation = observation(evaluationId, category, observedValue,
                sourceReference, evidence);
        HospitalityAnalysisSignal signal = new HospitalityAnalysisSignal(
                observation, journeyStages, "governed finding text", HospitalityAnalysisSignalStatus.QUALIFIED);
        return new HospitalityFinding(signal, HospitalityFindingStatus.VERIFIED_OBSERVED,
                HospitalityFindingKind.DEFICIENCY);
    }

    private static HospitalityObservation observation(UUID evaluationId,
                                                      HospitalityObservationCategory category,
                                                      String observedValue,
                                                      String sourceReference,
                                                      StructuredEvidence evidence) {
        return new HospitalityObservation(category, observedValue, sourceReference, evidence,
                evaluationId, UUID.randomUUID(), 1,
                com.antenapro.hotelcheck.evidence.EvidenceProvenance.DISCOVERED);
    }

    private static GuestJourneyAnalysis journey(UUID evaluationId,
                                                Set<HospitalityFinding> findings,
                                                Set<HospitalityAnalysisLimitation> limitations) {
        Set<GuestJourneyStageAnalysis> stages = new LinkedHashSet<>();
        for (GuestJourneyStage stage : GuestJourneyStage.values()) {
            Set<HospitalityFinding> impacts = findings.stream()
                    .filter(finding -> finding.journeyStages().contains(stage))
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
            Set<HospitalityAnalysisLimitation> stageLimitations = limitations.stream()
                    .filter(limitation -> limitation.journeyStages().contains(stage))
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
            GuestJourneyImpactState state = !impacts.isEmpty()
                    ? GuestJourneyImpactState.OBSERVED_IMPACT
                    : !stageLimitations.isEmpty()
                    ? GuestJourneyImpactState.LIMITATION
                    : GuestJourneyImpactState.UNSUPPORTED;
            stages.add(new GuestJourneyStageAnalysis(stage, state, impacts, stageLimitations));
        }
        Set<HospitalityAnalysisLimitation> unmapped = limitations.stream()
                .filter(limitation -> limitation.journeyStages().isEmpty())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        return new GuestJourneyAnalysis(evaluationId, stages, unmapped);
    }
}
