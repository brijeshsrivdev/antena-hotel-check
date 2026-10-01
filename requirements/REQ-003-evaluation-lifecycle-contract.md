# REQ-003 — Evaluation Lifecycle Contract

**Status:** PR_READY

**Owner:** Next implementation/specification session

**Requirement Type:** Product specification

## Objective

Define the product-level lifecycle of a hotel evaluation after a valid `CanonicalEvaluationRequest` exists and before detailed public-evidence acquisition, analysis, report assembly, and interactive preview implementation are built.

The lifecycle must preserve the core product outcome established by `SPEC-001`:

**Hotel input → analysis → analysis result + interactive hotel preview → owner explores preview**

This requirement exists to establish the durable contract for an evaluation as a governed unit of work, including lifecycle states, completion semantics, partial failure behavior, and the relationship between evidence, analysis, report, and preview outcomes.

## Repository context to read first

The session MUST read:

- `docs/context/PROJECT_CONTEXT.md`
- `docs/specs/SPEC-001-hotel-check.md`
- `docs/specs/SPEC-002-evaluation-input.md`
- `docs/specs/README.md`
- `docs/architecture/architecture-concerns.md`
- `requirements/README.md`
- this requirement file in full

## Scope

The resulting specification must define, at product/domain level:

1. What an evaluation represents.
2. The lifecycle from an accepted canonical input through evaluation completion.
3. Meaningful lifecycle states and allowed transitions.
4. What constitutes a started, partially completed, failed, and completed evaluation.
5. How partial evidence/acquisition failures affect the evaluation.
6. The distinction between evaluation failure and individual check/evidence failure.
7. The relationship between an evaluation and:
   - evidence
   - findings/analysis
   - owner-facing report
   - interactive preview
8. Completion criteria for the evaluation.
9. What must be true before an evaluation may be presented as completed to the owner.
10. What limitations must be retained and surfaced when evidence is unavailable.
11. Repeat/retry semantics at a product level, without selecting implementation infrastructure.
12. Idempotency/repeatability expectations at a conceptual level where appropriate.
13. Provenance/truthfulness constraints inherited from `SPEC-001`.
14. Security/access constraints inherited from `SPEC-001`.

## Important product invariant

A completed evaluation is not merely an analysis report.

A completed evaluation must represent the availability of both owner-facing outcomes:

- analysis result/report; and
- interactive Antena-hosted hotel preview.

The detailed preview content model, renderer, hosting implementation, and preview-specific specifications remain future work.

Do not implement the preview in this requirement.

## Partial failure principle

The specification MUST explicitly preserve the `SPEC-001` principle that failure or unavailability of one sub-check does not automatically invalidate the entire evaluation.

For example, a third-party booking engine may prevent reliable observation of a booking flow while other publicly observable hotel information remains analyzable.

The specification should distinguish:

- evaluation-level failure
- capability-level failure
- check-level failure
- unavailable/unknown evidence

Do not invent numerical scoring or confidence formulas unless necessary to define lifecycle semantics.

## Scope boundaries

Do NOT define or implement:

- crawler implementation
- scraping implementation
- browser automation
- search engine/maps APIs
- detailed source-selection algorithms
- hotel identity matching algorithms
- analysis rules/check catalog
- scoring/ranking model
- AI/model architecture
- report UI
- preview UI
- preview renderer/runtime
- preview hosting infrastructure
- database schema
- application framework
- queue/orchestration technology
- cloud provider
- authentication/tenancy implementation

Those require later requirements/specifications/architecture decisions.

## Acceptance criteria

The resulting specification must:

1. Define a clear evaluation lifecycle from accepted input to completion or terminal failure.
2. Define lifecycle states and valid transitions without prescribing implementation technology.
3. Define what `COMPLETED` means.
4. Explicitly require both analysis/report and interactive preview as outcomes of a completed evaluation, consistent with `SPEC-001`.
5. Define how partial/unavailable evidence is represented without falsely claiming success or failure.
6. Distinguish evaluation-level failure from individual capability/check failure.
7. Define when an evaluation may be marked incomplete, failed, unresolved, or completed.
8. Preserve evidence/provenance and public-web limitation requirements.
9. Define conceptual retry/re-run behavior without choosing infrastructure.
10. Define whether and how evaluation attempts/runs are conceptually distinguished.
11. Provide testable acceptance criteria for the future implementation.
12. Avoid introducing unapproved technology or architecture decisions.
13. Update the specification index if a new specification is created.

