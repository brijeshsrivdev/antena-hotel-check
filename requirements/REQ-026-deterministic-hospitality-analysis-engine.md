# REQ-026 — Deterministic Hospitality Analysis Engine

STATUS: PR_READY
REQUIREMENT_ID: REQ-026
TYPE: Implementation
BRANCH: `feature/deterministic-hospitality-analysis-engine`

## Objective

Move from isolated hospitality-analysis domain foundations to the first **usable deterministic analysis pipeline**.

REQ-026 consumes already-retained `StructuredEvidence` for one evaluation and deterministically produces the analysis artifacts that can be assembled into a `HospitalityAnalysisResult`:

```text
Structured Evidence
      ↓
Hospitality Observations
      ↓
Qualified Analysis Signals
      ↓
Verified Hospitality Findings
      +
Unable-to-Verify Limitations
      ↓
Coverage Representation
      ↓
Hospitality Analysis Result
```

It does **not** introduce crawling, browser automation, scoring, AI, report UI, persistence, or Antena preview integration.

## Product Question

The analysis remains organized around:

**Can a guest find, understand, trust, explore, and book this hotel online?**

The implementation remains hospitality-first rather than becoming a generic website audit.

## Scope

Implement a deterministic analysis application/service that:

1. accepts the already-produced structured evidence for exactly one evaluation;
2. invokes the existing `HospitalityObservationService`;
3. qualifies observations through the existing `HospitalityAnalysisSignalService`;
4. converts supported qualified signals into verified hospitality findings;
5. converts supported unavailable/failed evidence conditions into truthful unable-to-verify limitations where existing REQ-023 rules permit it;
6. derives the coverage **representation inputs** from what was actually assessable;
7. assembles the existing `HospitalityAnalysisResult`;
8. preserves evaluation identity and evidence/provenance traceability throughout.

The service supports the currently implemented hospitality observation categories:

- HOTEL_IDENTITY
- ROOMS
- AMENITIES
- CONTACT
- BOOKING
- DINING

Do not invent additional observation categories in this requirement merely to match all nine SPEC-006 dimensions.

## Finding Semantics

A verified finding is grounded in an existing qualified `HospitalityAnalysisSignal` and its supporting discovered evidence.

The implementation uses concise deterministic finding interpretations already present in the signal layer. It does not invent unsupported hotel-quality claims.

These remain observations of the public digital presence, not claims that the hotel is good, complete, available, or bookable.

Do not create negative findings merely because a positive observation was not found unless the evidence model and explicit deterministic rule establish that the relevant source was sufficiently observed and the missing content itself is a supported conclusion.

## Limitation Semantics

Reuse `HospitalityAnalysisLimitationService` and existing REQ-023 semantics.

A limitation may represent timeout, network failure, HTTP/public acquisition failure where the existing contract supports it, or another explicitly supported unable-to-verify condition.

A limitation must never become hotel capability absence, booking failure, room absence, amenity absence, or contact absence.

`NOT_ATTEMPTED` remains insufficient to establish a limitation unless the existing contract explicitly provides an inability-to-verify reason.

## Coverage Boundary

REQ-024 intentionally established **representation**, not classification.

REQ-026 does not invent a classifier or thresholds.

The engine constructs factual coverage inputs including intended journey scope, intended dimensions supported by current deterministic checks, assessable journey stages/dimensions, limited journey stages/dimensions, and supporting findings/limitations.

The final `HospitalityAnalysisCoverageState` remains a governed caller-supplied input. The engine does not derive it from page counts, finding counts, percentages, or thresholds.

## Analysis Result

Use the existing `HospitalityAnalysisResult` from REQ-025.

The returned result belongs to exactly one evaluation, preserves existing findings, limitations, and coverage objects, allows zero findings, retains provenance through existing objects, and remains immutable after creation.

## Deterministic Rule Boundary

All REQ-026 analysis rules are explicit and testable. Identical structured evidence produces equivalent analysis artifacts. There is no randomness, clock-dependent classification, external network access, LLM/model call, or mutable global state.

