# REQ-019 — Hospitality Observation Foundation

STATUS: PR_READY
REQUIREMENT_ID: REQ-019
TYPE: Implementation
BRANCH: `feature/hospitality-observation-foundation`

## Objective

Implement the smallest governed boundary that turns a successfully acquired and normalized public page into a bounded set of **hospitality-specific observations** that later analysis can consume.

Target flow:

`StructuredEvidence → HospitalityObservation(s)`

This slice establishes a narrow, deterministic observation layer. It does **not** establish a complete hotel-fact ontology or an analysis/scoring engine.

The observations produced here are evidence-backed representations of signals actually present in the observed source. They are not universal or independently verified hotel facts.

## Product / Truthfulness Boundary

### Source evidence

`StructuredEvidence` remains the source observation boundary established by REQ-018. The implementation consumes its retained observed content and source/provenance context without mutating it.

Only `StructuredEvidence` representing a successful available observation with retained content is eligible for hospitality observation extraction.

### Hospitality observations

A `HospitalityObservation` represents a bounded, directly observable hospitality-related signal in the source evidence.

The observation vocabulary is limited to:

- `HOTEL_IDENTITY`
- `ROOMS`
- `AMENITIES`
- `CONTACT`
- `BOOKING`
- `DINING`

An observation preserves enough source reference to answer:

> What was actually observed, where was it observed, and which evidence supports it?

Observed text and links are retained rather than converted into unsupported normalized hotel claims.

### OBSERVED

A produced hospitality observation is `OBSERVED` only when the corresponding signal is directly present in retained source content.

Example: a page containing a visible `Book Now` link may produce an observed `BOOKING` signal. It does not prove that a booking transaction succeeds.

### INFERRED

REQ-019 produces **no `INFERRED` observations**.

It does not infer service availability, inventory, booking success, quality, or guest-journey success from weak/context-free signals.

### DEMONSTRATION

REQ-019 produces **no `DEMONSTRATION` content**.

### UNAVAILABLE / NOT OBSERVED

If source evidence is unavailable, failed, not attempted, or has no retained content, the extractor produces **no hospitality observations** from that source.

This is not converted into proof that the hotel lacks rooms, amenities, dining, contact information, or booking.

## READ

The implementation session read:

- `requirements/README.md`
- `docs/specs/SPEC-001-hotel-check.md`
- `docs/specs/SPEC-002-evaluation-input.md`
- `docs/specs/SPEC-003-evaluation-lifecycle.md`
- `docs/specs/SPEC-004-public-evidence-acquisition.md`
- `docs/specs/SPEC-005-evidence-model.md`
- `docs/specs/SPEC-006-hospitality-analysis.md`
- `docs/architecture/EVALUATION-ARCHITECTURE.md`
- `docs/architecture/architecture-concerns.md`
- `requirements/REQ-015-public-web-acquisition-foundation.md`
- `requirements/REQ-017-evaluation-acquisition-integration.md`
- `requirements/REQ-018-evidence-normalization-foundation.md`
- `docs/engineering/AGENT-GUIDELINES.md`
- `docs/engineering/ENGINEERING-PRINCIPLES.md`
- `docs/engineering/BACKEND-PATTERNS.md`
- `docs/engineering/TESTING-STANDARDS.md`
- `docs/workflows/IMPLEMENTATION-SESSION.md`

## INSPECT

Inspected current `main` and the REQ-019 branch for:

- `StructuredEvidence` and `EvidenceNormalizationService`;
- `EvidenceProvenance` and acquisition outcome/method semantics;
- `EvaluationAcquisitionResult`, evaluation/attempt identity, and capability boundaries;
- existing evidence tests and deterministic backend test conventions;
- backend Maven dependencies.

No existing hospitality observation implementation or suitable generic fact abstraction was present. The backend did not already include an HTML parser dependency. A new external parsing dependency was therefore not introduced; the implementation uses a bounded, deterministic semantic-HTML signal matcher with no network access.

## DEPENDENCIES

- REQ-018 `StructuredEvidence` and its source-observation/provenance boundary.
- REQ-017 `EvaluationAcquisitionResult` and evaluation/attempt attribution retained by REQ-018.
- SPEC-005 evidence provenance, source traceability, unavailable/failed semantics, and derivation rules.
- SPEC-006 hospitality-first analysis dimensions and the distinction between observed deficiency and inability to verify.
- Existing backend conventions and deterministic test infrastructure.

