# REQ-032 — Evaluation Execution Orchestration Foundation

STATUS: READY
REQUIREMENT_ID: REQ-032
TYPE: Implementation
BRANCH: `feature/evaluation-execution-orchestration-foundation`

## Dependency Resolution

REQ-032 was previously blocked after Session 32 repository reconciliation identified a missing governed source for `HospitalityAnalysisCoverageState`.

The dependency is now resolved:

- REQ-033 — Hospitality Analysis Coverage Classification Contract — accepted.
- REQ-034 — Hospitality Analysis Coverage Classification Implementation — IMPLEMENTED / MERGED as PR #32.
- Current `main` includes the governed `HospitalityAnalysisCoverageClassifier`.

The classifier provides the required deterministic caller-supplied coverage state without moving product calibration into the orchestration layer.

REQ-032 is therefore restored to `READY` after reconciliation against current `main`.

## Reconciliation Record

Current `main` after REQ-034 merge:

`b4a9e63b541c215fb5138cd66200a0ae5ca96d99`

Confirmed merged:

- REQ-027 — Hospitality Deficiency Analysis Foundation;
- REQ-028 — Hospitality Analysis Completeness Foundation;
- REQ-029 — Guest Journey Analysis Foundation;
- REQ-030 — Hospitality Recommendation Foundation;
- REQ-031 — Hospitality Analysis Report Foundation;
- REQ-034 — Hospitality Analysis Coverage Classification Implementation.

The existing analysis service still requires:

```text
analyze(
    evaluationId,
    evidenceItems,
    coverageState
)
```

The governed coverage classifier now supplies `coverageState` from the existing `HospitalityAnalysisCoverage` representation using the REQ-033 calibration. REQ-032 must invoke that classifier rather than inventing or duplicating classification rules.

## Objective

Introduce the smallest explicit end-to-end orchestration boundary for executing one canonical hotel evaluation through the existing Antena Hotel Check analysis pipeline.

The product objective remains:

> **Can a guest find, understand, trust, explore, and book this hotel online?**

The current product focus remains trustworthy digital presence analysis. Antena-hosted hotel experience generation at `<hotel-name>.antenapro.com` remains downstream.

## Intended Execution Flow

```text
Canonical Evaluation Request
        ↓
Evaluation / Attempt
        ↓
Public Web Acquisition
        ↓
Acquisition Result
        ↓
Structured Evidence
        ↓
Hospitality Observation
        ↓
Qualified Hospitality Analysis Signal
        ↓
Coverage Classification
        ↓
Deterministic Hospitality Analysis
        ↓
Hospitality Finding / Deficiency / Limitation
        ↓
Hospitality Analysis Result
        ↓
Guest Journey Analysis
        ↓
Hospitality Recommendations
        ↓
Structured Hospitality Analysis Report
```

The orchestrator must remain a thin coordinator and must not become the source of coverage semantics.

## Scope

Implement only:

1. one canonical evaluation request boundary;
2. existing evaluation/attempt lifecycle usage;
3. existing acquisition integration exactly once;
4. existing evidence normalization;
5. governed coverage classification using `HospitalityAnalysisCoverageClassifier`;
6. existing deterministic hospitality analysis;
7. existing guest-journey analysis;
8. existing recommendation generation;
9. existing report assembly;
10. one evaluation-attributed orchestration result.

Do not redesign downstream contracts.

## Explicit Non-Scope

Do not add:

- acquisition behavior;
- crawler/browser changes;
- retries;
- raw evidence parsing;
- analysis rules;
- coverage classification rules inside orchestration;
- scoring;
- prioritization;
- AI;
- Google integrations;
- API;
- UI;
- persistence;
- Antena integration;
- preview generation;
- generic workflow engines.

## Governance

The coverage-classification dependency has been resolved by REQ-034. REQ-032 is now `READY` for implementation.

The implementation session must still reconcile the actual current `main` before coding and must stop if another material contract gap is discovered.

## Final-Head Validation Rule

The requirement implementation record must reference validation performed against the exact final PR head. If the requirement file changes after a successful CI run, Backend Validation must be rerun against the resulting final head.
