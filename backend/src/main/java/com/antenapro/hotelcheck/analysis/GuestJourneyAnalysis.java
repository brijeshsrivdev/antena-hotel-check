package com.antenapro.hotelcheck.analysis;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record GuestJourneyAnalysis(
        UUID evaluationId,
        Set<GuestJourneyStageAnalysis> stages,
        Set<HospitalityAnalysisLimitation> unmappedLimitations
) {
    public GuestJourneyAnalysis {
        Objects.requireNonNull(evaluationId, "evaluationId must not be null");
        Objects.requireNonNull(stages, "stages must not be null");
        Objects.requireNonNull(unmappedLimitations, "unmappedLimitations must not be null");
        stages = Set.copyOf(stages);
        unmappedLimitations = Set.copyOf(unmappedLimitations);

        Set<GuestJourneyStage> representedStages = stages.stream()
                .map(GuestJourneyStageAnalysis::stage)
                .collect(Collectors.toSet());
        if (!representedStages.equals(Set.of(GuestJourneyStage.values()))) {
            throw new IllegalArgumentException("journey analysis must contain exactly one result for each journey stage");
        }
        if (stages.stream().flatMap(stage -> stage.observedImpacts().stream())
                .anyMatch(finding -> !evaluationId.equals(finding.evaluationId()))) {
            throw new IllegalArgumentException("journey stage impacts must belong to evaluationId");
        }
        if (stages.stream().flatMap(stage -> stage.limitations().stream())
                .anyMatch(limitation -> !evaluationId.equals(limitation.evaluationId()))) {
            throw new IllegalArgumentException("journey stage limitations must belong to evaluationId");
        }
        if (unmappedLimitations.stream().anyMatch(limitation -> !evaluationId.equals(limitation.evaluationId()))) {
            throw new IllegalArgumentException("unmapped limitations must belong to evaluationId");
        }
    }

    public GuestJourneyStageAnalysis stage(GuestJourneyStage stage) {
        Objects.requireNonNull(stage, "stage must not be null");
        return stages.stream().filter(candidate -> candidate.stage() == stage).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("stage is not represented: " + stage));
    }
}
