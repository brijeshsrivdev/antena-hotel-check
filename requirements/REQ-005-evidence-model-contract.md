# REQ-005 — Evidence Model Contract

**Status:** PR_READY  
**Owner:** Next implementation/specification session  
**Type:** Specification

## Objective

Define the durable product/domain contract for representing evidence collected during a hotel evaluation.

The contract must connect public evidence acquisition to later hospitality analysis without allowing unsupported hotel facts to be presented as verified information.

The resulting model must support the provenance principles established by `SPEC-001` and the acquisition semantics established by `SPEC-004`.

## Repository Context to Read

Before starting, read:

- `docs/context/project-context.md`
- `docs/specs/SPEC-001-hotel-check.md`
- `docs/specs/SPEC-002-evaluation-input.md`
- `docs/specs/SPEC-003-evaluation-lifecycle.md`
- `docs/specs/SPEC-004-public-evidence-acquisition.md`
- relevant architecture and decision documents
- this requirement file completely

## Scope

Define an implementation-neutral evidence model covering at minimum:

1. Evidence item identity and association with an evaluation.
2. Source/provenance information.
3. Evidence state and acquisition outcome.
4. The distinction between directly discovered facts and later normalized/inferred/derived representations.
5. Source location/reference and observed timestamp semantics.
6. Evidence confidence/strength semantics only where product-level meaning is necessary; do not invent numerical scoring without justification.
7. Handling of duplicate or equivalent evidence from multiple sources.
8. Conflicting evidence from different sources.
9. Stale, unavailable, failed, or partially acquired evidence.
10. How downstream analysis may cite or trace a finding back to supporting evidence.
11. Requirements needed to prevent inferred/generated/demo content from being represented as discovered hotel fact.

## Provenance

Preserve the established provenance states:

- `DISCOVERED`
- `NORMALIZED`
- `INFERRED`
- `DEMONSTRATION`

Clearly define their meaning and allowed transitions/relationships.

Do not silently collapse these states into one generic "hotel data" representation.

## Source Semantics

Define product-level distinctions where relevant between:

- first-party hotel sources
- third-party public sources
- user-provided information
- derived information
- demonstration/generated information

Do not expand this into a broad web-data aggregation specification.

## Conflicts

Define what happens when credible evidence sources disagree.

The contract must not silently choose one value and present it as verified without preserving the conflict/provenance context.

## Non-Scope

Do NOT implement or specify in detail:

- crawler/browser implementation
- scraping infrastructure
- search APIs
- AI providers
- analysis rules
- scoring formulas
- report UI
- interactive preview UI/runtime
- preview hosting
- database schema or Java classes unless a conceptual example is essential
- booking integrations
- OTA integrations

Those belong to later bounded requirements.

## Acceptance Criteria

The specification must make it possible for a future implementation session to test that:

1. Every evidence item can be associated with an evaluation.
2. Evidence provenance is preserved.
3. Discovered, normalized, inferred, and demonstration information cannot be silently conflated.
4. Evidence can be traced to its source/reference and observation context where available.
5. Multiple supporting sources can coexist for the same conceptual fact.
6. Conflicting evidence is represented without silent overwriting.
7. Unavailable/failed/stale evidence is distinguishable from discovered evidence.
8. Downstream findings can identify their supporting evidence.
9. Generated or inferred information cannot be presented as verified discovered hotel information without explicit provenance.
10. The contract remains compatible with the completed-evaluation requirement that analysis and interactive preview are both eventual outputs.

## Deliverables

Create/update documentation only.

At minimum create:

`docs/specs/SPEC-005-evidence-model.md`

Update the specification index if required.

If a genuine architectural/product decision is discovered, document it separately rather than hiding it inside the specification.

## Git Workflow

Create a branch:

`spec/evidence-model-contract`

Create a PR against `main`.

Do not merge.

Before stopping, update this SAME requirement file with:

- session name/identifier
- branch
- PR number/link
- files changed
- summary of work
- validation performed
- open questions

The orchestrator will review the PR against this requirement.

## Review Protocol

If the orchestrator records review findings in this file or the PR conversation, address those findings in the same requirement file and preserve the review history.

Do not mark the requirement `APPROVED`; only the orchestrator can do that.

## Session Completion Record

### Session

Session 5 — Evidence Model Contract

### Branch

`spec/evidence-model-contract`

### PR

[#5 — docs: define REQ-005 evidence model contract](https://github.com/brijeshsrivdev/antena-hotel-check/pull/5)

### Files Changed

- `docs/specs/SPEC-005-evidence-model.md` — created the implementation-neutral evidence model contract covering evidence identity and evaluation/attempt association, source/provenance semantics, acquisition outcomes, provenance derivation, qualitative support semantics, duplicates/equivalence, conflicts, freshness/staleness, partial/unavailable evidence, downstream finding traceability, preview truthfulness, and security/trust boundaries.
- `docs/specs/README.md` — indexed SPEC-005.
- `requirements/REQ-005-evidence-model-contract.md` — records the Session 5 completion record and PR details.

### Summary

Defined the durable evidence/domain model that connects controlled public evidence acquisition to future hospitality analysis. The specification preserves `DISCOVERED`, `NORMALIZED`, `INFERRED`, and `DEMONSTRATION` as distinct provenance states; explicitly separates source observations from derived representations; associates evidence with the correct evaluation attempt/run; preserves first-party, third-party, user-provided, derived, and demonstration source relationships; handles duplicate/equivalent and conflicting evidence without silent overwrites; distinguishes available, unavailable, failed, and not-attempted observations; defines freshness/staleness semantics; and requires downstream findings to remain traceable to supporting evidence and material limitations.

The specification intentionally avoids numerical confidence scoring, universal source ranking, database schema, framework, crawler/acquisition implementation, AI architecture, report/preview UI, hosting, and other implementation decisions outside REQ-005.

No separate architecture or product decision record was created because the work establishes the required product/domain semantics without selecting implementation technology or infrastructure.

### Validation

- Inspected the repository and confirmed `main` is the default branch.
- Located the current `READY` requirement under `requirements/` before making changes and read `REQ-005` completely.
- Read the referenced `project-context`, SPEC-001, SPEC-002, SPEC-003, SPEC-004, specification index, architecture concerns, and decisions README documents before authoring the specification.
- Confirmed the repository currently has no additional substantive decision records beyond `docs/decisions/README.md`.
- Created documentation/specification changes only; no crawler, public-web acquisition implementation, analysis engine, scoring, AI, preview runtime, persistence schema, or infrastructure code was added.
- Verified SPEC-005 addresses all REQ-005 scope areas and acceptance criteria, including evaluation association, provenance preservation, source semantics, derivation relationships, duplicate/equivalent evidence, conflicts, freshness/staleness, unavailable/failed/partial evidence, downstream finding traceability, and prevention of inferred/generated/demo content being represented as discovered hotel fact.
- Indexed SPEC-005 in `docs/specs/README.md`.
- Created branch `spec/evidence-model-contract` from `main`.
- Created PR #5 against `main`; PR remains open and unmerged.
- No automated application tests were applicable because this session is documentation/specification-only.

### Open Questions

No material open question remains within the bounded REQ-005 evidence-model scope. Exact persistence/schema representation, content-retention strategy, freshness thresholds by evidence type, implementation of provenance references, analysis-specific source/support selection, and preview provenance UI remain intentionally deferred to later requirements/specifications/architecture decisions.

## Orchestrator Review History

<!-- Orchestrator review rounds are appended below without deleting prior history. -->
