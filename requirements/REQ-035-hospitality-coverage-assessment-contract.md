# REQ-035 — Hospitality Coverage Assessment Contract

STATUS: PR_READY
REQUIREMENT_ID: REQ-035
TYPE: Implementation
BRANCH: `feature/hospitality-coverage-assessment-contract`

## Implementation Summary

Implemented the runtime pre-classification `HospitalityCoverageAssessment` domain contract and reconciled the existing coverage classifier to consume it.

The runtime boundary is now:

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
```

The final `HospitalityAnalysisCoverage` representation remains unchanged.

## Runtime Model

`HospitalityCoverageAssessment` is an immutable Java record containing:

```text
evaluationId
intendedJourneyStages
intendedDimensions
assessableJourneyStages
assessableDimensions
limitedJourneyStages
limitedDimensions
```

It reuses the existing `UUID`, `GuestJourneyStage`, and `HospitalityAnalysisDimension` types. No duplicate enums or string-based taxonomy was introduced.

The assessment contains no classification state, findings, limitations, score, quality claim, or evidence-processing behavior.

## Validation

The constructor enforces:

- non-null evaluation identity;
- non-null intended/assessable/limited collections;
- assessable journey scope contained by intended journey scope;
- limited journey scope contained by intended journey scope;
- assessable dimension scope contained by intended dimension scope;
- limited dimension scope contained by intended dimension scope;
- assessable and limited journey scope are disjoint;
- assessable and limited dimension scope are disjoint.

Empty scope is valid, including an assessment with zero intended and zero covered scope, so the classifier can represent `INSUFFICIENT_COVERAGE`.

No raw evidence, HTML, page count, finding count, HTTP status, network call, AI output, or limitation taxonomy is inspected by the assessment.

## Covered Semantics

The assessment exposes:

```text
coveredJourneyStages() = assessableJourneyStages ∪ limitedJourneyStages
coveredDimensions()   = assessableDimensions ∪ limitedDimensions
```

Covered collections are derived rather than stored as independent mutable state and are immutable to callers.

Limited scope contributes to covered scope but remains distinct from assessable evidence.

## Classifier Integration

`HospitalityAnalysisCoverageClassifier.classify(...)` now accepts `HospitalityCoverageAssessment` rather than final `HospitalityAnalysisCoverage`.

REQ-033 calibration remains unchanged:

- all five journey stages covered;
- at least six dimensions covered;
- at least three journey stages assessable;
- otherwise meaningful coverage is partial;
- zero covered journey stages and zero covered dimensions is insufficient.

The classifier does not inspect evidence, findings, pages, recommendations, scores, or hotel content.

`HospitalityAnalysisService` remains unchanged and continues to receive the resulting `HospitalityAnalysisCoverageState` before creating final coverage.

## Immutability

The assessment defensively copies all constructor collections using immutable collections. Returned covered collections are also immutable.

Mutating source collections after construction does not mutate the assessment.

## Tests

Added focused unit coverage for:

- valid construction;
- empty coverage;
- covered-scope union semantics;
- journey assessable/limited overlap rejection;
- dimension assessable/limited overlap rejection;
- journey containment;
- dimension containment;
- null required inputs;
- defensive copying;
- returned collection immutability;
- evaluation independence;
- classifier substantial/partial/insufficient calibration;
- limited-scope semantics;
- classifier input immutability;
- deterministic classification;
- null classifier input.

## Regression Validation

The existing classifier tests were migrated from final `HospitalityAnalysisCoverage` input to `HospitalityCoverageAssessment` input while preserving the existing behavioral assertions.

The complete backend Maven test suite is required for final PR validation.

## Architectural Decisions

1. `HospitalityCoverageAssessment` is the pre-classification fact boundary.
2. `HospitalityAnalysisCoverage` remains the final downstream analysis coverage representation.
3. Coverage classification remains owned by `HospitalityAnalysisCoverageClassifier`.
4. REQ-033 thresholds remain unchanged.
5. No orchestrator, persistence, API, acquisition, evidence-processing, or external integration was added.

## Limitations

REQ-035 does not implement evaluation orchestration. REQ-032 remains `BLOCKED` until the orchestrator can consume the assessment/classifier boundary and pass the resulting state into deterministic analysis.

This requirement does not define how upstream acquisition/evidence processing decides that a scope item is assessable or limited. It only validates and represents those already-governed facts.

## Self-Review

- No duplicate journey-stage or dimension taxonomy introduced.
- No final coverage model replaced.
- No coverage state added to the assessment.
- No independent covered collections stored.
- No raw evidence inspection added.
- No orchestrator introduced.
- No persistence/API/network integration introduced.
- Diff is limited to the assessment contract, classifier boundary, and focused tests.

**PR_READY after final-head Backend Validation succeeds.**

**REQ-032 remains BLOCKED.**
