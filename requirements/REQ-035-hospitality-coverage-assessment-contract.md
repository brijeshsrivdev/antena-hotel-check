# REQ-035 — Hospitality Coverage Assessment Contract

STATUS: READY
REQUIREMENT_ID: REQ-035
TYPE: Specification / Product Contract
BRANCH: `feature/hospitality-coverage-assessment-contract`

## 1. Problem Statement

REQ-032 cannot currently execute the intended end-to-end sequence because the deterministic `HospitalityAnalysisService` requires a caller-supplied `HospitalityAnalysisCoverageState`, while the existing `HospitalityAnalysisCoverage` object is created by that same service only after the state has already been supplied.

The current dependency is therefore circular:

```text
HospitalityAnalysisService
        ↓ requires
HospitalityAnalysisCoverageState
        ↑ produced by
HospitalityAnalysisCoverageClassifier
        ↑ currently consumes
HospitalityAnalysisCoverage
        ↑ currently created by
HospitalityAnalysisService
```

The previous implementation session correctly stopped rather than inventing a default state, duplicating classification rules, or making the classifier inspect raw evidence.

REQ-035 establishes the missing pre-classification contract so the governed coverage classifier can receive coverage facts before deterministic analysis executes.

## 2. Architectural Context

The intended evaluation boundary is:

```text
Evidence / governed upstream facts
            ↓
HospitalityCoverageAssessment
            ↓
HospitalityAnalysisCoverageClassifier
            ↓
HospitalityAnalysisCoverageState
            ↓
HospitalityAnalysisService
            ↓
HospitalityAnalysisCoverage
            ↓
HospitalityAnalysisResult
```

`HospitalityCoverageAssessment` is a pre-classification fact representation. It is not the final coverage representation and it does not contain a coverage classification state.

The existing `HospitalityAnalysisCoverage` remains the final analysis coverage representation used by `HospitalityAnalysisResult`. REQ-024 intentionally established that representation, while REQ-033 established the product calibration and REQ-034 implemented the current deterministic classifier.

The separation is required because classification is a product-calibration decision and must not be hidden inside deterministic analysis orchestration.

## 3. Scope

REQ-035 defines the smallest useful immutable pre-classification coverage contract with:

- exactly one evaluation identity;
- intended guest-journey scope;
- intended hospitality-dimension scope;
- assessable journey stages;
- assessable hospitality dimensions;
- limited journey stages;
- limited hospitality dimensions.

The contract reuses the existing typed domain concepts:

- `UUID` for evaluation identity;
- `GuestJourneyStage` for the existing five-stage guest journey;
- `HospitalityAnalysisDimension` for the existing nine-dimension hospitality taxonomy.

The contract preserves the existing distinction that covered scope consists of explicitly assessable or explicitly limited scope.

For this contract:

```text
covered journey stages
    = assessable journey stages ∪ limited journey stages

covered dimensions
    = assessable dimensions ∪ limited dimensions
```

The union is semantic, not an additional duplicated mutable field. This prevents contradictory `covered` and `assessable` inputs.

## 4. Non-Scope

REQ-035 does not implement:

- Java classes or runtime behavior;
- changes to `HospitalityAnalysisCoverage`;
- changes to `HospitalityAnalysisCoverageState`;
- changes to `HospitalityAnalysisCoverageClassifier`;
- changes to `HospitalityAnalysisService`;
- evaluation orchestration;
- acquisition, crawling, browser automation, or network access;
- evidence parsing or normalization;
- new observations, signals, findings, limitations, or recommendation rules;
- coverage thresholds or new classification states;
- scoring, ranking, severity, grading, or quality metrics;
- persistence, schema, API, UI, report rendering, or preview generation;
- a generic assessment/rules framework;
- a second journey taxonomy or dimension taxonomy.

## 5. Contract Definition

The conceptual contract is an immutable value object equivalent to:

```text
HospitalityCoverageAssessment(
    evaluationId,
    intendedJourneyStages,
    intendedDimensions,
    assessableJourneyStages,
    assessableDimensions,
    limitedJourneyStages,
    limitedDimensions
)
```

### Evaluation identity

`evaluationId` is required and identifies exactly one canonical evaluation.

### Intended journey stages

`intendedJourneyStages` uses only the existing `GuestJourneyStage` type and the existing five-stage taxonomy:

```text
DISCOVER
UNDERSTAND
EXPLORE
TRUST
BOOK
```

The assessment must not introduce additional stages or aliases.

