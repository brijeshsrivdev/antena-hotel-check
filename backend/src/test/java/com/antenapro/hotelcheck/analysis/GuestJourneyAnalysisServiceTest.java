package com.antenapro.hotelcheck.analysis;

import com.antenapro.hotelcheck.acquisition.AcquisitionMethod;
import com.antenapro.hotelcheck.acquisition.AcquisitionOutcome;
import com.antenapro.hotelcheck.acquisition.AcquisitionResult;
import com.antenapro.hotelcheck.evidence.EvidenceProvenance;
import com.antenapro.hotelcheck.evidence.StructuredEvidence;
import com.antenapro.hotelcheck.evaluation.CapabilityOutcome;
import com.antenapro.hotelcheck.evaluation.CapabilityOutcomeStatus;
import com.antenapro.hotelcheck.evaluation.Evaluation;
import com.antenapro.hotelcheck.evaluation.EvaluationAcquisitionResult;
import com.antenapro.hotelcheck.evaluation.EvaluationAttempt;
import com.antenapro.hotelcheck.hospitality.HospitalityObservationCategory;
import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;
import com.antenapro.hotelcheck.input.HotelEvaluationInput;
import com.antenapro.hotelcheck.input.HotelEvaluationInputValidator;
import com.antenapro.hotelcheck.input.ValidationResult;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuestJourneyAnalysisServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-04T00:00:00Z"), ZoneOffset.UTC);
    private static final String WEBSITE = "https://hotel.example.com";
    private final HotelEvaluationInputValidator inputValidator = new HotelEvaluationInputValidator();
    private final HospitalityAnalysisService analysisService = new HospitalityAnalysisService();

    @Test
    void observedRoomDeficiencyMapsDeterministicallyToExplore() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/rooms", AcquisitionOutcome.SUCCESS,
                "<h1>Rooms</h1><p>Our rooms.</p>");

        GuestJourneyAnalysis analysis = analyze(evidence).guestJourneyAnalysis();
        GuestJourneyStageAnalysis explore = analysis.stage(GuestJourneyStage.EXPLORE);

        assertEquals(GuestJourneyImpactState.OBSERVED_IMPACT, explore.state());
        assertEquals(1, explore.observedImpacts().size());
        assertEquals(HospitalityFindingKind.DEFICIENCY, explore.observedImpacts().iterator().next().kind());
        assertEquals(evidence, explore.observedImpacts().iterator().next().supportingEvidence());
    }

    @Test
    void oneDeficiencyCanAffectMultipleJourneyStagesUsingTypedSignalSemantics() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/amenities", AcquisitionOutcome.SUCCESS,
                "<h1>Amenities</h1><p>Explore the property.</p>");

        GuestJourneyAnalysis analysis = analyze(evidence).guestJourneyAnalysis();

        assertEquals(GuestJourneyImpactState.OBSERVED_IMPACT, analysis.stage(GuestJourneyStage.UNDERSTAND).state());
        assertEquals(GuestJourneyImpactState.OBSERVED_IMPACT, analysis.stage(GuestJourneyStage.EXPLORE).state());
        assertEquals(Set.of(HospitalityObservationCategory.AMENITIES),
                analysis.stage(GuestJourneyStage.UNDERSTAND).observedImpacts().stream()
                        .map(HospitalityFinding::category).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void identityConflictUsesGovernedTypedSemanticsToAffectTrust() {
        Evaluation evaluation = evaluation();
        StructuredEvidence first = evidence(evaluation, "https://hotel.example.com/about", AcquisitionOutcome.SUCCESS,
                "<h1>Grand Hotel</h1>");
        StructuredEvidence second = evidence(evaluation, "https://directory.example.com/hotel", AcquisitionOutcome.SUCCESS,
                "<h1>Grand Resort Hotel</h1>");

        GuestJourneyStageAnalysis trust = analysisService
                .analyze(evaluation.evaluationId(), List.of(first, second), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED)
                .guestJourneyAnalysis().stage(GuestJourneyStage.TRUST);

        assertEquals(GuestJourneyImpactState.OBSERVED_IMPACT, trust.state());
        assertEquals(2, trust.observedImpacts().size());
        assertTrue(trust.observedImpacts().stream().allMatch(f -> f.category() == HospitalityObservationCategory.HOTEL_IDENTITY));
    }

    @Test
    void positiveEvidenceWithoutDeficiencyIsNoObservedIssueNotProofOfSuccess() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/rooms", AcquisitionOutcome.SUCCESS,
                "<h1>Rooms</h1><p>King bed, two guests, private bathroom.</p>");

        GuestJourneyStageAnalysis explore = analyze(evidence).guestJourneyAnalysis().stage(GuestJourneyStage.EXPLORE);

        assertEquals(GuestJourneyImpactState.NO_OBSERVED_ISSUE, explore.state());
        assertTrue(explore.observedImpacts().isEmpty());
        assertTrue(explore.limitations().isEmpty());
    }

    @Test
    void missingEvidenceDoesNotBecomeJourneyFailure() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/", AcquisitionOutcome.SUCCESS,
                "<title>Welcome</title><p>General information.</p>");

        GuestJourneyAnalysis analysis = analyze(evidence).guestJourneyAnalysis();

        assertTrue(analysis.stages().stream().allMatch(stage -> stage.state() == GuestJourneyImpactState.UNSUPPORTED));
    }

    @Test
    void unableToVerifyRemainsASeparateUnmappedLimitation() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/rooms", AcquisitionOutcome.TIMEOUT, null);

        HospitalityAnalysisResult result = analyze(evidence);
        GuestJourneyAnalysis analysis = result.guestJourneyAnalysis();

        assertEquals(1, analysis.unmappedLimitations().size());
        assertEquals(HospitalityAnalysisLimitationType.UNABLE_TO_VERIFY,
                analysis.unmappedLimitations().iterator().next().type());
        assertTrue(analysis.stages().stream().allMatch(stage -> stage.state() == GuestJourneyImpactState.UNSUPPORTED));
        assertTrue(analysis.stages().stream().allMatch(stage -> stage.limitations().isEmpty()));
    }

    @Test
    void explicitStageLimitationRemainsDistinguishableFromJourneyFailure() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/rooms", AcquisitionOutcome.TIMEOUT, null);
        HospitalityAnalysisResult base = analyze(evidence);
        HospitalityAnalysisLimitation limitation = new HospitalityAnalysisLimitation(
                Set.of(HospitalityObservationCategory.ROOMS),
                Set.of(GuestJourneyStage.EXPLORE),
                HospitalityAnalysisLimitationType.UNABLE_TO_VERIFY,
                evidence,
                "Room page could not be observed reliably.");
        HospitalityAnalysisResult result = new HospitalityAnalysisResult(
                base.evaluationId(), base.findings(), Set.of(limitation), base.coverage());

        GuestJourneyStageAnalysis explore = result.guestJourneyAnalysis().stage(GuestJourneyStage.EXPLORE);

        assertEquals(GuestJourneyImpactState.LIMITATION, explore.state());
        assertEquals(Set.of(limitation), explore.limitations());
        assertTrue(explore.observedImpacts().isEmpty());
    }

    @Test
    void unsupportedAcquisitionDoesNotBecomeJourneyFailure() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/rooms", AcquisitionOutcome.UNSUPPORTED_SCHEME, null);

        GuestJourneyAnalysis analysis = analyze(evidence).guestJourneyAnalysis();

        assertTrue(analysis.unmappedLimitations().isEmpty());
        assertTrue(analysis.stages().stream().allMatch(stage -> stage.state() == GuestJourneyImpactState.UNSUPPORTED));
    }

    @Test
    void noBookingObservationDoesNotCreateBookImpact() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/rooms", AcquisitionOutcome.SUCCESS,
                "<h1>Rooms</h1><p>King bed, two guests, private bathroom.</p>");

        HospitalityAnalysisResult result = analyze(evidence);
        GuestJourneyStageAnalysis book = result.guestJourneyAnalysis().stage(GuestJourneyStage.BOOK);

        assertEquals(GuestJourneyImpactState.UNSUPPORTED, book.state());
        assertTrue(book.observedImpacts().isEmpty());
        assertFalse(result.findings().stream().anyMatch(f -> f.category() == HospitalityObservationCategory.BOOKING
                && f.kind() == HospitalityFindingKind.DEFICIENCY));
    }

    @Test
    void journeyImpactPreservesEvaluationAttemptAndEvidenceProvenance() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/contact", AcquisitionOutcome.SUCCESS,
                "<h1>Contact</h1><p>Send us a message.</p>");

        HospitalityFinding impact = analyze(evidence).guestJourneyAnalysis()
                .stage(GuestJourneyStage.DISCOVER).observedImpacts().iterator().next();

        assertEquals(evidence.evaluationId(), impact.evaluationId());
        assertEquals(evidence.attemptId(), impact.attemptId());
        assertEquals(evidence.attemptNumber(), impact.attemptNumber());
        assertEquals(evidence, impact.supportingEvidence());
    }

    @Test
    void evaluationIsolationIsPreservedByAnalysisBoundary() {
        StructuredEvidence first = evidence("https://hotel.example.com/rooms", AcquisitionOutcome.SUCCESS,
                "<h1>Rooms</h1><p>Our rooms.</p>");
        StructuredEvidence second = evidence("https://other.example.com/rooms", AcquisitionOutcome.SUCCESS,
                "<h1>Rooms</h1><p>King bed, two guests.</p>");

        HospitalityAnalysisResult firstResult = analyze(first);
        HospitalityAnalysisResult secondResult = analyze(second);

        assertEquals(first.evaluationId(), firstResult.guestJourneyAnalysis().evaluationId());
        assertEquals(second.evaluationId(), secondResult.guestJourneyAnalysis().evaluationId());
        assertTrue(secondResult.guestJourneyAnalysis().stages().stream()
                .flatMap(stage -> stage.observedImpacts().stream())
                .allMatch(finding -> finding.evaluationId().equals(second.evaluationId())));
    }

    @Test
    void identicalAnalysisInputProducesIdenticalJourneyOutput() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/amenities", AcquisitionOutcome.SUCCESS,
                "<h1>Amenities</h1><p>Explore the property.</p>");

        HospitalityAnalysisResult result = analyze(evidence);
        assertEquals(result.guestJourneyAnalysis(), result.guestJourneyAnalysis());
        assertEquals(result.guestJourneyAnalysis(), new GuestJourneyAnalysisService().analyze(result));
    }

    @Test
    void ordinarySignalsRemainObservationsWhileReq027DeficienciesAreTyped() {
        StructuredEvidence evidence = evidence("https://hotel.example.com/rooms", AcquisitionOutcome.SUCCESS,
                "<h1>Rooms</h1><p>King bed, two guests, private bathroom.</p>");
        HospitalityAnalysisResult result = analyze(evidence);

        assertTrue(result.findings().stream().allMatch(f -> f.kind() == HospitalityFindingKind.OBSERVATION));
    }

    private HospitalityAnalysisResult analyze(StructuredEvidence evidence) {
        return analysisService.analyze(evidence.evaluationId(), List.of(evidence), HospitalityAnalysisCoverageState.PARTIALLY_ASSESSED);
    }

    private StructuredEvidence evidence(String url, AcquisitionOutcome outcome, String body) {
        return evidence(evaluation(), url, outcome, body);
    }

    private StructuredEvidence evidence(Evaluation evaluation, String url, AcquisitionOutcome outcome, String body) {
        Integer statusCode = outcome == AcquisitionOutcome.SUCCESS ? 200 : null;
        AcquisitionResult acquisition = new AcquisitionResult(
                outcome, url, url, statusCode, body == null ? null : "text/html", CLOCK.instant(),
                outcome == AcquisitionOutcome.TIMEOUT || outcome == AcquisitionOutcome.NETWORK_ERROR
                        ? AcquisitionMethod.UNAVAILABLE : AcquisitionMethod.HTTP_PUBLIC,
                body, Map.of(), List.of(), outcome == AcquisitionOutcome.TIMEOUT ? "request timed out" : null);
        EvaluationAttempt attempt = evaluation.currentAttempt();
        CapabilityOutcome capabilityOutcome = CapabilityOutcome.record(
                "public-web-acquisition",
                outcome == AcquisitionOutcome.SUCCESS ? CapabilityOutcomeStatus.SUCCEEDED : CapabilityOutcomeStatus.FAILED,
                outcome.name(), CLOCK.instant());
        EvaluationAcquisitionResult source = new EvaluationAcquisitionResult(evaluation, attempt, acquisition, capabilityOutcome);
        return new StructuredEvidence(evaluation.evaluationId(), attempt.attemptId(), attempt.attemptNumber(), source,
                EvidenceProvenance.DISCOVERED, outcome, acquisition.requestedUrl(), acquisition.finalUrl(),
                acquisition.retrievalTimestamp(), acquisition.acquisitionMethod(), acquisition.statusCode(),
                acquisition.contentType(), acquisition.body(), acquisition.errorMessage());
    }

    private Evaluation evaluation() {
        CanonicalEvaluationRequest request = ((ValidationResult.Accepted) inputValidator
                .validate(new HotelEvaluationInput(null, null, WEBSITE))).request();
        return Evaluation.create(request, CLOCK);
    }
}