REQ-019 does not redesign acquisition, evaluation lifecycle, or the REQ-018 evidence normalization contract.

## DO NOT TOUCH

REQ-019 does not implement or modify:

- public-web acquisition behavior;
- crawling or page discovery;
- browser/Playwright acquisition;
- SSRF, redirect, timeout, response-size, or public-access controls;
- evaluation lifecycle/state-machine behavior;
- evidence persistence or retention infrastructure;
- a generic `Fact`/`FactType` framework;
- a complete hospitality ontology;
- hospitality scoring or grading;
- guest-journey scoring;
- analysis findings, severity, recommendations, or coverage scoring;
- conflict resolution or source hierarchy;
- freshness thresholds;
- AI/LLM/model integration;
- report generation;
- Hotel Experience Model;
- preview generation/rendering/hosting;
- booking/OTA integrations;
- competitor analysis;
- generic SEO auditing/scoring;
- public APIs, authentication, queues, schedulers, or production infrastructure.

## IMPLEMENTATION BOUNDARY

Implemented one deterministic in-process boundary that accepts `StructuredEvidence` and returns zero or more bounded `HospitalityObservation` records.

The implementation performs no network access and only inspects retained source content.

Each emitted observation preserves:

- one of the six bounded observation categories;
- observed value/excerpt and relevant link where present;
- source reference derived from the supporting page/evidence;
- supporting `StructuredEvidence` object reference;
- evaluation ID;
- attempt ID and attempt number;
- `DISCOVERED` provenance.

The service rejects non-`DISCOVERED`, unsuccessful, unavailable, or contentless evidence and returns no observations for those inputs.

Matching is deliberately conservative. Signals are limited to semantic page elements (`title`, headings, anchors, and address elements) and explicit hospitality/contact link forms. Contextual negative checks prevent examples such as conference/server rooms and supplier/vendor contact links from becoming hospitality observations.

No analysis, scoring, inference, demonstration, booking-success conclusion, or hotel-quality judgment is produced.

## AI BOUNDARY

REQ-019 is deterministic/rule-based.

No LLM, embedding model, generative model, prompt framework, model API, or external AI service was introduced.

AI-assisted interpretation remains future scope and requires a separate governed requirement if later justified.

## ACCEPTANCE

### AC-1 — Structured evidence is the input boundary

Satisfied. `HospitalityObservationService.observe(...)` accepts `StructuredEvidence` and performs no acquisition.

### AC-2 — Successful observed content only

Satisfied. Only successful `DISCOVERED` evidence with retained non-blank content is processed.

### AC-3 — No false absence

Satisfied. Failed and contentless evidence returns an empty observation list without negative hotel assertions.

### AC-4 — Bounded hospitality vocabulary

Satisfied. `HospitalityObservationCategory` contains exactly `HOTEL_IDENTITY`, `ROOMS`, `AMENITIES`, `CONTACT`, `BOOKING`, and `DINING`.

### AC-5 — Direct observation only

Satisfied. Observations are emitted from explicit source text/link signals only. The implementation does not assert service availability, quality, booking success, or inventory.

### AC-6 — Source traceability

Satisfied. Each observation retains its supporting `StructuredEvidence`, evaluation/attempt attribution, observed value, and source reference.

### AC-7 — Provenance preservation

Satisfied. Every emitted observation uses `DISCOVERED` provenance and source evidence is not mutated.

### AC-8 — Evaluation/attempt attribution

Satisfied. Evaluation ID, attempt ID, and attempt number are copied from the supporting evidence.

### AC-9 — No analysis semantics

Satisfied. No score, severity, finding, recommendation, journey rating, or booking-success conclusion is produced.

### AC-10 — No inference or demonstration

Satisfied. The implementation emits only `DISCOVERED` observations.

### AC-11 — Deterministic behavior

Satisfied. Tests use fixed clocks, in-memory HTML strings, and no external network or model behavior. Same input produces the same observable signatures.

### AC-12 — Conservative matching

Satisfied. Tests cover context-free/ambiguous room text and supplier contact text and verify they do not become hospitality observations.

### AC-13 — Relevant hospitality signals

Satisfied. Tests cover all six categories and verify observed values, links, source references, provenance, and supporting evidence.

### AC-14 — Source content remains unchanged

