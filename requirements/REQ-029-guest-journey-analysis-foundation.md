# REQ-029 — Guest Journey Analysis Foundation

STATUS: PR_READY
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

Recommendations are a later capability.

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

## Session 29 Completion Record

### Repository/context reconciliation

- `main` at implementation start was `6fac2406d166f60e10881ad681315ae3bd38298d`, containing the REQ-029 READY requirement commit.
- REQ-027 was verified merged as PR #27.
- REQ-028 was verified merged as PR #28.
- `REQ-029` was verified present and `READY` before implementation.
- Existing `GuestJourneyStage` and `journeyStages` semantics from REQ-021/SPEC-006 were inspected rather than re-created.
- Existing finding, limitation, coverage, result, deficiency, observation, evidence, and evaluation contracts were inspected before implementation.

### Existing journey-related contracts inspected

- `GuestJourneyStage` already defined `DISCOVER`, `UNDERSTAND`, `EXPLORE`, `TRUST`, and `BOOK`.
- Qualified analysis signals already carried typed journey stages for governed hospitality observation categories.
- REQ-027 deficiencies preserved the originating signal journey stages.
- Existing limitations can optionally carry explicit journey stages, but the current acquisition-created limitations are unmapped; REQ-029 preserves those limitations rather than guessing a stage.
- REQ-028 established typed cross-source `HOTEL_IDENTITY` conflict semantics for `TRUST_AND_CLARITY`.

### Implementation summary

Implemented a small deterministic backend journey layer:

- `GuestJourneyImpactState` represents `OBSERVED_IMPACT`, `LIMITATION`, `UNSUPPORTED`, and `NO_OBSERVED_ISSUE`.
- `GuestJourneyStageAnalysis` represents one explicit result per journey stage and retains source deficiencies/limitations.
- `GuestJourneyAnalysis` aggregates all five stages for exactly one evaluation and preserves unmapped limitations.
- `GuestJourneyAnalysisService` derives the journey view from one `HospitalityAnalysisResult` only; it performs no acquisition, network access, scoring, prioritization, recommendation, or AI work.
- `HospitalityAnalysisResult.guestJourneyAnalysis()` exposes the derived layer at the existing result boundary without storing a duplicate analysis object.
- `HospitalityFindingKind` was added as the smallest explicit typed discriminator required to distinguish ordinary observed findings from REQ-027 observed deficiencies without parsing finding text.
- Existing REQ-027 deficiency creation now explicitly marks those findings as `DEFICIENCY`; ordinary findings remain `OBSERVATION` through the backwards-compatible constructor.

### Journey-stage mapping decisions

- Ordinary governed finding journey stages are reused directly from the existing typed `HospitalityAnalysisSignal.journeyStages()` contract.
- Room deficiencies affect `EXPLORE`.
- Amenities/guest-information deficiencies affect `UNDERSTAND` and `EXPLORE`.
- Contact/location deficiencies affect `DISCOVER`.
- Booking observations remain `BOOK` observations only; absence of a BOOKING signal never creates a BOOK impact.
- Material same-evaluation cross-source hotel-identity conflicts additionally affect `TRUST`, using the existing typed `isIdentityConflictWith(...)` semantics established by REQ-028. This does not change or inflate coverage.
- No generic keyword classifier or rule engine was introduced.

### Truth-boundary decisions

- Missing evidence and acquisition failures do not become journey failures.
- `UNSUPPORTED_SCHEME` and unrecognized observations remain unsupported rather than negative journey conclusions.
- Current acquisition-created unable-to-verify limitations have no governed stage mapping, so they are retained as `unmappedLimitations` rather than guessed into a stage.
- Explicit stage-scoped limitations remain `LIMITATION` and retain the source limitation.
- Positive evidence with no deficiency may produce `NO_OBSERVED_ISSUE` only where the existing coverage contract already marks that journey stage assessable; it is not treated as proof of success.
- A stage with no governed evidence/assessment remains `UNSUPPORTED`.
- The REQ-027 booking truth boundary remains unchanged.

### Files changed

