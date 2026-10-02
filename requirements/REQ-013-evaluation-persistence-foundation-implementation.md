# REQ-013 — Evaluation Persistence Foundation Implementation

**Status:** PR_READY  
**Owner:** Implementation session completed; awaiting orchestrator review  
**Type:** Implementation  
**Branch:** `feature/evaluation-persistence-foundation`

## Objective

Implement the smallest persistence slice required to durably store the REQ-012 evaluation lifecycle without changing the approved product contracts or introducing unrelated application infrastructure.

The implementation must allow an `Evaluation` and its distinguishable `EvaluationAttempt` records to survive process restart while preserving canonical input, lifecycle state, terminal outcome, retry history, and owner-facing completion outcomes.

## Required context

Before implementation, read these completely:

1. `docs/context/` relevant project context files.
2. `docs/specs/` relevant specifications, especially:
   - `SPEC-001` product foundation
   - evaluation input contract
   - evaluation lifecycle contract
   - evidence model contract
   - hospitality analysis contract
   - hotel experience model contract
   - interactive preview contract
3. `docs/architecture/` evaluation architecture contract.
4. `docs/decisions/` technology baseline decision.
5. `requirements/REQ-002-evaluation-input-contract.md`
6. `requirements/REQ-003-evaluation-lifecycle-contract.md`
7. `requirements/REQ-009-evaluation-architecture-contract.md`
8. `requirements/REQ-010-technology-baseline-decision.md`
9. `requirements/REQ-011-evaluation-input-foundation-implementation.md`
10. `requirements/REQ-012-evaluation-lifecycle-foundation-implementation.md`

Do not rely on chat history when repository documents are available.

## Scope

Implement only the persistence foundation for the existing evaluation lifecycle domain.

### 1. Persistence model

Persist at minimum:

- Evaluation identity.
- Canonical evaluation request / evaluation target.
- Evaluation attempts with stable attempt identity and attempt number.
- Attempt lifecycle state.
- Created/started/terminal timestamps where defined by the current domain model.
- Terminal outcome reason and metadata.
- Owner-facing report/preview availability outcome.
- Retry history and relationship between evaluation and attempts.
- Capability outcomes recorded by an attempt.

Preserve enough information to reconstruct the domain state without inventing new business semantics.

### 2. PostgreSQL persistence

Use the approved technology baseline.

Add the minimum PostgreSQL/JPA persistence configuration required for this slice.

Keep persistence concerns separated from the existing domain lifecycle rules.

### 3. Repository boundary

Introduce a persistence/repository boundary that allows the application/domain layer to save and load evaluations without coupling the lifecycle domain to JPA entities.

The repository must preserve attempt history rather than replacing prior attempts on retry.

### 4. Reconstruction

Loading a persisted evaluation must reconstruct its current lifecycle state and prior attempt history accurately.

A persisted terminal attempt must remain terminal after reload.

A retry created after reload must preserve the same canonical request and append a new attempt.

### 5. Tests

Add focused automated tests covering at least:

- save and reload of an accepted evaluation;
- save/reload of a running evaluation;
- save/reload of each terminal state;
- terminal outcome reason/metadata preservation;
- canonical request preservation;
- multiple attempts and retry history preservation;
- capability outcome preservation;
- owner-facing report/preview outcome preservation;
- reload followed by retry creates a new attempt without mutating the previous attempt;
- repository/domain separation behavior where practical.

Use PostgreSQL/Testcontainers where supported by the repository test setup. If the environment cannot execute container-backed tests, document the exact limitation and still provide the tests.

## Non-scope

Do NOT implement:

- REST/GraphQL endpoints.
- Authentication or authorization.
- Hotel resolution.
- Website crawling or evidence acquisition.
- Browser automation.
- Hospitality analysis/scoring.
- AI/model integration.
- Hotel Experience Model implementation.
- Interactive preview generation or hosting.
- Queues/workflow orchestration.
- Production deployment infrastructure.
- Booking integrations.
- New lifecycle states or business rules not already specified.

## Acceptance criteria

The requirement is complete only when:

