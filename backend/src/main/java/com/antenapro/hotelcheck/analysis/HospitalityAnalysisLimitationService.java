package com.antenapro.hotelcheck.analysis;

import com.antenapro.hotelcheck.acquisition.AcquisitionOutcome;
import com.antenapro.hotelcheck.evidence.EvidenceProvenance;
import com.antenapro.hotelcheck.evidence.StructuredEvidence;
import com.antenapro.hotelcheck.hospitality.HospitalityObservationCategory;

import java.util.Objects;
import java.util.Set;

public final class HospitalityAnalysisLimitationService {

    private static final Set<AcquisitionOutcome> SUPPORTED_LIMITATION_OUTCOMES = Set.of(
            AcquisitionOutcome.HTTP_ERROR,
            AcquisitionOutcome.TIMEOUT,
            AcquisitionOutcome.REDIRECT_LIMIT_EXCEEDED,
            AcquisitionOutcome.RESPONSE_TOO_LARGE,
            AcquisitionOutcome.NETWORK_ERROR
    );

    public HospitalityAnalysisLimitation create(StructuredEvidence evidence) {
        return create(evidence, Set.of(), Set.of());
    }

    public HospitalityAnalysisLimitation create(
            StructuredEvidence evidence,
            Set<HospitalityObservationCategory> categories,
            Set<GuestJourneyStage> journeyStages
    ) {
        Objects.requireNonNull(evidence, "evidence must not be null");
        Objects.requireNonNull(categories, "categories must not be null");
        Objects.requireNonNull(journeyStages, "journeyStages must not be null");

        if (evidence.sourceProvenance() != EvidenceProvenance.DISCOVERED) {
            throw new IllegalArgumentException("limitation requires discovered source evidence");
        }
        if (!SUPPORTED_LIMITATION_OUTCOMES.contains(evidence.acquisitionOutcome())) {
            throw new IllegalArgumentException(
                    "acquisition outcome does not support an inability-to-verify limitation: "
                            + evidence.acquisitionOutcome()
            );
        }

        String explanation = evidence.limitation();
        if (explanation == null || explanation.isBlank()) {
            explanation = "Unable to verify source because acquisition outcome was "
                    + evidence.acquisitionOutcome().name();
        }

        return new HospitalityAnalysisLimitation(
                categories,
                journeyStages,
                HospitalityAnalysisLimitationType.UNABLE_TO_VERIFY,
                evidence.acquisitionOutcome(),
                evidence,
                explanation
        );
    }
}
