# REQ-027 — Hospitality Deficiency Analysis Foundation

STATUS: PR_READY
REQUIREMENT_ID: REQ-027
TYPE: Implementation
BRANCH: `feature/hospitality-deficiency-analysis-foundation`

## Objective

Extend the deterministic hospitality analysis pipeline from primarily positive/observed signals to **evidence-grounded observed deficiencies and meaningful evidence gaps**.

REQ-026 established the executable path:

```text
Structured Evidence
      ↓
Hospitality Observations
      ↓
Qualified Analysis Signals
      ↓
Findings / Limitations
      ↓
Coverage
      ↓
HospitalityAnalysisResult
```

REQ-027 should make the analysis materially useful for the product question:

> Can a guest find, understand, trust, explore, and book this hotel online?

The engine must identify a problem only when the available evidence supports the problem. It must not turn missing evidence, failed acquisition, or unsupported analysis capability into a hotel deficiency.

## Product Rationale

REQ-026 can currently establish that useful hotel-facing signals were observed. That is necessary but insufficient for an owner-facing analysis product.

The next meaningful capability is to recognize cases such as:

- an observed public page has no visible booking path where a booking path is expected within the evaluated journey;
- an observed room entry point exists but the observed room content is materially insufficient for the intended guest decision;
- observed hotel information is contradictory across retained sources;
- an observed guest-facing path is materially broken or unusable;
- a relevant source is observable but the available content is insufficient for the intended analysis conclusion.

These must be represented as **analysis findings or insufficient-evidence semantics**, not invented hotel facts.

## Scope

Implement deterministic, evidence-grounded deficiency analysis over the existing REQ-026 pipeline.

The implementation may:

1. inspect existing qualified hospitality analysis signals and their supporting observations/evidence;
2. apply explicit deterministic rules to identify observed guest-facing deficiencies;
3. identify meaningful evidence gaps where the source was observed but the available content is insufficient for the intended conclusion;
4. create findings using the existing `HospitalityFinding` contract;
5. preserve the distinction between observed deficiency, insufficient evidence, and unable-to-verify limitation;
6. preserve journey-stage and analysis-dimension relationships;
7. preserve source/evidence/provenance traceability;
8. integrate the new findings into the existing `HospitalityAnalysisResult`.

The implementation should cover the currently supported hospitality capabilities rather than attempting the full nine-dimension ontology.

## Current Capability Boundary

REQ-027 may reason only from evidence and signals already supported by the current deterministic pipeline.

Current observation categories remain:

- HOTEL_IDENTITY
- ROOMS
- AMENITIES
- CONTACT
- BOOKING
- DINING

Do not add observation categories merely to manufacture deficiencies.

The full nine-dimension intended scope from SPEC-006 remains intact, but unsupported dimensions remain not currently assessable.

## Deficiency Semantics

A deficiency is valid only when the evidence establishes an observed guest-facing problem or a sufficiently explicit observed content gap.

Examples of acceptable semantics:

```text
Observed booking entry point exists, but the observed evaluated page/path
contains no usable booking action where the current deterministic rule
explicitly requires one.
```

```text
A room entry point was observed, but the retained room-detail evidence
contains no meaningful room description or decision-support information
under an explicit rule supported by the current contract.
```

```text
Two retained public sources expose materially contradictory hotel identity
or booking information, and the conflict itself is relevant to guest trust.
```

The finding must describe what was observed. It must not speculate about why the problem exists or what the hotel internally does.

## Mandatory Truth Boundary

The following transformations are prohibited:

```text
No signal found
    ≠
hotel feature absent
```

```text
Acquisition failed
    ≠
hotel feature absent
```

```text
NOT_ATTEMPTED
    ≠
hotel feature absent
```

```text
Unsupported analysis dimension
    ≠
hotel deficiency
```

```text
Could not verify booking
    ≠
booking does not work
```

A deficiency requires positive observational support from the retained evidence/context.

## Observed Deficiency vs Insufficient Evidence vs Limitation

REQ-027 must preserve three different semantics:

