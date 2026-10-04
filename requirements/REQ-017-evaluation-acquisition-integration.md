# REQ-017 — Evaluation Acquisition Integration

STATUS: READY
REQUIREMENT_ID: REQ-017
TYPE: Implementation
BRANCH: `feature/evaluation-acquisition-integration`

## Objective

Implement the smallest product vertical slice that connects the already-implemented evaluation input/lifecycle foundations to the already-implemented bounded HTTP public-web acquisition capability.

The goal is to prove this governed flow in-process:

`CanonicalEvaluationRequest → Evaluation/Attempt → single public-web acquisition → typed capability outcome`

This is the first real end-to-end Hotel Check slice across the existing foundations. It must not become a crawler, analyzer, report generator, or preview implementation.

## Context

The following foundations already exist in `main` and MUST be reused rather than reimplemented:

- REQ-011 — validated `CanonicalEvaluationRequest`
- REQ-012 — in-process evaluation/attempt lifecycle
- REQ-015 — bounded single-target HTTP public-web acquisition
- REQ-014 — backend CI validation
- REQ-016 — agent engineering/GSD guidance

The product contract remains hospitality-first. Acquisition output is evidence/acquisition data, not analysis or a hotel-quality conclusion.

The governing product flow from SPEC-001 remains:

`Hotel input → Evaluation → Evidence acquisition → Evidence model → Hospitality analysis → Hotel Experience Model → analysis result + interactive hotel preview`

REQ-017 implements only the connection between the first three stages.

## Required Context

The implementation session MUST read completely:

- `docs/specs/SPEC-001-hotel-check.md`
- `docs/specs/SPEC-002-evaluation-input.md`
- `docs/specs/SPEC-003-evaluation-lifecycle.md`
- `docs/specs/SPEC-004-public-evidence-acquisition.md`
- `docs/architecture/EVALUATION-ARCHITECTURE.md`
- `docs/architecture/architecture-concerns.md`
- `docs/decisions/ADR-001-technology-baseline.md`
- `requirements/README.md`
- `requirements/REQ-011-evaluation-input-foundation-implementation.md`
- `requirements/REQ-012-evaluation-lifecycle-foundation-implementation.md`
- `requirements/REQ-015-public-web-acquisition-foundation.md`
- `docs/engineering/ENGINEERING-PRINCIPLES.md`
- `docs/engineering/BACKEND-PATTERNS.md`
- `docs/engineering/TESTING-STANDARDS.md`
- `docs/engineering/AGENT-GUIDELINES.md`
- `docs/workflows/IMPLEMENTATION-SESSION.md`
- this requirement

If any referenced document is missing or materially contradicts this requirement, STOP and report the ambiguity.

## Scoped Context

### READ

Read the documents listed under Required Context.

### INSPECT

Before implementation, inspect the existing classes/tests for:

- canonical evaluation input
- evaluation lifecycle and attempts
- capability outcomes
- public-web acquisition service and request/result types

### DEPENDENCIES

The implementation should depend on the existing contracts from REQ-011, REQ-012, and REQ-015.

### DO NOT TOUCH

Do not modify unrelated:

- analysis
- evidence-model
- preview
- booking
- OTA
- authentication
- deployment
- frontend
- browser acquisition
- multi-page crawling

Do not redesign REQ-011, REQ-012, or REQ-015.

### ACCEPTANCE

Use the acceptance criteria in this requirement as the authoritative implementation contract.

### OUTPUT

Update this SAME requirement file, create/update the assigned PR, report validation, and STOP for orchestrator review.

## In Scope

1. Introduce the smallest application/domain service needed to connect a valid canonical evaluation request to one evaluation attempt and one public-web acquisition.
2. Start the evaluation attempt through the existing lifecycle rather than duplicating lifecycle state logic.
3. Convert the canonical website target into the existing REQ-015 acquisition request without reimplementing URL validation.
4. Execute exactly one bounded acquisition target for this requirement.
5. Map acquisition success/failure into the existing capability outcome model without inventing analysis semantics.
6. Preserve the acquisition result/provenance information needed by the lifecycle/application boundary.
7. Ensure acquisition failure is distinguishable from evidence that a hotel feature is absent.
8. Ensure an acquisition failure does not automatically become evaluation-level `FAILED` when the existing lifecycle supports partial/incomplete progress.
9. Add deterministic tests covering the normal flow and important failure paths.
10. Keep the integration replaceable and small so later evidence normalization/analysis can consume the acquisition result.
11. Document the integration boundary and test/run instructions where repository convention requires it.

## Explicit Non-Scope

Do NOT implement:

- multi-page crawling or page discovery
- browser/Playwright acquisition
- robots.txt processing beyond the existing acquisition contract
- evidence normalization/extraction
- evidence persistence beyond existing foundations
- hospitality analysis
- scoring/grades
- AI/model integration
- report generation
- Hotel Experience Model generation
- interactive preview rendering/hosting
- booking/OTA integrations
- public REST/GraphQL API
- authentication/authorization
- queues/schedulers/distributed orchestration
- production deployment/infrastructure
- new persistence schema
- a second acquisition implementation
- changes to the security guarantees already established by REQ-015

## Design Principles

- Reuse existing REQ-011/REQ-012/REQ-015 contracts.
- Do not duplicate validation, lifecycle transitions, or HTTP acquisition logic.
- Prefer one small cohesive application service over multiple speculative abstractions.
- Use existing capability outcome types where possible.
- Acquisition output is not analysis.
- Do not mark an evaluation `COMPLETED` merely because acquisition succeeded. The lifecycle's required owner-facing report and preview outcomes are not available at this stage.
- If acquisition fails, preserve the typed failure and lifecycle limitation rather than silently treating it as success or as proof that a hotel feature is absent.
- Keep the implementation deterministic and testable.
- No network call during domain validation; network access belongs only to the acquisition boundary.

## Acceptance Criteria

### AC-1 — Canonical request integration

A valid `CanonicalEvaluationRequest` can be passed into the new integration boundary without re-validating or reconstructing the input contract incorrectly.

### AC-2 — Evaluation lifecycle integration

The integration creates/uses an evaluation attempt through the existing REQ-012 lifecycle model and does not duplicate transition rules.

### AC-3 — Single acquisition

For a canonical website target, the integration invokes the existing REQ-015 acquisition service exactly once for the bounded target.

### AC-4 — Acquisition result propagation

The integration preserves the typed acquisition outcome and relevant provenance/result metadata for downstream consumers.

### AC-5 — Failure semantics

Timeout, HTTP error, invalid target, redirect-limit, oversized-response, and network failure outcomes remain distinguishable at the integration boundary where supported by REQ-015.

### AC-6 — No false completion

A successful acquisition does not mark the evaluation `COMPLETED`, because report and interactive preview outcomes are not yet available.

### AC-7 — Partial-failure semantics

An acquisition failure does not silently erase the attempt or become evidence that a hotel capability is absent. The lifecycle can represent the resulting limitation/outcome without violating REQ-012.

### AC-8 — No duplicate infrastructure

No second URL validator, HTTP client, DNS resolver, lifecycle state machine, or capability-outcome hierarchy is introduced.

### AC-9 — Deterministic tests

Tests cover at minimum:

- successful acquisition integration;
- acquisition timeout/failure propagation;
- HTTP error propagation;
- invalid target propagation where applicable;
- lifecycle state/outcome behavior after acquisition;
- proof that exactly one acquisition invocation occurs;
- proof that successful acquisition cannot produce `COMPLETED`.

Tests must not depend on live hotel websites.

### AC-10 — Existing security boundary preserved

The integration uses the existing REQ-015 public-web acquisition service and does not bypass or weaken its SSRF, HTTPS, redirect, timeout, or response-size protections.

### AC-11 — CI validation

The repository backend CI workflow executes the complete backend test suite successfully for the PR.

### AC-12 — Scope discipline

No crawler, analyzer, scoring, AI, preview, booking, OTA, public API, or new infrastructure is introduced.

### AC-13 — Requirement integrity

REQ-011, REQ-012, and REQ-015 contracts remain intact. The implementation does not redefine approved specifications.

## Validation Required Before PR

The implementation session must:

1. Run the complete documented backend Maven test suite.
2. Add deterministic tests for REQ-017.
3. Push the branch and verify backend CI executes.
4. Confirm actual CI success before claiming `PR_READY`.
5. Review the implementation against `docs/engineering/AGENT-GUIDELINES.md` before PR creation.
6. Update this SAME requirement file with the implementation summary, changed files, tests, CI result, branch, PR, and any blockers.

## Git / PR Requirements

- Create the exact branch: `feature/evaluation-acquisition-integration`.
- Create it from current `main`.
- Implement ONLY REQ-017.
- Use GSD according to the repository's agent guidance, but treat this requirement and existing architecture as authoritative.
- Update this SAME requirement file.
- Create the PR against `main`.
- Do not merge the PR.
- Stop after PR creation/update and wait for orchestrator review.

## Completion Record

To be filled by the implementation session.

## Orchestrator Review History

<!-- Orchestrator review rounds are appended below without deleting prior history. -->
