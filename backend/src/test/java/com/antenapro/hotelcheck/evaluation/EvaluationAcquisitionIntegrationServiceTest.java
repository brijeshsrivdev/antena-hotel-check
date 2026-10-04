package com.antenapro.hotelcheck.evaluation;

import com.antenapro.hotelcheck.acquisition.AcquisitionMethod;
import com.antenapro.hotelcheck.acquisition.AcquisitionOutcome;
import com.antenapro.hotelcheck.acquisition.AcquisitionResult;
import com.antenapro.hotelcheck.acquisition.PublicWebAcquisitionService;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class EvaluationAcquisitionIntegrationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-04T00:00:00Z"), ZoneOffset.UTC);
    private static final String WEBSITE = "https://hotel.example.com";
    private final HotelEvaluationInputValidator inputValidator = new HotelEvaluationInputValidator();

    @Test
    void successfulAcquisitionStartsEvaluationOnceAndPreservesResultAndProvenance() {
        PublicWebAcquisitionService acquisitionService = mock(PublicWebAcquisitionService.class);
        CanonicalEvaluationRequest request = request(WEBSITE);
        AcquisitionResult acquisitionResult = result(AcquisitionOutcome.SUCCESS, 200, "<html>hotel</html>", Map.of(
                "requestedUrl", WEBSITE,
                "finalUrl", WEBSITE,
                "acquisitionMethod", "HTTP_PUBLIC"));
        when(acquisitionService.acquire(request)).thenReturn(acquisitionResult);

        EvaluationAcquisitionResult result = service(acquisitionService).execute(request);

        assertEquals(EvaluationLifecycleState.RUNNING, result.evaluation().state());
        assertEquals(EvaluationLifecycleState.RUNNING, result.attempt().state());
        assertEquals(CapabilityOutcomeStatus.SUCCEEDED, result.capabilityOutcome().status());
        assertEquals(AcquisitionOutcome.SUCCESS, result.acquisitionResult().outcome());
        assertEquals("HTTP_PUBLIC", result.acquisitionResult().provenanceMetadata().get("acquisitionMethod"));
        assertSame(acquisitionResult, result.acquisitionResult());
        assertNull(result.attempt().terminalOutcome());

        verify(acquisitionService).acquire(request);
        verifyNoMoreInteractions(acquisitionService);
    }

    @Test
    void timeoutRemainsTypedFailureAndDoesNotFailEvaluation() {
        assertFailureRemainsRunning(AcquisitionOutcome.TIMEOUT, "request timed out");
    }

    @Test
    void httpErrorRemainsTypedFailureAndDoesNotFailEvaluation() {
        assertFailureRemainsRunning(AcquisitionOutcome.HTTP_ERROR, "HTTP request failed with status 503");
    }

    @Test
    void invalidTargetRemainsTypedFailureWithoutBecomingHotelFeatureAbsence() {
        PublicWebAcquisitionService acquisitionService = mock(PublicWebAcquisitionService.class);
        CanonicalEvaluationRequest request = request("https://127.0.0.1");
        AcquisitionResult acquisitionResult = result(
                AcquisitionOutcome.INVALID_TARGET,
                null,
                null,
                Map.of("reason", "non-public target"));
        when(acquisitionService.acquire(request)).thenReturn(acquisitionResult);

        EvaluationAcquisitionResult result = service(acquisitionService).execute(request);

        assertEquals(EvaluationLifecycleState.RUNNING, result.evaluation().state());
        assertEquals(CapabilityOutcomeStatus.FAILED, result.capabilityOutcome().status());
        assertEquals(AcquisitionOutcome.INVALID_TARGET, result.acquisitionResult().outcome());
        assertEquals("INVALID_TARGET", result.capabilityOutcome().detail());
        assertNull(result.attempt().terminalOutcome());

        verify(acquisitionService).acquire(request);
        verifyNoMoreInteractions(acquisitionService);
    }

    @Test
    void acquisitionFailureMetadataAndProvenanceArePreserved() {
        PublicWebAcquisitionService acquisitionService = mock(PublicWebAcquisitionService.class);
        CanonicalEvaluationRequest request = request(WEBSITE);
        Map<String, String> provenance = Map.of(
                "requestedUrl", WEBSITE,
                "finalUrl", "https://hotel.example.com/",
                "retrievalTimestamp", "2026-10-04T00:00:00Z");
        AcquisitionResult acquisitionResult = result(
                AcquisitionOutcome.REDIRECT_LIMIT_EXCEEDED,
                null,
                "redirect limit exceeded",
                provenance);
        when(acquisitionService.acquire(request)).thenReturn(acquisitionResult);

        EvaluationAcquisitionResult result = service(acquisitionService).execute(request);

        assertEquals(provenance, result.acquisitionResult().provenanceMetadata());
        assertEquals(List.of(WEBSITE), result.acquisitionResult().redirectChain());
        assertEquals("REDIRECT_LIMIT_EXCEEDED: redirect limit exceeded", result.capabilityOutcome().detail());
        assertEquals(CapabilityOutcomeStatus.FAILED,
                result.attempt().capabilityOutcomes().get("public-web-acquisition").status());
    }

    @Test
    void successfulAcquisitionDoesNotCompleteEvaluationBecauseReportAndPreviewAreNotAvailable() {
        PublicWebAcquisitionService acquisitionService = mock(PublicWebAcquisitionService.class);
        CanonicalEvaluationRequest request = request(WEBSITE);
        when(acquisitionService.acquire(request)).thenReturn(result(AcquisitionOutcome.SUCCESS, 200, "body", Map.of()));

        EvaluationAcquisitionResult result = service(acquisitionService).execute(request);

        assertEquals(EvaluationLifecycleState.RUNNING, result.evaluation().state());
        assertEquals(EvaluationLifecycleState.RUNNING, result.attempt().state());
        assertNull(result.attempt().ownerFacingOutcome());
        assertNull(result.attempt().terminalOutcome());
    }

    private void assertFailureRemainsRunning(AcquisitionOutcome outcome, String errorMessage) {
        PublicWebAcquisitionService acquisitionService = mock(PublicWebAcquisitionService.class);
        CanonicalEvaluationRequest request = request(WEBSITE);
        AcquisitionResult acquisitionResult = result(outcome, null, errorMessage, Map.of("outcome", outcome.name()));
        when(acquisitionService.acquire(request)).thenReturn(acquisitionResult);

        EvaluationAcquisitionResult result = service(acquisitionService).execute(request);

        assertEquals(EvaluationLifecycleState.RUNNING, result.evaluation().state());
        assertEquals(CapabilityOutcomeStatus.FAILED, result.capabilityOutcome().status());
        assertEquals(outcome, result.acquisitionResult().outcome());
        assertEquals(outcome.name(), result.acquisitionResult().provenanceMetadata().get("outcome"));
        assertNull(result.attempt().terminalOutcome());

        verify(acquisitionService).acquire(request);
        verifyNoMoreInteractions(acquisitionService);
    }

    private EvaluationAcquisitionIntegrationService service(PublicWebAcquisitionService acquisitionService) {
        return new EvaluationAcquisitionIntegrationService(acquisitionService, CLOCK);
    }

    private CanonicalEvaluationRequest request(String websiteUrl) {
        ValidationResult result = inputValidator.validate(new HotelEvaluationInput(null, null, websiteUrl));
        return ((ValidationResult.Accepted) result).request();
    }

    private AcquisitionResult result(
            AcquisitionOutcome outcome,
            Integer statusCode,
            String bodyOrError,
            Map<String, String> provenanceMetadata
    ) {
        boolean success = outcome == AcquisitionOutcome.SUCCESS;
        return new AcquisitionResult(
                outcome,
                WEBSITE,
                WEBSITE,
                statusCode,
                success ? "text/html" : null,
                CLOCK.instant(),
                success ? AcquisitionMethod.HTTP_PUBLIC : AcquisitionMethod.UNAVAILABLE,
                success ? bodyOrError : null,
                provenanceMetadata,
                outcome == AcquisitionOutcome.REDIRECT_LIMIT_EXCEEDED ? List.of(WEBSITE) : List.of(),
                success ? null : bodyOrError
        );
    }
}
