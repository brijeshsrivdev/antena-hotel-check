# REQ-012 — Evaluation Lifecycle Foundation Implementation

**Status:** PR_READY  
**Owner:** Next implementation session  
**Type:** Implementation  
**Branch:** `feature/evaluation-lifecycle-foundation`

## Objective

Implement the smallest production-oriented vertical slice for the evaluation lifecycle defined by SPEC-003, building directly on the validated `CanonicalEvaluationRequest` foundation from REQ-011.

This requirement establishes the in-process domain/application foundation for creating an evaluation, starting an attempt, progressing lifecycle state, recording terminal outcomes, and creating distinguishable retries without introducing persistence, queues, public APIs, evidence acquisition, analysis, or preview generation.

Governing flow remains:

**Hotel input → Evaluation → Evidence acquisition → Evidence model → Hospitality analysis → Hotel Experience Model → analysis result + interactive hotel preview**

REQ-012 implements only the lifecycle boundary:

**validated canonical request → evaluation/attempt lifecycle state machine**

## Read before execution

Read the complete contents of:

- `docs/context/project-context.md`
- `docs/specs/SPEC-001-hotel-check.md`
- `docs/specs/SPEC-002-evaluation-input.md`
- `docs/specs/SPEC-003-evaluation-lifecycle.md`
- `docs/specs/SPEC-004-public-evidence-acquisition.md`
- `docs/specs/SPEC-005-evidence-model.md`
- `docs/specs/SPEC-006-hospitality-analysis.md`
- `docs/specs/SPEC-007-hotel-experience-model.md`
- `docs/specs/SPEC-008-interactive-preview.md`
- `docs/architecture/EVALUATION-ARCHITECTURE.md`
- `docs/architecture/architecture-concerns.md`
- `docs/decisions/ADR-001-technology-baseline.md`
- `requirements/README.md`
- `requirements/REQ-011-evaluation-input-foundation-implementation.md`
- this requirement

If any referenced document is missing or materially contradicts this requirement, STOP and report the ambiguity.

## Scope

Implement only:

1. A domain representation of an evaluation created from an accepted `CanonicalEvaluationRequest`.
2. A domain representation of a distinguishable evaluation attempt/run.
3. Lifecycle states defined by SPEC-003:
   - `ACCEPTED`
   - `RUNNING`
   - `COMPLETED`
   - `INCOMPLETE`
   - `UNRESOLVED`
   - `FAILED`
4. Explicit enforcement of allowed lifecycle transitions.
5. Terminal-state protection: terminal evaluations/attempts must not silently transition back to active states.
6. Attempt/retry semantics that preserve the parent evaluation/request relationship and distinguish a new attempt from an earlier one.
7. Recording of terminal outcome/failure reason and material lifecycle metadata needed by the domain contract.
8. Minimal capability outcome representation needed to test the lifecycle's partial-failure principle; do not implement actual capabilities.
9. Domain/application tests covering the lifecycle acceptance criteria in SPEC-003 that are applicable without persistence or external integrations.
10. Minimal documentation of the lifecycle foundation and test/run instructions if repository convention requires it.

## Explicit non-scope

Do NOT implement:

- database persistence or schema;
- JPA entities/repositories;
- public REST/GraphQL API;
- HTTP acquisition or crawling;
- Playwright/browser automation;
- robots.txt handling;
- hotel identity resolution/matching algorithms;
- evidence persistence or acquisition;
- hospitality analysis/check catalog;
- scoring or grades;
- AI/model integration;
- report generation;
- Hotel Experience Model generation;
- interactive preview rendering or hosting;
- booking/OTA integrations;
- authentication/authorization;
- queues, schedulers, workflow engines, or distributed orchestration;
- production deployment/infrastructure;
- idempotency infrastructure or distributed concurrency mechanisms.

Do not expand the lifecycle beyond SPEC-003.

## Implementation principles

- Follow ADR-001 and all approved specifications.
- Build on the canonical input types from REQ-011 rather than duplicating or redefining the input contract.
- Keep lifecycle transition rules deterministic and independently unit-testable.
- Do not silently mutate a terminal attempt into another outcome.
- A retry/re-run must be distinguishable from the prior attempt and must preserve the same logical target association unless the caller explicitly supplies a new canonical request.
- Do not implement persistence; in-memory/domain state is sufficient for this requirement.
- Do not resolve a hotel or claim that evidence was acquired.
- Keep evaluation-level state distinct from capability/check/evidence outcomes.
- An individual failed/unavailable capability must not automatically force `FAILED` when the lifecycle contract permits continued progress.
- Do not invent completion semantics beyond SPEC-003. In particular, do not mark an evaluation `COMPLETED` unless the required report and interactive preview outcomes are represented as available.
- Preserve provenance/limitation fields conceptually where required by the lifecycle, without implementing the evidence model itself.

## Acceptance criteria

The implementation is complete only when:

