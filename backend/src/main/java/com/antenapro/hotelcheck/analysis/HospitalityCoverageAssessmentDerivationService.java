package com.antenapro.hotelcheck.analysis;

import com.antenapro.hotelcheck.hospitality.HospitalityObservationCategory;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Derives the factual pre-classification coverage scope for exactly one evaluation.
 *
 * <p>This service consumes already-governed signals, typed identity-conflict facts,
 * and explicitly scoped limitations. It does not classify coverage or create any
 * upstream analysis artifacts.</p>
 */
public final class HospitalityCoverageAssessmentDerivationService {

    private static final Set<GuestJourneyStage> INTENDED_JOURNEY_STAGES = Set.of(
            GuestJourneyStage.DISCOVER,
            GuestJourneyStage.UNDERSTAND,
            GuestJourneyStage.EXPLORE,
            GuestJourneyStage.TRUST,
            GuestJourneyStage.BOOK
    );

    private static final Set<HospitalityAnalysisDimension> INTENDED_DIMENSIONS = Set.of(
            HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING,
            HospitalityAnalysisDimension.DISCOVERABILITY_AND_NAVIGATION,
            HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION,
            HospitalityAnalysisDimension.AMENITIES_AND_GUEST_FACING_INFORMATION,
            HospitalityAnalysisDimension.CONTACT_AND_LOCATION,
            HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS,
            HospitalityAnalysisDimension.TRUST_AND_CLARITY,
            HospitalityAnalysisDimension.MOBILE_AND_TECHNICAL_GUEST_EXPERIENCE,
            HospitalityAnalysisDimension.SEO_AND_STRUCTURED_DATA_SUPPORTING_SIGNALS
    );

    public HospitalityCoverageAssessment derive(
            UUID evaluationId,
            Collection<HospitalityAnalysisSignal> qualifiedSignals,
            Collection<HospitalityFinding> typedIdentityConflictFacts,
            Collection<HospitalityAnalysisLimitation> limitations
    ) {
        Objects.requireNonNull(evaluationId, "evaluationId must not be null");
        Objects.requireNonNull(qualifiedSignals, "qualifiedSignals must not be null");
        Objects.requireNonNull(typedIdentityConflictFacts, "typedIdentityConflictFacts must not be null");
        Objects.requireNonNull(limitations, "limitations must not be null");

        validateEvaluationIsolation(evaluationId, qualifiedSignals, typedIdentityConflictFacts, limitations);

        EnumSet<GuestJourneyStage> assessableJourneyStages = EnumSet.noneOf(GuestJourneyStage.class);
        EnumSet<HospitalityAnalysisDimension> assessableDimensions =
                EnumSet.noneOf(HospitalityAnalysisDimension.class);

        for (HospitalityAnalysisSignal signal : qualifiedSignals) {
            if (signal.status() != HospitalityAnalysisSignalStatus.QUALIFIED) {
                throw new IllegalArgumentException("qualifiedSignals must contain only QUALIFIED signals");
            }

            assessableJourneyStages.addAll(signal.journeyStages());
            HospitalityAnalysisDimension dimension = dimensionFor(signal.originatingObservation().category());
            if (dimension != null) {
                assessableDimensions.add(dimension);
            }
        }

        if (hasTypedMaterialIdentityConflict(typedIdentityConflictFacts)) {
            assessableDimensions.add(HospitalityAnalysisDimension.TRUST_AND_CLARITY);
        }

        EnumSet<GuestJourneyStage> limitedJourneyStages = EnumSet.noneOf(GuestJourneyStage.class);
        EnumSet<HospitalityAnalysisDimension> limitedDimensions =
                EnumSet.noneOf(HospitalityAnalysisDimension.class);

        for (HospitalityAnalysisLimitation limitation : limitations) {
            limitedJourneyStages.addAll(limitation.journeyStages());
            for (HospitalityObservationCategory category : limitation.categories()) {
                HospitalityAnalysisDimension dimension = dimensionFor(category);
                if (dimension != null) {
                    limitedDimensions.add(dimension);
                }
            }
        }

        return new HospitalityCoverageAssessment(
                evaluationId,
                INTENDED_JOURNEY_STAGES,
                INTENDED_DIMENSIONS,
                assessableJourneyStages,
                assessableDimensions,
                limitedJourneyStages,
                limitedDimensions
        );
    }

    private static HospitalityAnalysisDimension dimensionFor(HospitalityObservationCategory category) {
        return switch (category) {
            case HOTEL_IDENTITY -> HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING;
            case ROOMS -> HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION;
            case AMENITIES, DINING -> HospitalityAnalysisDimension.AMENITIES_AND_GUEST_FACING_INFORMATION;
            case CONTACT -> HospitalityAnalysisDimension.CONTACT_AND_LOCATION;
            case BOOKING -> HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS;
        };
    }

    private static boolean hasTypedMaterialIdentityConflict(Collection<HospitalityFinding> facts) {
        for (HospitalityFinding left : facts) {
            if (left.status() != HospitalityFindingStatus.VERIFIED_OBSERVED
                    || left.category() != HospitalityObservationCategory.HOTEL_IDENTITY) {
                continue;
            }
            for (HospitalityFinding right : facts) {
                if (left == right) {
                    continue;
                }
                if (right.status() == HospitalityFindingStatus.VERIFIED_OBSERVED
                        && left.isIdentityConflictWith(right)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void validateEvaluationIsolation(
            UUID evaluationId,
            Collection<HospitalityAnalysisSignal> qualifiedSignals,
            Collection<HospitalityFinding> typedIdentityConflictFacts,
            Collection<HospitalityAnalysisLimitation> limitations
    ) {
        for (HospitalityAnalysisSignal signal : qualifiedSignals) {
            Objects.requireNonNull(signal, "qualifiedSignals must not contain null");
            if (!evaluationId.equals(signal.originatingObservation().evaluationId())) {
                throw new IllegalArgumentException("all qualifiedSignals must belong to evaluationId");
            }
        }

        for (HospitalityFinding fact : typedIdentityConflictFacts) {
            Objects.requireNonNull(fact, "typedIdentityConflictFacts must not contain null");
            if (!evaluationId.equals(fact.evaluationId())) {
                throw new IllegalArgumentException("all typedIdentityConflictFacts must belong to evaluationId");
            }
        }

        for (HospitalityAnalysisLimitation limitation : limitations) {
            Objects.requireNonNull(limitation, "limitations must not contain null");
            if (!evaluationId.equals(limitation.evaluationId())) {
                throw new IllegalArgumentException("all limitations must belong to evaluationId");
            }
        }
    }
}
