# REQ-018 — Evidence Normalization Foundation

STATUS: READY
REQUIREMENT_ID: REQ-018
TYPE: Implementation
BRANCH: `feature/evidence-normalization-foundation`

## Objective

Implement the smallest governed boundary that converts the existing REQ-017 acquisition output into a structured evidence representation suitable for later hospitality analysis.

The target flow is:

`AcquisitionResult → structured evidence`

For the current slice, structured evidence represents **one acquisition observation/limitation**. It must preserve acquisition provenance and make the distinction between source observation and later interpretation explicit.

REQ-018 does **not** extract hotel facts from HTML, perform hospitality analysis, or build a complete evidence pipeline.

## Context

The repository now has the following implemented flow on `main`:

`CanonicalEvaluationRequest → Evaluation/Attempt → PublicWebAcquisitionService → AcquisitionResult → CapabilityOutcome`

REQ-017 deliberately stopped before evidence normalization. `SPEC-005` defines the evidence model as the governed boundary between acquisition and later analysis. It requires source observations and evidence items to remain traceable, provenance states to remain distinguishable, unavailable evidence to remain distinct from absence, and derived representations to retain their supporting evidence relationships.

The next smallest useful slice is therefore a **single-observation normalization boundary**: take the already available acquisition result and expose its acquisition metadata/content as a structured evidence representation without interpreting the hotel content.

## Product / Truthfulness Boundary

REQ-018 must preserve these distinctions:

### Acquisition data

The existing `AcquisitionResult` is acquisition-layer output. It describes what the acquisition operation requested, observed, or failed to observe, including URL, final URL, HTTP metadata, retrieval timestamp, method, body, redirect information, provenance metadata, and typed acquisition outcome.

### Normalized evidence

REQ-018 produces a structured representation of that acquisition observation/limitation. Normalization may organize, rename, and consistently represent acquisition metadata and retained observed content, but must not add unsupported hotel meaning.

A normalized evidence representation must retain a relationship to the source acquisition result/observation and its evaluation/attempt context.

### Verified facts

REQ-018 creates **no independently verified hotel facts**.

A successful public source observation may carry the product's `DISCOVERED` provenance state because the source content was directly observed. That does **not** mean that every statement in the source has been independently verified, nor does it authorize extracting or asserting hotel facts.

### Inferred information

REQ-018 creates **no `INFERRED` information**.

### Demonstration information

REQ-018 creates **no `DEMONSTRATION` information**.

### Future analysis

Hospitality interpretation, guest-journey findings, scoring, confidence judgments, conflict resolution, recommendations, and hotel-fact extraction belong to later analysis requirements.

## READ

Read completely before implementation:

- `requirements/README.md`
- `docs/specs/SPEC-001-hotel-check.md`
- `docs/specs/SPEC-004-public-evidence-acquisition.md`
- `docs/specs/SPEC-005-evidence-model.md`
- `docs/architecture/EVALUATION-ARCHITECTURE.md`
- `docs/architecture/architecture-concerns.md`
- `requirements/REQ-015-public-web-acquisition-foundation.md`
- `requirements/REQ-017-evaluation-acquisition-integration.md`
- `docs/engineering/AGENT-GUIDELINES.md`
- `docs/workflows/IMPLEMENTATION-SESSION.md`

Inspect only additional directly relevant implementation/tests when required by the `INSPECT` section.

## INSPECT

Before implementation, inspect the existing `main` implementation for:

- `AcquisitionResult` and its outcome/method/provenance fields;
- `EvaluationAcquisitionResult` and the evaluation/attempt context produced by REQ-017;
- existing `CapabilityOutcome` / evaluation outcome conventions;
- existing test conventions for acquisition/evaluation boundaries;
- any existing evidence-related implementation or types.

Search for an existing evidence model before creating a new one. If an existing implementation already provides the required structured evidence boundary, extend/reuse it rather than introducing a duplicate abstraction.

If repository evidence is insufficient to choose the smallest representation without inventing product behavior, STOP and report the ambiguity.

## DEPENDENCIES

REQ-018 directly depends on:

- REQ-015 bounded public-web acquisition and its typed `AcquisitionResult` contract;
- REQ-017 evaluation/acquisition integration and its `EvaluationAcquisitionResult` boundary;
- `SPEC-005` evidence semantics and provenance requirements;
- existing evaluation/attempt identity/context already implemented on `main`.

REQ-018 must not redesign those contracts.

## Scope

### In scope

1. Define the smallest structured evidence representation required to represent one acquisition observation/limitation.
2. Provide a small normalization boundary/service that accepts the existing REQ-017 acquisition result and produces that structured evidence representation.
3. Preserve the evaluation and attempt/run attribution available from the REQ-017 result.
4. Preserve source/provenance information needed to understand what was observed, when, where, and by which acquisition method.
5. Preserve acquisition availability/failure semantics without converting them into hotel-feature absence.
6. Represent successful observed content as `DISCOVERED` evidence where supported by the acquisition result.
7. Represent acquisition limitations/failures as structured evidence limitations without inventing hotel facts.
8. Preserve the distinction between source observation and normalized representation; normalization must not mutate or overwrite the acquisition result.
9. Add deterministic unit tests for successful normalization and important acquisition-outcome/metadata cases.
10. Keep the boundary in-process and persistence-independent so later requirements can decide evidence storage/lifecycle.
11. Document only the new normalization boundary if repository documentation needs an update as part of the implementation.

## Normalization Contract

The implementation should establish one small semantic operation:

`EvaluationAcquisitionResult → StructuredEvidence`

