# REQ-033 — Hospitality Analysis Coverage Classification Contract

STATUS: READY
REQUIREMENT_ID: REQ-033
TYPE: Specification / Product Contract
BRANCH: `feature/hospitality-analysis-coverage-classification-contract`

## Objective

Establish the first governed semantic contract for converting the existing factual hospitality analysis scope into one of the three coverage states already defined by REQ-024:

- `SUBSTANTIALLY_ASSESSED`
- `PARTIALLY_ASSESSED`
- `INSUFFICIENT_COVERAGE`

REQ-033 resolves the contract gap discovered while preparing REQ-032. REQ-024 intentionally established representation only and explicitly left classification/calibration unspecified. REQ-032 cannot invent a coverage state while orchestrating an end-to-end evaluation.

The purpose of this requirement is to define the product semantics before implementation.

## Product Boundary

Coverage classification answers:

> Given the intended hospitality evaluation scope and the factual assessable/limited scope produced by the deterministic analysis pipeline, how much of the intended guest-facing evaluation was responsibly assessable for this evaluation?

Coverage classification does **not** answer:

- whether the hotel is good or bad;
- whether the hotel has a feature that was not observed;
- how many pages were crawled;
- how many findings were produced;
- a quality score;
- a ranking;
- a severity level;
- a conversion probability.

## Authoritative Inputs

The classifier may consume only existing governed analysis artifacts:

1. `HospitalityAnalysisCoverage.intendedJourneyStages`
2. `HospitalityAnalysisCoverage.intendedDimensions`
3. `HospitalityAnalysisCoverage.assessableJourneyStages`
4. `HospitalityAnalysisCoverage.assessableDimensions`
5. `HospitalityAnalysisCoverage.limitedJourneyStages`
6. `HospitalityAnalysisCoverage.limitedDimensions`
7. evaluation identity

The classifier must not consume page counts, raw HTML, URL counts, finding counts, recommendation counts, HTTP status counts, or arbitrary percentages.

The classifier does not inspect raw evidence itself.

## Coverage Semantics

### Covered scope

For classification purposes, a journey stage or dimension is **covered** when the existing analysis contract explicitly places it in either:

- assessable scope; or
- limited scope.

A limited area is not treated as successful or positively assessed. It is treated as explicitly accounted-for but constrained.

### Substantially assessed

`SUBSTANTIALLY_ASSESSED` means the evaluation has meaningful coverage across the intended guest journey and the currently governed hospitality scope is sufficiently represented that the report can reasonably be described as a substantial assessment rather than a narrow or fragmentary one.

The initial calibration is:

1. all five intended journey stages must be covered (assessable or explicitly limited); and
2. at least **six of the nine intended hospitality dimensions** must be covered (assessable or explicitly limited); and
3. at least **three of the five journey stages** must have assessable evidence rather than only limited status.

These are product calibration thresholds, not quality scores. They may be revised by a future product decision based on real evaluation data.

### Partially assessed

`PARTIALLY_ASSESSED` means the evaluation has meaningful hospitality coverage but does not satisfy the substantial-assessment calibration.

The initial calibration is:

- at least one intended journey stage or intended dimension is covered; and
- the evaluation does not satisfy `SUBSTANTIALLY_ASSESSED`.

### Insufficient coverage

`INSUFFICIENT_COVERAGE` means the evaluation does not contain enough governed assessable or explicitly limited scope to support a meaningful hospitality assessment.

The initial calibration is:

- zero intended journey stages covered; and
- zero intended dimensions covered.

This state must not be interpreted as a statement that the hotel lacks hospitality information or capabilities.

## Important Truth Boundaries

The classifier must preserve these distinctions:

```text
limited scope              ≠ hotel deficiency
unsupported dimension      ≠ hotel deficiency
missing evidence           ≠ absence
acquisition failure        ≠ hotel deficiency
NOT_ATTEMPTED              ≠ hotel deficiency
few findings               ≠ insufficient coverage
many findings              ≠ substantial coverage
many pages                 ≠ substantial coverage
```

A coverage state is a statement about the analysis performed, not about hotel quality.

## Unsupported Dimensions

An intended dimension that is neither assessable nor limited is **not covered**.

The current implementation may intentionally leave dimensions such as mobile/technical and SEO/structured-data unsupported. Their presence in intended scope must not be treated as coverage merely because the dimension exists in the model.

## Limited Scope

Limited scope contributes to coverage because the system explicitly accounted for the area and can explain why assessment was constrained.

However, limited scope does not contribute to the `three assessable journey stages` requirement for `SUBSTANTIALLY_ASSESSED`.

This prevents an evaluation dominated by inability-to-verify limitations from being called substantial merely because every area was attempted.

## Determinism

For identical governed coverage inputs, classification must always return the same state.

No:

- randomness;
- current time;
- network access;
- AI/LLM;
- mutable global state.

## Evaluation Isolation

Coverage classification must operate within exactly one evaluation identity.

Cross-evaluation coverage artifacts must be rejected rather than combined.

## Scope

REQ-033 defines the product classification contract and calibration only.

It does not implement:

- end-to-end orchestration;
- acquisition;
- evidence normalization;
- observations;
- signals;
- findings;
- limitations;
- guest journey analysis;
- recommendations;
- report assembly;
- API/UI;
- persistence;
- Google integrations;
- Antena integration;
- AI.

## Implementation Follow-up

A separate implementation requirement must introduce the smallest deterministic classifier using this contract before REQ-032 can supply a coverage state during end-to-end orchestration.

REQ-032 remains blocked until that governed classifier exists.

## Acceptance Criteria

1. The three REQ-024 coverage states have explicit semantic definitions.
2. Classification uses only governed coverage scope inputs.
3. The substantial calibration requires all five journey stages covered.
4. The substantial calibration requires at least six of nine intended dimensions covered.
5. The substantial calibration requires at least three journey stages with assessable evidence.
6. Partial classification requires meaningful covered scope but failure to satisfy substantial criteria.
7. Insufficient classification requires zero covered journey stages and zero covered dimensions.
8. Limited scope counts as covered scope but never as assessable evidence.
9. Unsupported dimensions remain uncovered.
10. Finding count does not influence classification.
11. Page count does not influence classification.
12. Missing evidence does not become a hotel deficiency.
13. Acquisition failure does not become a hotel deficiency.
14. Coverage state remains an analysis-capability statement, not a hotel-quality claim.
15. Same governed input produces the same classification.
16. Cross-evaluation inputs cannot be combined.
17. The contract does not introduce scoring, ranking, severity, recommendation priority, AI, or network behavior.

## Rationale

REQ-024 deliberately stopped at representation because `SPEC-006` did not define calibration. REQ-032 exposed the practical consequence: an end-to-end execution cannot honestly supply `HospitalityAnalysisCoverageState` without a governed source.

This requirement closes that semantic gap explicitly instead of hiding the decision inside the orchestrator.

The initial thresholds are deliberately simple and explainable. They can be recalibrated later using actual hotel-evaluation evidence without changing the fundamental truth boundary.

## Implementation Order

```text
REQ-033 — Coverage Classification Contract
        ↓
Coverage Classification Implementation
        ↓
REQ-032 — Evaluation Execution Orchestration
```

## Governance

This requirement is a product/specification decision and must be reviewed by the orchestrator before implementation begins.

**STOPPING FOR ORCHESTRATOR REVIEW.**
