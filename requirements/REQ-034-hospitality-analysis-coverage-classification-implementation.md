# REQ-034 — Hospitality Analysis Coverage Classification Implementation

STATUS: READY
REQUIREMENT_ID: REQ-034
TYPE: Implementation
BRANCH: `feature/hospitality-analysis-coverage-classification-implementation`

## Objective

Implement the deterministic coverage classifier governed by REQ-033 so the existing `HospitalityAnalysisCoverageState` can be produced by an explicit, testable domain service.

REQ-034 is intentionally separate from REQ-032. The execution orchestrator must consume this classifier; it must not own coverage calibration.

## Dependencies

- REQ-024 — Hospitality Analysis Coverage Foundation
- REQ-033 — Hospitality Analysis Coverage Classification Contract
- Existing `HospitalityAnalysisCoverage` domain model
- Existing evaluation identity/provenance conventions

## Governing Classification

Use the exact semantics defined by REQ-033.

### Covered scope

A journey stage or dimension is covered when it is present in either assessable scope or limited scope. Limited scope is accounted-for coverage but is not assessable evidence.

### SUBSTANTIALLY_ASSESSED

Return `SUBSTANTIALLY_ASSESSED` only when all are true:

1. all five intended journey stages are covered;
2. at least six of the nine intended hospitality dimensions are covered;
3. at least three intended journey stages have assessable evidence.

### PARTIALLY_ASSESSED

Return `PARTIALLY_ASSESSED` when at least one intended journey stage or dimension is covered and the substantial criteria are not satisfied.

### INSUFFICIENT_COVERAGE

Return `INSUFFICIENT_COVERAGE` only when zero intended journey stages and zero intended dimensions are covered.

## Input Boundary

The classifier consumes only one existing `HospitalityAnalysisCoverage` object. It does not consume raw evidence, acquisition results, page counts, URL counts, finding counts, recommendation counts, HTTP status counts, percentages, raw HTML, or human-readable finding text. It does not independently inspect or classify hotel content.

## Evaluation Isolation

The classifier validates the coverage object's existing evaluation traceability before classification. It does not combine coverage objects, and evaluation identity is not a classification signal.

`HospitalityAnalysisCoverage` already rejects supporting findings/limitations belonging to another evaluation. REQ-034 preserves that boundary and additionally validates the same invariant at the classifier boundary.

## No Mutation

The classifier does not mutate `HospitalityAnalysisCoverage` or any contained collections.

## Determinism

For identical coverage inputs, the classifier returns the same state. No randomness, current time, network, AI, or mutable global state is used.

## No New State / No Scoring

No new coverage states were added and the existing `HospitalityAnalysisCoverageState` enum was not changed. The thresholds are categorical product calibration rules, not a score. No percentages, points, quality scores, rankings, severity, or recommendation priority are exposed.

## Architectural Shape

Implemented the small explicit service:

```text
HospitalityAnalysisCoverage
        ↓
HospitalityAnalysisCoverageClassifier
        ↓
HospitalityAnalysisCoverageState
```

The thresholds are represented as explicit constants and direct deterministic logic. No generic rule engine, configurable scoring framework, reflection, registry, workflow engine, or scoring abstraction was introduced.

## Repository / Context Reconciliation — Session 34

The actual current `main` was inspected before implementation. The branch was created directly from the then-current `main` head:

`0be7e8fa522f16043c4e2f71abc13f97c89cf2f4` — `docs: add REQ-034 to roadmap sequence`.

Confirmed on `main`:

- REQ-027 merged as PR #27.
- REQ-028 merged as PR #28.
- REQ-029 merged as PR #29.
- REQ-030 merged as PR #30.
- REQ-031 merged as PR #31.
- REQ-033 exists with `STATUS: READY` and establishes the governed coverage-classification contract.
- REQ-034 exists with `STATUS: READY` and is the assigned implementation requirement.
- `HospitalityAnalysisCoverageState` contains exactly the three governed states.
- `HospitalityAnalysisDimension` contains the existing nine governed dimensions.
- `GuestJourneyStage` contains exactly `DISCOVER`, `UNDERSTAND`, `EXPLORE`, `TRUST`, and `BOOK`.
- `HospitalityAnalysisCoverage` is immutable, defensively copies collections, restricts assessable/limited scope to intended scope, and validates supporting finding/limitation evaluation traceability.
- The existing analysis service still receives coverage state as caller-supplied input, so REQ-032 remains correctly blocked until this classifier is merged.

No material repository/specification mismatch was found, so implementation proceeded.

## REQ-033 Contract Verification

The implementation was checked against the authoritative REQ-033 semantics: covered scope is assessable ∪ limited; substantial requires all five journey stages, at least six of nine governed dimensions, and at least three assessable journey stages; partial requires meaningful covered scope without satisfying substantial; insufficient requires zero covered stages and zero covered dimensions; limited scope contributes to coverage but not assessable-stage count; unsupported dimensions remain uncovered; finding/page/recommendation counts and severity/scores are not inputs; booking absence is not converted into booking failure; and classification is deterministic and evaluation-local.

## Implementation Summary

Added `HospitalityAnalysisCoverageClassifier` in the existing `analysis` package.

The classifier accepts one `HospitalityAnalysisCoverage`, validates evaluation traceability, computes covered journey stages as assessable ∪ limited, computes covered dimensions as assessable ∪ limited, applies the explicit REQ-033 thresholds, returns partial when meaningful covered scope exists but substantial criteria are not met, and returns insufficient only when both covered sets are empty.

The classifier does not inspect evidence, acquisition outcomes, findings, pages, URLs, finding text, recommendations, scores, severity, network state, time, or AI output.

