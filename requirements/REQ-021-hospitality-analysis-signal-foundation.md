# REQ-021 — Hospitality Analysis Signal Foundation

**STATUS:** VALIDATION_PENDING  
**REQUIREMENT_ID:** REQ-021  
**TYPE:** Implementation  
**BRANCH:** `feature/hospitality-analysis-signal-foundation`

## Objective

Establish the smallest deterministic analysis layer after REQ-019 that turns bounded hospitality observations into traceable, qualified analysis signals without prematurely implementing a complete findings, scoring, recommendation, report, or AI engine.

The current scope is only the analysis-signal boundary. Antena-hosted hotel experience integration is downstream and must not be implemented by this requirement.

## READ

Before implementation, read completely:

- `docs/context/ORCHESTRATOR-CONTEXT.md`
- `docs/context/CURRENT-STATE.md`
- `docs/context/DECISION-LOG.md`
- `docs/context/NEXT-STEPS.md`
- `docs/workflows/ORCHESTRATOR-HANDOFF.md`
- `docs/specs/SPEC-005-evidence-model.md`
- `docs/specs/SPEC-006-hospitality-analysis.md`
- `requirements/REQ-017-evaluation-acquisition-integration.md`
- `requirements/REQ-018-evidence-normalization-foundation.md`
- `requirements/REQ-019-hospitality-observation-foundation.md`
- `docs/engineering/AGENT-GUIDELINES.md`
- `docs/engineering/ENGINEERING-PRINCIPLES.md`
- `docs/engineering/BACKEND-PATTERNS.md`
- `docs/engineering/TESTING-STANDARDS.md`
- `docs/workflows/IMPLEMENTATION-SESSION.md`

## SCOPE

Implement a deterministic, in-memory analysis-signal boundary consuming `HospitalityObservation` only.

The signal preserves:

- hospitality observation category;
- explicit guest-journey stage(s);
- concise guest-facing interpretation;
- originating observation/evidence traceability;
- evaluation/attempt attribution already present in the observation;
- qualified state indicating an analysis signal rather than a verified business capability or transaction result.

The implementation must be conservative and must not invent hotel facts.

## NON-SCOPE / DO NOT TOUCH

Do not implement findings, severity, scores, recommendations, coverage, conflict resolution, stale-evidence policy, generic analysis/rule frameworks, AI/LLM providers, crawling/browser acquisition, new network access, persistence, report/UI, preview runtime, Antena integration, booking/OTA integrations, generic SEO auditing, or competitor analysis.

Do not weaken or modify REQ-015/017/018/019 contracts.

## TRUTHFULNESS BOUNDARY

The implementation must preserve:

```text
OBSERVED SIGNAL
    ↓
QUALIFIED INTERPRETATION
    ↓
future FINDING
    ↓
future SCORE / RECOMMENDATION
```

`BOOKING` may mean a booking entry point was observed; it must not mean booking succeeds or that the hotel accepts reservations.

`ROOMS` may mean room-related information or an entry point was observed; it must not mean rooms are available.

Missing, failed, unavailable, or insufficient evidence must not become a negative hotel claim.

A contact/location path is a discoverability signal. It does not establish trust by itself.

## ACCEPTANCE

1. The analysis-signal service accepts `HospitalityObservation` as its sole domain input.
2. No external network access is introduced.
3. Processing is deterministic for the same observation input.
4. Every emitted signal is traceable to its originating observation and therefore to supporting evidence.
5. Evaluation and attempt attribution are preserved.
6. Guest-journey mapping is explicit and limited to mappings justified by the existing contract.
7. The output represents a qualified interpretation rather than a verified hotel capability.
8. `BOOKING` does not imply booking success.
9. `ROOMS` does not imply room availability.
10. Missing observations do not become negative findings.
11. Failed/unavailable acquisition evidence does not become a negative signal.
12. No severity, score, recommendation, finding, or coverage result is produced.
13. No AI/LLM dependency is introduced.
14. Source observations are not mutated.
15. Deterministic tests cover supported positive mappings and conservative negative cases.
16. Unsupported/ambiguous observations are not over-interpreted.
17. Existing REQ-019 observation semantics remain unchanged.
18. Backend CI validation passes.
19. `CONTACT` maps to `DISCOVER` only; it does not map to `TRUST` unless a governed specification explicitly establishes that mapping.

## OUTPUT

The implementation session must create branch `feature/hospitality-analysis-signal-foundation` from current `main`, implement only this requirement, add deterministic backend tests, validate with backend CI, update this requirement with implementation/validation/PR/limitations details, set `STATUS: PR_READY` only after actual validation is complete, create a PR against `main`, and stop for orchestrator review.

