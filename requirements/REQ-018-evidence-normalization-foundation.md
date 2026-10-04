# REQ-018 — Evidence Normalization Foundation

STATUS: PR_READY
REQUIREMENT_ID: REQ-018
TYPE: Implementation
BRANCH: `feature/evidence-normalization-foundation`

## Objective

Implement the smallest governed boundary that converts the existing REQ-017 acquisition output into a structured evidence representation suitable for later hospitality analysis.

Target flow:

`EvaluationAcquisitionResult → StructuredEvidence`

The current slice represents one acquisition observation or limitation. It preserves acquisition provenance and source traceability without interpreting hotel meaning.

## Product / Truthfulness Boundary

### Acquisition data

`AcquisitionResult` remains acquisition-layer output containing requested/final URL, HTTP metadata, retrieval timestamp, acquisition method, body, provenance metadata, redirects, and typed acquisition outcome.

### Normalized evidence

`StructuredEvidence` is a separate in-process representation that retains the source `EvaluationAcquisitionResult`, evaluation/attempt identity, acquisition metadata, observed content, outcome, and limitation.

### Verified facts

REQ-018 creates no independently verified hotel facts. Directly observed public content retains `DISCOVERED` source provenance; normalization does not upgrade it to a verified hotel fact.

### Inferred information

REQ-018 creates no `INFERRED` information.

### Demonstration information

REQ-018 creates no `DEMONSTRATION` information.

### Future analysis

Hotel-fact extraction, hospitality interpretation, guest-journey findings, scoring, confidence, conflict resolution, recommendations, reporting, and preview generation remain later work.

## READ

Authoritative context read before implementation:

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

## INSPECT

Inspected existing acquisition/evaluation contracts and tests before implementation:

- `AcquisitionResult`
- `AcquisitionOutcome`
- `AcquisitionMethod`
- `EvaluationAcquisitionResult`
- `Evaluation`
- `EvaluationAttempt`
- `CapabilityOutcome`
- existing evaluation/acquisition tests
- repository evidence-model search

No pre-existing evidence implementation was found, so REQ-018 introduced the minimal new evidence boundary rather than duplicating an existing model.

## DEPENDENCIES

- REQ-015 bounded public-web acquisition and typed `AcquisitionResult` contract.
- REQ-017 evaluation/acquisition integration and `EvaluationAcquisitionResult` boundary.
- SPEC-005 evidence semantics and provenance requirements.
- Existing evaluation/attempt identity and lifecycle contracts.

Existing acquisition and lifecycle contracts were not redesigned.

## DO NOT TOUCH

REQ-018 does not implement or modify crawling, page discovery, browser acquisition, HTML hotel-fact extraction, room/amenity/contact/booking/dining extraction, hospitality analysis, scoring, AI/LLM, inference, confidence, conflict resolution, reports, Hotel Experience Model, preview generation, booking/OTA, persistence, queues, schedulers, public APIs, authentication, or REQ-015 security behavior.

No speculative evidence registry, repository, factory, persistence abstraction, or future analysis framework was introduced.

## IMPLEMENTATION

Implemented the single normalization boundary:

`EvaluationAcquisitionResult → StructuredEvidence`

`StructuredEvidence` preserves:

- evaluation identity;
- attempt identity and number;
- traceable source `EvaluationAcquisitionResult`;
- source provenance;
- requested/final URL;
- retrieval timestamp;
- acquisition method;
- HTTP status and content type;
- retained observed body when available;
- acquisition outcome;
- acquisition limitation/error.

Successful observed content remains `DISCOVERED`.

Failed/limited acquisition retains its typed outcome and limitation, with no fabricated content and no representation of hotel-feature absence.

No HTML semantic extraction or hotel-domain interpretation occurs.

The original `AcquisitionResult` is not mutated.

## ACCEPTANCE

- Existing REQ-017 `EvaluationAcquisitionResult` is the normalization input boundary.
- Structured evidence is distinct from acquisition data and traceable to its source observation.
- Evaluation/attempt attribution is preserved.
- Requested/final URL, retrieval timestamp, acquisition method, status, content type, content, outcome, and limitation are preserved.
- Successful public observation retains `DISCOVERED` provenance.
- Failure/unavailability remains distinguishable from successful observation and never becomes a negative hotel fact.
- No room, amenity, policy, contact, booking, dining, or other hotel facts are extracted.
- No `INFERRED` or `DEMONSTRATION` evidence is produced.
- Source acquisition data remains unchanged.
- No persistence or asynchronous infrastructure is introduced.
- Existing REQ-015/REQ-017 contracts remain intact.
- Deterministic tests contain no live website or external-network dependency.
- Complete backend CI validation succeeds.

## TESTS

Added deterministic unit coverage for:

1. successful normalization;
2. `DISCOVERED` provenance preservation;
3. requested/final URL and retrieval timestamp;
4. acquisition method and HTTP/content metadata;
5. observed-content retention without hotel semantic extraction;
6. evaluation/attempt attribution;
7. timeout/failure normalization as a limitation;
8. HTTP error distinction;
9. absence of observed content on failure;
10. no mutation/replacement of the original acquisition result.

## FILES CHANGED

- `backend/src/main/java/com/antenapro/hotelcheck/evidence/EvidenceProvenance.java`
- `backend/src/main/java/com/antenapro/hotelcheck/evidence/StructuredEvidence.java`
- `backend/src/main/java/com/antenapro/hotelcheck/evidence/EvidenceNormalizationService.java`
- `backend/src/test/java/com/antenapro/hotelcheck/evidence/EvidenceNormalizationServiceTest.java`
- this requirement file

## VALIDATION

### Backend CI

GitHub Actions workflow: **Backend Validation**

Run: `37172048369`

Job: `Java 21 / Maven tests`

Result: **SUCCESS**

The complete backend Maven test command executed successfully in CI. The job completed successfully at 2026-10-04T02:46:58Z.

### Local validation

A local Maven execution was not performed in this session environment. CI is the observed execution of the documented complete backend validation.

## PR

PR: #18 — REQ-018: Evidence Normalization Foundation

Base: `main`

Head: `feature/evidence-normalization-foundation`

PR status: OPEN

PR merge status: NOT MERGED

## SELF-REVIEW

### Scope

Only the REQ-018 acquisition-to-structured-evidence boundary was implemented.

### Existing code

Existing acquisition/evaluation contracts were reused.

### Duplication

Repository search found no existing evidence implementation before the new boundary was introduced.

### Truthfulness

The output contains source observation data and provenance, not hotel-domain facts. Failure remains a limitation rather than an absence assertion.

### Provenance

The original `EvaluationAcquisitionResult` is retained as the source observation, while normalized fields preserve source metadata and acquisition outcome.

### Simplicity

The implementation is an in-process record plus one normalization service and deterministic unit tests. No persistence or generic future-facing abstraction was added.

### Security / production

REQ-018 performs no network access, does not alter acquisition security controls, and does not add unbounded processing or external-service behavior.

## LIMITATIONS / OPEN QUESTIONS

- Evidence persistence/lifecycle is intentionally deferred.
- Evidence extraction from observed HTML is intentionally deferred to a later requirement.
- Broader provenance/source taxonomy remains governed by SPEC-005 and is not expanded here.
- Local Maven validation was unavailable in the session environment; GitHub Actions backend validation is successful.

## GOVERNANCE

REQ-018 is complete for implementation review. No REQ-019 work has been started.

STOPPING FOR ORCHESTRATOR REVIEW.
