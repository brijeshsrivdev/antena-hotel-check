# REQ-021 — Hospitality Analysis Signal Foundation

**STATUS:** PR_READY  
**REQUIREMENT_ID:** REQ-021  
**TYPE:** Implementation  
**BRANCH:** `feature/hospitality-analysis-signal-foundation`

## Objective

Establish the smallest deterministic analysis layer after REQ-019 that turns bounded hospitality observations into traceable, qualified analysis signals without prematurely implementing a complete findings, scoring, recommendation, report, or AI engine.

The product direction is:

```text
Hotel digital presence
    ↓
Antena Hotel Check
    ↓
Evidence
    ↓
Hospitality observations
    ↓
Analysis signals
    ↓
Mature, trustworthy analysis
    ↓
Later Antena integration
    ↓
<hotel-name>.antenapro.com
```

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

Inspect the actual current implementation on `main` before designing new types.

## INSPECT

Determine:

1. What `HospitalityObservation` actually contains.
2. What provenance and evaluation/attempt identity are already available.
3. Whether an existing analysis/finding/signal type already exists.
4. The smallest deterministic transformation from an observation to a qualified analysis signal.
5. How the output can preserve traceability without duplicating the entire evidence model.
6. Whether an existing value object or enum can be reused.
7. What later findings/scoring/recommendations would need, without implementing them now.

Do not infer architecture from names alone; inspect callers and tests.

## DEPENDENCIES

- REQ-019 — Hospitality Observation Foundation
- REQ-018 — Evidence Normalization Foundation
- SPEC-005 — Evidence Model
- SPEC-006 — Hospitality Analysis
- Existing backend and test conventions

## SCOPE

Implement a deterministic, in-memory analysis-signal boundary consuming `HospitalityObservation` only.

The signal must preserve, at minimum:

- hospitality observation category;
- relevant guest-journey stage or stages where the mapping is explicit;
- a concise guest-facing interpretation of what was observed;
- supporting observation/evidence traceability;
- evaluation/attempt attribution already present in the observation;
- qualified state indicating that this is an analysis signal, not a verified business capability or transaction result.

The implementation must be conservative. It may interpret a bounded observation according to explicit deterministic rules, but it must not invent hotel facts.

A signal such as `BOOKING` from a visible `Book Now` entry point may become a signal that a booking entry point was observed. It must not become `booking succeeds`, `booking is usable`, or `hotel accepts reservations` unless those conditions are directly supported by a later governed requirement.

A missing observation must not automatically become a negative signal or deficiency.

The implementation may support only the smallest subset of observation-to-signal mappings that can be justified by the existing specification and current observation vocabulary. It does not need to cover every SPEC-006 dimension in this requirement.

## NON-SCOPE / DO NOT TOUCH

Do not implement:

- full hospitality findings;
- severity or importance scoring;
- numerical scores or ranking;
- recommendations;
- analysis completeness/coverage calculation;
- conflict resolution;
- stale-evidence policy;
- generic `AnalysisEngine`, `FindingEngine`, `RuleEngine`, or similar framework;
- AI/LLM/model providers;
- crawling or browser acquisition;
- new network access;
- persistence/database schema;
- report generation/UI;
- interactive hotel preview;
- Antena integration;
- `<hotel-name>.antenapro.com` hosting/runtime;
- booking/OTA integrations;
- generic SEO auditing;
- competitor analysis.

Do not weaken or modify REQ-015/017/018/019 contracts.

## TRUTHFULNESS BOUNDARY

The implementation must preserve these distinctions:

```text
OBSERVED SIGNAL
    ↓
QUALIFIED INTERPRETATION
    ↓
future FINDING
    ↓
future SCORE / RECOMMENDATION
```

This requirement implements only the first two levels.

The output must never silently claim:

- booking success;
- room availability;
- hotel service availability beyond the observed signal;
- quality or reputation;
- absence of a feature because no observation exists.

Unavailable, failed, or insufficient source evidence must remain distinguishable from an observed deficiency.

## ACCEPTANCE

1. The analysis-signal service accepts `HospitalityObservation` as its sole domain input.
2. No external network access is introduced.
3. Processing is deterministic for the same observation input.
4. Every emitted signal is traceable to its originating observation and therefore to supporting evidence.
5. Evaluation and attempt attribution are preserved.
6. Guest-journey mapping is explicit and limited to mappings justified by the existing contract.
7. The output clearly represents an analysis signal/qualified interpretation rather than a verified hotel capability.
8. `BOOKING` observations do not imply booking success or transaction completion.
9. `ROOMS` observations do not imply room inventory or availability.
10. Missing observations do not become negative hotel findings.
11. Failed/unavailable acquisition evidence does not become a negative signal.
12. No severity, score, recommendation, finding, or coverage result is produced.
13. No AI/LLM dependency is introduced.
14. Source observations are not mutated.
15. Deterministic tests cover supported positive mappings and conservative negative cases.
16. Tests demonstrate that unsupported/ambiguous observations are not over-interpreted.
17. Existing REQ-019 observation semantics remain unchanged.
18. Backend CI validation passes.

## OUTPUT

The implementation session must:

- create branch `feature/hospitality-analysis-signal-foundation` from current `main`;
- implement only this requirement;
- add/update deterministic backend tests;
- validate with the repository's backend CI;
- update this same requirement file with implementation, validation, PR, and limitation details;
- set this requirement to `PR_READY` only after actual validation is complete;
- create a PR against `main`;
- stop for orchestrator review.

