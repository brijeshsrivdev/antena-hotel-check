# REQ-030 — Hospitality Recommendation Foundation

STATUS: PR_READY
REQUIREMENT_ID: REQ-030
TYPE: Implementation
BRANCH: `feature/hospitality-recommendation-foundation`

## Objective

Introduce the first deterministic, evidence-grounded recommendation layer over the completed hospitality analysis and guest-journey analysis foundations.

REQ-030 produces recommendations only from existing governed findings, deficiencies, limitations, and guest-journey impacts. It must not invent hotel facts, introduce generic advice unrelated to evidence, or become an optimization/scoring engine.

## Repository Baseline

REQ-030 consumes the existing pipeline:

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
      ↓
Bounded Hospitality Recommendations
```

It must not redesign acquisition, evidence, observations, signals, findings, deficiencies, limitations, coverage, result, or journey contracts.

## Scope

Implement a small deterministic recommendation domain that:

- derives recommendations from existing evidence-grounded deficiencies;
- uses existing journey-stage impact where available;
- preserves traceability to the source finding/limitation;
- distinguishes actionable deficiency recommendations from evidence limitations;
- provides bounded category/action values rather than arbitrary prose;
- remains hospitality-specific;
- is deterministic for identical analysis input.

## Recommendation Truth Boundary

A recommendation is an action derived from an existing governed problem, not a newly discovered hotel fact.

```text
Missing evidence              ≠ recommendation to fix a hotel feature
Acquisition failure           ≠ proof that a hotel feature is broken
NOT_ATTEMPTED                 ≠ recommendation to add/remove a feature
Unsupported dimension         ≠ recommendation
Unable to verify              ≠ observed deficiency
No finding                    ≠ recommendation
```

A limitation may justify an action only when existing governed limitation semantics explicitly support that bounded action. Current `UNABLE_TO_VERIFY` semantics do not, so they remain recommendations-free.

## Bounded Action Categories

Use this small explicit deterministic vocabulary:

- `IMPROVE_BOOKING_DISCOVERABILITY`
- `IMPROVE_ROOM_INFORMATION`
- `IMPROVE_GUEST_FACING_INFORMATION`
- `IMPROVE_CONTACT_LOCATION_INFORMATION`
- `RESOLVE_INFORMATION_CONFLICT`
- `IMPROVE_TRUST_CLARITY`

Do not introduce a large configurable taxonomy. Unsupported deficiency semantics produce no speculative recommendation.

## Hospitality-First Boundary

Recommendations address the hospitality guest experience. Technical, SEO, accessibility, structured-data, or performance recommendations are allowed only when an existing governed finding establishes a meaningful guest-facing issue and supports a bounded action.

Do not turn REQ-030 into a generic SEO checker, generic website recommendation engine, marketing generator, or competitor engine.

## Journey Relationship

Use the existing `GuestJourneyAnalysis` and `GuestJourneyStage` semantics. Do not create a second journey model. A recommendation may affect multiple stages when the source journey analysis already represents those impacts. Never infer journey impact from recommendation wording.

## Priority / Scoring Boundary

Do not introduce numerical scores, severity scores, weighted recommendations, ROI/conversion estimates, business-value rankings, priority tiers, or recommendation ranking. Any technical stable ordering must not represent business priority.

## No AI / Network / External Integrations

No LLM, prompt, model provider, generative recommendation text, network access, crawling, browser automation, target discovery, acquisition changes, Google integrations, OTA APIs, booking APIs, analytics APIs, UI/report/PDF/email, Antena preview generation, or Antena package mapping.

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

Do not duplicate the evidence model or create a second provenance framework. Do not mix evaluations.

## Evaluation Isolation

The recommendation service operates within one evaluation's existing `GuestJourneyAnalysis` boundary. Evaluation A must never leak findings, limitations, evidence, or journey impacts into evaluation B.

## Determinism

For identical `HospitalityAnalysisResult` + `GuestJourneyAnalysis`, output must be identical. No randomness, current-time dependency, mutable global state, network, or AI.

## Booking Truth Boundary

```text
successful room evidence + no BOOKING signal
        ↓
no booking deficiency
        ↓
