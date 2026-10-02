package com.antenapro.hotelcheck.evaluation;

public enum EvaluationLifecycleState {
    ACCEPTED,
    RUNNING,
    COMPLETED,
    INCOMPLETE,
    UNRESOLVED,
    FAILED;

    public boolean isTerminal() {
        return switch (this) {
            case COMPLETED, INCOMPLETE, UNRESOLVED, FAILED -> true;
            case ACCEPTED, RUNNING -> false;
        };
    }
}