The session must not merge the PR or begin REQ-022.

## Product sequencing

The current product focus is digital-presence analysis. Antena is the owning hospitality product and the eventual downstream opportunity is an Antena-hosted improved hotel experience at `<hotel-name>.antenapro.com`. That integration must remain deferred until the Hotel Check analysis capability is mature enough to produce a trustworthy result.

## Implementation Record — Session 21

**Status:** PR_READY.

### Implementation summary

Implemented the smallest deterministic in-memory boundary from `HospitalityObservation` to a qualified `HospitalityAnalysisSignal`.

The signal preserves the originating observation by reference, so the existing observation retains its supporting evidence, provenance, evaluation identity, and attempt attribution without duplicating the complete evidence object.

Explicit deterministic mappings cover the current six REQ-019 observation categories:

- `HOTEL_IDENTITY` → `DISCOVER`, `UNDERSTAND`
- `ROOMS` → `EXPLORE`
- `AMENITIES` → `UNDERSTAND`, `EXPLORE`
- `CONTACT` → `DISCOVER`, `TRUST`
- `BOOKING` → `BOOK`
- `DINING` → `UNDERSTAND`, `EXPLORE`

The interpretation text remains observation-level and does not assert hotel capability, booking success, room availability, quality, or absence.

The signal service accepts only `DISCOVERED` observations backed by successful evidence with retained content. Failed/unavailable evidence and inferred observations produce no signal.

No network, AI/LLM, persistence, finding, scoring, recommendation, coverage, report, preview, booking, OTA, or Antena integration behavior was added.

### Files changed

- `backend/src/main/java/com/antenapro/hotelcheck/analysis/GuestJourneyStage.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisSignal.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisSignalService.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisSignalStatus.java`
- `backend/src/test/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisSignalServiceTest.java`
- `requirements/REQ-021-hospitality-analysis-signal-foundation.md`

### Tests

Added deterministic unit coverage for:

- booking entry-point → `BOOK` signal;
- room observation without availability/inventory inference;
- all six current observation-category journey mappings;
- originating observation and evidence traceability;
- evaluation/attempt attribution preservation;
- failed evidence rejection;
- inferred observation rejection;
- ambiguous room observation conservative interpretation;
- repeatability/determinism;
- source observation immutability;
- null input not becoming a negative signal.

No live hotel website or external network dependency is used by the tests.

### Validation

Local Maven validation was not available in the execution environment.

GitHub Actions Backend Validation run **#71** (`37174522519`) completed successfully for the implementation head `aa5ace68c78655c01128a6d19c7b2accb7e26801`.

The executed command was:

`mvn --batch-mode --no-transfer-progress test`

Result:

- **BUILD SUCCESS**
- **94 tests**
- **0 failures**
- **0 errors**
- REQ-021 `HospitalityAnalysisSignalServiceTest`: **9 tests**, all passing
- REQ-019 `HospitalityObservationServiceTest`: **8 tests**, all passing

A prior CI run on the initial test commit failed only because the test fixture used nonexistent `Evaluation#getId()` and `EvaluationAttempt#id()` accessors. The fixture was corrected to use the repository's actual `evaluationId()` and `attemptId()` contracts, and the subsequent full backend validation passed.

### CI

**PASS** — Backend Validation run #71 / run ID `37174522519`.

### PR

- **PR:** #21 — `REQ-021: Hospitality Analysis Signal Foundation`
- **Base:** `main`
- **Head:** `feature/hospitality-analysis-signal-foundation`
- **Status:** OPEN
- **Merged:** No

### Limitations

- The signal vocabulary is intentionally limited to the current six REQ-019 observation categories.
- Journey mappings are explicit deterministic mappings from SPEC-006 semantics; they are not scores or completeness judgments.
- The signal layer does not produce findings, severity, scores, recommendations, conflict resolution, freshness decisions, or coverage conclusions.
- Unsupported future observation categories should remain unmapped rather than being interpreted implicitly; the current enum contains only the six governed REQ-019 categories.

### Self-review

#### Scope

Only REQ-021 behavior was added. REQ-015/017/018/019 runtime behavior was not modified.

#### Architecture

The implementation uses one immutable signal record, two small enums, and one cohesive deterministic service. No generic analysis/rule/finding framework was introduced.

#### Truthfulness

`BOOKING` is interpreted only as a booking entry point being observed. `ROOMS` is interpreted only as room-related information/entry point being observed. Missing or failed evidence is not converted into absence or deficiency.

#### Traceability

The signal retains the originating `HospitalityObservation` by reference, which retains the supporting `StructuredEvidence`, provenance, source reference, evaluation ID, attempt ID, and attempt number.

#### Determinism

The service contains no clock, network, external state, random behavior, or AI/model dependency. Identical observations produce identical signals.

#### Tests

The focused REQ-021 test suite covers positive mappings and conservative negative cases. Complete backend CI passed with 94 tests and zero failures/errors.

#### Repository hygiene

The PR contains only the five production/test files for this capability plus this requirement completion record. No generated artifacts or unrelated refactoring were added.

## Governance

`STATUS: PR_READY` — implementation committed, PR #21 created against `main`, and backend CI passed.

**STOPPING FOR ORCHESTRATOR REVIEW.**
