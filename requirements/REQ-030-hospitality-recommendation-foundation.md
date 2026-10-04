# REQ-030 — Hospitality Recommendation Foundation

STATUS: IMPLEMENTED_PENDING_VALIDATION
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

## Implementation Record — Session 30

### Repository/context reconciliation

Current `main` at session start: `d0bdb0fa62e331a2be9f8a72efb94f1df5e0471b`.

Confirmed from repository state:

- REQ-027 is merged as PR #27.
- REQ-028 is merged as PR #28.
- REQ-029 is merged as PR #29.
- REQ-030 exists on `main` with READY status and requirement commit `8014f57a5f6844e4da33c2d13fa6c8dc5f6c995a`.
- The current analysis code exposes typed findings/deficiencies, `HospitalityAnalysisResult`, `GuestJourneyAnalysis`, typed journey stages, and evaluation-boundary validation required by REQ-030.

The roadmap contains an older stale statement describing REQ-027 as in progress, but the current-state/next-steps records and actual merged PRs establish the newer repository state. No product decision was inferred from that stale roadmap text.

### Inspected contracts

Inspected the REQ-030 requirement, orchestrator/current-state/next-step/decision/handoff context, evidence and hospitality-analysis specifications, engineering/testing guidance, and the implemented finding, limitation, analysis-result, deficiency, and guest-journey contracts on `main`.

The relevant implemented semantics are:

- only `DEFICIENCY` findings are guest-journey observed impacts;
- finding provenance reaches observation/evidence/evaluation through the retained source chain;
- identity conflicts are typed by existing `HospitalityFinding.isIdentityConflictWith(...)` semantics;
- `GuestJourneyAnalysis` enforces a single evaluation boundary and retains stage-specific observed impacts/limitations;
- `UNABLE_TO_VERIFY` is the only current limitation type and has no explicit recommendation-supporting subtype.

### Implementation summary

Implemented a small deterministic recommendation domain:

- `RecommendationCategory` — explicit six-category bounded vocabulary from REQ-030;
- `HospitalityRecommendation` — immutable recommendation record retaining evaluation identity, category, bounded action, journey stages, and exactly one source finding/limitation;
- `HospitalityRecommendationService` — deterministic typed mapping over one `GuestJourneyAnalysis` boundary.

Current mappings:

- `BOOKING` deficiency → `IMPROVE_BOOKING_DISCOVERABILITY`;
- `ROOMS` deficiency → `IMPROVE_ROOM_INFORMATION`;
- `AMENITIES` / `DINING` deficiency → `IMPROVE_GUEST_FACING_INFORMATION`;
- `CONTACT` deficiency → `IMPROVE_CONTACT_LOCATION_INFORMATION`;
- typed cross-source `HOTEL_IDENTITY` conflict → `RESOLVE_INFORMATION_CONFLICT`;
- unsupported/non-conflict deficiency families → no recommendation;
- current `UNABLE_TO_VERIFY` limitations → no recommendation because the repository has no explicit bounded limitation semantic authorizing one.

No human-readable finding text is parsed. Identity-conflict mapping uses the existing typed conflict method. Journey stages are taken from existing `GuestJourneyAnalysis` stage impacts, including any governed multi-stage impact already represented there.

### Truth-boundary decisions

- No recommendation is created from missing evidence, absent findings, unsupported stages, or limitations alone.
- No booking recommendation is created from absence of a BOOKING observation; only an existing governed BOOKING deficiency could map to the booking category.
- Acquisition failure is represented only through existing journey limitations and produces no hotel-feature recommendation.
- No `NOT_ATTEMPTED` semantics were invented.
- Unable-to-verify limitations remain limitations because the current limitation contract does not explicitly authorize a bounded improvement recommendation.
- Recommendations contain source finding/limitation references rather than duplicating evidence.
- No recommendation text claims completion, conversion, revenue, or other unsupported business outcomes.

### Architectural decisions

The implementation follows the requirement's preferred shape: one immutable recommendation record, one explicit enum, and one small deterministic mapping service. It does not introduce a generic rule engine, registry, reflection, scoring, AI abstraction, network access, persistence, API, UI, or Antena integration.

The service accepts a single `GuestJourneyAnalysis` rather than combining independent evaluations. This uses the existing evaluation-bound journey boundary and prevents cross-evaluation leakage by construction. Source findings remain the existing finding objects, so provenance continues through the existing `finding → signal → observation → StructuredEvidence → evaluation/attempt` chain.

### Files changed

- `backend/src/main/java/com/antenapro/hotelcheck/analysis/RecommendationCategory.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityRecommendation.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityRecommendationService.java`
- `backend/src/test/java/com/antenapro/hotelcheck/analysis/HospitalityRecommendationServiceTest.java`
- `requirements/REQ-030-hospitality-recommendation-foundation.md`

### Tests added

Focused deterministic tests cover:

- each currently supported typed deficiency family;
- typed identity-conflict mapping without finding-text parsing;
- unsupported deficiency → no speculative recommendation;
- no BOOKING observation → no booking recommendation;
- limitation-only input → no recommendation;
- preservation of existing journey-stage impact and source finding provenance;
- evaluation A/B isolation;
- deterministic repeatability;
- non-deficiency findings → no recommendation;
- existing `GuestJourneyAnalysis` cross-evaluation guard.

### Validation

Local Maven execution is not available in this environment because direct repository cloning cannot resolve `github.com`; this limitation was observed before claiming local test execution. Backend Validation on the exact final PR head is required before changing this record to `PR_READY`.

### CI

Pending final PR head validation.

### PR

Branch created from current `main` as required:

`feature/hospitality-recommendation-foundation`

PR will target `main` and will remain unmerged for orchestrator review.

### Limitations

- The current repository has no explicit limitation subtype that authorizes a recommendation, so `UNABLE_TO_VERIFY` limitations are conservatively preserved without recommendations.
- `IMPROVE_TRUST_CLARITY` remains part of the bounded category vocabulary but is not emitted because no current typed deficiency semantics justify a separate trust-clarity action beyond the existing identity-conflict mapping.
- The current merged analysis foundation does not produce a governed BOOKING deficiency from a missing BOOKING signal; the booking mapping therefore remains a guarded downstream capability for any future explicit governed booking deficiency.

### Self-review

#### Scope
Only REQ-030 recommendation-domain behavior and focused tests were added. No acquisition, evidence, observation, signal, finding, limitation, coverage, journey, UI, integration, or Antena behavior was changed.

#### Correctness
Mapping is based on typed observation category/finding kind/status and existing identity-conflict semantics, never on human-readable text. Journey impact is read from the existing journey analysis.

#### Determinism
No clock, randomness, mutable global state, AI, or network dependency is introduced. Identical input produces equal recommendation sets.

#### Provenance / isolation
Each recommendation retains the source finding or limitation and evaluation identity. The service consumes one evaluation-bound `GuestJourneyAnalysis` and rejects out-of-bound findings through the existing journey contract.

#### Security / production
The implementation performs no network access, persistence, external calls, or unbounded processing. It operates only on already-produced in-memory analysis objects.

#### Testing
Focused tests were added for the material truth boundaries and evaluation isolation. Repository-level CI is still required before PR_READY.

STATUS remains `IMPLEMENTED_PENDING_VALIDATION` until the exact final PR head has passed Backend Validation.
