package com.antenapro.hotelcheck.persistence.jpa;

import com.antenapro.hotelcheck.evaluation.CapabilityOutcome;
import com.antenapro.hotelcheck.evaluation.CapabilityOutcomeStatus;
import com.antenapro.hotelcheck.evaluation.Evaluation;
import com.antenapro.hotelcheck.evaluation.EvaluationLifecycleState;
import com.antenapro.hotelcheck.evaluation.EvaluationRepository;
import com.antenapro.hotelcheck.evaluation.OwnerFacingOutcome;
import com.antenapro.hotelcheck.evaluation.TerminalOutcome;
import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(JpaEvaluationRepository.class)
class JpaEvaluationRepositoryTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private EvaluationRepository repository;

    @Test
    void savesAndReloadsAcceptedEvaluationAndCanonicalRequest() {
        Evaluation evaluation = Evaluation.create(request());
        Evaluation saved = repository.save(evaluation);
        Evaluation reloaded = repository.findById(saved.evaluationId()).orElseThrow();

        assertThat(reloaded.evaluationId()).isEqualTo(saved.evaluationId());
        assertThat(reloaded.request()).isEqualTo(request());
        assertThat(reloaded.state()).isEqualTo(EvaluationLifecycleState.ACCEPTED);
        assertThat(reloaded.attempts()).hasSize(1);
        assertThat(reloaded.currentAttempt().attemptId()).isEqualTo(saved.currentAttempt().attemptId());
    }

    @Test
    void savesAndReloadsRunningEvaluationWithTimestamps() {
        Evaluation evaluation = Evaluation.create(request());
        evaluation.start();
        Evaluation saved = repository.save(evaluation);
        Evaluation reloaded = repository.findById(saved.evaluationId()).orElseThrow();

        assertThat(reloaded.state()).isEqualTo(EvaluationLifecycleState.RUNNING);
        assertThat(reloaded.currentAttempt().createdAt()).isEqualTo(saved.currentAttempt().createdAt());
        assertThat(reloaded.currentAttempt().startedAt()).isEqualTo(saved.currentAttempt().startedAt());
    }

    @ParameterizedTest
    @EnumSource(value = EvaluationLifecycleState.class, names = {"COMPLETED", "INCOMPLETE", "UNRESOLVED", "FAILED"})
    void savesAndReloadsEveryTerminalState(EvaluationLifecycleState expectedState) {
        Evaluation evaluation = Evaluation.create(request());
        evaluation.start();
        switch (expectedState) {
            case COMPLETED -> evaluation.currentAttempt().complete(new OwnerFacingOutcome(true, true));
            case INCOMPLETE -> evaluation.currentAttempt().incomplete("preview unavailable", new OwnerFacingOutcome(true, false));
            case UNRESOLVED -> evaluation.currentAttempt().unresolved("hotel identity ambiguous");
            case FAILED -> evaluation.currentAttempt().failed("evaluation execution failed", Map.of("category", "TIMEOUT"));
            default -> throw new IllegalArgumentException("Unexpected test state: " + expectedState);
        }

        Evaluation saved = repository.save(evaluation);
        Evaluation reloaded = repository.findById(saved.evaluationId()).orElseThrow();

        assertThat(reloaded.state()).isEqualTo(expectedState);
        assertThat(reloaded.currentAttempt().terminalOutcome()).isNotNull();
        assertThat(reloaded.currentAttempt().terminalOutcome().state()).isEqualTo(expectedState);
        assertThat(reloaded.currentAttempt().terminalOutcome().reason()).isNotBlank();
    }

    @Test
    void preservesTerminalMetadataAndOwnerFacingOutcome() {
        Evaluation evaluation = Evaluation.create(request());
        evaluation.start();
        evaluation.currentAttempt().failed("execution failed", Map.of("category", "TIMEOUT", "source", "browser"));
        Evaluation saved = repository.save(evaluation);
        Evaluation reloaded = repository.findById(saved.evaluationId()).orElseThrow();
        TerminalOutcome outcome = reloaded.currentAttempt().terminalOutcome();

        assertThat(outcome.metadata()).containsEntry("category", "TIMEOUT").containsEntry("source", "browser");
        assertThat(outcome.recordedAt()).isNotNull();

        Evaluation complete = Evaluation.create(request());
        complete.start();
        complete.currentAttempt().complete(new OwnerFacingOutcome(true, true));
        Evaluation completeSaved = repository.save(complete);
        Evaluation completeReloaded = repository.findById(completeSaved.evaluationId()).orElseThrow();

        assertThat(completeReloaded.currentAttempt().ownerFacingOutcome()).isEqualTo(new OwnerFacingOutcome(true, true));
    }

    @Test
    void preservesCapabilityOutcomes() {
        Evaluation evaluation = Evaluation.create(request());
        evaluation.start();
        Instant recordedAt = Instant.parse("2026-10-02T01:00:00Z");
        evaluation.currentAttempt().recordCapabilityOutcome(
                CapabilityOutcome.record("public-evidence", CapabilityOutcomeStatus.PARTIAL, "booking engine unavailable", recordedAt));
        Evaluation saved = repository.save(evaluation);

        Evaluation reloaded = repository.findById(saved.evaluationId()).orElseThrow();
        CapabilityOutcome outcome = reloaded.currentAttempt().capabilityOutcomes().get("public-evidence");

        assertThat(outcome).isNotNull();
        assertThat(outcome.status()).isEqualTo(CapabilityOutcomeStatus.PARTIAL);
        assertThat(outcome.detail()).isEqualTo("booking engine unavailable");
        assertThat(outcome.recordedAt()).isEqualTo(recordedAt);
    }

    @Test
    void reloadThenRetryAppendsAttemptWithoutMutatingPreviousTerminalAttempt() {
        Evaluation evaluation = Evaluation.create(request());
        evaluation.start();
        evaluation.currentAttempt().failed("first attempt failed", Map.of("attempt", "1"));
        Evaluation saved = repository.save(evaluation);

        Evaluation reloaded = repository.findById(saved.evaluationId()).orElseThrow();
        UUID firstAttemptId = reloaded.currentAttempt().attemptId();
        Instant firstTerminalTime = reloaded.currentAttempt().terminalOutcome().recordedAt();
        reloaded.retry();
        Evaluation afterRetry = repository.save(reloaded);

        Evaluation persisted = repository.findById(afterRetry.evaluationId()).orElseThrow();
        assertThat(persisted.attempts()).hasSize(2);
        assertThat(persisted.attempts().get(0).attemptId()).isEqualTo(firstAttemptId);
        assertThat(persisted.attempts().get(0).state()).isEqualTo(EvaluationLifecycleState.FAILED);
        assertThat(persisted.attempts().get(0).terminalOutcome().recordedAt()).isEqualTo(firstTerminalTime);
        assertThat(persisted.attempts().get(0).terminalOutcome().metadata()).containsEntry("attempt", "1");
        assertThat(persisted.attempts().get(1).attemptNumber()).isEqualTo(2);
        assertThat(persisted.attempts().get(1).state()).isEqualTo(EvaluationLifecycleState.RUNNING);
        assertThat(persisted.attempts().get(1).request()).isEqualTo(persisted.request());
        assertThat(persisted.attempts().get(1).attemptId()).isNotEqualTo(firstAttemptId);
    }

    private CanonicalEvaluationRequest request() {
        return new CanonicalEvaluationRequest(
                new CanonicalEvaluationRequest.RequestInput("  Hotel Example  ", " Pune ", "https://hotel.example/rooms#details"),
                new CanonicalEvaluationRequest.NormalizedInput("Hotel Example", "Pune", "https://hotel.example/rooms"),
                new CanonicalEvaluationRequest.EvaluationTarget(
                        com.antenapro.hotelcheck.input.EvaluationTargetType.WEBSITE,
                        null, null, "https://hotel.example/rooms",
                        new CanonicalEvaluationRequest.IdentityContext("Hotel Example", "Pune")
                )
        );
    }
}