## Deliverables

Create/update documentation only.

Expected primary deliverable:

`docs/specs/SPEC-003-evaluation-lifecycle.md`

Update `docs/specs/README.md` if required.

If a genuine architecture/product decision is discovered that should be durable separately, record it under the appropriate `docs/architecture/` or `docs/decisions/` document rather than hiding it in the requirement.

## Completion protocol

When the session completes the work:

1. Update this SAME requirement file.
2. Change status to `PR_READY`.
3. Record branch name.
4. Record PR number/link.
5. Record documents changed.
6. Record decisions made.
7. Record validation performed.
8. Record any open questions.
9. Preserve any orchestrator review history added later.

Create branch:

`spec/req-003-evaluation-lifecycle`

Create a PR against `main`.

Do NOT merge.

The orchestrator will review the PR against this exact requirement and may require multiple review rounds.

## Non-goal

Do not move from specification into feature implementation merely because the lifecycle specification is complete.

## Session Completion Record

### Status

PR_READY

### Branch

`spec/req-003-evaluation-lifecycle`

### PR

[#3 — docs: define REQ-003 evaluation lifecycle](https://github.com/brijeshsrivdev/antena-hotel-check/pull/3)

### Documents Changed

- `docs/specs/SPEC-003-evaluation-lifecycle.md` — created the implementation-neutral evaluation lifecycle contract, including lifecycle states/transitions, completion semantics, partial-failure behavior, evidence/capability/check distinctions, provenance, retry/re-run semantics, and future implementation acceptance criteria.
- `docs/specs/README.md` — indexed SPEC-003.
- `requirements/REQ-003-evaluation-lifecycle-contract.md` — records this session completion and PR details.

### Decisions Made

- An evaluation is the governed domain unit connecting one accepted canonical hotel target to its analysis/report and interactive preview outcomes.
- `ACCEPTED` and `RUNNING` are active lifecycle states; `COMPLETED`, `INCOMPLETE`, `UNRESOLVED`, and `FAILED` are terminal evaluation outcomes.
- `COMPLETED` requires both the owner-facing analysis/report and an interactive Antena-hosted hotel preview; neither outcome substitutes for the other.
- Partial evidence, check, or capability failure does not automatically fail an evaluation. A completed evaluation may retain unavailable checks when the limitations are surfaced and the completion contract remains truthful.
- Evaluation-level failure is distinct from capability-level, check-level, and evidence-level failure.
- Unresolved or materially mismatched hotel identity must never silently proceed against a different property.
- Retries/re-runs are conceptually separate attempts associated with the same target; they must preserve prior provenance and outcomes rather than silently overwrite them.
- A material change to hotel identity or canonical website target constitutes a new evaluation request rather than an invisible retry.
- No separate architecture decision record was created because this specification establishes product/domain semantics without selecting implementation technology or infrastructure.

### Open Questions

No material open question remains within the bounded REQ-003 scope. Detailed acquisition behavior, analysis rules, preview content/runtime, persistence, orchestration infrastructure, and technology choices remain future work as explicitly scoped.

### Validation Performed

- Inspected the repository and confirmed `main` is the default branch.
- Located and read the current `READY` requirement under `requirements/`.
- Read the referenced project context, SPEC-001, SPEC-002, specifications index, architecture concerns, requirements workflow, and the requirement itself.
- The requirement references `docs/context/PROJECT_CONTEXT.md`, while the repository contains the same project context at lowercase `docs/context/project-context.md`; the existing repository file was treated as the intended referenced context without changing repository structure.
- Created only documentation/specification changes; no application feature code was added.
- Confirmed the specification explicitly covers lifecycle states/transitions, completion, incomplete/unresolved/failed semantics, partial evidence/capability/check failures, report/preview relationship, provenance, public-access/security constraints, retry/re-run semantics, repeatability, and testable future implementation criteria.
- Indexed SPEC-003 in `docs/specs/README.md`.
- Compared `spec/req-003-evaluation-lifecycle` against `main`: 3 commits ahead, 0 behind, with documentation-only changes in the specification, specification index, and requirement completion record.
- No automated application tests were applicable because this session is documentation/specification-only.

### Orchestrator Review History

No orchestrator review has been performed yet. This PR is awaiting orchestrator review.
