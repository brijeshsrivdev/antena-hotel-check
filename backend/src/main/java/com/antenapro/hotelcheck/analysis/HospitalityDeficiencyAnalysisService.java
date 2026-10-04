package com.antenapro.hotelcheck.analysis;

import com.antenapro.hotelcheck.evidence.EvidenceProvenance;
import com.antenapro.hotelcheck.evidence.StructuredEvidence;
import com.antenapro.hotelcheck.hospitality.HospitalityObservation;
import com.antenapro.hotelcheck.hospitality.HospitalityObservationCategory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Applies the small set of REQ-027 deficiency rules directly to retained,
 * qualified hospitality signals. Rules intentionally require successful,
 * discovered page context before making a negative guest-facing claim.
 */
public final class HospitalityDeficiencyAnalysisService {

    private static final String ROOM_DECISION_SUPPORT_PATTERN =
            "(?i)\\b(?:bed|beds|sleep(?:s|ing)?|guest(?:s)?|occupancy|person(?:s)?|size|sqm|m²|square\\s+met(?:re|er)s?|amenities|description|view|balcony|bathroom)\\b";
    private static final String CONTACT_DETAIL_PATTERN =
            "(?i)(?:mailto:|tel:|<address\\b|\\b(?:phone|telephone|email|address|directions?|location|map)\\b)";
    private static final String AMENITY_DETAIL_PATTERN =
            "(?i)\\b(?:pool|swimming\\s+pool|spa|fitness|gym|wi[- ]?fi|parking|front\\s+desk|housekeeping|concierge|restaurant|dining|breakfast|bar|cafe|café)\\b";

    public Set<HospitalityFinding> analyze(
            List<StructuredEvidence> evidenceItems,
            List<HospitalityAnalysisSignal> qualifiedSignals
    ) {
        Objects.requireNonNull(evidenceItems, "evidenceItems must not be null");
        Objects.requireNonNull(qualifiedSignals, "qualifiedSignals must not be null");

        Set<HospitalityFinding> findings = new LinkedHashSet<>();
        List<HospitalityAnalysisSignal> validSignals = qualifiedSignals.stream()
                .filter(this::isValidSignal)
                .toList();

        for (StructuredEvidence evidence : evidenceItems) {
            List<HospitalityAnalysisSignal> pageSignals = validSignals.stream()
                    .filter(signal -> signal.originatingObservation().supportingEvidence().equals(evidence))
                    .toList();
            if (pageSignals.isEmpty()) {
                continue;
            }

            addBookingDeficiency(findings, pageSignals, evidence);
            addRoomInformationDeficiency(findings, pageSignals, evidence);
            addContactDeficiency(findings, pageSignals, evidence);
            addGuestInformationDeficiency(findings, pageSignals, evidence);
        }

        addIdentityConflictFindings(findings, validSignals);
        return findings;
    }

    private void addBookingDeficiency(
            Set<HospitalityFinding> findings,
            List<HospitalityAnalysisSignal> pageSignals,
            StructuredEvidence evidence
    ) {
        if (!isRoomContext(evidence) || hasCategory(pageSignals, HospitalityObservationCategory.BOOKING)) {
            return;
        }

        firstSignal(pageSignals, HospitalityObservationCategory.ROOMS)
                .ifPresent(signal -> findings.add(deficiency(signal,
                        "The observed room journey page does not expose a usable booking action for the guest.")));
    }

    private void addRoomInformationDeficiency(
            Set<HospitalityFinding> findings,
            List<HospitalityAnalysisSignal> pageSignals,
            StructuredEvidence evidence
    ) {
        if (!isRoomContext(evidence)) {
            return;
        }

        firstSignal(pageSignals, HospitalityObservationCategory.ROOMS)
                .filter(signal -> !containsRoomDecisionSupport(evidence.observedContent()))
                .ifPresent(signal -> findings.add(deficiency(signal,
                        "The observed room context does not contain meaningful room decision-support information.")));
    }

    private void addContactDeficiency(
            Set<HospitalityFinding> findings,
            List<HospitalityAnalysisSignal> pageSignals,
            StructuredEvidence evidence
    ) {
        if (!isContactContext(evidence)) {
            return;
        }

        firstSignal(pageSignals, HospitalityObservationCategory.CONTACT)
                .filter(signal -> !CONTACT_DETAIL_PATTERN.matcher(evidence.observedContent()).find())
                .ifPresent(signal -> findings.add(deficiency(signal,
                        "The observed contact/location page does not expose a usable guest-facing contact or location detail.")));
    }

    private void addGuestInformationDeficiency(
            Set<HospitalityFinding> findings,
            List<HospitalityAnalysisSignal> pageSignals,
            StructuredEvidence evidence
    ) {
        if (!isGuestInformationContext(evidence)) {
            return;
        }

        firstGuestInformationSignal(pageSignals)
                .filter(signal -> !AMENITY_DETAIL_PATTERN.matcher(evidence.observedContent()).find())
                .ifPresent(signal -> findings.add(deficiency(signal,
                        "The observed guest-information context does not contain meaningful stay-related decision-support information.")));
    }

