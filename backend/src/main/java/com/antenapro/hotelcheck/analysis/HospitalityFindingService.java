package com.antenapro.hotelcheck.analysis;

import com.antenapro.hotelcheck.evidence.EvidenceProvenance;
import com.antenapro.hotelcheck.hospitality.HospitalityObservation;

import java.util.Objects;
import java.util.Optional;

public final class HospitalityFindingService {

    public Optional<HospitalityFinding> create(HospitalityAnalysisSignal signal) {
        Objects.requireNonNull(signal, "signal must not be null");

        if (signal.status() != HospitalityAnalysisSignalStatus.QUALIFIED) {
            return Optional.empty();
        }

        HospitalityObservation observation = signal.originatingObservation();
        if (observation.provenance() != EvidenceProvenance.DISCOVERED
                || !observation.supportingEvidence().representsSuccessfulObservation()
                || !observation.supportingEvidence().hasObservedContent()
                || observation.supportingEvidence().observedContent().isBlank()) {
            return Optional.empty();
        }

        return Optional.of(new HospitalityFinding(
                signal,
                HospitalityFindingStatus.VERIFIED_OBSERVED
        ));
    }
}