- `backend/src/main/java/com/antenapro/hotelcheck/analysis/GuestJourneyAnalysis.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/GuestJourneyAnalysisService.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/GuestJourneyImpactState.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/GuestJourneyStageAnalysis.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisResult.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityDeficiencyAnalysisService.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityFinding.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityFindingKind.java`
- `backend/src/test/java/com/antenapro/hotelcheck/analysis/GuestJourneyAnalysisServiceTest.java`

### Tests

Focused REQ-029 coverage was added in `GuestJourneyAnalysisServiceTest`, including:

- deterministic stage mapping;
- multi-stage impact;
- governed identity-conflict → TRUST mapping;
- no-observed-issue semantics;
- missing/unsupported evidence truth boundaries;
- explicit and unmapped limitations;
- booking absence regression;
- evaluation/attempt/evidence provenance;
- evaluation isolation;
- determinism;
- typed observation versus deficiency distinction.

### Validation

- Backend Validation run `37208791707` passed for the earlier implementation head `0de2dd92d889959f167d0cc149cb7caa5cfeb121`.
- Backend Validation run `37208928038` then passed against PR head `76cdcc1fa0295f8396f3559620994ce92197e69a`, which included the first correction to this requirement's validation record.
- Backend Validation run `37209427523` passed against the exact subsequent PR head `749b0f0597c43f2b4fc8f5284c7827aade57f76c`, which included the final-head validation record update in this section.
- The successful run completed the `Java 21 / Maven tests` job and its `Run backend Maven tests` step.
- The workflow executes the complete backend Maven test suite (`mvn --batch-mode --no-transfer-progress test`), including the focused REQ-029 test class.
- The PR diff was inspected for unrelated application changes; the change set is confined to the REQ-029 journey/domain/test scope.

### Architectural decisions

- Keep guest journey analysis as a derived domain layer over `HospitalityAnalysisResult` rather than duplicating evidence or introducing persistence.
- Preserve existing coverage as-is; journey analysis does not calculate coverage or completeness.
- Use explicit typed enums/records and one small deterministic service rather than reflection, dynamic registries, workflow engines, or configurable rules.
- Use the existing finding provenance chain; no second evidence/provenance model was introduced.
- Use the existing REQ-028 typed identity-conflict helper for the only additional stage mapping required by current governed trust semantics.

### Limitations

- The current acquisition limitation producer does not assign journey stages, so an unable-to-verify acquisition result cannot responsibly be assigned to a specific journey stage by REQ-029. It remains explicitly preserved as an unmapped limitation.
- Current upstream observation coverage remains intentionally bounded; REQ-029 does not make unsupported discoverability, mobile/technical, or SEO dimensions assessable.
- No booking transaction success/failure can be inferred from the current bounded booking observation contract.

### Self-review

- No journey score, percentage, threshold, ranking, prioritization, recommendation, AI/model call, external integration, network access, crawling, browser automation, UI, report, PDF, preview generation, or Antena conversion logic was introduced.
- No finding-text keyword classification was introduced.
- Evaluation identity is validated at the existing result boundary and again across derived journey impacts/limitations.
- Existing REQ-027 and REQ-028 contracts remain behaviorally covered by the complete backend suite.

### Final-head CI result

- Exact PR head validated by Backend Validation run `37209427523`: `749b0f0597c43f2b4fc8f5284c7827aade57f76c`.
- Backend Validation run: `37209427523` (`Backend Validation`, run #265).
- Result: `success`.
- `Java 21 / Maven tests`: `success`.
- `Run backend Maven tests`: `success`.
- The requirement file update containing this validation record is included in that validated PR head.

## Required Validation

Before marking `PR_READY`:

1. Run focused REQ-029 tests.
2. Run the complete backend Maven test suite.
3. Run GitHub Actions Backend Validation against the exact final PR head.
4. Verify no unrelated application changes were introduced.
5. Verify the requirement file contains the final validation result.

All five checks are satisfied for the recorded PR-ready validation head `749b0f0597c43f2b4fc8f5284c7827aade57f76c`. No implementation changes were made in response to the validation issue; the corrections were limited to recording exact-head successful CI validation.

## Expected Session Outcome

The implementation session must:

- create branch `feature/guest-journey-analysis-foundation` from current `main`;
- implement only REQ-029;
- add appropriate tests;
- update this requirement to `PR_READY` only after final validation;
- create a PR against `main`;
- stop for orchestrator review.

The session must not merge the PR or begin REQ-030.
