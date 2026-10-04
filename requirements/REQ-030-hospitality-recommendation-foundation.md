# REQ-030 — Hospitality Recommendation Foundation

STATUS: READY
REQUIREMENT_ID: REQ-030
TYPE: Implementation
BRANCH: `feature/hospitality-recommendation-foundation`

## Objective

Introduce the first deterministic, evidence-grounded recommendation layer over the completed hospitality analysis and guest-journey analysis foundations.

The product should move from:

```text
What is observed?
What is deficient?
Where does it affect the guest journey?
```

toward:

```text
What should the hotel improve next?
```

REQ-030 must produce recommendations only from existing governed findings, deficiencies, limitations, and guest-journey impacts. It must not invent hotel facts, introduce generic advice unrelated to evidence, or become an optimization/scoring engine.

## Repository Baseline

REQ-029 establishes the first explicit guest-journey representation:

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
GuestJourneyAnalysis
```

REQ-030 consumes this existing analysis output. It must not redesign acquisition, evidence, observations, signals, findings, deficiencies, limitations, coverage, result, or journey contracts.

## Scope

Implement a small deterministic recommendation domain that:

- derives recommendations from existing evidence-grounded deficiencies;
- may use existing journey-stage impact to explain why a recommendation matters;
- preserves traceability to the source finding/limitation;
- distinguishes actionable deficiency recommendations from evidence limitations;
- provides a bounded recommendation category/action rather than arbitrary prose generation;
- remains hospitality-specific;
- is deterministic for identical analysis input.

The exact implementation shape must be determined from the current repository contracts.

## Recommendation Truth Boundary

A recommendation is an action suggested by an observed, governed problem. It is not a newly discovered fact.

Mandatory rules:

```text
Missing evidence              ≠ recommendation to fix a hotel feature
Acquisition failure           ≠ proof that a hotel feature is broken
NOT_ATTEMPTED                 ≠ recommendation to add/remove a feature
Unsupported dimension         ≠ recommendation
Unable to verify              ≠ observed deficiency
No finding                    ≠ recommendation
```

A limitation may justify a bounded recommendation such as improving observability or making information verifiable only if an existing governed limitation explicitly supports that action. Do not infer a hotel-specific defect from inability to verify.

## Recommendation Semantics

Each recommendation should have, at minimum, enough typed information to explain:

- the affected hospitality dimension;
- the affected guest-journey stage(s), when available;
- the source finding or governed limitation;
- a bounded action category;
- a concise action statement derived from the known issue.

Recommendations must not claim that the suggested action has already been completed or that it will definitely improve conversion/revenue.

## Bounded Action Categories

Use a small explicit deterministic set. The implementation may refine these only where the repository evidence requires it.

Initial categories may include:

- `IMPROVE_BOOKING_DISCOVERABILITY`
- `IMPROVE_ROOM_INFORMATION`
- `IMPROVE_GUEST_FACING_INFORMATION`
- `IMPROVE_CONTACT_LOCATION_INFORMATION`
- `RESOLVE_INFORMATION_CONFLICT`
- `IMPROVE_TRUST_CLARITY`

Do not introduce a large configurable recommendation taxonomy.

If an existing deficiency cannot be mapped responsibly to a bounded action, preserve the source deficiency without creating a speculative recommendation.

## Hospitality-First Boundary

Recommendations must remain focused on the hotel guest experience.

Technical, SEO, accessibility, structured-data, or performance recommendations may be created only when an existing governed finding establishes a meaningful guest-facing issue and provides enough typed information to support a bounded action.

Do not turn REQ-030 into:

- a generic SEO checklist;
- a generic website audit recommendation engine;
- a marketing content generator;
- a competitor recommendation engine.

## Journey Relationship

Use the existing `GuestJourneyAnalysis` and typed journey stages where available.

Do not create a second journey model.

A recommendation may affect multiple stages when the source finding already does.

Do not infer journey impact from recommendation wording.

## Priority / Scoring Boundary

REQ-030 must NOT introduce:

- numerical scores;
- weighted recommendation scores;
- ROI estimates;
- conversion estimates;
- severity rankings;
- business-value rankings;
- automatic priority tiers;
- arbitrary formulas.

Recommendation ordering must remain unspecified unless an existing governed contract already establishes ordering.

If deterministic output ordering is needed for stable tests/API behavior, use a technical stable ordering such as typed category then source identifier, not a business priority claim.

## No AI

No LLM.

No prompt-based recommendation generation.

No model provider.

No generative copy engine.

Recommendations must be deterministic and derived from typed existing analysis semantics.

## No External Integrations

Do not implement or call:

- Google Business Profile;
- Google Search Console;
- GA4;
- Google Places;
- OTA APIs;
- booking providers;
- external analytics APIs.

Connected digital-performance data remains a future plan-gated capability.

## No Network / Acquisition Changes

Do not add:

- crawling;
- browser automation;
- HTTP access;
- target discovery;
- acquisition retries;
- acquisition schema changes.

REQ-030 consumes existing analysis output only.

## No Customer-Facing Report UI

Do not build:

- report pages;
- dashboard UI;
- PDF output;
- public report rendering;
- email generation.

This requirement establishes a backend/domain recommendation boundary for future consumers.

## No Antena Integration

Do not generate or host `<hotel-name>.antenapro.com`.

Do not create Antena product/package recommendations.

The Antena-hosted hotel experience remains downstream after analysis maturity.

## Provenance

Every recommendation must remain traceable:

```text
Recommendation
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

