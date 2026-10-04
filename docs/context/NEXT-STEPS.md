# Next Steps

## Current Position

REQ-033 — Hospitality Analysis Coverage Classification Contract is accepted and merged.

REQ-034 — Hospitality Analysis Coverage Classification Implementation is merged as PR #32.

REQ-032 — Evaluation Execution Orchestration Foundation is **BLOCKED**.

Session 35 identified that the current classifier consumes final `HospitalityAnalysisCoverage`, while `HospitalityAnalysisService` requires the classifier's `HospitalityAnalysisCoverageState` before it creates that final coverage object. This is a real circular contract dependency.

## Current Next Slice

REQ-035 — Hospitality Coverage Assessment Contract — **READY**.

Requirement:

`requirements/REQ-035-hospitality-coverage-assessment-contract.md`

REQ-035 defines the pre-classification coverage fact boundary and does not change runtime behavior.

## Required Sequence

```text
REQ-035 — Coverage Assessment Contract — READY
        ↓
REQ-035 implementation / classifier-boundary integration
        ↓
REQ-032 — Evaluation Execution Orchestration
        ↓
End-to-End Real Evaluation Validation
```

REQ-032 must not be implemented by inventing a coverage state, duplicating REQ-033 rules, or constructing final `HospitalityAnalysisCoverage` merely to obtain the state.

## Coverage Assessment Contract

The intended conceptual boundary is:

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

REQ-035 reuses the existing typed `GuestJourneyStage` and `HospitalityAnalysisDimension` concepts and represents intended, assessable, and limited scope for exactly one evaluation.

Covered scope is assessable ∪ limited scope. Limited scope contributes to coverage but not to the assessable-stage threshold.

## REQ-032 Direction

Once unblocked, REQ-032 creates a single explicit orchestration service for one canonical evaluation execution.

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

REQ-024 establishes final coverage representation. REQ-033 and REQ-034 establish and implement governed classification. REQ-035 now establishes the missing pre-classification contract, but its runtime implementation does not yet exist.

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

Do not bypass the coverage-assessment/classification contract by defaulting a state inside orchestration. Do not duplicate calibration rules in the orchestrator. Do not make the classifier inspect raw evidence. Do not start REQ-032 until the pre-classification contract is implemented and the circular dependency is actually removed.
