package com.antenapro.hotelcheck.analysis;

import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;
import com.antenapro.hotelcheck.input.EvaluationTargetType;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HospitalityAnalysisReportServiceTest {

    private final HospitalityAnalysisReportService service = new HospitalityAnalysisReportService();

    @Test
    void assemblesOneEvaluationWithoutReinterpretingExistingOutputs() {
        UUID evaluationId = UUID.randomUUID();
        HospitalityFinding deficiency = finding(evaluationId, HospitalityFindingKind.DEFICIENCY);
        HospitalityAnalysisLimitation limitation = limitation(evaluationId);
        HospitalityAnalysisResult result = result(evaluationId, Set.of(deficiency), Set.of(limitation));
        GuestJourneyAnalysis journey = journey(evaluationId, deficiency, limitation);
        HospitalityRecommendation recommendation = HospitalityRecommendation.fromFinding(
                deficiency,
                RecommendationCategory.IMPROVE_ROOM_INFORMATION,
                "Improve room information for guest decision-making.",
                Set.of(GuestJourneyStage.EXPLORE));
        CanonicalEvaluationRequest.EvaluationTarget target = target();

        HospitalityAnalysisReport report = service.assemble(
                result, journey, Set.of(recommendation), target);

        assertThat(report.evaluationId()).isEqualTo(evaluationId);
        assertThat(report.hotelTarget()).isSameAs(target);
        assertThat(report.analysisResult()).isSameAs(result);
        assertThat(report.guestJourneyAnalysis()).isSameAs(journey);
        assertThat(report.coverage()).isSameAs(result.coverage());
        assertThat(report.limitations()).containsExactly(limitation);
        assertThat(report.deficiencies()).containsExactly(deficiency);
        assertThat(report.recommendations()).containsExactly(recommendation);
        assertThat(report.strengths()).isEmpty();
        assertThat(report.executiveSummary().deficiencyCount()).isEqualTo(1);
        assertThat(report.executiveSummary().limitationCount()).isEqualTo(1);
        assertThat(report.executiveSummary().recommendationCount()).isEqualTo(1);
        assertThat(report.executiveSummary().stagesWithObservedImpact())
                .containsExactly(GuestJourneyStage.EXPLORE);
    }

    @Test
    void ordinaryObservationIsNotPromotedToStrength() {
        UUID evaluationId = UUID.randomUUID();
        HospitalityFinding observation = finding(evaluationId, HospitalityFindingKind.OBSERVATION);
        HospitalityAnalysisResult result = result(evaluationId, Set.of(observation), Set.of());
        GuestJourneyAnalysis journey = emptyJourney(evaluationId);

        HospitalityAnalysisReport report = service.assemble(
                result, journey, Set.of(), target());

        assertThat(report.deficiencies()).isEmpty();
        assertThat(report.strengths()).isEmpty();
        assertThat(report.executiveSummary().deficiencyCount()).isZero();
    }

    @Test
    void missingHotelIdentityRemainsMissingRatherThanBeingInvented() {
        UUID evaluationId = UUID.randomUUID();
        HospitalityAnalysisResult result = result(evaluationId, Set.of(), Set.of());

        HospitalityAnalysisReport report = service.assemble(
                result, emptyJourney(evaluationId), Set.of(), null);

        assertThat(report.hotelTarget()).isNull();
    }

    @Test
    void recommendationsAreReusedAndNeverGeneratedByTheReportService() {
        UUID evaluationId = UUID.randomUUID();
        HospitalityFinding deficiency = finding(evaluationId, HospitalityFindingKind.DEFICIENCY);
        HospitalityAnalysisResult result = result(evaluationId, Set.of(deficiency), Set.of());
        GuestJourneyAnalysis journey = journey(evaluationId, deficiency, null);

        HospitalityAnalysisReport report = service.assemble(
                result, journey, Set.of(), target());

        assertThat(report.recommendations()).isEmpty();
        assertThat(report.executiveSummary().recommendationCount()).isZero();
        assertThat(report.deficiencies()).containsExactly(deficiency);
    }

    @Test
    void evaluationARejectsRecommendationFromEvaluationB() {
        UUID evaluationA = UUID.randomUUID();
        UUID evaluationB = UUID.randomUUID();
        HospitalityFinding findingB = finding(evaluationB, HospitalityFindingKind.DEFICIENCY);
        HospitalityRecommendation recommendationB = HospitalityRecommendation.fromFinding(
                findingB,
                RecommendationCategory.IMPROVE_ROOM_INFORMATION,
                "Improve room information for guest decision-making.",
                Set.of(GuestJourneyStage.EXPLORE));

        HospitalityAnalysisResult resultA = result(evaluationA, Set.of(), Set.of());

        assertThatThrownBy(() -> service.assemble(
                resultA, emptyJourney(evaluationA), Set.of(recommendationB), target()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("recommendations must belong to analysisResult evaluationId");
    }

    @Test
    void evaluationARejectsJourneyFromEvaluationB() {
        UUID evaluationA = UUID.randomUUID();
        UUID evaluationB = UUID.randomUUID();
        HospitalityAnalysisResult resultA = result(evaluationA, Set.of(), Set.of());

        assertThatThrownBy(() -> service.assemble(
                resultA, emptyJourney(evaluationB), Set.of(), target()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("guestJourneyAnalysis must belong to analysisResult evaluationId");
    }

    @Test
    void bookingTruthRemainsUnchangedByReportAggregation() {
        UUID evaluationId = UUID.randomUUID();
        HospitalityAnalysisResult result = result(evaluationId, Set.of(), Set.of());

        HospitalityAnalysisReport report = service.assemble(
                result, emptyJourney(evaluationId), Set.of(), target());

        assertThat(report.deficiencies()).isEmpty();
        assertThat(report.recommendations()).isEmpty();
    }

    @Test
    void identicalInputsProduceEqualReports() {
        UUID evaluationId = UUID.randomUUID();
        HospitalityFinding deficiency = finding(evaluationId, HospitalityFindingKind.DEFICIENCY);
        HospitalityAnalysisLimitation limitation = limitation(evaluationId);
        HospitalityAnalysisResult result = result(evaluationId, Set.of(deficiency), Set.of(limitation));
        GuestJourneyAnalysis journey = journey(evaluationId, deficiency, limitation);
        HospitalityRecommendation recommendation = HospitalityRecommendation.fromFinding(
                deficiency,
                RecommendationCategory.IMPROVE_ROOM_INFORMATION,
                "Improve room information for guest decision-making.",
                Set.of(GuestJourneyStage.EXPLORE));
        CanonicalEvaluationRequest.EvaluationTarget target = target();

        HospitalityAnalysisReport first = service.assemble(result, journey, Set.of(recommendation), target);
        HospitalityAnalysisReport second = service.assemble(result, journey, Set.of(recommendation), target);

        assertThat(first).isEqualTo(second);
    }

    private static HospitalityFinding finding(UUID evaluationId, HospitalityFindingKind kind) {
        HospitalityFinding finding = mock(HospitalityFinding.class);
        when(finding.evaluationId()).thenReturn(evaluationId);
        when(finding.kind()).thenReturn(kind);
        return finding;
    }

    private static HospitalityAnalysisLimitation limitation(UUID evaluationId) {
        HospitalityAnalysisLimitation limitation = mock(HospitalityAnalysisLimitation.class);
        when(limitation.evaluationId()).thenReturn(evaluationId);
        return limitation;
    }

    private static HospitalityAnalysisResult result(
            UUID evaluationId,
            Set<HospitalityFinding> findings,
            Set<HospitalityAnalysisLimitation> limitations
    ) {
        return new HospitalityAnalysisResult(
                evaluationId,
                findings,
                limitations,
                coverage(evaluationId, findings, limitations));
    }

    private static HospitalityAnalysisCoverage coverage(
            UUID evaluationId,
            Set<HospitalityFinding> findings,
            Set<HospitalityAnalysisLimitation> limitations
    ) {
        Set<GuestJourneyStage> intendedStages = Set.of(
                GuestJourneyStage.DISCOVER,
                GuestJourneyStage.UNDERSTAND,
                GuestJourneyStage.EXPLORE,
                GuestJourneyStage.TRUST,
                GuestJourneyStage.BOOK);
        Set<HospitalityAnalysisDimension> intendedDimensions = Set.of(HospitalityAnalysisDimension.values());
        return new HospitalityAnalysisCoverage(
                evaluationId,
                intendedStages,
                intendedDimensions,
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                findings,
                limitations,
                HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED,
                "Report test coverage representation.");
    }

    private static GuestJourneyAnalysis journey(
            UUID evaluationId,
            HospitalityFinding deficiency,
            HospitalityAnalysisLimitation limitation
    ) {
        Set<HospitalityAnalysisLimitation> exploreLimitations = limitation == null
                ? Set.of()
                : Set.of();
        Set<GuestJourneyStageAnalysis> stages = new LinkedHashSet<>();
        for (GuestJourneyStage stage : GuestJourneyStage.values()) {
            if (stage == GuestJourneyStage.EXPLORE && deficiency != null) {
                stages.add(new GuestJourneyStageAnalysis(
                        stage,
                        GuestJourneyImpactState.OBSERVED_IMPACT,
                        Set.of(deficiency),
                        exploreLimitations));
            } else {
                stages.add(new GuestJourneyStageAnalysis(
                        stage,
                        GuestJourneyImpactState.UNSUPPORTED,
                        Set.of(),
                        Set.of()));
            }
        }
        Set<HospitalityAnalysisLimitation> unmapped = limitation == null
                ? Set.of()
                : Set.of(limitation);
        return new GuestJourneyAnalysis(evaluationId, stages, unmapped);
    }

    private static GuestJourneyAnalysis emptyJourney(UUID evaluationId) {
        return journey(evaluationId, null, null);
    }

    private static CanonicalEvaluationRequest.EvaluationTarget target() {
        return new CanonicalEvaluationRequest.EvaluationTarget(
                EvaluationTargetType.WEBSITE,
                "Grand Hotel",
                "Pune",
                "https://hotel.example.com",
                new CanonicalEvaluationRequest.IdentityContext("Grand Hotel", "Pune"));
    }
}