1. An evaluation can be persisted to PostgreSQL and loaded again with equivalent domain state.
2. Evaluation attempts remain distinguishable and ordered after persistence/reload.
3. Canonical evaluation input is preserved exactly enough to maintain target identity semantics.
4. Lifecycle and terminal outcomes are preserved without silently changing state during reconstruction.
5. Retry after reload creates a new attempt while preserving prior terminal history.
6. Capability outcomes and owner-facing report/preview outcomes survive persistence.
7. Domain lifecycle rules remain enforced by the domain model rather than duplicated inconsistently in persistence code.
8. Automated tests cover the persistence behaviors above.
9. No out-of-scope capability is introduced.
10. The same requirement file is updated with completion details, validation, PR information, and review history.

## Branch and Git rules

The implementation session MUST:

1. Inspect current `main` first.
2. Confirm this requirement is `READY`.
3. Create exactly this branch from current `main`:

   `feature/evaluation-persistence-foundation`

4. Implement only this requirement.
5. Update this same requirement file on the branch with completion details.
6. Commit and push the branch.
7. Create a PR against `main`.
8. Do NOT merge the PR.
9. Stop and wait for orchestrator review.

## Completion record

### Completion status

PR_READY — implementation committed; PR creation is the final session step.

### Exact branch

`feature/evaluation-persistence-foundation`

### PR

- **PR:** Pending creation after completion-record commit
- **URL:** Pending creation after completion-record commit
- **Base:** `main`
- **Merged:** No

### Files changed

- `backend/README.md`
- `backend/pom.xml`
- `backend/src/main/java/com/antenapro/hotelcheck/evaluation/Evaluation.java`
- `backend/src/main/java/com/antenapro/hotelcheck/evaluation/EvaluationAttempt.java`
- `backend/src/main/java/com/antenapro/hotelcheck/evaluation/EvaluationRepository.java`
- `backend/src/main/java/com/antenapro/hotelcheck/persistence/jpa/EvaluationJpaEntity.java`
- `backend/src/main/java/com/antenapro/hotelcheck/persistence/jpa/EvaluationAttemptJpaEntity.java`
- `backend/src/main/java/com/antenapro/hotelcheck/persistence/jpa/CapabilityOutcomeJpaEntity.java`
- `backend/src/main/java/com/antenapro/hotelcheck/persistence/jpa/SpringDataEvaluationRepository.java`
- `backend/src/main/java/com/antenapro/hotelcheck/persistence/jpa/JpaEvaluationRepository.java`
- `backend/src/test/java/com/antenapro/hotelcheck/persistence/jpa/JpaEvaluationRepositoryTest.java`
- `requirements/REQ-013-evaluation-persistence-foundation-implementation.md`

### Validation performed

- Inspected the current `main` repository state and confirmed REQ-013 was the current `READY` requirement before creating the exact required branch.
- Created `feature/evaluation-persistence-foundation` directly from current `main` (`ef2b73ba79690941dc2c6bcb0638b7a6a4698184`).
- Read the required repository context, product specifications, evaluation architecture, technology decision, and REQ-011/REQ-012 implementation requirements before implementation.
- Compared `feature/evaluation-persistence-foundation` against `main` and confirmed the change set is limited to the evaluation persistence foundation, domain rehydration seam, backend documentation/build dependencies, persistence adapter/entities, and persistence tests.
- Added PostgreSQL/JPA persistence with a repository boundary separate from the lifecycle domain.
- Added reconstruction validation for attempt ordering, active/terminal state consistency, terminal outcomes, owner-facing completion outcomes, and canonical request preservation.
- Added Testcontainers PostgreSQL integration tests covering accepted, running, all terminal states, terminal metadata, canonical request, capability outcomes, owner-facing outcomes, and retry history preservation.
- `mvn test` could not be executed because Maven is not installed in the session environment (`mvn: command not found`).
- Container-backed tests could not be executed because Docker is not installed in the session environment (`docker: command not found`). The Testcontainers suite is included for CI/review execution.
- No REST/GraphQL API, authentication, acquisition, browser automation, analysis, AI, preview generation, queue/workflow, deployment, or booking integration was introduced.

### Open questions

- Full Maven/Testcontainers execution remains to be performed by an environment with Maven and Docker/CI support.
- The persistence schema intentionally uses Hibernate/JPA schema generation for this foundation slice; explicit production migration tooling and retention policy remain future bounded decisions.
- No other material open question was identified within REQ-013 scope.

## Orchestrator Review History

No review yet.