## Covered vs Assessable Semantics

Assessable scope contributes to both coverage and assessable-stage counting. Limited scope contributes to coverage only. Limited journey stages do not satisfy the three-assessable-stage substantial criterion.

Therefore an evaluation can explicitly account for all five stages and six dimensions through limited scope while still remaining `PARTIALLY_ASSESSED` when fewer than three journey stages have assessable evidence.

`UNABLE_TO_VERIFY`, `NOT_ATTEMPTED`, and `UNSUPPORTED` are not converted into positive evidence by the classifier. Their semantics remain upstream; they can affect classification only through the existing coverage representation that explicitly places an area in limited or assessable scope.

Missing evidence is not interpreted as hotel deficiency, and the classifier does not create any booking deficiency when booking evidence is absent.

## Testing

Added focused `HospitalityAnalysisCoverageClassifierTest` covering:

- 5 stages / 6 dimensions / 3 assessable stages → `SUBSTANTIALLY_ASSESSED`;
- 5 stages / 6 dimensions / 2 assessable stages → `PARTIALLY_ASSESSED`;
- 5 stages / 5 dimensions / 3 assessable stages → `PARTIALLY_ASSESSED`;
- 4 covered stages / 6 dimensions / 3 assessable stages → `PARTIALLY_ASSESSED`;
- 1 covered stage → `PARTIALLY_ASSESSED`;
- 1 covered dimension with no covered stages → `PARTIALLY_ASSESSED`;
- 0 covered stages / 0 covered dimensions → `INSUFFICIENT_COVERAGE`;
- limited scope counts as coverage but not assessable evidence;
- unsupported dimensions remain uncovered;
- absence of booking evidence does not create booking failure;
- cross-evaluation supporting artifacts are rejected by the existing coverage boundary;
- classifier does not mutate coverage input;
- identical input produces identical classification;
- null classifier input is rejected.

Existing `HospitalityAnalysisCoverageTest` already verifies the REQ-024 state vocabulary, nine-dimension scope, traceability, cross-evaluation rejection, limitation semantics, and defensive copying. The new classifier tests build on that existing contract.

## Files Changed

- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisCoverageClassifier.java`
- `backend/src/test/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisCoverageClassifierTest.java`
- `requirements/REQ-034-hospitality-analysis-coverage-classification-implementation.md`

No REQ-032 orchestration, acquisition, API, persistence, UI, integration, AI, or network code was changed.

## Architectural Decisions

1. **Classification owns calibration.** Coverage calibration is implemented in a dedicated classifier so REQ-032 remains a thin orchestration layer.
2. **Use existing domain types.** The classifier consumes `HospitalityAnalysisCoverage` and returns the existing `HospitalityAnalysisCoverageState`.
3. **Use direct deterministic logic.** Explicit thresholds and set unions are sufficient; no generic rules/scoring framework is justified.
4. **Keep evaluation identity out of classification semantics.** Identity validates isolation but never increases or decreases the coverage state.
5. **Respect limited-vs-assessable distinction.** Limited scope is accounted for but cannot satisfy the assessable-stage threshold.
6. **Preserve upstream truth boundaries.** The classifier does not reinterpret limitations, missing evidence, booking signals, or findings.

## Limitations

- REQ-034 classifies the existing coverage representation; it does not expand the dimensions or journey evidence that upstream analysis can produce.
- The classifier intentionally does not interpret the semantic meaning of `UNABLE_TO_VERIFY`, `NOT_ATTEMPTED`, or `UNSUPPORTED`; those semantics remain owned by upstream analysis/coverage construction.
- Some governed dimensions remain unsupported upstream; the classifier leaves them uncovered unless upstream coverage explicitly marks them assessable or limited.
- REQ-032 remains outside this session and must be reconciled against `main` after this PR is merged.

## Self-Review

### Scope

Only REQ-034 classification implementation, focused tests, and the assigned requirement completion record were changed. No REQ-032 implementation was started.

### Context

Current `main`, REQ-033, REQ-024 through REQ-032, required context/engineering documents, existing coverage model/state/dimension/journey contracts, and existing coverage tests were inspected before implementation.

### Design

The implementation is one small explicit service with direct threshold logic and existing domain types. No speculative framework or abstraction was added.

### Correctness

Boundary tests cover all governing thresholds, limited-vs-assessable behavior, partial/insufficient states, unsupported dimensions, booking truth, evaluation isolation, immutability, and determinism.

### Testing

Focused REQ-034 tests were added. Local Maven execution is not claimed because the execution environment used for previous repository sessions cannot reliably clone the GitHub repository. GitHub Actions Backend Validation is the repository-level validation gate.

### Security / Production

The classifier is pure in-memory logic. It performs no network access, file access, persistence, dynamic execution, external calls, or unbounded processing beyond the existing bounded enum/set collections.

### Repository Hygiene

Only the two implementation/test files and the assigned REQ-034 requirement record are changed relative to `main`. The PR targets `main` and is not merged.

## CI / PR

PR created:

- **PR:** #32 — `REQ-034: Hospitality Analysis Coverage Classification Implementation`
- **Base:** `main`
- **Head:** `feature/hospitality-analysis-coverage-classification-implementation`
- **Initial head:** `a1ec5f4ab507e08195c98208eab752db55cf21dc`
- **Backend Validation run:** #318 / run ID `37222181043`
- **Initial run status:** IN PROGRESS when this requirement update was prepared.

The requirement remains `STATUS: READY` until Backend Validation passes against the exact final PR head after all requirement-file updates.

## Governance

Do not merge. Do not start REQ-032 implementation.

**STOPPING FOR ORCHESTRATOR REVIEW after final-head CI validation.**