Satisfied. Tests verify the retained source content/provenance remains unchanged after observation extraction.

### AC-15 — Existing boundaries remain intact

Satisfied. No changes were made to REQ-015, REQ-017, or REQ-018 behavior.

### AC-16 — Complete backend validation

Satisfied by GitHub Actions Backend Validation run **#70** on commit `c16d15efc035db46452dfb63a18c8338550f95e6`:

- `mvn --batch-mode --no-transfer-progress test`
- 85 tests
- 0 failures
- 0 errors
- BUILD SUCCESS

The REQ-019 test class specifically reported:

- `HospitalityObservationServiceTest`
- 8 tests
- 0 failures
- 0 errors

## OUTPUT

### Implementation summary

Implemented the hospitality observation boundary using three new production types:

- `HospitalityObservationCategory`
- `HospitalityObservation`
- `HospitalityObservationService`

Added deterministic unit tests for the six observation categories, attribution/provenance, source traceability, unavailable evidence, inference exclusion, deterministic repeatability, source immutability, and conservative negative matching.

### Files changed

- `backend/src/main/java/com/antenapro/hotelcheck/hospitality/HospitalityObservationCategory.java`
- `backend/src/main/java/com/antenapro/hotelcheck/hospitality/HospitalityObservation.java`
- `backend/src/main/java/com/antenapro/hotelcheck/hospitality/HospitalityObservationService.java`
- `backend/src/test/java/com/antenapro/hotelcheck/hospitality/HospitalityObservationServiceTest.java`
- `requirements/REQ-019-hospitality-observation-foundation.md`

No unrelated files were changed.

### Validation

Local/container validation could not be executed because the execution environment could not resolve `github.com` for repository cloning. Repository CI was therefore used as the authoritative executed validation.

GitHub Actions Backend Validation run **#70** completed successfully on `c16d15efc035db46452dfb63a18c8338550f95e6` with all 85 backend tests passing, including all 8 REQ-019 tests.

An earlier CI run failed on the intentionally conservative supplier-contact test because plural `suppliers` was not covered by the negative matcher. That failure was fixed by strengthening the matcher to handle supplier/vendor/partner plural forms. The subsequent full backend validation passed.

### CI

**PASS** — Backend Validation run #70.

### PR

Pull request: **#19** — `REQ-019: Hospitality Observation Foundation`

Base: `main`

Head: `feature/hospitality-observation-foundation`

PR remains open and unmerged for orchestrator review.

### Limitations / open questions

- The HTML matching is intentionally lightweight and conservative; it is not a general HTML parser or semantic NLP system.
- The observation vocabulary is intentionally limited to the six REQ-019 categories.
- Some valid hospitality signals outside the conservative semantic/link patterns will not be observed yet; absence of such an observation is not treated as hotel-feature absence.
- Exact downstream aggregation, conflict handling, freshness, analysis findings, scoring/importance, and evidence coverage remain later requirements.

### Engineering self-review

#### Scope

Only REQ-019 was implemented. No REQ-020 work or unrelated refactoring was introduced.

#### Existing code

Existing `StructuredEvidence`, `EvidenceProvenance`, acquisition contracts, and evaluation/attempt attribution were reused directly. No generic fact framework was introduced.

#### Security

The new service has no network path, URL fetching, browser access, persistence, authentication, or external-service call. It operates only on already-retained evidence.

#### Truthfulness

Observations are explicitly `DISCOVERED`, retain their supporting evidence, and do not claim verification of hotel capabilities, inventory, quality, or booking success.

#### Matching

The matcher uses semantic elements and explicit link/contact signals, plus negative context for known ambiguous examples. Tests cover conference/server rooms and supplier contact to guard against naive keyword detection.

#### Provenance

Every observation contains the supporting `StructuredEvidence`, evaluation ID, attempt ID/number, source reference, and `DISCOVERED` provenance.

#### Simplicity

The implementation uses a small enum, immutable record, and single cohesive service. No parser dependency, registry, factory, persistence layer, or generic framework was introduced.

#### Tests

The final CI run executed the complete backend suite and the eight REQ-019 tests with zero failures/errors. No live hotel website or external network is required by the REQ-019 tests.

## Governance

REQ-019 implementation is complete and the requirement is `PR_READY`.

PR #19 is open against `main` and has **not** been merged.

No REQ-020 work has been started.

STOPPING FOR ORCHESTRATOR REVIEW.
