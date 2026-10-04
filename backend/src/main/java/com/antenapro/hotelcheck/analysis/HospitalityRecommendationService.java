package com.antenapro.hotelcheck.analysis;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maps governed hospitality deficiencies to a small deterministic set of bounded actions.
 * It consumes one guest-journey analysis and never discovers or infers new hotel facts.
 */
public final class HospitalityRecommendationService {

    public Set<HospitalityRecommendation> recommend(GuestJourneyAnalysis journeyAnalysis) {
        Objects.requireNonNull(journeyAnalysis, "journeyAnalysis must not be null");

        List<HospitalityFinding> deficiencies = journeyAnalysis.stages().stream()
                .flatMap(stage -> stage.observedImpacts().stream())
                .filter(finding -> finding.kind() == HospitalityFindingKind.DEFICIENCY)
                .filter(finding -> journeyAnalysis.evaluationId().equals(finding.evaluationId()))
                .distinct()
                .toList();

        Set<HospitalityRecommendation> recommendations = new LinkedHashSet<>();
        for (HospitalityFinding finding : deficiencies) {
            RecommendationMapping mapping = mappingFor(finding, deficiencies);
            if (mapping == null) {
                continue;
            }

            Set<GuestJourneyStage> stages = journeyStagesFor(journeyAnalysis, finding);
            if (!stages.isEmpty()) {
                recommendations.add(HospitalityRecommendation.fromFinding(
                        finding, mapping.category(), mapping.action(), stages));
            }
        }
        return Set.copyOf(recommendations);
    }

    private static RecommendationMapping mappingFor(
            HospitalityFinding finding,
            List<HospitalityFinding> deficiencies
    ) {
        if (finding.status() != HospitalityFindingStatus.VERIFIED_OBSERVED) {
            return null;
        }

        return switch (finding.category()) {
            case BOOKING -> new RecommendationMapping(
                    RecommendationCategory.IMPROVE_BOOKING_DISCOVERABILITY,
                    "Make the booking entry point easier to discover.");
            case ROOMS -> new RecommendationMapping(
                    RecommendationCategory.IMPROVE_ROOM_INFORMATION,
                    "Improve room information for guest decision-making.");
            case AMENITIES, DINING -> new RecommendationMapping(
                    RecommendationCategory.IMPROVE_GUEST_FACING_INFORMATION,
                    "Improve guest-facing stay information.");
            case CONTACT -> new RecommendationMapping(
                    RecommendationCategory.IMPROVE_CONTACT_LOCATION_INFORMATION,
                    "Make contact and location information clearer.");
            case HOTEL_IDENTITY -> isIdentityConflict(finding, deficiencies)
                    ? new RecommendationMapping(
                            RecommendationCategory.RESOLVE_INFORMATION_CONFLICT,
                            "Resolve conflicting hotel identity information.")
                    : null;
        };
    }

    private static boolean isIdentityConflict(HospitalityFinding finding, List<HospitalityFinding> deficiencies) {
        return deficiencies.stream()
                .filter(other -> other != finding)
                .anyMatch(finding::isIdentityConflictWith);
    }

    private static Set<GuestJourneyStage> journeyStagesFor(
            GuestJourneyAnalysis journeyAnalysis,
            HospitalityFinding finding
    ) {
        return journeyAnalysis.stages().stream()
                .filter(stage -> stage.observedImpacts().contains(finding))
                .map(GuestJourneyStageAnalysis::stage)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private record RecommendationMapping(RecommendationCategory category, String action) {
    }
}