### Observed deficiency

The relevant source/path/content was successfully observed and an explicit deterministic rule establishes a guest-facing problem.

### Insufficient evidence

The source was sufficiently observed to know that the intended conclusion cannot responsibly be established, but the evidence is not strong enough to call the hotel experience deficient.

### Unable to verify

The relevant source/path could not be reliably observed because of an acquisition or access limitation.

These states must never be collapsed.

## Initial Rule Families

Implement only a small set of high-confidence deterministic rule families supported by current evidence.

### 1. Booking discoverability deficiency

Where a page is successfully observed and is within the current booking-relevant journey, an explicit rule may identify a missing visible booking entry point when the page/context establishes that a booking action should be present.

Do not infer a missing booking capability merely because a generic page lacks a booking keyword.

### 2. Room-information deficiency

Where a room listing/detail context is explicitly observed, an explicit rule may identify materially insufficient guest-facing room information.

Do not infer that the hotel lacks rooms because room evidence was unavailable.

### 3. Contact/location deficiency

Where a relevant contact/location context is successfully observed, an explicit rule may identify a materially missing guest-facing contact or location path.

Do not infer absence from inaccessible pages.

### 4. Guest-facing information deficiency

Where an amenities/guest-information context is successfully observed, an explicit rule may identify materially missing decision-support information when the applicable rule is explicit and testable.

### 5. Trust/conflict finding

Where retained sources materially contradict each other, the inconsistency itself may become a finding when it affects a guest's ability to understand, trust, or book.

Do not silently select one source and discard the conflict.

### 6. Broken observed journey path

Where the evidence explicitly establishes that a relevant guest-facing path is broken or unusable, create a deficiency finding tied to the affected journey stage.

A technical error or access failure that prevented observation remains a limitation unless the current evidence explicitly establishes the guest-facing path itself is broken.

These rule families are deliberately conservative. Do not expand into generic SEO auditing, accessibility scoring, performance scoring, or broad technical auditing.

## No Keyword-Only Rules

Context-free keyword absence is insufficient.

Do not implement rules such as:

```text
if page.contains("book") == false → booking deficiency
```

or:

```text
if page.contains("room") == false → no rooms
```

Rules must consider the observation category, page/source context, successful observation state, and existing signal semantics.

## Findings

Reuse the existing `HospitalityFinding` contract.

A deficiency finding should preserve:

- evaluation identity;
- affected journey stage(s);
- affected analysis dimension(s);
- concise guest-facing problem statement;
- observational basis;
- supporting evidence references;
- relevant signal/observation provenance;
- qualification/status already supported by the domain contract.

Do not create a second finding type unless the existing model is demonstrably incapable of representing the required distinction and the orchestrator approves the architectural gap.

## Severity

REQ-027 must not invent numerical severity or score formulas.

If the existing `HospitalityFinding` contract already supports qualitative importance, use only semantics that are explicitly governed by the repository.

Do not introduce new severity calibration merely to rank the new deficiencies.

## Recommendations

Do not implement recommendation generation in REQ-027.

The deficiency finding is the input for a later recommendation capability.

## Coverage

REQ-027 does not change the coverage classifier.

It may improve factual coverage inputs by producing additional findings or limitations, but it must not invent thresholds or derive `HospitalityAnalysisCoverageState`.

The full intended nine-dimension scope remains independent of current assessable dimensions.

## Provenance

Every deficiency finding must remain traceable through the existing chain:

```text
Deficiency Finding
      ↓
Qualified Signal / deterministic analysis basis
      ↓
Observation
      ↓
Structured Evidence
      ↓
Evaluation / acquisition context
```

Where the finding is based on a material source conflict, preserve the conflicting evidence references.

Do not flatten provenance into strings.

## Determinism

For identical structured evidence and identical governed inputs:

```text
same input → same deficiency findings
```

No randomness, LLM, external network, wall-clock dependency, or mutable global state.

## Multi-Evidence and Conflicts

Continue to support multiple evidence items for one evaluation.