The resulting representation should contain, at minimum, the information necessary to answer:

- Which evaluation/attempt produced this evidence?
- Which source URL was requested/observed?
- What final URL was observed, if applicable?
- When was the observation made?
- Which acquisition method was used?
- What acquisition availability/outcome occurred?
- What content type/status metadata was observed, if available?
- What retained observed content is available, if any?
- What provenance state applies to the representation?
- What acquisition limitation/error information materially affects interpretation, if any?

The implementation may choose different field names consistent with repository conventions, but it must not add unsupported hotel-domain semantics merely to make the representation appear richer.

For a successful acquisition:

- the structured evidence represents the observed source content;
- provenance remains `DISCOVERED`;
- acquisition metadata remains attributable to the source observation;
- no hotel attribute is extracted or asserted.

For an unavailable/failed/not-successful acquisition:

- the structured evidence preserves the acquisition limitation/outcome;
- no hotel feature is marked absent;
- no successful source content is fabricated;
- the representation remains useful as a limitation/observation record where appropriate.

A normalized evidence item must not silently change `UNAVAILABLE`, `FAILED`, or `NOT_ATTEMPTED` into evidence that a hotel feature does not exist.

## DO NOT TOUCH

Do not implement or modify:

- multi-page crawling or page discovery;
- browser/Playwright acquisition;
- robots/access-policy expansion beyond existing acquisition behavior;
- HTML parsing for hotel facts;
- room/amenity/contact/booking/dining extraction;
- hospitality analysis;
- guest-journey scoring or grading;
- AI/LLM integration;
- inference/confidence engine;
- conflict resolution policy;
- report generation;
- Hotel Experience Model;
- interactive preview generation/hosting;
- booking or OTA integrations;
- persistence/database schema/migrations;
- asynchronous queues/workflows;
- public REST/GraphQL API;
- authentication/authorization;
- new acquisition implementation;
- changes to REQ-015 SSRF, timeout, redirect, response-size, or public-web security guarantees;
- redesign of REQ-011/REQ-012/REQ-015/REQ-017 contracts.

Do not create speculative abstractions for future evidence types or future analysis consumers.

## ACCEPTANCE

### AC-1 — Existing acquisition output is the input boundary

The normalization boundary accepts the existing REQ-017 acquisition result rather than introducing a second acquisition contract or reconstructing acquisition data from raw fields elsewhere.

### AC-2 — Structured evidence is distinct from acquisition data

The implementation produces a structured evidence representation that is separate from `AcquisitionResult` while preserving a traceable relationship to the acquisition observation.

### AC-3 — Provenance preservation

Successful directly observed public content remains represented as `DISCOVERED`. The implementation does not upgrade, downgrade, or silently replace provenance based on normalization alone.

### AC-4 — Source traceability

Structured evidence preserves sufficient source and observation metadata to identify the requested/observed source, acquisition method, observation timestamp, and relevant outcome/limitation.

### AC-5 — Evaluation/attempt attribution

Structured evidence remains attributable to the evaluation and attempt/run that produced the acquisition result.

### AC-6 — Availability semantics preserved

`AVAILABLE`/successful acquisition and acquisition limitations/failures remain distinguishable. A failure or unavailable source is never converted into a negative hotel fact.

### AC-7 — No silent hotel-fact extraction

REQ-018 does not parse the acquired page to assert room, amenity, policy, contact, booking, dining, or other hotel facts.

### AC-8 — No inference or demonstration

REQ-018 produces no `INFERRED` or `DEMONSTRATION` evidence.

### AC-9 — No mutation of source acquisition data

Normalization creates a related structured representation and does not rewrite or mutate the original `AcquisitionResult` provenance/content/outcome.

### AC-10 — Deterministic tests

Tests cover at minimum:

- successful acquisition normalization;
- preservation of requested/final URL and retrieval timestamp;
- preservation of acquisition method/content metadata where present;
- preservation of observed content without semantic extraction;
- acquisition failure/unavailability normalization;
- proof that failure is not represented as hotel-feature absence;
- evaluation/attempt attribution;
- provenance state behavior.

Tests must not use live hotel websites or external network access.

### AC-11 — Persistence independence

The implementation does not introduce a database schema, repository, migration, or storage lifecycle for evidence.

### AC-12 — Scope discipline

No analysis, scoring, AI, preview, booking, OTA, crawler, asynchronous orchestration, or public API is introduced.

### AC-13 — Existing contracts remain intact

REQ-015 and REQ-017 behavior and security guarantees remain intact. The normalization boundary consumes their existing outputs rather than redefining them.

### AC-14 — CI validation

The repository backend CI workflow executes the complete backend test suite successfully for the implementation PR.

## OUTPUT

The implementation session must:

1. Create the exact branch named in `BRANCH` from the current `main` at session start.
2. Implement ONLY REQ-018.
3. Add deterministic tests for the acceptance criteria.
4. Run the documented backend validation and report only observed results.
5. Perform the repository self-review defined by `docs/engineering/AGENT-GUIDELINES.md`.
6. Update this SAME requirement file with implementation summary, changed files, validation, branch, PR information, limitations/open questions, and final status.
7. Create/update the implementation PR against `main`.
8. Do not merge the PR.
9. Stop for orchestrator review.

## Governance

REQ-018 is a bounded implementation contract. The implementation session must not expand scope because later consumers may eventually need richer evidence semantics.

If implementation reveals that the existing contracts do not provide enough information to create the minimal structured evidence boundary without inventing semantics, STOP and report the missing repository decision rather than extending scope unilaterally.