### Intended dimensions

`intendedDimensions` uses only the existing `HospitalityAnalysisDimension` type and the existing nine-dimension taxonomy:

1. `HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING`
2. `DISCOVERABILITY_AND_NAVIGATION`
3. `ROOMS_AND_ROOM_INFORMATION`
4. `AMENITIES_AND_GUEST_FACING_INFORMATION`
5. `CONTACT_AND_LOCATION`
6. `BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS`
7. `TRUST_AND_CLARITY`
8. `MOBILE_AND_TECHNICAL_GUEST_EXPERIENCE`
9. `SEO_AND_STRUCTURED_DATA_SUPPORTING_SIGNALS`

The assessment must not create, rename, alias, or duplicate dimensions.

### Assessable scope

`assessableJourneyStages` and `assessableDimensions` identify areas for which governed evidence is sufficiently assessable under the existing upstream contracts.

Assessable scope is positive analysis capability. It must not be inferred merely from attempted acquisition or the existence of a taxonomy entry.

### Limited scope

`limitedJourneyStages` and `limitedDimensions` identify areas that were explicitly accounted for but could not be fully assessed under existing evidence/provenance constraints.

Limited scope contributes to covered scope but is not assessable evidence.

### Structural invariants

The contract must preserve these relationships:

```text
assessableJourneyStages ⊆ intendedJourneyStages
limitedJourneyStages    ⊆ intendedJourneyStages

assessableDimensions    ⊆ intendedDimensions
limitedDimensions       ⊆ intendedDimensions
```

An implementation of this contract must defensively preserve collection immutability and reject null required values. It must not silently infer scope from page counts, finding counts, URLs, HTTP statuses, or raw content.

## 6. Covered vs Assessable Semantics

The distinction is mandatory:

```text
covered ≠ assessable
```

A stage or dimension can be covered while not being assessable because it is explicitly limited.

Example:

```text
ROOMS:
    covered = true
    assessable = false
    limited = true
```

This is valid and must remain distinguishable from an area that was never accounted for.

The following must not become positive evidence:

- `UNABLE_TO_VERIFY` by itself;
- `NOT_ATTEMPTED`;
- `UNSUPPORTED`.

An acquisition or verification failure may justify limited scope only where the existing governed limitation contract supports that conclusion. Missing evidence must not be converted into hotel-feature absence.

## 7. Journey-Stage Semantics

The assessment uses the existing five-stage journey without modification:

```text
DISCOVER → UNDERSTAND → EXPLORE → TRUST → BOOK
```

A journey stage is covered when it is explicitly assessable or explicitly limited.

An assessable stage contributes to the REQ-033 assessable-stage threshold. A limited stage contributes to covered-stage counts but does not contribute to that assessable-stage threshold.

The assessment does not infer guest-journey success, failure, quality, or conversion probability.

## 8. Dimension Semantics

The assessment uses the existing nine intended hospitality dimensions exactly as governed by SPEC-006 and REQ-024/026.

A dimension is covered when it is explicitly assessable or explicitly limited.

An unsupported dimension remains uncovered unless an upstream governed contract explicitly establishes assessable or limited scope for that dimension.

The assessment must not mark a dimension assessable merely because the dimension exists in the intended ontology.

No new dimension is introduced for dining, OTA, mobile, SEO, or any other capability.

## 9. Evaluation Identity

Every `HospitalityCoverageAssessment` belongs to exactly one `evaluationId`.

All scope facts in the assessment are interpreted within that evaluation.

Cross-evaluation composition is invalid. A future implementation must reject attempts to combine or classify coverage facts belonging to different evaluations rather than merging them.

The evaluation identity is an isolation/provenance boundary, not a classification signal.

## 10. Relationship to REQ-033

REQ-033 remains the authoritative product contract for classification.

The assessment must provide the facts required to apply the existing calibration without redefining it:

### SUBSTANTIALLY_ASSESSED

```text
all 5 journey stages covered
AND
at least 6 of 9 dimensions covered
AND
at least 3 journey stages have assessable evidence
```

### INSUFFICIENT_COVERAGE

```text
0 covered journey stages
AND
0 covered dimensions
```

### PARTIALLY_ASSESSED

Everything else with meaningful coverage.

REQ-035 does not add thresholds, alter the three states, or reinterpret limited scope. A subsequent implementation must adapt the classifier boundary so these existing rules consume `HospitalityCoverageAssessment` rather than requiring a final `HospitalityAnalysisCoverage` object that does not yet exist.

