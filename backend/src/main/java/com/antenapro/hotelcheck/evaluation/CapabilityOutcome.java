package com.antenapro.hotelcheck.evaluation;

import java.time.Instant;
import java.util.Objects;

public record CapabilityOutcome(
        String capability,
        CapabilityOutcomeStatus status,
        String detail,
        Instant recordedAt
) {
    public CapabilityOutcome {
        Objects.requireNonNull(capability, "capability must not be null");
        if (capability.isBlank()) {
            throw new IllegalArgumentException("capability must not be blank");
        }
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(recordedAt, "recordedAt must not be null");
    }

    public static CapabilityOutcome record(
            String capability,
            CapabilityOutcomeStatus status,
            String detail,
            Instant recordedAt
    ) {
        return new CapabilityOutcome(capability, status, detail, recordedAt);
    }
}