If two sources disagree:

- do not silently overwrite one source;
- retain the conflict when material;
- only create a trust/clarity deficiency when the conflict itself is sufficiently supported and relevant;
- do not invent a universal first-party-over-third-party source hierarchy.

## Integration Boundary

REQ-027 should integrate with `HospitalityAnalysisService` or a small explicit collaborator invoked by it.

Prefer:

```text
HospitalityAnalysisService
    ↓
ObservationService
    ↓
SignalService
    ↓
DeficiencyAnalysisService
    ↓
Finding / Limitation
    ↓
Coverage
    ↓
HospitalityAnalysisResult
```

Do not create a generic rule engine, plugin architecture, strategy registry, reflection-based dispatcher, or configurable rules framework.

The deterministic rules should remain explicit, local, and testable.

## Non-Scope

Do NOT implement:

- AI/LLM analysis;
- numerical scoring;
- generic SEO auditing;
- generic accessibility audit;
- generic performance scoring;
- complete nine-dimension analysis;
- recommendation generation;
- report generation/UI/PDF;
- persistence/database schema;
- REST API;
- browser/crawler acquisition;
- acquisition expansion;
- booking/OTA integration;
- competitor analysis;
- interactive Antena preview;
- `<hotel-name>.antenapro.com` generation;
- Antena API integration;
- universal source hierarchy;
- automatic coverage classification.

## Dependencies

- SPEC-005 — Evidence Model
- SPEC-006 — Hospitality Analysis Contract
- REQ-019 — Hospitality Observation Foundation
- REQ-021 — Hospitality Analysis Signal Foundation
- REQ-022 — Hospitality Finding Foundation
- REQ-023 — Hospitality Analysis Limitation Foundation
- REQ-024 — Hospitality Analysis Coverage Foundation
- REQ-025 — Hospitality Analysis Result Foundation
- REQ-026 — Deterministic Hospitality Analysis Engine
- existing evaluation/evidence identity conventions

## Acceptance Criteria

1. REQ-026 behavior remains intact.
2. Deficiency analysis operates only on successfully retained/observable evidence and existing deterministic signals.
3. At least the supported high-confidence booking, room-information, contact/location, guest-information, conflict, and observed-path rule families are covered where current evidence contracts support them.
4. No deficiency is created solely from missing evidence, failed acquisition, NOT_ATTEMPTED, or unsupported dimensions.
5. Context-free keyword absence cannot create a hotel deficiency.
6. Observed deficiency, insufficient evidence, and unable-to-verify limitation remain distinguishable.
7. Deficiency findings retain evaluation identity and provenance traceability.
8. Material source conflicts remain visible and may produce a trust/clarity finding without silently overwriting sources.
9. Multiple evidence items remain supported.
10. Equivalent deficiency findings are deduplicated deterministically without losing distinct source context.
11. Zero deficiency findings remains valid.
12. Coverage state remains caller-governed; no threshold/classifier is introduced.
13. No new observation categories are added solely to populate deficiencies.
14. Existing full backend tests and focused REQ-027 tests pass.
15. No external network, AI, persistence, report, preview, or Antena integration is introduced.

## Testing Requirements

Tests must cover at minimum:

- observed booking context with explicit missing booking path → deficiency;
- inaccessible booking context → limitation, not deficiency;
- observed room context with explicitly insufficient room information → deficiency;
- inaccessible room context → not a deficiency;
- observed contact/location context with explicit missing guest-facing contact/location path → deficiency;
- observed amenities/guest-information context with explicit insufficient information → deficiency;
- material conflicting hotel information → trust/clarity finding with both sources preserved;
- observed broken guest-facing path → deficiency;
- NOT_ATTEMPTED → no deficiency;
- unsupported dimension → no deficiency;
- keyword-only absence → no deficiency;
- zero deficiencies → valid result;
- cross-evaluation evidence remains rejected;
- provenance remains traceable;
- deterministic repeat execution produces equivalent results;
- existing REQ-026 regression suite remains green.

## Engineering Expectations

