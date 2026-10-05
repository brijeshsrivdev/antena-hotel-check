# Next Steps

## Current Position

REQ-033 — Hospitality Analysis Coverage Classification Contract is accepted and merged.

REQ-034 — Hospitality Analysis Coverage Classification Implementation is merged as PR #32.

REQ-035 — Hospitality Coverage Assessment Contract is implemented in PR #34 and is awaiting orchestrator review/merge.

REQ-032 — Evaluation Execution Orchestration Foundation remains **BLOCKED** as a separate implementation slice.

## REQ-035 Implementation

The runtime pre-classification boundary is now:

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

`HospitalityCoverageAssessment` is immutable and contains exactly one evaluation identity plus intended, assessable, and limited journey/dimension scope. Covered scope is derived as assessable ∪ limited scope.

The classifier now consumes the assessment directly. REQ-033 calibration remains unchanged.

## Validation Status

PR #34's GitHub Actions Backend Validation completed successfully against commit `a9e0ceeac492585d8382af82b9f3447484a5e497` before the durable-context updates. Because these context files were changed afterward, the final PR head requires another Backend Validation run before REQ-035 can be considered fully PR_READY.

The final-head rule is intentional: documentation/context is part of the deliverable and CI must validate the exact final PR head.

## REQ-032 Direction

Once REQ-035 is reviewed and merged, REQ-032 can be reconsidered as the next implementation slice.

It should create a single explicit orchestration service for one canonical evaluation execution:

```text
Canonical Evaluation Request
        ↓
Evaluation Execution Orchestrator
        ↓
Existing acquisition integration
        ↓
Structured evidence
        ↓
Governed coverage assessment
        ↓
Coverage classification
        ↓
Existing deterministic analysis pipeline
        ↓
Guest journey
        ↓
Recommendations
        ↓
Structured report
```

The orchestrator should coordinate existing contracts rather than absorb their responsibilities.

## Existing Coverage Classification

REQ-033 remains authoritative:

- `SUBSTANTIALLY_ASSESSED`: all five journey stages covered; at least six of nine intended dimensions covered; at least three journey stages have assessable evidence.
- `PARTIALLY_ASSESSED`: at least one intended journey stage or dimension is covered, but substantial criteria are not satisfied.
- `INSUFFICIENT_COVERAGE`: zero intended journey stages and zero intended dimensions are covered.

These are analysis-capability classifications, not hotel-quality scores.

## Completed Analysis Foundation

```text
Acquisition
  ↓
Structured Evidence
  ↓
Hospitality Observation
  ↓
Qualified Analysis Signal
  ↓
Governed Coverage Assessment
  ↓
Governed Coverage Classification
  ↓
Hospitality Finding / Deficiency / Unable-to-Verify Limitation
  ↓
Hospitality Analysis Coverage
  ↓
Hospitality Analysis Result
  ↓
Guest Journey Analysis
  ↓
Actionable Recommendations
  ↓
Structured Analysis Report
```

REQ-023 establishes the truthful distinction between unable-to-verify and an observed hotel deficiency.

REQ-024 establishes final coverage representation. REQ-033 and REQ-034 establish and implement governed classification. REQ-035 now provides the missing runtime pre-classification contract.

## Future Sequence

After REQ-032, reassess the actual repository state before defining the next slice. Likely future areas include:

- real-hotel end-to-end validation against controlled public targets;
- API boundary for starting/retrieving an evaluation;
- persistence and evaluation history;
- analysis quality/calibration and additional supported dimensions;
- customer-facing report rendering;
- connected digital-performance signals;
- eventually Antena integration and `<hotel-name>.antenapro.com` experience generation.

These are not current implementation requirements unless separately specified and marked READY.

## Standard Continuation Lifecycle

```text
Inspect main
    ↓
Identify next bounded slice
    ↓
Create/refine READY requirement on main
    ↓
Implementation session
    ↓
Tests + CI
    ↓
PR
    ↓
Orchestrator review
    ↓
Fix/re-review if required
    ↓
Merge
    ↓
Update durable context
```

## Guardrails

Do not bypass the coverage-assessment/classification contract by defaulting a state inside orchestration. Do not duplicate calibration rules in the orchestrator. Do not make the classifier inspect raw evidence. Do not start REQ-032 until REQ-035 is reviewed/merged and the separate orchestration requirement is ready.