1. A valid `CanonicalEvaluationRequest` can create an evaluation in `ACCEPTED`.
2. `ACCEPTED → RUNNING` is supported.
3. No non-running state can directly become a terminal outcome without first being `RUNNING`.
4. Only `RUNNING` evaluations can reach `COMPLETED`, `INCOMPLETE`, `UNRESOLVED`, or `FAILED`.
5. Terminal states cannot silently transition back to `ACCEPTED` or `RUNNING`.
6. A terminal outcome retains its reason/metadata where applicable.
7. A new retry/attempt is distinguishable from the previous attempt and remains associated with the same logical evaluation/request target.
8. A retry does not overwrite or erase the previous attempt's terminal outcome.
9. Materially changing the canonical hotel target is represented as a new evaluation request rather than a retry of the old target.
10. `COMPLETED` cannot be reached unless the lifecycle model represents both an owner-facing analysis/report outcome and an interactive Antena-hosted preview outcome as available.
11. If either required owner-facing outcome is unavailable while the evaluation remains trustworthy, the lifecycle can represent `INCOMPLETE` rather than falsely marking it `COMPLETED`.
12. `UNRESOLVED` and `FAILED` remain distinguishable from `INCOMPLETE`.
13. An individual capability outcome can be failed/unavailable/partial without automatically forcing evaluation-level `FAILED`.
14. The lifecycle can retain material limitations/outcomes needed by downstream report/preview consumers without implementing those consumers.
15. Automated tests cover the lifecycle transition matrix, completion gating, partial failure, unresolved/failed distinction, terminal-state protection, retry semantics, and target-preservation behavior.
16. No external network access, database, queue, crawler, analyzer, AI, or preview implementation is introduced.
17. `mvn test` (or the repository's documented Maven test command) passes.
18. The implementation does not redefine SPEC-003 or the approved input contract.
19. The requirement's completion record identifies files changed, tests run, branch, and PR.

## Git / PR requirements

- Create the exact branch: `feature/evaluation-lifecycle-foundation`.
- Create it from the current `main`.
- Do not invent, rename, or substitute the branch.
- Implement only REQ-012.
- Update this SAME requirement file with the completion record.
- Include exact branch, PR number, PR URL, changed files, validation performed, and open questions.
- Create the PR against `main`.
- Do not merge the PR; the orchestrator will review and merge approved PRs.

## Session Completion Record

### Completion status

PR_READY — implementation committed and PR created; awaiting orchestrator review.

### Exact branch

`feature/evaluation-lifecycle-foundation`

### PR

- **PR:** #12
- **URL:** https://github.com/brijeshsrivdev/antena-hotel-check/pull/12
- **Base:** `main`
- **Merged:** No

### Files changed

- `backend/README.md`
- `backend/src/main/java/com/antenapro/hotelcheck/evaluation/CapabilityOutcome.java`
- `backend/src/main/java/com/antenapro/hotelcheck/evaluation/CapabilityOutcomeStatus.java`
- `backend/src/main/java/com/antenapro/hotelcheck/evaluation/Evaluation.java`
- `backend/src/main/java/com/antenapro/hotelcheck/evaluation/EvaluationAttempt.java`
- `backend/src/main/java/com/antenapro/hotelcheck/evaluation/EvaluationLifecycleState.java`
- `backend/src/main/java/com/antenapro/hotelcheck/evaluation/InvalidLifecycleTransitionException.java`
- `backend/src/main/java/com/antenapro/hotelcheck/evaluation/OwnerFacingOutcome.java`
- `backend/src/main/java/com/antenapro/hotelcheck/evaluation/TerminalOutcome.java`
- `backend/src/test/java/com/antenapro/hotelcheck/evaluation/EvaluationLifecycleTest.java`
- `requirements/REQ-012-evaluation-lifecycle-foundation-implementation.md`

### Validation performed

- Inspected current `main` at commit `9b70639b70025a7a3089454e80ba244cb89f907c` and confirmed REQ-011 is merged into `main` via PR #11 before creating the required branch.
- Created the exact required branch `feature/evaluation-lifecycle-foundation` from current `main`.
- Compiled the new lifecycle production/domain classes plus the REQ-011 canonical request types with `javac --release 21`; compilation passed.
- Added JUnit 5 coverage for accepted → running, terminal transition enforcement, completion gating, incomplete outcome, capability partial-failure semantics, terminal-state protection, unresolved/failed distinction, retry preservation/distinction, active-retry protection, and materially changed target separation.
- `mvn test` could not be executed in the session environment because Maven is not installed (`mvn: command not found`). No Maven wrapper is present in the repository.
- No network access, database, queue, crawler, browser automation, analyzer, AI, persistence, public API, or preview implementation was introduced.

### Open questions

- Maven is unavailable in the current session environment, so the full Maven/JUnit suite remains to be executed by the available CI/review environment or orchestrator.
- The lifecycle is intentionally in-memory/domain-only per REQ-012; persistence, public API, orchestration infrastructure, and external integrations remain future requirements.

### Orchestrator Review History

No orchestrator review has occurred yet.
