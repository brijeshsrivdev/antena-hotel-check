# REQ-029 — Guest Journey Analysis Foundation

STATUS: READY
REQUIREMENT_ID: REQ-029
TYPE: Implementation
BRANCH: `feature/guest-journey-analysis-foundation`

## Objective

Introduce the first explicit hospitality guest-journey representation over the existing deterministic analysis outputs.

The product question established by SPEC-006 is:

> Can a guest find, understand, trust, explore, and book this hotel online?

REQ-029 creates a small, deterministic journey-analysis layer that explains how existing evidence-grounded findings and limitations affect the established journey stages:

```text
DISCOVER → UNDERSTAND → EXPLORE → TRUST → BOOK
```

The purpose is to move from a collection of independent findings toward a traceable explanation of **where the guest journey is affected**, without introducing scoring, recommendations, AI, or customer-facing report UI.

## Repository Baseline

REQ-027 established evidence-grounded deficiency analysis.

REQ-028 established the first conservative completeness expansion and made `TRUST_AND_CLARITY` assessable only where existing typed evidence establishes a material same-evaluation cross-source identity conflict.

Current analysis boundary:

```text
StructuredEvidence
      ↓
HospitalityObservation
      ↓
Qualified HospitalityAnalysisSignal
      ↓
HospitalityFinding / Deficiency / Limitation
      ↓
HospitalityAnalysisCoverage
      ↓
HospitalityAnalysisResult
      ↓
REQ-029 Guest Journey Analysis
```

REQ-029 must consume existing analysis outputs. It must not redesign upstream acquisition, evidence, observation, signal, finding, deficiency, limitation, coverage, or result contracts.

## Journey Contract

SPEC-006 defines these journey stages:

1. `DISCOVER`
2. `UNDERSTAND`
3. `EXPLORE`
4. `TRUST`
5. `BOOK`

The journey is a product lens, not a requirement that every finding map to exactly one stage.

A finding may affect multiple stages where the existing finding semantics justify that relationship.

## Scope

Implement the smallest deterministic domain capability that:

- represents guest-journey impact of existing analysis findings;
- preserves the relationship between journey impact and the originating finding;
- preserves evidence/provenance through the existing finding chain;
- represents journey limitations when the underlying analysis is limited or unable to verify;
- distinguishes unsupported journey conclusions from observed journey impact;
- integrates with the existing `HospitalityAnalysisResult` boundary only where the current result contract can safely support it.

The exact implementation shape must be determined from the actual repository contracts.

## Journey Semantics

Journey-stage assignment must be evidence/analysis grounded.

It must not be based solely on generic keyword matching in finding text.

Where an existing finding category, dimension, or typed analysis property already establishes journey relevance, reuse that contract.

If the current finding model does not contain sufficient typed information to establish a journey-stage relationship, the implementation must use the smallest explicit deterministic mapping justified by existing governed semantics. Do not introduce a generic rule engine.

## Stage Intent

### DISCOVER

Whether a prospective guest can locate the hotel and reach important guest-facing information.

Potential governed inputs include existing identity, contact/location, navigation/discoverability, and relevant technical-access findings where supported.

### UNDERSTAND

Whether the guest can understand what the property is, where it is, what it offers, and important information needed before considering a stay.

Potential governed inputs include identity, rooms, amenities, guest-facing information, contact/location, and trust/clarity findings.

### EXPLORE

Whether the guest can meaningfully explore rooms and relevant aspects of the stay.

Potential governed inputs include rooms, amenities, dining, location, and relevant navigation findings.

### TRUST

Whether information is sufficiently clear, consistent, attributable, and transparent for the guest to continue toward booking.

Potential governed inputs include trust/clarity findings, material conflicts, important information gaps, and transparent contact/booking-path findings.

### BOOK

Whether the guest can discover and reach a relevant booking journey where technically possible.

Potential governed inputs include existing booking observations/findings and explicit booking-path limitations.

A booking limitation must not be represented as a failed booking capability when the booking path could not be reliably observed.

## Truth Boundary

The following rules are mandatory:

```text
Missing evidence              ≠ journey failure
Acquisition failure           ≠ journey failure
NOT_ATTEMPTED                 ≠ journey failure
Unsupported dimension         ≠ journey failure
Unable to verify              ≠ journey failure
Unrecognized observation      ≠ journey failure
No journey mapping available  ≠ journey failure
```

A journey stage may be marked as limited/insufficient only when the existing analysis explicitly provides the corresponding limitation semantics.

Do not infer a broken journey merely because no finding exists.

Do not infer that a stage is successful merely because no deficiency was found.

## Important Distinction

REQ-029 must support at least these conceptual states where the existing analysis can justify them:

### Observed impact

An existing evidence-grounded finding materially affects the journey stage.

### Limitation

The underlying analysis cannot reliably establish the relevant journey condition.

### Unsupported

The current analysis contracts do not provide enough governed information to assess the journey stage.

### No observed issue

The current evidence/analysis contains no governed deficiency for the stage, but this must not be presented as exhaustive proof that the stage is perfect.

The exact representation may follow existing repository result/value conventions.

## Findings and Provenance

Every journey impact must remain traceable to its source finding where an impact is derived from a finding.

The expected conceptual chain is:

```text
Journey Impact
      ↓
Hospitality Finding / Limitation
      ↓
Hospitality Analysis Signal
      ↓
Hospitality Observation
      ↓
StructuredEvidence
      ↓
Evaluation / Attempt
```

