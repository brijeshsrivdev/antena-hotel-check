package com.antenapro.hotelcheck.evaluation;

import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class EvaluationAttempt {
    private final UUID attemptId;
    private final int attemptNumber;
    private final CanonicalEvaluationRequest request;
    private final Instant createdAt;
    private final Clock clock;
    private final Map<String, CapabilityOutcome> capabilityOutcomes = new LinkedHashMap<>();
    private EvaluationLifecycleState state;
    private Instant startedAt;
    private TerminalOutcome terminalOutcome;
    private OwnerFacingOutcome ownerFacingOutcome;

    private EvaluationAttempt(UUID attemptId, int attemptNumber, CanonicalEvaluationRequest request,
                              Clock clock, Instant createdAt) {
        this.attemptId = Objects.requireNonNull(attemptId, "attemptId must not be null");
        if (attemptNumber < 1) throw new IllegalArgumentException("attemptNumber must be positive");
        this.attemptNumber = attemptNumber;
        this.request = Objects.requireNonNull(request, "request must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.state = EvaluationLifecycleState.ACCEPTED;
    }

    static EvaluationAttempt first(CanonicalEvaluationRequest request, Clock clock) {
        return new EvaluationAttempt(UUID.randomUUID(), 1, request, clock, clock.instant());
    }

    static EvaluationAttempt retry(UUID attemptId, int attemptNumber, CanonicalEvaluationRequest request, Clock clock) {
        return new EvaluationAttempt(attemptId, attemptNumber, request, clock, clock.instant());
    }

    public static EvaluationAttempt rehydrate(UUID attemptId, int attemptNumber, CanonicalEvaluationRequest request,
                                              Instant createdAt, Instant startedAt, EvaluationLifecycleState state,
                                              TerminalOutcome terminalOutcome, OwnerFacingOutcome ownerFacingOutcome,
                                              Map<String, CapabilityOutcome> capabilityOutcomes, Clock clock) {
        EvaluationAttempt attempt = new EvaluationAttempt(attemptId, attemptNumber, request, clock, createdAt);
        attempt.startedAt = startedAt;
        attempt.state = Objects.requireNonNull(state, "state must not be null");
        attempt.terminalOutcome = terminalOutcome;
        attempt.ownerFacingOutcome = ownerFacingOutcome;
        if (capabilityOutcomes != null) attempt.capabilityOutcomes.putAll(capabilityOutcomes);
        if (state == EvaluationLifecycleState.ACCEPTED && startedAt != null) {
            throw new IllegalArgumentException("accepted attempt cannot have startedAt");
        }
        if (state != EvaluationLifecycleState.ACCEPTED && startedAt == null) {
            throw new IllegalArgumentException("started attempt must have startedAt");
        }
        if (state.isTerminal() && terminalOutcome == null) {
            throw new IllegalArgumentException("terminal attempt must have terminal outcome");
        }
        if (!state.isTerminal() && terminalOutcome != null) {
            throw new IllegalArgumentException("active attempt cannot have terminal outcome");
        }
        if (terminalOutcome != null && terminalOutcome.state() != state) {
            throw new IllegalArgumentException("terminal outcome state must match attempt state");
        }
        if (state == EvaluationLifecycleState.COMPLETED &&
                (ownerFacingOutcome == null || !ownerFacingOutcome.complete())) {
            throw new IllegalArgumentException("completed attempt must have complete owner-facing outcome");
        }
        return attempt;
    }

    public UUID attemptId() { return attemptId; }
    public int attemptNumber() { return attemptNumber; }
    public CanonicalEvaluationRequest request() { return request; }
    public Instant createdAt() { return createdAt; }
    public EvaluationLifecycleState state() { return state; }
    public Instant startedAt() { return startedAt; }
    public TerminalOutcome terminalOutcome() { return terminalOutcome; }
    public OwnerFacingOutcome ownerFacingOutcome() { return ownerFacingOutcome; }
    public Map<String, CapabilityOutcome> capabilityOutcomes() { return Map.copyOf(capabilityOutcomes); }

    public void start() {
        transitionTo(EvaluationLifecycleState.RUNNING);
        startedAt = clock.instant();
    }

    public void recordCapabilityOutcome(CapabilityOutcome outcome) {
        Objects.requireNonNull(outcome, "outcome must not be null");
        ensureActive();
        capabilityOutcomes.put(outcome.capability(), outcome);
    }

    public void complete(OwnerFacingOutcome outcome) {
        Objects.requireNonNull(outcome, "outcome must not be null");
        ensureRunning();
        if (!outcome.complete()) throw new IllegalArgumentException(
                "COMPLETED requires both analysis report and interactive preview outcomes");
        ownerFacingOutcome = outcome;
        finish(EvaluationLifecycleState.COMPLETED, "Evaluation completed", Map.of());
    }

    public void incomplete(String reason, OwnerFacingOutcome outcome) {
        Objects.requireNonNull(outcome, "outcome must not be null");
        ensureRunning();
        ownerFacingOutcome = outcome;
        finish(EvaluationLifecycleState.INCOMPLETE, reason, Map.of());
    }

    public void unresolved(String reason) { ensureRunning(); finish(EvaluationLifecycleState.UNRESOLVED, reason, Map.of()); }
    public void failed(String reason, Map<String, String> metadata) { ensureRunning(); finish(EvaluationLifecycleState.FAILED, reason, metadata); }

    private void finish(EvaluationLifecycleState terminalState, String reason, Map<String, String> metadata) {
        transitionTo(terminalState);
        terminalOutcome = new TerminalOutcome(terminalState, reason, metadata, clock.instant());
    }

    private void transitionTo(EvaluationLifecycleState nextState) {
        if (state.isTerminal()) throw new InvalidLifecycleTransitionException(state, nextState);
        boolean allowed = switch (state) {
            case ACCEPTED -> nextState == EvaluationLifecycleState.RUNNING;
            case RUNNING -> nextState == EvaluationLifecycleState.RUNNING || nextState.isTerminal();
            case COMPLETED, INCOMPLETE, UNRESOLVED, FAILED -> false;
        };
        if (!allowed) throw new InvalidLifecycleTransitionException(state, nextState);
        state = nextState;
    }

    private void ensureRunning() {
        if (state != EvaluationLifecycleState.RUNNING) throw new InvalidLifecycleTransitionException(state, EvaluationLifecycleState.RUNNING);
    }

    private void ensureActive() {
        if (state.isTerminal()) throw new InvalidLifecycleTransitionException(state, EvaluationLifecycleState.RUNNING);
    }
}