## Input Boundary

The engine consumes `StructuredEvidence` only. It does not invoke crawling, browser acquisition, HTTP clients, external services, or acquisition retries.

## Multi-Evidence Handling

The engine supports multiple `StructuredEvidence` items belonging to the same evaluation. It processes each eligible evidence item, preserves evidence identity and traceability, deterministically deduplicates equivalent generated findings, preserves materially distinct sources, and rejects cross-evaluation input.

No universal source hierarchy is introduced.

## Conflict Handling

Materially different observations are not silently overwritten. Multiple traceable findings are retained.

## Hospitality Journey

The current implemented signal mappings remain aligned with DISCOVER, UNDERSTAND, EXPLORE, TRUST, and BOOK. Existing multi-stage signal assignments are preserved.

## Deduplication

Deduplication occurs through deterministic generated-artifact equality so equivalent signals from the same evidence/source context may be represented once, while distinct source evidence remains traceable.

## Explicit Non-Scope

Do NOT implement browser/crawler acquisition, scraping infrastructure, search APIs, AI/LLM/model confidence, numerical scoring, hotel quality score, severity calibration, ranking/grading, recommendation generation, report generation/UI/PDF/dashboard, persistence/database schema, REST API, interactive preview, booking/OTA integration, competitor analysis, generic SEO auditing, universal source hierarchy, full nine-dimension ontology expansion, arbitrary coverage thresholds, or arbitrary coverage classification.

## Dependencies

- `SPEC-005` — Evidence Model
- `SPEC-006` — Hospitality Analysis Contract
- REQ-019 — Hospitality Observation Foundation
- REQ-021 — Hospitality Analysis Signal Foundation
- REQ-022 — Hospitality Finding Foundation
- REQ-023 — Hospitality Analysis Limitation Foundation
- REQ-024 — Hospitality Analysis Coverage Foundation
- REQ-025 — Hospitality Analysis Result Foundation
- existing evaluation/evidence identity conventions
- existing backend engineering/testing standards

## Acceptance Criteria

1. A single analysis service processes multiple `StructuredEvidence` items belonging to one evaluation.
2. Successful discovered evidence is passed through the existing observation service.
3. Eligible observations are passed through the existing signal qualification service.
4. Qualified signals become verified hospitality findings without unsupported hotel claims.
5. Supported acquisition/evidence failures become truthful unable-to-verify limitations through the existing REQ-023 contract.
6. Unsupported/not-attempted states do not silently become hotel-feature absence or unsupported limitations.
7. Cross-evaluation evidence is rejected and never mixed.
8. Findings preserve signal/evidence/provenance traceability.
9. Limitations preserve supporting evidence/acquisition provenance.
10. Multiple distinct evidence sources remain traceable; conflicts are not silently overwritten.
11. Equivalent duplicate signals are handled deterministically without losing distinct source context.
12. Zero findings is valid and does not imply absence of hotel capabilities.
13. Coverage inputs are derived only from actual assessable evidence and existing artifacts.
14. No arbitrary coverage threshold or classification rule is invented.
15. The existing `HospitalityAnalysisResult` is the final aggregation boundary.
16. The complete pipeline is deterministic and has no external/network/AI dependency.
17. Existing upstream contracts remain intact.
18. Focused REQ-026 coverage and complete backend CI pass.

## Engineering Expectations

Prefer a small application service/orchestrator with explicit collaborators:

```text
HospitalityAnalysisService
    ↓
ObservationService
    ↓
SignalService
    ↓
Finding creation
    ↓
Limitation creation
    ↓
Coverage representation
    ↓
HospitalityAnalysisResult
```

Do not create a generic rule engine, plugin framework, strategy hierarchy, or reflection-based pipeline. Reuse existing services rather than duplicating their rules.

## Testing Requirements

