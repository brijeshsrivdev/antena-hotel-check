# REQ-019 — Hospitality Observation Foundation

STATUS: READY
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

`StructuredEvidence` remains the source observation boundary established by REQ-018. The implementation must consume its retained observed content and source/provenance context without mutating it.

Only `StructuredEvidence` representing a successful available observation with retained content is eligible for hospitality observation extraction.

### Hospitality observations

A `HospitalityObservation` represents a bounded, directly observable hospitality-related signal in the source evidence.

For this slice, the observation vocabulary is intentionally limited to:

- `HOTEL_IDENTITY` — a page signal that can help identify the property, such as an observed property-name candidate in an appropriate page element;
- `ROOMS` — an observed room/suite-related signal or relevant room entry point;
- `AMENITIES` — an observed amenity/facility-related signal or relevant guest-information entry point;
- `CONTACT` — an observed contact/location signal such as a contact path, address, phone, or email entry point;
- `BOOKING` — an observed booking/reservation/check-availability entry point;
- `DINING` — an observed restaurant/dining/breakfast/bar-related signal where applicable.

These categories are deliberately small and are not a complete hospitality ontology.

An observation must preserve enough source reference to answer:

> What was actually observed, and which evidence supports it?

Where an observation carries text or a link/value, it must retain the relevant observed value/excerpt/reference rather than replacing it with an unsupported normalized claim.

### OBSERVED

A produced hospitality observation is `OBSERVED` only when the corresponding signal is directly present in the retained source content.

Example: a page containing a visible `Book Now` link may produce an observed `BOOKING` signal. It must not be represented as proof that a booking transaction succeeds.

### INFERRED

REQ-019 produces **no `INFERRED` observations**.

It must not infer facts such as:

- a hotel definitely offers a service merely because a keyword appears in unrelated text;
- a hotel has rooms because the word "room" appears without a relevant hospitality context;
- booking is operational merely because a booking link exists;
- an amenity exists merely because a generic navigation label suggests it.

The observation layer records bounded source signals. Later analysis may interpret them.

### DEMONSTRATION

REQ-019 produces **no `DEMONSTRATION` content**.

### UNAVAILABLE / NOT OBSERVED

If the source evidence is unavailable, failed, not attempted, or has no retained content, the extractor must produce **no hospitality observations** from that source.

This must not be converted into an assertion that the hotel lacks rooms, amenities, dining, contact information, or booking.

If a successfully observed page contains no recognized signal for one of the bounded categories, that absence of an observation is not itself a hotel-fact assertion. Later analysis decides whether the observed page and coverage are sufficient to support an observed deficiency or an inability to verify.

## READ

The implementation session MUST read:

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
- `docs/workflows/IMPLEMENTATION-SESSION.md`

## INSPECT

Before implementation, inspect current `main` for:

- `StructuredEvidence` and `EvidenceNormalizationService`;
- `EvidenceProvenance` and acquisition outcome/method semantics;
- `EvaluationAcquisitionResult`, evaluation/attempt identity, and existing capability boundaries;
- existing hospitality/evidence/fact/observation classes, interfaces, enums, utilities, and tests;
- existing HTML/content parsing dependencies before introducing a new dependency;
- existing deterministic test conventions.

Before introducing a new abstraction, search the repository and inspect callers/tests. Extend an existing type only if that type genuinely owns the new responsibility.

If repository evidence does not support the required implementation choice, STOP and report the ambiguity rather than inventing a framework or domain model.

## DEPENDENCIES

- REQ-018 `StructuredEvidence` and its source-observation/provenance boundary.
- REQ-017 `EvaluationAcquisitionResult` and evaluation/attempt attribution retained by REQ-018.
- SPEC-005 evidence provenance, source traceability, unavailable/failed semantics, and derivation rules.
- SPEC-006 hospitality-first analysis dimensions and the distinction between observed deficiency and inability to verify.
- Existing backend conventions and deterministic test infrastructure.

REQ-019 must not redesign acquisition, evaluation lifecycle, or the REQ-018 evidence normalization contract.

## DO NOT TOUCH

REQ-019 must not implement or modify:

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

Do not turn keyword/signal detection into claims about hotel quality or service availability.

## IMPLEMENTATION BOUNDARY

Implement one deterministic in-process boundary that accepts a successful `StructuredEvidence` observation and returns zero or more bounded `HospitalityObservation` records.

The implementation may inspect the retained HTML/content and relevant source metadata already present in `StructuredEvidence`, but it must perform no network access.

The implementation should preserve the following for every produced observation:

- evaluation/attempt attribution available from the source evidence;
- observation category from the bounded REQ-019 vocabulary;
- directly observed value, excerpt, or source reference sufficient to explain the observation;
- reference to the supporting `StructuredEvidence`;
- `DISCOVERED` provenance for the directly observed source-backed observation, without changing the source evidence;
- explicit observed status rather than an inferred/verified-hotel status.