    private void addIdentityConflictFindings(
            Set<HospitalityFinding> findings,
            List<HospitalityAnalysisSignal> validSignals
    ) {
        List<HospitalityAnalysisSignal> identitySignals = validSignals.stream()
                .filter(signal -> signal.originatingObservation().category() == HospitalityObservationCategory.HOTEL_IDENTITY)
                .toList();

        for (int i = 0; i < identitySignals.size(); i++) {
            HospitalityAnalysisSignal left = identitySignals.get(i);
            for (int j = i + 1; j < identitySignals.size(); j++) {
                HospitalityAnalysisSignal right = identitySignals.get(j);
                if (!left.originatingObservation().evaluationId().equals(right.originatingObservation().evaluationId())
                        || sameSource(left, right)
                        || !materiallyConflictingIdentity(left.originatingObservation().observedValue(),
                        right.originatingObservation().observedValue())) {
                    continue;
                }

                String text = "Retained public sources expose conflicting hotel identity information: '"
                        + left.originatingObservation().observedValue() + "' versus '"
                        + right.originatingObservation().observedValue() + "'.";
                findings.add(deficiency(left, text));
                findings.add(deficiency(right, text));
            }
        }
    }

    private static boolean isValidSignal(HospitalityAnalysisSignal signal) {
        if (signal == null || signal.status() != HospitalityAnalysisSignalStatus.QUALIFIED) {
            return false;
        }
        HospitalityObservation observation = signal.originatingObservation();
        StructuredEvidence evidence = observation.supportingEvidence();
        return observation.provenance() == EvidenceProvenance.DISCOVERED
                && evidence.representsSuccessfulObservation()
                && evidence.hasObservedContent()
                && evidence.observedContent() != null
                && !evidence.observedContent().isBlank();
    }

    private static boolean hasCategory(
            List<HospitalityAnalysisSignal> signals,
            HospitalityObservationCategory category
    ) {
        return signals.stream().anyMatch(signal -> signal.originatingObservation().category() == category);
    }

    private static java.util.Optional<HospitalityAnalysisSignal> firstSignal(
            List<HospitalityAnalysisSignal> signals,
            HospitalityObservationCategory category
    ) {
        return signals.stream()
                .filter(signal -> signal.originatingObservation().category() == category)
                .findFirst();
    }

    private static java.util.Optional<HospitalityAnalysisSignal> firstGuestInformationSignal(
            List<HospitalityAnalysisSignal> signals
    ) {
        return signals.stream()
                .filter(signal -> signal.originatingObservation().category() == HospitalityObservationCategory.AMENITIES
                        || signal.originatingObservation().category() == HospitalityObservationCategory.DINING)
                .findFirst();
    }

    private static HospitalityFinding deficiency(HospitalityAnalysisSignal source, String interpretation) {
        HospitalityAnalysisSignal deficiencySignal = new HospitalityAnalysisSignal(
                source.originatingObservation(),
                source.journeyStages(),
                interpretation,
                HospitalityAnalysisSignalStatus.QUALIFIED
        );
        return new HospitalityFinding(deficiencySignal, HospitalityFindingStatus.VERIFIED_OBSERVED);
    }

    private static boolean sameSource(HospitalityAnalysisSignal left, HospitalityAnalysisSignal right) {
        return left.originatingObservation().sourceReference()
                .equals(right.originatingObservation().sourceReference());
    }

    private static boolean materiallyConflictingIdentity(String left, String right) {
        String normalizedLeft = normalize(left);
        String normalizedRight = normalize(right);
        return !normalizedLeft.equals(normalizedRight)
                && !normalizedLeft.contains(normalizedRight)
                && !normalizedRight.contains(normalizedLeft);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static boolean isRoomContext(StructuredEvidence evidence) {
        return pathContains(evidence, "room", "suite", "accommodation", "stay");
    }

    private static boolean isContactContext(StructuredEvidence evidence) {
        return pathContains(evidence, "contact", "location", "directions");
    }

    private static boolean isGuestInformationContext(StructuredEvidence evidence) {
        return pathContains(evidence, "amenit", "facilit", "guest", "dining", "restaurant");
    }

    private static boolean pathContains(StructuredEvidence evidence, String... fragments) {
        String url = evidence.finalUrl() != null ? evidence.finalUrl() : evidence.requestedUrl();
        if (url == null) {
            return false;
        }
        String normalized = url.toLowerCase(Locale.ROOT);
        for (String fragment : fragments) {
            if (normalized.contains("/" + fragment) || normalized.endsWith(fragment)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsRoomDecisionSupport(String content) {
        return content != null && content.matches("(?s).*" + ROOM_DECISION_SUPPORT_PATTERN + ".*");
    }
}
