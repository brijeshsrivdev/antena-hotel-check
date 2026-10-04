package com.antenapro.hotelcheck.hospitality;

import com.antenapro.hotelcheck.acquisition.AcquisitionMethod;
import com.antenapro.hotelcheck.acquisition.AcquisitionOutcome;
import com.antenapro.hotelcheck.acquisition.AcquisitionResult;
import com.antenapro.hotelcheck.evidence.EvidenceNormalizationService;
import com.antenapro.hotelcheck.evidence.EvidenceProvenance;
import com.antenapro.hotelcheck.evidence.StructuredEvidence;
import com.antenapro.hotelcheck.evaluation.CapabilityOutcome;
import com.antenapro.hotelcheck.evaluation.CapabilityOutcomeStatus;
import com.antenapro.hotelcheck.evaluation.Evaluation;
import com.antenapro.hotelcheck.evaluation.EvaluationAcquisitionResult;
import com.antenapro.hotelcheck.evaluation.EvaluationAttempt;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HospitalityObservationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-04T00:00:00Z"), ZoneOffset.UTC);
    private static final String WEBSITE = "https://hotel.example.com";
    private final HotelEvaluationInputValidator inputValidator = new HotelEvaluationInputValidator();
    private final EvidenceNormalizationService normalizationService = new EvidenceNormalizationService();
    private final HospitalityObservationService service = new HospitalityObservationService();

    @Test
    void extractsAllSixBoundedHospitalityObservationCategories() {
        String html = """
                <html><head><title>Sunrise Hotel</title></head>
                <body>
                  <h1>Sunrise Hotel</h1>
                  <nav>
                    <a href="/rooms">Rooms &amp; Suites</a>
                    <a href="/amenities">Amenities</a>
                    <a href="/contact">Contact Us</a>
                    <a href="/book">Book Now</a>
                    <a href="/dining">Dining</a>
                  </nav>
                </body></html>
                """;

        StructuredEvidence evidence = evidence(successfulAcquisition(html));
        List<HospitalityObservation> observations = service.observe(evidence);

        assertTrue(observations.stream().anyMatch(o -> o.category() == HospitalityObservationCategory.HOTEL_IDENTITY
                && o.observedValue().equals("Sunrise Hotel")));
        assertTrue(observations.stream().anyMatch(o -> o.category() == HospitalityObservationCategory.ROOMS
                && o.observedValue().contains("Rooms & Suites")));
        assertTrue(observations.stream().anyMatch(o -> o.category() == HospitalityObservationCategory.AMENITIES
                && o.observedValue().contains("Amenities")));
        assertTrue(observations.stream().anyMatch(o -> o.category() == HospitalityObservationCategory.CONTACT
                && o.observedValue().contains("Contact Us")));
        assertTrue(observations.stream().anyMatch(o -> o.category() == HospitalityObservationCategory.BOOKING
                && o.observedValue().contains("Book Now")));
        assertTrue(observations.stream().anyMatch(o -> o.category() == HospitalityObservationCategory.DINING
                && o.observedValue().contains("Dining")));
        assertTrue(observations.stream().allMatch(o -> o.provenance() == EvidenceProvenance.DISCOVERED));
    }

    @Test
    void preservesAttributionEvidenceAndObservedReference() {
        StructuredEvidence evidence = evidence(successfulAcquisition(
                "<html><h1>Sunrise Hotel</h1><a href=\"/book\">Book Now</a><a href=\"mailto:stay@hotel.example.com\">Email us</a></html>"));

        List<HospitalityObservation> observations = service.observe(evidence);
        HospitalityObservation booking = observations.stream()
                .filter(o -> o.category() == HospitalityObservationCategory.BOOKING)
                .findFirst()
                .orElseThrow();
        HospitalityObservation contact = observations.stream()
                .filter(o -> o.category() == HospitalityObservationCategory.CONTACT)
                .findFirst()
                .orElseThrow();

        assertEquals(evidence.evaluationId(), booking.evaluationId());
        assertEquals(evidence.attemptId(), booking.attemptId());
        assertEquals(evidence.attemptNumber(), booking.attemptNumber());
        assertSame(evidence, booking.supportingEvidence());
        assertEquals(EvidenceProvenance.DISCOVERED, booking.provenance());
        assertTrue(booking.observedValue().contains("/book"));
        assertTrue(booking.sourceReference().contains("href=/book"));
        assertSame(evidence, contact.supportingEvidence());
        assertTrue(contact.observedValue().contains("mailto:stay@hotel.example.com"));
    }

    @Test
    void recognizesStrongContactSignalsButRejectsSupplierContact() {
        String html = """
                <html><body>
                  <a href="tel:+911234567890">Call us</a>
                  <a href="mailto:stay@hotel.example.com">Guest Email</a>
                  <a href="/suppliers">Contact our suppliers</a>
                </body></html>
                """;

        List<HospitalityObservation> observations = service.observe(evidence(successfulAcquisition(html)));

        assertEquals(2, observations.stream()
                .filter(o -> o.category() == HospitalityObservationCategory.CONTACT)
                .count());
        assertFalse(observations.stream()
                .anyMatch(o -> o.category() == HospitalityObservationCategory.CONTACT
                        && o.observedValue().contains("suppliers")));
    }

    @Test
    void rejectsContextFreeConferenceRoomsAsHotelRooms() {
        String html = "<html><body><p>Conference rooms available for business meetings.</p></body></html>";

        List<HospitalityObservation> observations = service.observe(evidence(successfulAcquisition(html)));

        assertTrue(observations.stream().noneMatch(o -> o.category() == HospitalityObservationCategory.ROOMS));
    }

    @Test
    void rejectsGenericRoomKeywordOutsideSemanticHospitalityContext() {
        String html = "<html><body><p>The server room is restricted to staff.</p></body></html>";

        List<HospitalityObservation> observations = service.observe(evidence(successfulAcquisition(html)));

        assertTrue(observations.stream().noneMatch(o -> o.category() == HospitalityObservationCategory.ROOMS));
    }

    @Test
    void failedOrContentlessEvidenceProducesNoObservations() {
        StructuredEvidence timeout = evidence(new AcquisitionResult(
                AcquisitionOutcome.TIMEOUT,
                WEBSITE,
                WEBSITE,
                null,
                null,
                CLOCK.instant(),
                AcquisitionMethod.UNAVAILABLE,
                null,
                Map.of(),
                List.of(),
                "request timed out"
        ));
        StructuredEvidence contentless = evidence(new AcquisitionResult(
                AcquisitionOutcome.SUCCESS,
                WEBSITE,
                WEBSITE,
                204,
                "text/html",
                CLOCK.instant(),
                AcquisitionMethod.HTTP_PUBLIC,
                "",
                Map.of(),
                List.of(),
                null
        ));

        assertTrue(service.observe(timeout).isEmpty());
        assertTrue(service.observe(contentless).isEmpty());
    }

    @Test
    void nonDiscoveredEvidenceDoesNotBecomeObservedHospitalityFact() {
        StructuredEvidence source = evidence(successfulAcquisition("<h1>Sunrise Hotel</h1>"));
        StructuredEvidence nonDiscovered = new StructuredEvidence(
                source.evaluationId(),
                source.attemptId(),
                source.attemptNumber(),
                source.sourceObservation(),
                EvidenceProvenance.INFERRED,
                source.acquisitionOutcome(),
                source.requestedUrl(),
                source.finalUrl(),
                source.retrievalTimestamp(),
                source.acquisitionMethod(),
                source.statusCode(),
                source.contentType(),
                source.observedContent(),
                source.limitation()
        );

        assertTrue(service.observe(nonDiscovered).isEmpty());
    }

    @Test
    void observationIsDeterministicAndDoesNotMutateEvidence() {
        String html = "<html><head><title>Sunrise Hotel</title></head><body><a href=\"/rooms\">Rooms</a><a href=\"/book\">Book Now</a></body></html>";
        StructuredEvidence evidence = evidence(successfulAcquisition(html));
        String originalContent = evidence.observedContent();

        List<HospitalityObservation> first = service.observe(evidence);
        List<HospitalityObservation> second = service.observe(evidence);

        assertEquals(first.stream().map(this::signature).toList(), second.stream().map(this::signature).toList());
        assertEquals(originalContent, evidence.observedContent());
        assertEquals(EvidenceProvenance.DISCOVERED, evidence.sourceProvenance());
        assertSame(evidence.sourceObservation().acquisitionResult(), evidence.sourceObservation().acquisitionResult());
    }

    private String signature(HospitalityObservation observation) {
        return observation.category() + "|" + observation.observedValue() + "|"
                + observation.sourceReference() + "|" + observation.provenance();
    }

    private StructuredEvidence evidence(AcquisitionResult acquisition) {
        return normalizationService.normalize(source(acquisition));
    }

    private EvaluationAcquisitionResult source(AcquisitionResult acquisition) {
        CanonicalEvaluationRequest request = request(WEBSITE);
        Evaluation evaluation = Evaluation.create(request, CLOCK);
        EvaluationAttempt attempt = evaluation.currentAttempt();
        CapabilityOutcome outcome = CapabilityOutcome.record(
                "public-web-acquisition",
                acquisition.outcome() == AcquisitionOutcome.SUCCESS
                        ? CapabilityOutcomeStatus.SUCCEEDED
                        : CapabilityOutcomeStatus.FAILED,
                acquisition.outcome().name(),
                CLOCK.instant()
        );
        return new EvaluationAcquisitionResult(evaluation, attempt, acquisition, outcome);
    }

    private AcquisitionResult successfulAcquisition(String body) {
        return new AcquisitionResult(
                AcquisitionOutcome.SUCCESS,
                WEBSITE,
                WEBSITE,
                200,
                "text/html",
                CLOCK.instant(),
                AcquisitionMethod.HTTP_PUBLIC,
                body,
                Map.of(),
                List.of(),
                null
        );
    }

    private CanonicalEvaluationRequest request(String websiteUrl) {
        ValidationResult result = inputValidator.validate(new HotelEvaluationInput(null, null, websiteUrl));
        return ((ValidationResult.Accepted) result).request();
    }
}