Do not duplicate evidence or create a second provenance model.

A journey impact must not be detached from the evaluation that produced the source finding.

## Evaluation Isolation

Evidence and findings from evaluation A must never produce journey impacts for evaluation B.

Journey analysis must operate on a single evaluation's existing analysis result/input set.

## Coverage Interaction

REQ-029 must preserve existing coverage semantics.

Do not introduce:

- journey scores;
- percentage completion;
- weighted stage scores;
- new coverage formulas;
- arbitrary thresholds.

A stage that cannot be responsibly assessed should remain limited/unsupported according to existing semantics.

Overall analysis coverage must not be inflated merely because a journey object was created.

## No Scoring

Do not introduce a numerical or ordinal guest-journey score.

Do not produce statements such as:

```text
Booking = 82/100
Trust = 60/100
Overall Journey = 71/100
```

No such concept is authorized by this requirement.

## No Prioritization

Do not rank findings or stages by business value, severity, conversion value, or any new prioritization model.

Finding prioritization is a later capability.

## No Recommendations

Do not generate recommendations.

A journey impact explains the existing analysis; it does not prescribe an action.

Recommendations are a later requirement.

## No AI

No LLM, model provider, prompt, semantic classifier, or AI-generated journey interpretation.

REQ-029 remains deterministic.

## No External Integrations

Do not implement or call:

- Google Business Profile;
- Google Search Console;
- GA4;
- Google Places;
- OTA APIs;
- booking providers;
- external analytics APIs.

Connected digital performance is a future plan-gated capability documented in the roadmap.

## No Acquisition Changes

Do not introduce:

- new crawler behavior;
- browser automation;
- network access;
- target discovery;
- acquisition retries;
- acquisition schema changes.

REQ-029 consumes existing analysis output only.

## No Report UI

Do not build:

- report pages;
- frontend components;
- dashboard UI;
- PDF generation;
- public report rendering.

The journey model is a backend/domain boundary that future report consumers can use.

## No Antena Integration

Do not generate or host `<hotel-name>.antenapro.com`.

Do not map findings to Antena products in this requirement.

Antena conversion opportunity remains downstream after analysis maturity.

## Hospitality-First Boundary

The journey model must remain organized around hospitality guest experience.

Technical/SEO observations may affect a journey stage only where existing analysis already establishes meaningful guest-facing relevance.

Do not turn REQ-029 into a generic website-audit journey model.

## Determinism

For identical analysis input:

```text
same HospitalityAnalysisResult
        ↓
same GuestJourneyAnalysis
```

No randomness.

No current-time dependency.

No external calls.

No mutable global state.

## Backward Compatibility

Existing REQ-027 and REQ-028 behavior must remain unchanged.

In particular:

```text
successful room evidence + no BOOKING signal
        ↓
no booking deficiency
```

REQ-029 must not reinterpret the absence of a booking observation as booking failure.

Likewise, absence of a deficiency must not be converted into a positive claim that a journey stage is fully successful.

## Testing Acceptance Criteria

Automated tests must demonstrate at least:

1. Existing hospitality findings can be mapped to the appropriate journey stage(s) using governed deterministic semantics.
2. A finding can affect more than one journey stage when the existing semantics justify it.
3. A limitation/insufficient-evidence condition remains distinguishable from a journey failure.
4. Missing findings do not automatically become journey failures.
5. Unsupported dimensions do not become supported journey stages merely because the stage exists in the journey model.
6. Booking absence remains non-deficiency and does not become a BOOK-stage failure.
7. Journey impacts preserve source finding/evaluation traceability.
8. Evidence from evaluation A cannot create journey impacts for evaluation B.
9. Existing REQ-027 deficiencies remain unchanged.
10. Existing REQ-028 trust/clarity behavior remains unchanged.
11. Identical input produces identical journey output.
12. No scoring or prioritization is present.
13. No recommendations are produced.
14. No AI or external network dependency is required.
15. Existing backend tests continue to pass.

## Architectural Guidance

Prefer a small explicit journey domain representation over a generic rules framework.

Do not introduce:

- reflection-based mappings;
- dynamic rule registries;
- configurable scoring engines;
- generic workflow engines;
- plugin systems;
- large hospitality ontology frameworks.

The implementation should be understandable from the SPEC-006 journey semantics and existing analysis contracts.

## Requirement Update

Before `PR_READY`, update this same file with:

- repository/context reconciliation;
- existing journey-related contracts inspected;
- implementation summary;
- journey-stage mapping decisions;
- truth-boundary decisions;
- files changed;
- tests;
- validation;
- architectural decisions;
- limitations;
- self-review;
- final-head CI result.

Only change:

`STATUS: PR_READY`

after implementation and final-head CI validation succeed.

## Required Validation

Before marking `PR_READY`:

1. Run focused REQ-029 tests.
2. Run the complete backend Maven test suite.
3. Run GitHub Actions Backend Validation against the exact final PR head.
4. Verify no unrelated application changes were introduced.
5. Verify the requirement file contains the final validation result.

Do not claim final CI success until the final PR head has passed.

## Expected Session Outcome

The implementation session must:

- create branch `feature/guest-journey-analysis-foundation` from current `main`;
- implement only REQ-029;
- add appropriate tests;
- update this requirement to `PR_READY` only after final validation;
- create a PR against `main`;
- stop for orchestrator review.

The session must not merge the PR or begin REQ-030.