The session must not merge the PR or begin REQ-022.

## Product sequencing

The current product focus is digital-presence analysis. Antena is the owning hospitality product and the eventual downstream opportunity is an Antena-hosted improved hotel experience at `<hotel-name>.antenapro.com`. That integration remains deferred until Hotel Check analysis is mature enough to produce a trustworthy result.

## Implementation Record — Session 21

### Initial implementation

Implemented the smallest deterministic in-memory boundary from `HospitalityObservation` to a qualified `HospitalityAnalysisSignal`.

The signal preserves the originating observation by reference, so existing evidence, provenance, evaluation identity, and attempt attribution remain traceable without duplicating the complete evidence object.

### Review correction

PR #21 review identified semantic inflation in the original mapping:

```text
CONTACT → DISCOVER, TRUST
```

No explicit SPEC-006 contract was found establishing that a contact/location path itself is a trust signal. The mapping was therefore corrected to:

```text
CONTACT → DISCOVER
```

The interpretation remains:

`A guest contact or location path was observed.`

A focused regression test now explicitly asserts that a `CONTACT` observation does not contain `TRUST`.

The remaining mappings are unchanged:

- `HOTEL_IDENTITY` → `DISCOVER`, `UNDERSTAND`
- `ROOMS` → `EXPLORE`
- `AMENITIES` → `UNDERSTAND`, `EXPLORE`
- `CONTACT` → `DISCOVER`
- `BOOKING` → `BOOK`
- `DINING` → `UNDERSTAND`, `EXPLORE`

The signal service accepts only `DISCOVERED` observations backed by successful evidence with retained content. Failed/unavailable evidence and inferred observations produce no signal.

### Files changed

- `backend/src/main/java/com/antenapro/hotelcheck/analysis/GuestJourneyStage.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisSignal.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisSignalService.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisSignalStatus.java`
- `backend/src/test/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisSignalServiceTest.java`
- `requirements/REQ-021-hospitality-analysis-signal-foundation.md`

### Tests

Existing deterministic coverage retained for booking success non-inference, room availability non-inference, journey mappings, traceability, attribution, failed evidence, inferred observations, determinism, immutability, and null input.

Added/adjusted explicit coverage:

- `CONTACT` observation → `DISCOVER`
- `CONTACT` observation does **not** → `TRUST`

No live hotel website or external network dependency is used by the tests.

### Validation

Final corrected commit:

`d608d70010f479dcba9fda21c897d66fcc55b7a9`

GitHub Actions Backend Validation run **#104** (`37174937409`) is currently executing against the corrected commit. The Maven test step is in progress.

The previously completed backend validation run #71 (`37174522519`) validated the pre-review implementation and passed with 94 tests, but it does not validate the corrected contact mapping. Therefore it is not being treated as final validation for this review cycle.

### CI

**PENDING** — Backend Validation run #104 / run ID `37174937409`.

### PR

- **PR:** #21 — `REQ-021: Hospitality Analysis Signal Foundation`
- **Base:** `main`
- **Head:** `feature/hospitality-analysis-signal-foundation`
- **Current head:** `d608d70010f479dcba9fda21c897d66fcc55b7a9`
- **Status:** OPEN
- **Merged:** No

### Limitations

- The signal vocabulary remains limited to the six REQ-019 observation categories.
- Journey mappings are explicit deterministic mappings; they are not scores or completeness judgments.
- The signal layer does not produce findings, severity, scores, recommendations, conflict resolution, freshness decisions, or coverage conclusions.
- `TRUST` is intentionally not inferred from a generic contact/location observation.

### Self-review

#### Scope

Only the requested REQ-021 correction was made. No acquisition, evaluation, evidence normalization, or observation-foundation behavior was changed.

#### Architecture

The correction remains local to the existing signal mapping and focused test. No new abstraction was introduced.

#### Truthfulness

The contact signal now describes discoverability only. It does not imply trustworthiness, reputation, quality, or verified accuracy of the contact information.

#### Traceability

The signal still retains the originating `HospitalityObservation` by reference, preserving its evidence and evaluation/attempt attribution.

#### Determinism

The correction is a static deterministic mapping with no network, clock, random, or AI dependency.

#### Tests

The explicit contact discover-only test guards the semantic boundary identified in review. Complete backend CI must pass before this requirement returns to `PR_READY`.

## Governance

`STATUS: VALIDATION_PENDING` — review correction committed to PR #21; final Backend Validation is still running.

**STOPPING ONLY AFTER FINAL CI VALIDATION.**
