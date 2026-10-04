package com.antenapro.hotelcheck.analysis;

import com.antenapro.hotelcheck.evidence.StructuredEvidence;
import com.antenapro.hotelcheck.hospitality.HospitalityObservation;
import com.antenapro.hotelcheck.hospitality.HospitalityObservationCategory;
import com.antenapro.hotelcheck.hospitality.HospitalityObservationService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class HospitalityAnalysisService {

    private static final Set<GuestJourneyStage> INTENDED_JOURNEY_STAGES =
            Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND,
                    GuestJourneyStage.EXPLORE, GuestJourneyStage.TRUST, GuestJourneyStage.BOOK);

    private static final Set<HospitalityAnalysisDimension> FULL_INTENDED_DIMENSIONS = Set.of(
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

    private static final Map<HospitalityObservationCategory, HospitalityAnalysisDimension> DIMENSION_BY_CATEGORY =
            Map.of(
                    HospitalityObservationCategory.HOTEL_IDENTITY,
                    HospitalityAnalysisDimension.HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING,
                    HospitalityObservationCategory.ROOMS,
                    HospitalityAnalysisDimension.ROOMS_AND_ROOM_INFORMATION,
                    HospitalityObservationCategory.AMENITIES,
                    HospitalityAnalysisDimension.AMENITIES_AND_GUEST_FACING_INFORMATION,
                    HospitalityObservationCategory.CONTACT,
                    HospitalityAnalysisDimension.CONTACT_AND_LOCATION,
                    HospitalityObservationCategory.BOOKING,
                    HospitalityAnalysisDimension.BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS,
                    HospitalityObservationCategory.DINING,
                    HospitalityAnalysisDimension.AMENITIES_AND_GUEST_FACING_INFORMATION
            );

    private final HospitalityObservationService observationService;
    private final HospitalityAnalysisSignalService signalService;
    private final HospitalityFindingService findingService;
    private final HospitalityAnalysisLimitationService limitationService;

    public HospitalityAnalysisService() {
        this(new HospitalityObservationService(),
                new HospitalityAnalysisSignalService(),
                new HospitalityFindingService(),
                new HospitalityAnalysisLimitationService());
    }

    HospitalityAnalysisService(
            HospitalityObservationService observationService,
            HospitalityAnalysisSignalService signalService,
            HospitalityFindingService findingService,
            HospitalityAnalysisLimitationService limitationService
    ) {
        this.observationService = Objects.requireNonNull(observationService, "observationService must not be null");
        this.signalService = Objects.requireNonNull(signalService, "signalService must not be null");
        this.findingService = Objects.requireNonNull(findingService, "findingService must not be null");
        this.limitationService = Objects.requireNonNull(limitationService, "limitationService must not be null");
    }

    public HospitalityAnalysisResult analyze(
            UUID evaluationId,
            List<StructuredEvidence> evidenceItems,
            HospitalityAnalysisCoverageState coverageState
    ) {
        Objects.requireNonNull(evaluationId, "evaluationId must not be null");
        Objects.requireNonNull(evidenceItems, "evidenceItems must not be null");
        Objects.requireNonNull(coverageState, "coverageState must not be null");

        validateEvaluationIntegrity(evaluationId, evidenceItems);

        Set<HospitalityFinding> findings = new LinkedHashSet<>();
        Set<HospitalityAnalysisLimitation> limitations = new LinkedHashSet<>();

        for (StructuredEvidence evidence : evidenceItems) {
            if (supportsUnableToVerify(evidence)) {
                limitations.add(limitationService.create(evidence));
                continue;
            }

            for (HospitalityObservation observation : observationService.observe(evidence)) {
                signalService.qualify(observation)
                        .flatMap(findingService::create)
                        .ifPresent(findings::add);
            }
        }

        HospitalityAnalysisCoverage coverage = createCoverage(
                evaluationId,
                findings,
                limitations,
                coverageState
        );

        return new HospitalityAnalysisResult(evaluationId, findings, limitations, coverage);
    }

    private static boolean supportsUnableToVerify(StructuredEvidence evidence) {
        return switch (evidence.acquisitionOutcome()) {
            case HTTP_ERROR, TIMEOUT, REDIRECT_LIMIT_EXCEEDED, RESPONSE_TOO_LARGE, NETWORK_ERROR -> true;
            case SUCCESS, UNSUPPORTED_SCHEME, INVALID_TARGET -> false;
        };
    }

    private static HospitalityAnalysisCoverage createCoverage(
            UUID evaluationId,
            Set<HospitalityFinding> findings,
            Set<HospitalityAnalysisLimitation> limitations,
            HospitalityAnalysisCoverageState state
    ) {
        Set<GuestJourneyStage> assessableJourneyStages = new LinkedHashSet<>();
        Set<HospitalityAnalysisDimension> assessableDimensions = new LinkedHashSet<>();
        for (HospitalityFinding finding : findings) {
            assessableJourneyStages.addAll(finding.journeyStages());
            HospitalityAnalysisDimension dimension = DIMENSION_BY_CATEGORY.get(finding.category());
            if (dimension != null) {
                assessableDimensions.add(dimension);
            }
        }

        Set<GuestJourneyStage> limitedJourneyStages = new LinkedHashSet<>();
        Set<HospitalityAnalysisDimension> limitedDimensions = new LinkedHashSet<>();
        for (HospitalityAnalysisLimitation limitation : limitations) {
            limitedJourneyStages.addAll(limitation.journeyStages());
            for (HospitalityObservationCategory category : limitation.categories()) {
                HospitalityAnalysisDimension dimension = DIMENSION_BY_CATEGORY.get(category);
                if (dimension != null) {
                    limitedDimensions.add(dimension);
                }
            }
        }

        return new HospitalityAnalysisCoverage(
                evaluationId,
                INTENDED_JOURNEY_STAGES,
                FULL_INTENDED_DIMENSIONS,
                assessableJourneyStages,
                assessableDimensions,
                limitedJourneyStages,
                limitedDimensions,
                findings,
                limitations,
                state,
                "Coverage state is supplied by the governed caller; this engine derives only factual assessable and limited scope from generated analysis artifacts."
        );
    }

    private static void validateEvaluationIntegrity(UUID evaluationId, List<StructuredEvidence> evidenceItems) {
        for (StructuredEvidence evidence : evidenceItems) {
            Objects.requireNonNull(evidence, "evidenceItems must not contain null");
            if (!evaluationId.equals(evidence.evaluationId())) {
                throw new IllegalArgumentException("all evidenceItems must belong to evaluationId");
            }
            if (!evaluationId.equals(evidence.sourceObservation().evaluation().evaluationId())) {
                throw new IllegalArgumentException("evidence source observation must belong to evaluationId");
            }
            if (!evidence.attemptId().equals(evidence.sourceObservation().attempt().attemptId())
                    || evidence.attemptNumber() != evidence.sourceObservation().attempt().attemptNumber()) {
                throw new IllegalArgumentException("evidence attempt attribution must match source observation");
            }
        }
    }
}
