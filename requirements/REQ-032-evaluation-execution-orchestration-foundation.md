# REQ-032 — Evaluation Execution Orchestration Foundation

STATUS: BLOCKED
REQUIREMENT_ID: REQ-032
TYPE: Implementation
BRANCH: `feature/evaluation-execution-orchestration-foundation`

## Dependency Resolution Status

REQ-032 is currently blocked by a contract gap discovered while reconciling the intended execution sequence against the actual current `main`.

The existing deterministic analysis service requires:

```text
analyze(
    evaluationId,
    evidenceItems,
    coverageState
)
```

The governed `HospitalityAnalysisCoverageClassifier` exists, but its current input is `HospitalityAnalysisCoverage`. That final coverage object is created by `HospitalityAnalysisService` only after the caller has already supplied `coverageState`.

This creates a circular dependency:

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

REQ-033 and REQ-034 correctly established and implemented coverage calibration, but they did not provide a pre-classification source for the coverage facts. Therefore REQ-032 must not invent a workaround.

REQ-035 — Hospitality Coverage Assessment Contract — now defines the missing pre-classification contract.

REQ-032 remains `BLOCKED` until the REQ-035 contract is implemented and the classifier boundary is updated accordingly.

## Reconciliation Record

Current `main` at session start:

`8a9ade5700410776136a8ad7b94d8fd8272b58e6`

Confirmed existing foundations include:

- REQ-024 — Hospitality Analysis Coverage Foundation;
- REQ-026 — Deterministic Hospitality Analysis Engine;
- REQ-033 — Hospitality Analysis Coverage Classification Contract;
- REQ-034 — Hospitality Analysis Coverage Classification Implementation.

The repository implementation was inspected directly. `HospitalityAnalysisService` creates `HospitalityAnalysisCoverage` only after receiving a caller-supplied `HospitalityAnalysisCoverageState`; `HospitalityAnalysisCoverageClassifier` currently consumes that final coverage object and returns its state.

No pre-analysis coverage assessment contract exists on `main`.

## Objective

Introduce the smallest explicit end-to-end orchestration boundary for executing one canonical hotel evaluation through the existing Antena Hotel Check analysis pipeline, once the pre-classification coverage contract is implemented.

The product objective remains:

> **Can a guest find, understand, trust, explore, and book this hotel online?**

The interactive Antena-hosted hotel experience remains downstream.

## Intended Execution Flow

The corrected conceptual sequence is:

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
Governed Coverage Assessment
        ↓
HospitalityAnalysisCoverageClassifier
        ↓
HospitalityAnalysisCoverageState
        ↓
Deterministic Hospitality Analysis
        ↓
Hospitality Finding / Deficiency / Limitation
        ↓
Hospitality Analysis Coverage
        ↓
Hospitality Analysis Result
        ↓
Guest Journey Analysis
        ↓
Hospitality Recommendations
        ↓
Structured Hospitality Analysis Report
```

At the higher-level product boundary this is:

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

The orchestrator remains a thin coordinator and must not become the source of coverage semantics.

## Scope

When unblocked, implement only:

1. one canonical evaluation request boundary;
2. existing evaluation/attempt lifecycle usage;
3. existing acquisition integration exactly once;
4. existing evidence normalization;
5. governed pre-classification coverage assessment;
6. governed coverage classification using `HospitalityAnalysisCoverageClassifier`;
7. existing deterministic hospitality analysis;
8. existing guest-journey analysis;
9. existing recommendation generation;
10. existing report assembly;
11. one evaluation-attributed orchestration result.

Do not redesign downstream contracts.

## Explicit Non-Scope

Do not add:

- acquisition behavior;
- crawler/browser changes;
- retries;
- raw evidence parsing;
- analysis rules;
- coverage classification rules inside orchestration;
- duplicate coverage rules;
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

## Coverage Contract Boundary

REQ-035 establishes `HospitalityCoverageAssessment` as the pre-classification fact boundary.

The conceptual relationship is:

```text
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

The orchestrator must not construct a final `HospitalityAnalysisCoverage` merely to obtain the state required by analysis.

The orchestrator must not default, guess, or derive a state independently.

The classifier remains the sole owner of the REQ-033 coverage calibration.

## Governance

REQ-032 is blocked by the REQ-035 contract and its subsequent implementation.

Required sequence before REQ-032 implementation:

```text
REQ-035 — Hospitality Coverage Assessment Contract — READY
        ↓
REQ-035 implementation / classifier-boundary integration
        ↓
REQ-032 — Evaluation Execution Orchestration
```

The blocker is explicitly architectural/contractual, not an implementation invitation to move classification logic into the orchestrator.

## Final-Head Validation Rule

The requirement implementation record must reference validation performed against the exact final PR head. If the requirement file changes after a successful CI run, Backend Validation must be rerun against the resulting final head.

**Blocked by REQ-035 contract and subsequent implementation.**
