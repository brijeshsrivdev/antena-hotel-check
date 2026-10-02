# REQ-013 — Evaluation Persistence Foundation Implementation

**Status:** READY  
**Owner:** Next implementation session  
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

READY — awaiting execution.

### Exact branch

`feature/evaluation-persistence-foundation`

### PR

- **PR:** Not created yet
- **URL:** Not created yet
- **Base:** `main`
- **Merged:** No

### Files changed

To be completed by implementation session.

### Validation performed

To be completed by implementation session.

### Open questions

To be completed by implementation session.

## Orchestrator Review History

No review yet.
