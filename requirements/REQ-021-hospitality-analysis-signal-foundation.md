# REQ-021 — Hospitality Analysis Signal Foundation

**STATUS:** PR_READY  
**REQUIREMENT_ID:** REQ-021  
**TYPE:** Implementation  
**BRANCH:** `feature/hospitality-analysis-signal-foundation`

## Objective

Establish the smallest deterministic analysis layer after REQ-019 that turns bounded hospitality observations into traceable, qualified analysis signals without prematurely implementing a complete findings, scoring, recommendation, report, or AI engine.

The current scope is only the analysis-signal boundary. Antena-hosted hotel experience integration is downstream and must not be implemented by this requirement.

## SCOPE

Implement a deterministic, in-memory analysis-signal boundary consuming `HospitalityObservation` only.

The signal preserves:

- hospitality observation category;
- explicit guest-journey stage(s);
- concise guest-facing interpretation;
- originating observation/evidence traceability;
- evaluation/attempt attribution already present in the observation;
- qualified state indicating an analysis signal rather than a verified business capability or transaction result.

The implementation is conservative and does not invent hotel facts.

## TRUTHFULNESS BOUNDARY

`BOOKING` may mean a booking entry point was observed; it does not mean booking succeeds or that the hotel accepts reservations.

`ROOMS` may mean room-related information or an entry point was observed; it does not mean rooms are available.

Missing, failed, unavailable, or insufficient evidence does not become a negative hotel claim.

A contact/location path is a discoverability signal. It does not establish trust by itself.

## ACCEPTANCE

1. The analysis-signal service accepts `HospitalityObservation` as its sole domain input.
2. No external network access is introduced.
3. Processing is deterministic for the same observation input.
4. Every emitted signal is traceable to its originating observation and supporting evidence.
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

## Implementation Record — Session 21

### Implementation summary

Implemented the smallest deterministic in-memory boundary from `HospitalityObservation` to a qualified `HospitalityAnalysisSignal`.

The signal preserves the originating observation by reference, so existing evidence, provenance, evaluation identity, and attempt attribution remain traceable without duplicating the complete evidence object.

### Review correction

PR #21 review identified semantic inflation in the original mapping:

```text
CONTACT → DISCOVER, TRUST
```

No explicit SPEC-006 contract establishes that a contact/location path itself is a trust signal. The mapping was corrected to:

```text
CONTACT → DISCOVER
```

A focused regression test explicitly asserts that a `CONTACT` observation does not contain `TRUST`.

Final mappings:

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

Final backend validation covered:

- `CONTACT` → `DISCOVER`
- `CONTACT` does not → `TRUST`
- booking entry point without booking-success inference;
- room information without availability inference;
- all six observation-category mappings;
- evidence traceability;
- evaluation/attempt attribution;
- failed evidence rejection;
- inferred observation rejection;
- deterministic repeatability;
- source immutability;
- null input handling.

No live hotel website or external network dependency is used by the tests.

### Validation

Corrected implementation commit:

`d608d70010f479dcba9fda21c897d66fcc55b7a9`

GitHub Actions Backend Validation run **#104** (`37174937409`) completed successfully against the corrected implementation.

Executed command:

`mvn --batch-mode --no-transfer-progress test`

Result:

- **BUILD SUCCESS**
- **95 tests**
- **0 failures**
- **0 errors**
- `HospitalityAnalysisSignalServiceTest`: **10 tests**, all passing
- `HospitalityObservationServiceTest`: **8 tests**, all passing

The test suite completed with Maven reporting `BUILD SUCCESS`. GitHub Actions also reported only non-blocking dependency/tooling warnings unrelated to REQ-021.

The earlier run #71 validated the pre-review implementation and passed with 94 tests; it is superseded for this review cycle by run #104 on the corrected head.

### CI

**PASS** — Backend Validation run #104 / run ID `37174937409`.

### PR

- **PR:** #21 — `REQ-021: Hospitality Analysis Signal Foundation`
- **Base:** `main`
- **Head:** `feature/hospitality-analysis-signal-foundation`
- **Validated head:** `d608d70010f479dcba9fda21c897d66fcc55b7a9`
- **Status:** OPEN
- **Merged:** No

### Limitations

- The signal vocabulary remains limited to the six REQ-019 observation categories.
- Journey mappings are explicit deterministic mappings; they are not scores or completeness judgments.
- The signal layer does not produce findings, severity, scores, recommendations, conflict resolution, freshness decisions, or coverage conclusions.
- `TRUST` is intentionally not inferred from a generic contact/location observation.

### Self-review

#### Scope

Only REQ-021 behavior was implemented and the requested CONTACT semantic correction was made. No acquisition, evaluation, evidence normalization, or observation-foundation behavior was changed.

#### Architecture

The correction remains local to the existing signal mapping and focused test. No new abstraction was introduced.

#### Truthfulness

The contact signal now describes discoverability only. It does not imply trustworthiness, reputation, quality, or verified accuracy of the contact information.

#### Traceability

The signal retains the originating `HospitalityObservation` by reference, preserving its evidence and evaluation/attempt attribution.

#### Determinism

The correction is a static deterministic mapping with no network, clock, random, or AI dependency.

#### Tests

The explicit contact discover-only test guards the semantic boundary identified in review. Complete backend CI passed with 95 tests and zero failures/errors.

## Governance

`STATUS: PR_READY` — review correction committed to PR #21, complete Backend Validation run #104 passed, and PR remains open for orchestrator review.

**STOPPING FOR ORCHESTRATOR REVIEW.**