no booking recommendation
```

Absence of a BOOKING signal never creates a booking recommendation. A booking recommendation requires an existing governed BOOKING deficiency or equivalent explicit limitation supporting that action.

## Testing Acceptance Criteria

Automated tests must demonstrate:

1. Each supported deficiency family maps to the correct bounded category.
2. Source finding/evaluation provenance is retained.
3. Existing journey-stage semantics are preserved.
4. Missing evidence creates no recommendation.
5. Acquisition failure creates no hotel-feature recommendation.
6. `NOT_ATTEMPTED` creates no recommendation.
7. Unsupported dimensions create no recommendation.
8. Unable-to-verify limitations do not automatically become hotel-defect recommendations.
9. Booking absence creates no booking recommendation.
10. Evaluation A cannot leak into evaluation B.
11. Identical input produces identical recommendations.
12. No numerical scoring/prioritization exists.
13. No AI/network dependency exists.
14. REQ-027/028/029 behavior remains unchanged.
15. Complete backend tests pass.

## Architectural Guidance

Prefer a small immutable recommendation record, explicit category enum, and small deterministic mapping service. Do not introduce generic rule engines, dynamic registries, reflection mapping, configurable scoring, workflow engines, large ontologies, or AI abstractions.

## Completion Requirements

Before `PR_READY`:

- inspect current `main` and referenced requirements/specifications;
- implement only REQ-030;
- add focused deterministic tests;
- run complete backend Maven tests;
- run Backend Validation against the exact final PR head;
- update this same requirement file with implementation, tests, validation, architectural decisions, truth-boundary decisions, limitations, and self-review;
- ensure the final requirement-file update is included in the successfully validated final PR head.

Do not merge. Do not start REQ-031.

## Implementation Record — Session 30

### Repository/context reconciliation

Current `main` at session start: `d0bdb0fa62e331a2be9f8a72efb94f1df5e0471b`.

Confirmed:

- REQ-027 merged as PR #27.
- REQ-028 merged as PR #28.
- REQ-029 merged as PR #29.
- REQ-030 exists on `main` with READY status and requirement commit `8014f57a5f6844e4da33c2d13fa6c8dc5f6c995a`.
- Existing analysis code matches the required typed finding, limitation, result, guest-journey, and evaluation-boundary contracts.

An older roadmap statement described REQ-027 as in progress; actual merged PRs and current repository state are newer and were treated as authoritative. No decision was invented from the stale statement.

### Inspected contracts

Inspected the REQ-030 requirement, orchestrator/context/workflow records, evidence and hospitality-analysis specifications, engineering/testing guidance, and the implemented finding, limitation, deficiency, analysis-result, and guest-journey contracts.

Relevant semantics confirmed:

- guest-journey observed impacts contain `DEFICIENCY` findings only;
- finding provenance reaches signal → observation → evidence → evaluation/attempt;
- identity conflicts have existing typed `HospitalityFinding.isIdentityConflictWith(...)` semantics;
- `GuestJourneyAnalysis` enforces one evaluation identity and retains stage-specific impacts/limitations;
- `UNABLE_TO_VERIFY` is the only current limitation type and does not explicitly authorize a recommendation.

### Implementation summary

Added:

- `RecommendationCategory` — explicit six-category bounded vocabulary;
- `HospitalityRecommendation` — immutable record retaining evaluation identity, category, bounded action, journey stages, and exactly one source finding/limitation;
- `HospitalityRecommendationService` — deterministic typed mapping over one `GuestJourneyAnalysis` boundary;
- focused `HospitalityRecommendationServiceTest` coverage.

Mappings:

- `BOOKING` deficiency → `IMPROVE_BOOKING_DISCOVERABILITY`;
- `ROOMS` deficiency → `IMPROVE_ROOM_INFORMATION`;
- `AMENITIES` / `DINING` deficiency → `IMPROVE_GUEST_FACING_INFORMATION`;
- `CONTACT` deficiency → `IMPROVE_CONTACT_LOCATION_INFORMATION`;
- typed cross-source `HOTEL_IDENTITY` conflict → `RESOLVE_INFORMATION_CONFLICT`;
- unsupported/non-conflict deficiency semantics → no recommendation;
- current `UNABLE_TO_VERIFY` limitations → no recommendation.

No human-readable finding text is parsed. Identity conflicts use the existing typed conflict method. Journey stages come from existing journey-stage impacts.

### Truth-boundary decisions

- Missing evidence, absent findings, unsupported stages, and limitations alone do not create recommendations.
- No booking recommendation is created from absence of BOOKING observation.
- Acquisition failure remains a limitation and creates no hotel-feature recommendation.
- No `NOT_ATTEMPTED` semantics were invented.
- Unable-to-verify remains unable-to-verify because no current limitation subtype supports a bounded action.
- Source finding/limitation references are retained instead of duplicating evidence.
- Recommendation text does not claim completion, conversion, revenue, or other unsupported outcomes.

### Architectural decisions

The implementation follows the required smallest shape: one record, one enum, one deterministic mapping service. It introduces no generic rule engine, registry, reflection, scoring, AI, network, persistence, API, UI, or Antena integration.

The service accepts one evaluation-bound `GuestJourneyAnalysis`, preventing cross-evaluation leakage by construction. Existing source objects preserve the provenance chain without evidence duplication.

### Files changed

- `backend/src/main/java/com/antenapro/hotelcheck/analysis/RecommendationCategory.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityRecommendation.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityRecommendationService.java`
- `backend/src/test/java/com/antenapro/hotelcheck/analysis/HospitalityRecommendationServiceTest.java`
- `requirements/REQ-030-hospitality-recommendation-foundation.md`

### Tests

Focused tests cover supported deficiency mappings, typed identity conflict handling without text parsing, unsupported deficiencies, booking absence, limitation-only input, journey-stage/provenance preservation, evaluation A/B isolation, deterministic repeatability, and the existing cross-evaluation guard.

### Validation

The first PR-head validation run (#279 / run ID `37210126305`) correctly exposed one invalid test assumption: a non-deficiency finding cannot be inserted into `GuestJourneyAnalysis` because that existing contract requires observed impacts to contain deficiencies only. The invalid test was removed; no production code was changed for this correction.

Backend Validation was then run against the exact implementation/test head `800f3ce4ff945082de3bd7d69ee7a0948eb60888`:

- Workflow: `Backend Validation`
- Run: **#280**
- Run ID: **`37210229504`**
- Validated head: **`800f3ce4ff945082de3bd7d69ee7a0948eb60888`**
- Command: `mvn --batch-mode --no-transfer-progress test`
- Result: **BUILD SUCCESS**
- Tests: **184**
- Failures: **0**
- Errors: **0**
- `HospitalityRecommendationServiceTest`: **9 tests**, all passing.

After that validation, the requirement-file update produced PR head `85a72f43c22bba1b6e6804eb2f67697d217a25dd`. Backend Validation run **#283** / run ID **`37210337937`** was executed against that exact PR head and passed:

- Workflow: `Backend Validation`
- Run: **#283**
- Run ID: **`37210337937`**
- Validated head: **`85a72f43c22bba1b6e6804eb2f67697d217a25dd`**
- Command: `mvn --batch-mode --no-transfer-progress test`
- Result: **BUILD SUCCESS**
- Tests: **184**
- Failures: **0**
- Errors: **0**

Run #283 checked out the PR merge ref containing PR #30 head `85a72f43c22bba1b6e6804eb2f67697d217a25dd` and its base `d0bdb0fa62e331a2be9f8a72efb94f1df5e0471b`. The complete backend suite passed, including all 9 recommendation tests.

Because this validation record is itself being updated now, the resulting commit is the new final head and requires one more Backend Validation run before this record can be considered fully final.

### CI

- Run #280 validated implementation/test head `800f3ce4ff945082de3bd7d69ee7a0948eb60888`.
- Run #283 validated PR head `85a72f43c22bba1b6e6804eb2f67697d217a25dd` with the prior requirement-file update included.
- The current requirement-file update is the final planned record update; Backend Validation must pass against the resulting commit.

### PR

- PR: **#30** — `REQ-030: Hospitality Recommendation Foundation`
- Base: `main`
- Head branch: `feature/hospitality-recommendation-foundation`
- PR remains open and unmerged.

### Limitations

- Current limitation semantics do not authorize a recommendation from `UNABLE_TO_VERIFY` alone.
- `IMPROVE_TRUST_CLARITY` remains in the explicit category vocabulary but is not emitted because current typed deficiency semantics do not justify a separate trust-clarity mapping beyond identity conflict resolution.
- The current analysis foundation does not create a BOOKING deficiency from a missing BOOKING signal; the booking mapping is therefore guarded for any future explicit governed booking deficiency.

### Self-review

#### Scope
Only REQ-030 recommendation behavior, tests, and this requirement record were changed. No acquisition, evidence, observation, signal, finding, limitation, coverage, journey implementation, UI, integration, or Antena behavior was changed.

#### Correctness
Mappings use typed category/kind/status and existing identity-conflict semantics, never finding-text keyword parsing. Journey impact is read from existing `GuestJourneyAnalysis`.

#### Determinism
No clock, randomness, mutable global state, AI, or network dependency exists.

#### Provenance / isolation
Each recommendation retains source finding/limitation and evaluation identity. The service consumes one evaluation-bound journey analysis.

#### Security / production
No network, persistence, external calls, or unbounded processing are introduced.

#### Testing
The complete backend suite passed in Backend Validation runs #280 and #283. The final requirement-file update requires one final validation run against its resulting commit.

### Final-head validation record

This requirement-file update is the final planned change for Session 30. Backend Validation must pass against the resulting commit before this `PR_READY` status is considered valid.

STATUS: PR_READY

**STOPPING FOR ORCHESTRATOR REVIEW.**