Do not duplicate evidence.

Do not create a second provenance model.

Do not mix recommendations from different evaluations.

## Evaluation Isolation

Recommendations for evaluation A must never contain findings, limitations, evidence, or journey impacts from evaluation B.

The recommendation service must operate on one evaluation's analysis result/journey analysis boundary.

## Determinism

For identical analysis input:

```text
same HospitalityAnalysisResult
      +
same GuestJourneyAnalysis
        ↓
same recommendations
```

No randomness.

No current-time dependency.

No mutable global state.

No external dependency.

## Recommendation Text

Recommendation text must be concise and deterministic.

Do not generate long-form AI-style prose.

Prefer bounded action statements such as:

- improve room information;
- make the booking entry point easier to discover;
- resolve conflicting hotel identity information;
- improve guest-facing information;
- make contact/location information clearer.

The text must not claim facts not present in the source finding.

## Booking Truth Boundary

Preserve the REQ-027/REQ-029 rule:

```text
successful room evidence + no BOOKING signal
        ↓
no booking deficiency
        ↓
no booking recommendation
```

Absence of a booking signal must never create a recommendation to add/fix booking.

A booking recommendation requires an existing governed booking deficiency or equivalent explicit limitation that supports the action.

## Limitations

Do not convert every limitation into an action.

A limitation should produce a recommendation only when an explicit, bounded improvement to verifiability or guest-facing clarity is supported by the limitation semantics.

Otherwise preserve the limitation without recommendation.

## Testing Acceptance Criteria

Automated tests must demonstrate at least:

1. Each supported deficiency family maps to the appropriate bounded recommendation category.
2. Recommendation retains source finding/evaluation provenance.
3. Journey stages are preserved from the source finding/guest-journey analysis where available.
4. Missing evidence does not create a recommendation.
5. Acquisition failure does not create a hotel-feature recommendation.
6. `NOT_ATTEMPTED` does not create a recommendation.
7. Unsupported dimensions do not create recommendations.
8. Unable-to-verify limitations do not automatically create hotel-defect recommendations.
9. Booking absence does not create a booking recommendation.
10. Evaluation A cannot leak recommendations into evaluation B.
11. Identical input produces identical recommendations.
12. No numerical scoring or prioritization exists.
13. No AI or network dependency exists.
14. Existing REQ-027, REQ-028, and REQ-029 behavior remains unchanged.
15. Complete backend tests pass.

## Architectural Guidance

Prefer a small explicit recommendation record, bounded enum/category, and deterministic mapping service.

Do not introduce:

- generic rule engines;
- dynamic recommendation registries;
- reflection-based mapping;
- configurable scoring systems;
- workflow engines;
- large hospitality ontology frameworks;
- AI abstractions.

The implementation should be understandable directly from the existing deficiency and journey contracts.

## Completion Requirements

Before marking this requirement `PR_READY`:

- inspect current `main` and all referenced requirements/specifications;
- implement only REQ-030;
- add focused deterministic tests;
- run the complete backend Maven test suite;
- run Backend Validation against the exact final PR head;
- update this same requirement file with implementation, tests, validation, architectural decisions, truth-boundary decisions, limitations, and self-review;
- ensure the final requirement-file update is itself included in a successfully validated final PR head.

Do not merge.
Do not start REQ-031.
