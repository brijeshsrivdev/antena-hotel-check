package com.antenapro.hotelcheck.analysis;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Derives the guest-journey lens from one immutable hospitality analysis result.
 * No new evidence, coverage, score, recommendation, or external data is created.
 */
public final class GuestJourneyAnalysisService {

    public GuestJourneyAnalysis analyze(HospitalityAnalysisResult result) {
        Objects.requireNonNull(result, "result must not be null");

        UUID evaluationId = result.evaluationId();
        Set<GuestJourneyStageAnalysis> stages = new LinkedHashSet<>();
        for (GuestJourneyStage stage : GuestJourneyStage.values()) {
            Set<HospitalityFinding> impacts = result.findings().stream()
                    .filter(finding -> finding.kind() == HospitalityFindingKind.DEFICIENCY)
                    .filter(finding -> finding.journeyStages().contains(stage))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            Set<HospitalityAnalysisLimitation> limitations = result.limitations().stream()
                    .filter(limitation -> limitation.journeyStages().contains(stage))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            GuestJourneyImpactState state;
            if (!impacts.isEmpty()) {
                state = GuestJourneyImpactState.OBSERVED_IMPACT;
            } else if (!limitations.isEmpty()) {
                state = GuestJourneyImpactState.LIMITATION;
            } else if (result.coverage().assessableJourneyStages().contains(stage)) {
                state = GuestJourneyImpactState.NO_OBSERVED_ISSUE;
            } else {
                state = GuestJourneyImpactState.UNSUPPORTED;
            }

            stages.add(new GuestJourneyStageAnalysis(stage, state, impacts, limitations));
        }

        Set<HospitalityAnalysisLimitation> unmappedLimitations = result.limitations().stream()
                .filter(limitation -> limitation.journeyStages().isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new GuestJourneyAnalysis(evaluationId, stages, unmappedLimitations);
    }
}
