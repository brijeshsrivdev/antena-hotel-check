package com.antenapro.hotelcheck.evaluation;

import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Evaluation {
    private final UUID evaluationId;
    private final CanonicalEvaluationRequest request;
    private final Clock clock;
    private final List<EvaluationAttempt> attempts = new ArrayList<>();

    private Evaluation(UUID evaluationId, CanonicalEvaluationRequest request, Clock clock) {
        this.evaluationId = Objects.requireNonNull(evaluationId, "evaluationId must not be null");
        this.request = Objects.requireNonNull(request, "request must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public static Evaluation create(CanonicalEvaluationRequest request) {
        return create(request, Clock.systemUTC());
    }

    public static Evaluation create(CanonicalEvaluationRequest request, Clock clock) {
        Evaluation evaluation = new Evaluation(UUID.randomUUID(), request, clock);
        evaluation.attempts.add(EvaluationAttempt.first(request, clock));
        return evaluation;
    }

    static Evaluation rehydrate(UUID evaluationId, CanonicalEvaluationRequest request,
                                List<EvaluationAttempt> attempts, Clock clock) {
        Objects.requireNonNull(attempts, "attempts must not be null");
        if (attempts.isEmpty()) {
            throw new IllegalArgumentException("evaluation must contain at least one attempt");
        }

        Evaluation evaluation = new Evaluation(evaluationId, request, clock);
        int expectedAttemptNumber = 1;
        for (EvaluationAttempt attempt : attempts) {
            if (attempt.attemptNumber() != expectedAttemptNumber) {
                throw new IllegalArgumentException("attempt numbers must be contiguous from 1");
            }
            if (!evaluation.request().equals(attempt.request())) {
                throw new IllegalArgumentException("attempt request must match evaluation request");
            }
            evaluation.attempts.add(attempt);
            expectedAttemptNumber++;
        }
        return evaluation;
    }

    public UUID evaluationId() { return evaluationId; }
    public CanonicalEvaluationRequest request() { return request; }
    public EvaluationLifecycleState state() { return currentAttempt().state(); }
    public EvaluationAttempt currentAttempt() { return attempts.get(attempts.size() - 1); }
    public List<EvaluationAttempt> attempts() { return List.copyOf(attempts); }

    public void start() { currentAttempt().start(); }

    public EvaluationAttempt retry() {
        if (!currentAttempt().state().isTerminal()) {
            throw new InvalidLifecycleTransitionException(currentAttempt().state(), EvaluationLifecycleState.RUNNING);
        }
        EvaluationAttempt retry = EvaluationAttempt.retry(UUID.randomUUID(), attempts.size() + 1, request, clock);
        attempts.add(retry);
        retry.start();
        return retry;
    }
}