Tests cover the end-to-end deterministic pipeline, multiple evidence, mixed successful/failed evidence, zero eligible observations, repeated equivalent evidence, deterministic repeat execution, truthfulness of limitations, cross-evaluation rejection, output evaluation integrity, provenance, source conflicts, coverage representation, and regression of the existing suite.

No live hotel website or external network dependency is permitted.

## Validation

The final PR head was validated by GitHub Actions Backend Validation run **#183** (`37181319818`) with the complete backend Maven suite passing: **146 tests, 0 failures, 0 errors**. The new `HospitalityAnalysisServiceTest` executed as part of that suite with **11 tests, 0 failures, 0 errors**.

A local Maven run was not available in the execution environment because Maven was not installed and the environment could not clone GitHub directly; GitHub Actions provided the authoritative full-suite validation.

## Implementation Record

### Context reconciliation

Current `main` was inspected before implementation. REQ-025 is merged on `main`. Existing observation, signal, finding, limitation, coverage, result, structured-evidence, evaluation, and attempt contracts were reused. No upstream contract was modified.

### Implementation summary

Added `HospitalityAnalysisService` as the bounded deterministic orchestration boundary. The service validates evaluation/attempt identity, processes evidence through the existing observation and signal services, creates findings through the existing finding service, creates supported unable-to-verify limitations through the existing limitation service, derives factual coverage representation inputs, and assembles `HospitalityAnalysisResult`.

The implementation intentionally does not classify coverage. `HospitalityAnalysisCoverageState` is a governed caller input. The engine derives assessable/limited scope only from actual findings and limitations.

### Files changed

- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisService.java`
- `backend/src/test/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisServiceTest.java`
- `requirements/REQ-026-deterministic-hospitality-analysis-engine.md`

### Tests

- `HospitalityAnalysisServiceTest`: 11 end-to-end tests covering success, multiple evidence, failed evidence, unsupported outcomes, no positive signal, duplicates, conflicts, cross-evaluation rejection, evaluation integrity, coverage boundary, and determinism.
- Complete backend Maven suite: 146 tests passed.

### CI

GitHub Actions Backend Validation run #183 passed against final PR head `9ed23bb56d5b93419bdb4f6b65bbd74a9d57cfe3`.

### PR

PR #26 — `REQ-026: Deterministic Hospitality Analysis Engine` — open against `main`; not merged.

### Coverage classification decision/gap

No classifier was invented. The final coverage state is explicitly supplied by the governed caller. This preserves the REQ-024 representation boundary until a future requirement defines deterministic classification rules.

### Architectural decisions

- Keep one small `HospitalityAnalysisService` orchestration boundary.
- Reuse existing observation, signal, finding, limitation, and result contracts.
- Use deterministic ordered-set accumulation for duplicate suppression while retaining distinct evidence context.
- Reject mixed evaluation/attempt identity rather than discarding or reassigning evidence.
- Keep coverage classification outside this engine.

### Limitations

The current observation contracts provide only six deterministic hospitality categories; no additional categories or full nine-dimension ontology were added. Coverage limitations caused by failed acquisition remain truthful but are only mapped to dimensions/stages when the existing limitation artifact itself carries that scope.

### Self-review

- Scope: REQ-026 only.
- Pipeline: StructuredEvidence → Observation → Signal → Finding/Limitation → Coverage → AnalysisResult.
- Reuse: existing upstream services/contracts reused.
- Truthfulness: no negative finding is created from absence alone.
- Provenance: findings and limitations retain existing supporting artifacts.
- Evaluation integrity: mixed evaluation and attempt attribution is rejected.
- Coverage: no thresholds, percentages, page-count scoring, or hidden classifier.
- Conflicts: distinct evidence is retained.
- Determinism: no network, clock-dependent classification, randomness, or mutable global state.
- Simplicity: no generic engine/framework introduced.

## Requirement Governance

`STATUS: PR_READY` is set only after implementation, test validation, CI validation, this requirement update, and PR creation against `main`.

**STOPPING FOR ORCHESTRATOR REVIEW.**