## 11. Relationship to REQ-032

REQ-032 is blocked by this contract gap and must not proceed to implementation until the assessment boundary is implemented and integrated.

The corrected conceptual execution sequence is:

```text
acquisition
→ evidence
→ governed coverage assessment
→ coverage classifier
→ deterministic analysis
→ journey
→ recommendations
→ report
```

REQ-032 must consume the classifier's governed state and pass that state into `HospitalityAnalysisService`.

REQ-032 must not:

- invent a default coverage state;
- duplicate REQ-033 thresholds;
- classify from page/finding counts;
- inspect raw evidence for classification;
- construct a final `HospitalityAnalysisCoverage` solely to obtain a state.

## 12. Relationship to Existing `HospitalityAnalysisCoverage`

`HospitalityCoverageAssessment` does not replace or rename `HospitalityAnalysisCoverage`.

The intended relationship is:

```text
HospitalityCoverageAssessment
        ↓
CoverageClassifier
        ↓
HospitalityAnalysisCoverageState
        ↓
HospitalityAnalysisCoverage
```

The assessment is intentionally narrower than final coverage. It contains governed scope facts required before classification but does not contain:

- the final classification state;
- supporting findings;
- supporting limitations;
- final coverage rationale.

`HospitalityAnalysisCoverage` remains the final analysis representation and remains part of `HospitalityAnalysisResult`.

The final coverage may continue to retain its existing findings/limitations and state after deterministic analysis has executed. REQ-035 does not redesign that model.

## 13. Failure / Validation Expectations

A conforming implementation of this contract must reject structurally invalid assessments, including:

- null evaluation identity;
- null required scope collections;
- assessable journey stages outside intended journey stages;
- limited journey stages outside intended journey stages;
- assessable dimensions outside intended dimensions;
- limited dimensions outside intended dimensions;
- cross-evaluation artifacts when such artifacts are attached by a later implementation boundary.

The contract must not reject an assessment merely because:

- coverage is partial;
- coverage is insufficient;
- a dimension is unsupported;
- a journey stage is limited;
- an area has no positive finding.

Those are product-analysis semantics, not structural invalidity.

The contract must not infer a hotel deficiency from inability to assess an area.

## 14. Acceptance Criteria

1. A pre-classification assessment can be attributed to exactly one evaluation.
2. The assessment reuses the existing `GuestJourneyStage` taxonomy.
3. The assessment reuses the existing nine `HospitalityAnalysisDimension` values.
4. Intended journey and dimension scope are explicitly represented.
5. Assessable journey and dimension scope are explicitly represented.
6. Limited journey and dimension scope are explicitly represented.
7. Covered journey scope is deterministically represented by assessable ∪ limited scope.
8. Covered dimension scope is deterministically represented by assessable ∪ limited scope.
9. Limited scope contributes to coverage but not assessable-stage counting.
10. `UNABLE_TO_VERIFY`, `NOT_ATTEMPTED`, and `UNSUPPORTED` are not converted into positive evidence by this contract.
11. Missing evidence does not become a hotel deficiency.
12. Scope is constrained to intended scope.
13. Cross-evaluation composition is rejectable.
14. The assessment contains no coverage classification state.
15. The assessment contains no score, percentage, grade, severity, ranking, recommendation priority, or quality claim.
16. The assessment does not require page counts, finding counts, URL counts, HTTP status counts, raw HTML, AI output, or network access.
17. REQ-033's existing three-state calibration can be applied without redefining its rules.
18. The final `HospitalityAnalysisCoverage` remains unchanged as the downstream analysis coverage representation.
19. REQ-032's dependency can be expressed as assessment → classifier → state → analysis without a circular dependency.
20. No runtime behavior is changed by REQ-035 itself.

## 15. Explicit Non-Goals

REQ-035 is not:

- a second coverage classifier;
- a replacement for `HospitalityAnalysisCoverage`;
- an evidence model;
- an acquisition model;
- an analysis engine;
- an orchestration engine;
- a scoring system;
- a quality model;
- a report model;
- a persistence contract;
- an API contract;
- a generic rules framework.

It exists solely to establish the missing pre-classification coverage fact boundary needed to make the existing governed classification and deterministic analysis contracts composable.

## Governance

This requirement is a specification/architecture reconciliation artifact. It is `READY` for a separate implementation session.

No Java source, tests, persistence, API, or runtime behavior is changed by this requirement.

**STOPPING FOR ORCHESTRATOR REVIEW.**
