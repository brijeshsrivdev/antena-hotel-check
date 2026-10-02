package com.antenapro.hotelcheck.evaluation;

import java.time.Instant;
import java.util.Map;

public record TerminalOutcome(
        EvaluationLifecycleState state,
        String reason,
        Map<String, String> metadata,
        Instant recordedAt
) {
    public TerminalOutcome {
        if (state == null || !state.isTerminal()) {
            throw new IllegalArgumentException("state must be terminal");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        if (recordedAt == null) {
            throw new IllegalArgumentException("recordedAt must not be null");
        }
    }
}
