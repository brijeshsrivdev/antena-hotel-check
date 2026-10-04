# REQ-034 — Hospitality Analysis Coverage Classification Implementation

STATUS: PR_READY
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

A journey stage or dimension is covered when present in assessable scope or limited scope. Limited scope is covered but is not assessable evidence.

`SUBSTANTIALLY_ASSESSED` requires all five journey stages covered, at least six of the nine governed hospitality dimensions covered, and at least three journey stages with assessable evidence.

`PARTIALLY_ASSESSED` applies when at least one intended journey stage or dimension is covered but the substantial criteria are not satisfied.

`INSUFFICIENT_COVERAGE` applies only when zero intended journey stages and zero intended dimensions are covered.

## Input Boundary

The classifier consumes only one existing `HospitalityAnalysisCoverage` object. It does not consume raw evidence, acquisition results, page/URL counts, finding/recommendation counts, HTTP status counts, percentages, raw HTML, human-readable finding text, severity, scores, or network/AI output.

## Evaluation Isolation

The classifier validates the coverage object's existing evaluation traceability before classification. It does not combine coverage objects, and evaluation identity is not a classification signal.

`HospitalityAnalysisCoverage` already rejects supporting findings/limitations belonging to another evaluation. REQ-034 preserves that boundary and validates the same invariant at the classifier boundary.

## No Mutation / Determinism

The classifier does not mutate coverage or contained collections. Identical governed coverage inputs produce the same state. No randomness, current time, network, AI, or mutable global state is used.

## No New State / No Scoring

No new coverage states were added and `HospitalityAnalysisCoverageState` was not changed. The thresholds are categorical calibration rules, not a score. No percentages, points, quality scores, rankings, severity, or recommendation priority are exposed.

## Architectural Shape

Implemented:

```text
HospitalityAnalysisCoverage
        ↓
HospitalityAnalysisCoverageClassifier
        ↓
HospitalityAnalysisCoverageState
```

The implementation uses explicit constants and direct set/threshold logic. No generic rule engine, configurable scoring framework, reflection, registry, workflow engine, or scoring abstraction was introduced.

## Repository / Context Reconciliation — Session 34

The actual current `main` was inspected before implementation. The branch was created directly from:

`0be7e8fa522f16043c4e2f71abc13f97c89cf2f4` — `docs: add REQ-034 to roadmap sequence`.

Confirmed on `main`:

- REQ-027 merged as PR #27.
- REQ-028 merged as PR #28.
- REQ-029 merged as PR #29.
- REQ-030 merged as PR #30.
- REQ-031 merged as PR #31.
- REQ-033 exists with `STATUS: READY` and establishes the governed coverage-classification contract.
- REQ-034 exists with `STATUS: READY` and is the assigned implementation requirement.
- `HospitalityAnalysisCoverageState` has exactly the three governed states.
- `HospitalityAnalysisDimension` has the existing nine governed dimensions.
- `GuestJourneyStage` has exactly `DISCOVER`, `UNDERSTAND`, `EXPLORE`, `TRUST`, and `BOOK`.
- `HospitalityAnalysisCoverage` is immutable, defensively copies collections, restricts assessable/limited scope to intended scope, and validates supporting finding/limitation evaluation traceability.
- The existing analysis service still receives coverage state as caller-supplied input, so REQ-032 remains blocked until this classifier is merged.

No material repository/specification mismatch was found.

## REQ-033 Contract Verification

Verified implementation against the governed contract: assessable ∪ limited defines covered scope; all five stages are required for substantial; six of nine dimensions are required; three assessable stages are required; partial requires meaningful covered scope; insufficient requires zero covered scope; limited scope contributes to coverage but not assessable-stage count; unsupported dimensions remain uncovered; counts/severity/scores are not inputs; booking absence is not a booking failure; and classification is deterministic and evaluation-local.

## Implementation Summary

Added `HospitalityAnalysisCoverageClassifier` in the existing `analysis` package. It:

1. accepts one `HospitalityAnalysisCoverage`;
2. validates evaluation traceability;
3. computes covered stages as assessable ∪ limited;
4. computes covered dimensions as assessable ∪ limited;
5. applies the explicit REQ-033 thresholds;
6. returns partial when covered scope exists but substantial criteria are not met;
7. returns insufficient only when both covered sets are empty.

`UNABLE_TO_VERIFY`, `NOT_ATTEMPTED`, and `UNSUPPORTED` are not converted into positive evidence by the classifier; their semantics remain upstream. Missing evidence is not converted into hotel deficiency, and absent booking evidence does not create booking failure.

## Covered vs Assessable Semantics

Assessable scope contributes to coverage and assessable-stage counting. Limited scope contributes to coverage only. Therefore an evaluation may cover all five stages and six dimensions through limited scope while remaining `PARTIALLY_ASSESSED` if fewer than three stages have assessable evidence.

## Testing

Added focused `HospitalityAnalysisCoverageClassifierTest` for:

- 5 stages / 6 dimensions / 3 assessable stages → `SUBSTANTIALLY_ASSESSED`;
- 5 stages / 6 dimensions / 2 assessable stages → not substantial;
- 5 stages / 5 dimensions / 3 assessable stages → not substantial;
- fewer than 5 covered stages → not substantial;
- 1 covered stage → partial;
- 1 covered dimension with no covered stages → partial;
- 0 covered stages / 0 covered dimensions → insufficient;
- limited scope counted as coverage but not assessable;
- unsupported dimensions remain uncovered;
- booking absence does not create booking failure;
- cross-evaluation supporting artifacts rejected by the existing coverage boundary;
- no mutation;
- determinism;
- null input rejection.

Existing `HospitalityAnalysisCoverageTest` continues to cover REQ-024 state vocabulary, nine-dimension scope, traceability, cross-evaluation rejection, limitation semantics, and defensive copying.

## Validation History

### Initial Backend Validation — failed, corrected

Backend Validation run **#318** / ID **`37222181043`** ran against initial head `a1ec5f4ab507e08195c98208eab752db55cf21dc` and failed only in the newly added classifier tests. The implementation compiled and the pre-existing tests passed; three classifier fixtures incorrectly populated assessable/limited arguments, causing false substantial classifications and one incorrect mutation assertion.

The test fixtures were corrected in commit `d547af2ea61da034dccd2164557e6e7955e4ac0a`.

### Backend Validation — passed

Backend Validation run **#321** / ID **`37222294525`** passed against head `d547af2ea61da034dccd2164557e6e7955e4ac0a`.

- Command: `mvn --batch-mode --no-transfer-progress test`
- Full backend suite: **206 tests, 0 failures, 0 errors, 0 skipped**
- Focused `HospitalityAnalysisCoverageClassifierTest`: **14 tests, all passing**
- Existing `HospitalityAnalysisCoverageTest`: **9 tests, all passing**

### Final-head rule

This requirement file was updated after run #321, so that result is not treated as the final validation by itself. The exact final PR head is the requirement-update commit produced by this update. Backend Validation must therefore pass again against that final head before this status is considered final.

## Files Changed

- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisCoverageClassifier.java`
- `backend/src/test/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisCoverageClassifierTest.java`
- `requirements/REQ-034-hospitality-analysis-coverage-classification-implementation.md`

No REQ-032 orchestration, acquisition, API, persistence, UI, integration, AI, or network code was changed.

## Architectural Decisions

1. Coverage calibration belongs to a dedicated classifier, not REQ-032 orchestration.
2. Existing coverage/state/dimension/journey domain types are reused; no parallel model was introduced.
3. Direct deterministic threshold logic is sufficient; no generic rules/scoring framework is justified.
4. Evaluation identity validates isolation but is never a classification signal.
5. Limited scope is covered but cannot satisfy the assessable-stage threshold.
6. Upstream truth boundaries for limitations, missing evidence, booking, and findings are preserved.

## Limitations

- REQ-034 classifies the existing coverage representation; it does not expand upstream evidence capability.
- The classifier does not interpret the semantic meaning of `UNABLE_TO_VERIFY`, `NOT_ATTEMPTED`, or `UNSUPPORTED`; upstream coverage construction owns those semantics.
- Some governed dimensions remain unsupported upstream and therefore remain uncovered unless explicitly represented in coverage.
- REQ-032 remains outside this session.

## Self-Review

### Scope
Only REQ-034 implementation, focused tests, and the assigned requirement record were changed. No REQ-032 implementation was started.

### Context
Current `main`, REQ-033, REQ-024 through REQ-032, required context/engineering documents, existing coverage model/state/dimension/journey contracts, and existing coverage tests were inspected before implementation.

### Design
One small explicit service with existing domain types and direct threshold logic. No speculative framework was added.

### Correctness
Boundary tests cover substantial/partial/insufficient thresholds, limited-vs-assessable semantics, unsupported dimensions, booking truth, evaluation isolation, immutability, and determinism.

### Testing
Repository-level Backend Validation is green at run #321 for the corrected implementation. Local Maven execution is not claimed because the execution environment used for previous repository sessions cannot reliably clone the GitHub repository.

### Security / Production
Pure in-memory logic only. No network, file access, persistence, dynamic execution, external calls, or mutable global state.

### Repository Hygiene
The PR targets `main`, contains only the three files listed above relative to `main`, and is not merged.

## PR / Governance

- **PR:** #32 — `REQ-034: Hospitality Analysis Coverage Classification Implementation`
- **Base:** `main`
- **Branch:** `feature/hospitality-analysis-coverage-classification-implementation`
- **Do not merge.**
- **Do not start REQ-032.**

## Requirement Status

`STATUS: PR_READY`

This status is set because the implementation has passed the corrected Backend Validation and the requirement has been updated with the validation history. Per the final-head CI rule above, the final requirement-update commit must itself pass Backend Validation before this PR is treated as ready for orchestrator approval.

**STOPPING FOR ORCHESTRATOR REVIEW.**