Keep the implementation simple and explicit.

Prefer a small `HospitalityDeficiencyAnalysisService` or equivalent collaborator if that matches the existing code structure.

Do not introduce a general-purpose rules framework.

Rules should be readable enough that a future engineer can understand exactly why a finding was produced.

Before adding an abstraction, ask whether a small private method or explicit rule object is sufficient.

## Security / Reliability

Treat structured evidence and retained public content as untrusted data.

Do not execute HTML, scripts, expressions, URLs, or external content.

Do not perform new network calls.

Do not swallow analysis errors or silently reinterpret infrastructure failures as hotel deficiencies.

## Requirement Governance

Implementation sessions must:

1. inspect actual current `main` first;
2. read this requirement and all dependency contracts before coding;
3. stop if a required semantic distinction cannot be implemented without inventing a product rule;
4. update this same requirement with implementation/validation evidence;
5. create a PR against `main`;
6. stop for orchestrator review;
7. never merge their own PR.

`READY` becomes `PR_READY` only after implementation and final-head CI validation.

**STOPPING FOR IMPLEMENTATION SESSION.**

## Implementation Record — Session 27

### Context reconciliation

Inspected the current `main` contracts for REQ-026 and the existing observation, signal, finding, limitation, coverage, result, structured-evidence, evaluation/provenance, and test layers before implementation. REQ-026 is present on `main`; no upstream contract was changed.

The current observation categories remain `HOTEL_IDENTITY`, `ROOMS`, `AMENITIES`, `CONTACT`, `BOOKING`, and `DINING`. The current finding model supports traceability through an originating qualified signal and retains the existing governed finding-status vocabulary.

The current evidence model does not expose a governed `NOT_ATTEMPTED` state or an explicit successful broken-path observation contract. Those cases therefore remain conservative boundaries rather than being manufactured by REQ-027.

### Implementation summary

Added `HospitalityDeficiencyAnalysisService` as the explicit deterministic collaborator invoked by `HospitalityAnalysisService` after observation and signal qualification.

The service operates only on qualified signals whose retained observations are discovered and backed by successful observed content. Rules are page-context aware and do not use generic keyword absence as a deficiency rule.

Implemented supported high-confidence rule families for:

- room-context booking discoverability: a successfully observed room journey page with no observed booking signal produces a booking-action deficiency;
- room information: an observed room context without deterministic room decision-support indicators produces a room-information deficiency;
- contact/location: an observed contact/location context without usable contact/location details produces a deficiency;
- guest-facing information: an observed amenities/guest-information context without deterministic stay-related decision-support details produces a deficiency;
- materially conflicting hotel identity information across retained sources produces trust/clarity findings while retaining each source-specific provenance chain.

No recommendation, severity, score, coverage classifier, acquisition, observation category, AI, network, persistence, API, or preview capability was added.

### Files changed

- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityDeficiencyAnalysisService.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisService.java`
- `backend/src/test/java/com/antenapro/hotelcheck/analysis/HospitalityDeficiencyAnalysisServiceTest.java`
- `requirements/REQ-027-hospitality-deficiency-analysis-foundation.md`

### Tests

Focused REQ-027 coverage includes:

- observed room context without booking action → booking deficiency;
- observed room context without decision-support information → room-information deficiency;
- observed contact context without guest-facing detail → contact/location deficiency;
- observed amenities context without stay-related detail → guest-information deficiency;
- materially conflicting hotel identity sources → trust/clarity findings with both supporting evidence items retained;
- generic successful page with no hospitality context → zero deficiency findings;
- acquisition timeout → limitation and no deficiency;
- unsupported acquisition → no deficiency;
- deterministic repeated execution → equivalent results.

GitHub Actions Backend Validation executed Maven tests on the final PR head. The final run reported **158 tests, 0 failures, 0 errors, 0 skipped** and **BUILD SUCCESS**. The focused `HospitalityDeficiencyAnalysisServiceTest` ran **8 tests**, all passing. Existing REQ-026 and earlier tests remained green.

### CI

**PASS** — GitHub Actions `Backend Validation`, run **#205**, workflow run ID `37201270103`.

Validated PR head: `19e965142ce925785a890b83e0646ec9c5efb2de`.

The PR merge ref used by GitHub Actions was `36beb94e86a74efbfbb56329a5fc8cb48523f78c`, whose head corresponds to the final PR head above.

### Architectural decisions

1. A small `HospitalityDeficiencyAnalysisService` was used instead of a generic rule engine, registry, plugin model, or reflection dispatcher.
2. The existing `HospitalityFinding` model was reused. Deficiency findings retain the existing signal → observation → evidence → evaluation provenance chain.
3. No upstream acquisition, evidence normalization, observation, signal, limitation, coverage, or result contracts were changed.
4. Conflict handling preserves source-specific findings rather than silently selecting a preferred source hierarchy. Each conflict finding remains traceable to its retained source evidence.
5. Rules use evaluated page/path context plus existing observation categories and successful evidence state. They do not treat a missing keyword on a generic page as proof of a hotel deficiency.

### Truth-boundary decisions

- Missing evidence is never converted to a deficiency.
- Acquisition/access failure remains a limitation or produces no deficiency according to the existing limitation contract.
- Unsupported dimensions remain unsupported.
- Booking unavailability is not inferred from inability to access a booking engine.
- Booking-success semantics are not introduced.
- Room availability/inventory is not inferred.
- No new `NOT_ATTEMPTED` semantics were invented because the current evidence contract does not expose that state.
- Broken-path findings were not fabricated because the current successful observation contract does not explicitly encode a verified broken guest-facing path. Access failure remains distinct from a broken path.
- No source hierarchy was invented for conflicting retained information.

### Limitations

- The current deterministic evidence/observation contracts do not provide an explicit verified broken-path state, so the broken observed-path family is not synthesized in this requirement.
- The current contract does not expose a governed `NOT_ATTEMPTED` state, so no new semantics were added solely for this requirement.
- Conflict findings are represented as source-specific `HospitalityFinding` instances because the existing finding contract exposes a single supporting-evidence chain per finding; both source-specific findings are retained and traceable. No parallel deficiency object was introduced.
- Insufficient-evidence status vocabulary already exists in the finding model, but REQ-027 does not fabricate that status from unsupported upstream inputs.

### Self-review

#### Scope

Only REQ-027 behavior was added. No acquisition, evidence normalization, observation extraction, signal qualification, coverage classification, result aggregation, recommendation, reporting, preview, or Antena integration was introduced.

#### Truthfulness

All deficiency rules require successful observed evidence plus explicit page/source context. Generic keyword absence, missing evidence, acquisition failure, unsupported dimensions, and inability to verify are not treated as hotel deficiencies.

#### Context

Booking, room, contact/location, and guest-information rules require explicit evaluated page/path context and the corresponding existing observation category.

#### Provenance

Every deficiency finding is derived from an existing qualified signal and therefore remains traceable through observation, structured evidence, and evaluation/attempt attribution.

#### Evaluation

The existing `HospitalityAnalysisService` evaluation-integrity validation remains unchanged, and deficiency analysis consumes only the validated evidence/signal set for the requested evaluation.

#### Conflicts

Conflicting hotel identity values from distinct retained sources are preserved as source-specific findings. No first-party-over-third-party hierarchy was introduced.

#### Limitations

Acquisition failures continue through the existing limitation path. No infrastructure error is reinterpreted as a hotel-facing deficiency.

#### Unsupported scope

No new observation categories were added, and unsupported dimensions cannot generate deficiencies because the deficiency service only operates on existing qualified signals.

#### Simplicity

Rules remain local private methods with explicit deterministic conditions. No configurable or reflective rule framework was introduced.

### Governance

`STATUS: PR_READY` — implementation is complete, final-head GitHub Actions Backend Validation passed, and PR #27 remains open and unmerged for orchestrator review.

**STOPPING FOR ORCHESTRATOR REVIEW.**