The implementation should prefer conservative positive observations over broad semantic guessing. A weak or ambiguous textual match should not be emitted merely to increase coverage.

The exact parsing library or implementation mechanism is not selected by this requirement. Existing repository dependencies/patterns must be inspected first; adding a dependency requires justification within the implementation scope and must not become an architecture decision by accident.

## AI BOUNDARY

REQ-019 is deterministic/rule-based.

No LLM, embedding model, generative model, prompt framework, model API, or external AI service is required or authorized for this slice.

AI-assisted interpretation may be evaluated later if deterministic observation coverage proves insufficient, but that is future scope and must not be introduced implicitly into REQ-019.

## ACCEPTANCE

### AC-1 — Structured evidence is the input boundary

The observation extractor accepts `StructuredEvidence` and does not accept raw URLs or perform its own acquisition.

### AC-2 — Successful observed content only

Hospitality observations are produced only from `StructuredEvidence` that represents an available successful observation with retained content.

### AC-3 — No false absence

For failed, unavailable, not-attempted, or contentless evidence, the extractor returns no hospitality observations and does not create a negative hotel fact.

### AC-4 — Bounded hospitality vocabulary

The implementation emits only the six REQ-019 observation categories:

`HOTEL_IDENTITY`, `ROOMS`, `AMENITIES`, `CONTACT`, `BOOKING`, `DINING`.

No generic website-audit or unrestricted semantic category is introduced.

### AC-5 — Direct observation only

Each emitted observation is supported by content actually present in the supplied source evidence. The implementation must not infer service availability, quality, guest-journey success, or hotel capability from weak/context-free signals.

### AC-6 — Source traceability

Every emitted observation can be traced back to its supporting `StructuredEvidence` and retains enough directly observed value/excerpt/source-reference information to explain why it was emitted.

### AC-7 — Provenance preservation

Produced source-backed observations retain `DISCOVERED` provenance and do not mutate or overwrite the source `StructuredEvidence` or its provenance.

### AC-8 — Evaluation/attempt attribution

An observation retains the evaluation/attempt context of its supporting evidence so that a later retry cannot appear to have produced the observation from an earlier attempt.

### AC-9 — No analysis semantics

The implementation does not produce severity, score, recommendation, guest-journey rating, finding, or booking-success conclusion.

### AC-10 — No inference or demonstration

The implementation produces neither `INFERRED` nor `DEMONSTRATION` observations.

### AC-11 — Deterministic behavior

Given the same `StructuredEvidence` content and metadata, the extractor produces the same observation result. Tests must not require a live hotel website, external network, LLM, or nondeterministic model behavior.

### AC-12 — Conservative matching

Tests demonstrate that generic/context-free keyword occurrences do not automatically become hospitality observations when the surrounding content does not support the bounded observation category.

### AC-13 — Relevant hospitality signals

Deterministic tests cover representative positive observations for at least:

- hotel identity;
- rooms;
- amenities;
- contact/location;
- booking entry point;
- dining.

The tests must verify that each observation retains its supporting source evidence/reference and observed value/excerpt/link where applicable.

### AC-14 — Source content remains unchanged

Observation extraction does not mutate `StructuredEvidence`, the retained source observation, or the original acquisition result.

### AC-15 — Existing boundaries remain intact

REQ-015, REQ-017, and REQ-018 behavior and contracts remain intact. No acquisition security behavior, evaluation lifecycle behavior, or evidence normalization behavior is redesigned as part of REQ-019.

### AC-16 — Complete backend validation

The implementation session runs the repository's documented backend validation and reports the actual result. CI must pass before the implementation session marks the requirement `PR_READY`.

## OUTPUT

The implementation session must:

1. create the exact branch `feature/hospitality-observation-foundation` from the then-current `main`;
2. implement only REQ-019;
3. add/update deterministic tests required by the acceptance criteria;
4. update this SAME requirement file with implementation summary, files changed, validation, CI result, PR information, limitations, and self-review;
5. create a PR against `main`;
6. do not merge the PR;
7. stop for orchestrator review.

The implementation session must not start REQ-020 or broaden this requirement into hospitality analysis, scoring, AI, reporting, or preview generation.

## Why this is the smallest next slice

The repository now has an implemented path from canonical evaluation input through evaluation/attempt, bounded public acquisition, and structured evidence. REQ-018 intentionally stopped before HTML semantic extraction. SPEC-005 and SPEC-006 establish that downstream analysis needs evidence-backed, traceable hospitality observations, while analysis itself remains a separate boundary.

REQ-019 therefore closes only the next semantic boundary: **observed source content → bounded hospitality observation**. It provides a useful deterministic input for later analysis without prematurely defining the full hotel fact model, findings, scoring, AI, or preview model.

## Governance

This is a requirement-definition artifact only. No implementation branch is created by this session, no application code is changed, and no PR is created by this session.

STOPPING FOR ORCHESTRATOR REVIEW.
