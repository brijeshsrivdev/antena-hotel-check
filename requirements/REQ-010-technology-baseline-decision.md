# REQ-010 — Technology Baseline Decision

**Status:** PR_READY  
**Owner:** Next implementation/specification session  
**Type:** Architecture / Decision  
**Branch:** `spec/technology-baseline-decision`

## Objective

Close only the minimum material technology decisions required to begin implementation of Antena Hotel Check, while preserving the approved product, evidence, analysis, Hotel Experience Model, preview, security, and architecture contracts.

The governing product flow remains:

**Hotel input → Evaluation → Evidence acquisition → Evidence model → Hospitality analysis → Hotel Experience Model → analysis result + interactive hotel preview**

This requirement is a decision exercise, not application implementation. The output must make the repository implementation-ready without prematurely deciding every operational detail.

## Read before execution

The session MUST read the complete contents of:

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
- `docs/decisions/` relevant documents
- `requirements/README.md`
- this requirement

If a referenced document does not exist or materially contradicts this requirement, STOP and report the ambiguity instead of inventing a resolution.

## Scope

Evaluate and record the minimum technology decisions needed to start the first implementation slice, including:

1. Backend language/runtime/framework.
2. Frontend language/runtime/framework for the owner-facing application/preview where applicable.
3. Primary persistence technology.
4. Initial execution model for the first implementation slice: synchronous, asynchronous, or hybrid.
5. Initial public-evidence acquisition technology boundary, without committing to unrestricted crawling or bypass mechanisms.
6. Test/tooling baseline sufficient for implementation.
7. Local development/repository structure required by the selected baseline.
8. Technology choices that must explicitly remain open for later ADRs.

The decision must evaluate alternatives against the repository's actual product and architecture constraints, not personal preference alone.

## Explicit non-scope

Do NOT implement application features.

Do NOT implement the crawler, analyzer, preview renderer, API, database schema, UI, deployment, or production infrastructure.

Do NOT decide every operational detail. In particular, keep open unless genuinely required by the first implementation slice:

- exact cloud provider/service topology;
- production CDN/DNS/domain setup;
- production queue/workflow platform;
- production browser-isolation infrastructure;
- exact AI provider/model;
- exact report/preview rendering engine;
- detailed API schemas;
- detailed database schemas;
- retention periods and operational numeric limits;
- authentication provider;
- production observability vendor.

Do NOT redefine approved behavior in SPEC-001 through SPEC-008.
Do NOT introduce new hospitality capabilities.
Do NOT turn this requirement into application implementation.

## Required decision principles

The selected baseline MUST:

- support the approved evaluation architecture and bounded public evidence model;
- preserve provenance and attempt/run traceability;
- support clear separation between analysis findings and Hotel Experience Model facts;
- support an interactive hotel preview as a first-class outcome;
- allow partial failure and retry semantics to be implemented later without architectural rework;
- provide practical automated testing for core domain behavior;
- avoid unnecessary operational complexity for the initial product slice;
- keep later technology decisions replaceable where the architecture intentionally leaves seams open;
- be realistic for a small product team and the current implementation stage;
- document meaningful trade-offs and rejected alternatives.

## Required output

Create/update documentation only:

1. `docs/decisions/ADR-001-technology-baseline.md` — the approved technology baseline decision, alternatives considered, trade-offs, consequences, and explicitly deferred decisions.
2. `docs/decisions/README.md` — add ADR-001 to the decisions index, creating/updating the index according to repository convention.
3. This requirement file — update the Session Completion Record when work is complete.

If the repository already contains a materially equivalent ADR, update/reconcile it rather than creating a duplicate, and explain the reconciliation in the completion record.

## Acceptance criteria

The completed decision MUST:

- identify the selected backend language/runtime/framework and rationale;
- identify the selected frontend language/runtime/framework and rationale;
- identify the selected primary persistence technology and rationale;
- define the initial execution model and why it fits the first implementation slice;
- define the initial public-evidence acquisition technology boundary without selecting unsafe bypass mechanisms;
- define the minimum test/tooling baseline;
- explain at least the meaningful alternatives considered for each material choice;
- document consequences and migration/replacement implications;
- explicitly identify decisions deferred to later ADRs;
- remain consistent with SPEC-001 through SPEC-008 and the evaluation architecture;
- avoid application code or feature implementation;
- produce a durable ADR that future implementation sessions can rely on.

