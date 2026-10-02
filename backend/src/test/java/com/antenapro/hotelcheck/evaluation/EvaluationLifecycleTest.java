package com.antenapro.hotelcheck.evaluation;

import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;
import com.antenapro.hotelcheck.input.HotelEvaluationInput;
import com.antenapro.hotelcheck.input.HotelEvaluationInputValidator;
import com.antenapro.hotelcheck.input.ValidationResult;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluationLifecycleTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-02T00:00:00Z"), ZoneOffset.UTC);
    private final HotelEvaluationInputValidator inputValidator = new HotelEvaluationInputValidator();

    @Test
    void acceptedRequestCreatesAcceptedEvaluationAndCanStartRunning() {
        Evaluation evaluation = Evaluation.create(request("The Grand Hotel", "Mumbai", null), CLOCK);

        assertEquals(EvaluationLifecycleState.ACCEPTED, evaluation.state());
        assertEquals(1, evaluation.currentAttempt().attemptNumber());

        evaluation.start();

        assertEquals(EvaluationLifecycleState.RUNNING, evaluation.state());
        assertEquals(CLOCK.instant(), evaluation.currentAttempt().startedAt());
    }

    @Test
    void terminalOutcomeRequiresRunningState() {
        Evaluation evaluation = Evaluation.create(request("The Grand Hotel", "Mumbai", null), CLOCK);

        assertThrows(InvalidLifecycleTransitionException.class,
                () -> evaluation.currentAttempt().failed("not started", Map.of()));
        assertEquals(EvaluationLifecycleState.ACCEPTED, evaluation.state());
    }

    @Test
    void onlyRunningCanReachEachTerminalState() {
        Evaluation completed = runningEvaluation();
        completed.currentAttempt().complete(new OwnerFacingOutcome(true, true));
        assertEquals(EvaluationLifecycleState.COMPLETED, completed.state());

        Evaluation incomplete = runningEvaluation();
        incomplete.currentAttempt().incomplete("preview unavailable", new OwnerFacingOutcome(true, false));
        assertEquals(EvaluationLifecycleState.INCOMPLETE, incomplete.state());

        Evaluation unresolved = runningEvaluation();
        unresolved.currentAttempt().unresolved("target is ambiguous");
        assertEquals(EvaluationLifecycleState.UNRESOLVED, unresolved.state());

        Evaluation failed = runningEvaluation();
        failed.currentAttempt().failed("systemic execution failure", Map.of("source", "test"));
        assertEquals(EvaluationLifecycleState.FAILED, failed.state());
    }

    @Test
    void completedRequiresBothOwnerFacingOutcomes() {
        Evaluation evaluation = runningEvaluation();

        assertThrows(IllegalArgumentException.class,
                () -> evaluation.currentAttempt().complete(new OwnerFacingOutcome(true, false)));
        assertEquals(EvaluationLifecycleState.RUNNING, evaluation.state());

        evaluation.currentAttempt().complete(new OwnerFacingOutcome(true, true));
        assertEquals(EvaluationLifecycleState.COMPLETED, evaluation.state());
    }

    @Test
    void incompleteCanRepresentMissingRequiredOutcomeWithoutClaimingCompletion() {
        Evaluation evaluation = runningEvaluation();
        OwnerFacingOutcome outcome = new OwnerFacingOutcome(true, false);

        evaluation.currentAttempt().incomplete("interactive preview unavailable", outcome);

        assertEquals(EvaluationLifecycleState.INCOMPLETE, evaluation.state());
        assertEquals(outcome, evaluation.currentAttempt().ownerFacingOutcome());
    }

    @Test
    void capabilityFailureDoesNotAutomaticallyFailEvaluation() {
        Evaluation evaluation = runningEvaluation();
        evaluation.currentAttempt().recordCapabilityOutcome(
                CapabilityOutcome.record(
                        "booking-observation",
                        CapabilityOutcomeStatus.FAILED,
                        "third-party flow unavailable",
                        CLOCK.instant()));

        evaluation.currentAttempt().complete(new OwnerFacingOutcome(true, true));

        assertEquals(EvaluationLifecycleState.COMPLETED, evaluation.state());
        assertEquals(CapabilityOutcomeStatus.FAILED,
                evaluation.currentAttempt().capabilityOutcomes().get("booking-observation").status());
    }

    @Test
    void terminalStateIsProtectedFromMutation() {
        Evaluation evaluation = runningEvaluation();
        evaluation.currentAttempt().failed("execution failed", Map.of("attempt", "1"));

        TerminalOutcome terminalOutcome = evaluation.currentAttempt().terminalOutcome();
        assertEquals(EvaluationLifecycleState.FAILED, terminalOutcome.state());
        assertEquals("execution failed", terminalOutcome.reason());
        assertEquals("1", terminalOutcome.metadata().get("attempt"));

        assertThrows(InvalidLifecycleTransitionException.class,
                () -> evaluation.currentAttempt().start());
        assertThrows(InvalidLifecycleTransitionException.class,
                () -> evaluation.currentAttempt().complete(new OwnerFacingOutcome(true, true)));
        assertEquals(terminalOutcome, evaluation.currentAttempt().terminalOutcome());
    }

    @Test
    void retryCreatesDistinctAttemptAndPreservesPreviousTerminalOutcome() {
        Evaluation evaluation = runningEvaluation();
        evaluation.currentAttempt().failed("transient failure", Map.of("cause", "timeout"));
        EvaluationAttempt firstAttempt = evaluation.currentAttempt();
        TerminalOutcome firstOutcome = firstAttempt.terminalOutcome();

        EvaluationAttempt retry = evaluation.retry();

        assertNotEquals(firstAttempt.attemptId(), retry.attemptId());
        assertEquals(1, firstAttempt.attemptNumber());
        assertEquals(2, retry.attemptNumber());
        assertEquals(EvaluationLifecycleState.FAILED, firstAttempt.state());
        assertEquals(firstOutcome, firstAttempt.terminalOutcome());
        assertEquals(EvaluationLifecycleState.RUNNING, retry.state());
        assertEquals(evaluation.request(), retry.request());
        assertEquals(2, evaluation.attempts().size());
    }

    @Test
    void retryCannotBeStartedWhileCurrentAttemptIsActive() {
        Evaluation evaluation = runningEvaluation();

        assertThrows(InvalidLifecycleTransitionException.class, evaluation::retry);
        assertEquals(1, evaluation.attempts().size());
    }

    @Test
    void materiallyChangedTargetRequiresSeparateEvaluation() {
        Evaluation first = Evaluation.create(request("The Grand Hotel", "Mumbai", null), CLOCK);
        Evaluation changedTarget = Evaluation.create(request("The Grand Hotel", "Pune", null), CLOCK);

        assertNotEquals(first.evaluationId(), changedTarget.evaluationId());
        assertNotEquals(first.request().evaluationTarget().city(),
                changedTarget.request().evaluationTarget().city());
        assertEquals(1, first.attempts().size());
        assertEquals(1, changedTarget.attempts().size());
    }

    @Test
    void unresolvedAndFailedRemainDistinctFromIncomplete() {
        Evaluation unresolved = runningEvaluation();
        unresolved.currentAttempt().unresolved("identity mismatch");

        Evaluation failed = runningEvaluation();
        failed.currentAttempt().failed("execution failure", Map.of());

        Evaluation incomplete = runningEvaluation();
        incomplete.currentAttempt().incomplete("report unavailable", new OwnerFacingOutcome(false, true));

        assertTrue(unresolved.state() != failed.state());
        assertTrue(failed.state() != incomplete.state());
        assertTrue(unresolved.state() != incomplete.state());
    }

    private Evaluation runningEvaluation() {
        Evaluation evaluation = Evaluation.create(request("The Grand Hotel", "Mumbai", null), CLOCK);
        evaluation.start();
        return evaluation;
    }

    private CanonicalEvaluationRequest request(String hotelName, String city, String websiteUrl) {
        ValidationResult result = inputValidator.validate(new HotelEvaluationInput(hotelName, city, websiteUrl));
        return ((ValidationResult.Accepted) result).request();
    }
}
