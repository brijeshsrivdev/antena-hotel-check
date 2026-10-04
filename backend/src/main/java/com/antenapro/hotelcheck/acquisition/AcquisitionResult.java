package com.antenapro.hotelcheck.acquisition;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record AcquisitionResult(
        AcquisitionOutcome outcome,
        String requestedUrl,
        String finalUrl,
        Integer statusCode,
        String contentType,
        Instant retrievalTimestamp,
        AcquisitionMethod acquisitionMethod,
        String body,
        Map<String, String> provenanceMetadata,
        List<String> redirectChain,
        String errorMessage
) {
    public AcquisitionResult {
        if (provenanceMetadata == null) {
            provenanceMetadata = Collections.emptyMap();
        } else {
            provenanceMetadata = Map.copyOf(provenanceMetadata);
        }
        if (redirectChain == null) {
            redirectChain = Collections.emptyList();
        } else {
            redirectChain = List.copyOf(redirectChain);
        }
    }

    public boolean isSuccess() {
        return outcome == AcquisitionOutcome.SUCCESS;
    }

    public static AcquisitionResult failure(
            AcquisitionOutcome outcome,
            String requestedUrl,
            String finalUrl,
            String errorMessage,
            List<String> redirectChain
    ) {
        return new AcquisitionResult(
                outcome,
                requestedUrl,
                finalUrl != null ? finalUrl : requestedUrl,
                null,
                null,
                Instant.now(),
                AcquisitionMethod.UNAVAILABLE,
                null,
                Collections.emptyMap(),
                redirectChain,
                errorMessage
        );
    }

    public static AcquisitionResult failure(
            AcquisitionOutcome outcome,
            String requestedUrl,
            String errorMessage
    ) {
        return failure(outcome, requestedUrl, requestedUrl, errorMessage, Collections.emptyList());
    }
}