## Git / PR requirements

- Create the exact branch specified above: `spec/technology-baseline-decision`.
- Create the branch from the current `main`.
- Do not use another branch name.
- Documentation/architecture/decision changes only.
- Update this SAME requirement file with the completion record.
- Include exact branch, PR number, PR URL, changed files, validation performed, and open questions.
- Create the PR against `main`.
- Do not merge the PR.

## Session Completion Record

### Completion status

PR_READY — implementation complete and PR created; awaiting orchestrator review.

### Session

Next implementation/specification session executing REQ-010 only.

### Branch

`spec/technology-baseline-decision` — created from current `main` at commit `f4bff449b303b3a7176d1e438d598736c6c32a73`.

### Pull request

- PR: #10
- URL: https://github.com/brijeshsrivdev/antena-hotel-check/pull/10
- Base: `main`
- State: Open
- Merged: No

### Files changed

- `docs/decisions/ADR-001-technology-baseline.md`
- `docs/decisions/README.md`
- `requirements/REQ-010-technology-baseline-decision.md`

### Summary of work

Created ADR-001 and recorded the minimum technology baseline required to begin implementation while preserving the approved architecture seams and product contracts.

Selected baseline:

- Backend: Java 21 LTS + Spring Boot 3.x.
- Owner-facing application/preview: TypeScript + Next.js.
- Primary persistence: PostgreSQL.
- Execution model: hybrid — synchronous intake/acceptance with asynchronous long-running evaluation work.
- Public evidence: HTTP-first with justified browser-rendered escalation; Playwright is the initial browser-automation baseline, subject to the existing public-access and non-bypass contract.
- Testing/tooling: Maven, JUnit 5, Mockito, Testcontainers/PostgreSQL, TypeScript checks, repository-native lint/format checks, and targeted Playwright browser acceptance tests.
- Local development: separable backend/frontend/test/documentation boundaries with reproducible local PostgreSQL, preferably containerized.

The ADR also documents meaningful alternatives, trade-offs, replaceability/migration implications, and technology decisions explicitly deferred to later ADRs. No application features, schemas, APIs, crawler implementation, preview renderer, deployment, or production infrastructure were implemented.

### Validation performed

- Inspected the repository and confirmed `main` was the current source branch before creating the required branch.
- Read the complete READY requirement and all referenced context/specification/architecture/decision documents available in the repository.
- Confirmed the referenced `docs/decisions/` directory contained only the decisions README before creating ADR-001; no materially equivalent ADR existed.
- Reviewed ADR-001 against every REQ-010 acceptance criterion.
- Verified the ADR preserves the approved hospitality-first product flow, evidence/provenance semantics, partial-failure/retry semantics, interactive-preview outcome, and public-web non-bypass boundary.
- Verified the changes are documentation/decision changes only.
- Verified the PR targets `main` and remains open/unmerged.

No application test suite was run because REQ-010 explicitly limits the work to documentation/architecture/decision changes.

### Open questions

The following remain intentionally deferred and are recorded in ADR-001 for later bounded decisions:

- exact framework/dependency versions within the selected baseline;
- exact HTTP client/parsing libraries;
- exact Playwright browser/version and browser isolation;
- queue/workflow/job technology and worker topology;
- production cloud, CDN, DNS, hostname/custom-domain, and deployment topology;
- object/static asset storage;
- authentication, authorization, tenancy, and production access control;
- exact report and preview rendering/serving deployment model;
- AI provider/model and model-assisted analysis strategy;
- retention periods and numeric operational limits;
- production observability and abuse/rate-limit infrastructure.

These are not blockers for REQ-010 and should be resolved only by later requirements/ADRs when implementation constraints justify them.

## Orchestrator Review History

No orchestrator review has occurred yet.
