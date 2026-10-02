package com.antenapro.hotelcheck.evaluation;

public final class InvalidLifecycleTransitionException extends IllegalStateException {
    public InvalidLifecycleTransitionException(EvaluationLifecycleState from, EvaluationLifecycleState to) {
        super("Lifecycle transition not allowed: " + from + " -> " + to);
    }
}
