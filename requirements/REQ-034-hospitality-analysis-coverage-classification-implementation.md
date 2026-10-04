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

A journey stage or dimension is covered when it is present in either:

- assessable scope; or
- limited scope.

Limited scope is accounted-for coverage but is not assessable evidence.

### SUBSTANTIALLY_ASSESSED

Return `SUBSTANTIALLY_ASSESSED` only when all are true:

1. all five intended journey stages are covered;
2. at least six of the nine intended hospitality dimensions are covered;
3. at least three intended journey stages have assessable evidence.

### PARTIALLY_ASSESSED

Return `PARTIALLY_ASSESSED` when:

- at least one intended journey stage or dimension is covered; and
- the substantial criteria are not satisfied.

### INSUFFICIENT_COVERAGE

Return `INSUFFICIENT_COVERAGE` only when:

- zero intended journey stages are covered; and
- zero intended dimensions are covered.

## Input Boundary

The classifier consumes only one existing `HospitalityAnalysisCoverage` object.

It must not consume:

- raw evidence;
- acquisition results;
- page counts;
- URL counts;
- finding counts;
- recommendation counts;
- HTTP status counts;
- percentages;
- raw HTML;
- human-readable finding text.

The classifier must not independently inspect or classify hotel content.

## Evaluation Isolation

The classifier must validate that the coverage object is internally coherent according to existing domain identity semantics.

It must not combine coverage objects from different evaluations.

If a caller supplies a coverage object with a valid evaluation identity, the classifier returns the state for that object; evaluation identity is not itself a classification signal.

## No Mutation

The classifier must not mutate `HospitalityAnalysisCoverage` or any contained collections.

## Determinism

For identical coverage inputs, the classifier must return the same state.

No randomness, current time, network, AI, or mutable global state.

## No New State

Do not add new coverage states.

Do not change the existing `HospitalityAnalysisCoverageState` enum.

## No Scoring

The thresholds are categorical product calibration rules, not a score.

Do not expose:

- coverage percentages;
- points;
- quality scores;
- rankings;
- severity;
- recommendation priority.

## Architectural Shape

Prefer a small service such as:

```text
HospitalityAnalysisCoverage
        ↓
HospitalityAnalysisCoverageClassifier
        ↓
HospitalityAnalysisCoverageState
```

Do not introduce a generic rule engine or configurable scoring framework.

The thresholds are governed by REQ-033 and should be represented as explicit domain constants or equally simple deterministic logic.

## Testing Acceptance Criteria

At minimum test:

1. all five journey stages + six dimensions + three assessable journey stages → `SUBSTANTIALLY_ASSESSED`;
2. all five journey stages + six dimensions but fewer than three assessable journey stages → not substantial;
3. fewer than six dimensions → not substantial;
4. fewer than five covered journey stages → not substantial;
5. one covered journey stage → `PARTIALLY_ASSESSED`;
6. one covered dimension with no covered journey stages → `PARTIALLY_ASSESSED`;
7. zero covered journey stages and zero covered dimensions → `INSUFFICIENT_COVERAGE`;
8. limited scope counts as covered;
9. limited scope does not count toward the three assessable journey stages;
10. unsupported dimensions remain uncovered;
11. finding count has no influence;
12. page count is not an input;
13. acquisition failure semantics remain outside the classifier;
14. same coverage input produces same state;
15. no mutation of input coverage;
16. existing REQ-024 behavior remains unchanged;
17. complete backend Maven tests pass.

## Requirement Update

Update this same requirement file before `PR_READY` with:

- repository/context reconciliation;
- REQ-033 contract verification;
- implementation summary;
- tests;
- CI;
- files changed;
- architectural decisions;
- limitations;
- self-review.

The final requirement update must be included in the exact final PR head validated by Backend Validation.

## Non-Scope

Do not implement:

- REQ-032 orchestration;
- acquisition;
- evidence normalization;
- observations;
- analysis findings;
- journey analysis;
- recommendations;
- report assembly;
- API;
- UI;
- persistence;
- Google integrations;
- AI;
- Antena integration.

## Governance

Do not merge. Do not start REQ-032 implementation.

**STOPPING FOR ORCHESTRATOR REVIEW.**
