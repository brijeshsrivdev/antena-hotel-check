package com.antenapro.hotelcheck.analysis;

import com.antenapro.hotelcheck.evidence.EvidenceProvenance;
import com.antenapro.hotelcheck.hospitality.HospitalityObservation;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class HospitalityAnalysisSignalService {

    public Optional<HospitalityAnalysisSignal> qualify(HospitalityObservation observation) {
        Objects.requireNonNull(observation, "observation must not be null");

        if (observation.provenance() != EvidenceProvenance.DISCOVERED
                || !observation.supportingEvidence().representsSuccessfulObservation()
                || !observation.supportingEvidence().hasObservedContent()
                || observation.supportingEvidence().observedContent().isBlank()) {
            return Optional.empty();
        }

        return switch (observation.category()) {
            case HOTEL_IDENTITY -> signal(
                    observation,
                    Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.UNDERSTAND),
                    "Hotel/property identity information was observed.");
            case ROOMS -> signal(
                    observation,
                    Set.of(GuestJourneyStage.EXPLORE),
                    "A room-related entry point or room information was observed.");
            case AMENITIES -> signal(
                    observation,
                    Set.of(GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE),
                    "Guest-facing amenity or facility information was observed.");
            case CONTACT -> signal(
                    observation,
                    Set.of(GuestJourneyStage.DISCOVER, GuestJourneyStage.TRUST),
                    "A guest contact or location path was observed.");
            case BOOKING -> signal(
                    observation,
                    Set.of(GuestJourneyStage.BOOK),
                    "A booking entry point was observed.");
            case DINING -> signal(
                    observation,
                    Set.of(GuestJourneyStage.UNDERSTAND, GuestJourneyStage.EXPLORE),
                    "Guest-facing dining information was observed.");
        };
    }

    private static Optional<HospitalityAnalysisSignal> signal(
            HospitalityObservation observation,
            Set<GuestJourneyStage> journeyStages,
            String interpretation
    ) {
        return Optional.of(new HospitalityAnalysisSignal(
                observation,
                journeyStages,
                interpretation,
                HospitalityAnalysisSignalStatus.QUALIFIED
        ));
    }
}
