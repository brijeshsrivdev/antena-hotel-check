package com.antenapro.hotelcheck.analysis;

import java.util.Objects;
import java.util.Set;

public record GuestJourneyStageAnalysis(
        GuestJourneyStage stage,
        GuestJourneyImpactState state,
        Set<HospitalityFinding> observedImpacts,
        Set<HospitalityAnalysisLimitation> limitations
) {
    public GuestJourneyStageAnalysis {
        Objects.requireNonNull(stage, "stage must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(observedImpacts, "observedImpacts must not be null");
        Objects.requireNonNull(limitations, "limitations must not be null");
        observedImpacts = Set.copyOf(observedImpacts);
        limitations = Set.copyOf(limitations);

        if (observedImpacts.stream().anyMatch(finding -> finding.kind() != HospitalityFindingKind.DEFICIENCY)) {
            throw new IllegalArgumentException("observedImpacts must contain deficiencies only");
        }
        if (state == GuestJourneyImpactState.OBSERVED_IMPACT && observedImpacts.isEmpty()) {
            throw new IllegalArgumentException("observed impact state requires a source deficiency");
        }
        if (state == GuestJourneyImpactState.LIMITATION && limitations.isEmpty()) {
            throw new IllegalArgumentException("limitation state requires a stage limitation");
        }
        if (state == GuestJourneyImpactState.NO_OBSERVED_ISSUE
                && (!observedImpacts.isEmpty() || !limitations.isEmpty())) {
            throw new IllegalArgumentException("no observed issue cannot contain impacts or limitations");
        }
        if (state == GuestJourneyImpactState.UNSUPPORTED
                && (!observedImpacts.isEmpty() || !limitations.isEmpty())) {
            throw new IllegalArgumentException("unsupported stage cannot contain impacts or limitations");
        }
    }
}
