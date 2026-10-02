# REQ-011 — Evaluation Input Foundation Implementation

**Status:** READY  
**Owner:** Next implementation session  
**Type:** Implementation  
**Branch:** `feature/evaluation-input-foundation`

## Objective

Implement the smallest production-oriented vertical slice that establishes the validated hotel evaluation input boundary defined by SPEC-002 and the technology baseline in ADR-001.

This is the first application-code requirement. It must prove the repository can accept and validate the two supported hotel-input forms without implementing evidence acquisition, analysis, scoring, preview generation, or external integrations.

Governing flow remains:

**Hotel input → Evaluation → Evidence acquisition → Evidence model → Hospitality analysis → Hotel Experience Model → analysis result + interactive hotel preview**

REQ-011 implements only the first boundary: **Hotel input → validated evaluation input**.

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
- this requirement

If any referenced document is missing or materially contradicts this requirement, STOP and report the ambiguity.

## Scope

Implement only:

1. Repository application structure required by ADR-001 for the backend foundation.
2. Backend project using the selected Java 21 + Spring Boot baseline.
3. A domain representation for the two supported input forms:
   - hotel name + city;
   - hotel website URL.
4. Validation rules required by SPEC-002, including rejection of ambiguous, malformed, or unsupported input.
5. A clear typed result/error model for accepted versus rejected input.
6. Unit tests covering the input validation contract and important boundary cases.
7. Minimal application wiring needed to demonstrate that the input boundary can be invoked, if required by the existing architecture; do not build a full public API unless the approved specifications explicitly require it at this stage.
8. Documentation of the implemented foundation and local test/run instructions if needed by repository convention.

## Explicit non-scope

Do NOT implement:

- public website crawling or HTTP acquisition;
- Playwright/browser automation;
- robots.txt handling;
- evidence persistence;
- hospitality analysis;
- scoring or grades;
- AI/model integration;
- Hotel Experience Model generation;
- report generation;
- interactive preview rendering/hosting;
- booking integrations;
- authentication/authorization;
- production deployment;
- queues/workflow infrastructure;
- detailed production database schema.

Do not silently expand the input contract beyond SPEC-002.

## Implementation principles

- Follow ADR-001 and approved specifications.
- Prefer a small, explicit domain model over premature framework abstractions.
- Keep validation deterministic and independently unit-testable.
- Do not normalize an invalid input into a valid one merely to accept it.
- Preserve enough structured information for the later Evaluation/Lifecycle layer to create an attempt without reinterpreting the user's original input.
- Never perform network access during input validation.
- Do not resolve or guess a hotel from ambiguous name/city input.
- Keep the boundary ready for later API/application-layer integration without implementing that later layer prematurely.

## Acceptance criteria

The implementation is complete only when:

1. The repository contains a buildable Java 21/Spring Boot backend foundation consistent with ADR-001.
2. The domain supports exactly the two approved input forms.
3. Valid hotel-name-plus-city input is accepted according to SPEC-002.
4. Valid hotel-website-URL input is accepted according to SPEC-002.
5. Missing required fields, malformed URLs, unsupported URL forms, blank values, and ambiguous combinations are rejected according to the specification.
6. Validation performs no external network calls.
7. Validation results/errors are deterministic and machine-testable.
8. Tests cover normal cases plus meaningful invalid/boundary cases.
9. `mvn test` (or the repository's documented Maven test command) passes.
10. No crawler, analyzer, preview, AI, or production infrastructure code is introduced.
11. The implementation does not redefine any approved specification.
12. The requirement's completion record identifies files changed, tests run, branch, and PR.

## Git / PR requirements

- Create the exact branch: `feature/evaluation-input-foundation`.
- Create it from the current `main`.
- Do not invent, rename, or substitute the branch.
- Implement only REQ-011.
- Update this SAME requirement file with the completion record.
- Include exact branch, PR number, PR URL, changed files, validation performed, and open questions.
- Create the PR against `main`.
- Do not merge the PR; the orchestrator will review and merge approved PRs.

## Session Completion Record

### Completion status

READY — awaiting execution.

### Orchestrator Review History

No orchestrator review has occurred yet.
