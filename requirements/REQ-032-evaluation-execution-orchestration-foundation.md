# REQ-032 — Evaluation Execution Orchestration Foundation

STATUS: BLOCKED
REQUIREMENT_ID: REQ-032
TYPE: Implementation
BRANCH: `feature/evaluation-execution-orchestration-foundation`

## Blocker

REQ-032 is intentionally blocked after Session 32 repository reconciliation identified a missing governed source for `HospitalityAnalysisCoverageState`.

The existing `HospitalityAnalysisService.analyze(...)` contract requires a caller-supplied `HospitalityAnalysisCoverageState`. REQ-024 explicitly established coverage representation only and states that the coverage state must be an already-governed classification; it does not calculate or classify the state. The current `CanonicalEvaluationRequest` contains no coverage classification input.

Therefore REQ-032 cannot safely invent a default such as `PARTIALLY_ASSESSED` or derive a state from page/finding counts or arbitrary thresholds.

### Required dependency

`REQ-033 — Hospitality Analysis Coverage Classification Contract` now defines the product calibration required to create a governed classifier.

Implementation order:

```text
REQ-033 — Coverage Classification Contract
        ↓
Coverage Classification Implementation
        ↓
REQ-032 — Evaluation Execution Orchestration
```

REQ-032 must remain `BLOCKED` until the governed coverage classifier is implemented and merged.

## Session 32 Reconciliation Record

The implementation session inspected current `main` at:

`9425d4209069c51c8441b5da78f67854dd8bf53b`

Confirmed on `main`:

- REQ-027 merged;
- REQ-028 merged;
- REQ-029 merged;
- REQ-030 merged;
- REQ-031 merged;
- REQ-032 exists and was `READY` before this reconciliation;
- acquisition integration exists;
- evidence normalization exists;
- deterministic hospitality analysis exists;
- guest-journey analysis exists;
- recommendation generation exists;
- structured report assembly exists.

The analysis service currently requires:

```text
analyze(
    evaluationId,
    evidenceItems,
    coverageState
)
```

The coverage state is not derived by that service. The existing coverage model explicitly treats classification as governed caller input and does not define thresholds. The canonical evaluation request also does not contain a coverage state.

The session correctly stopped without creating a branch, modifying application code, or creating a PR.

## Original Objective

Once the coverage-classification dependency is available, REQ-032 will introduce the smallest explicit end-to-end orchestration boundary for executing one canonical hotel evaluation through the existing Antena Hotel Check analysis pipeline.

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

## Scope After Blocker Resolution

When unblocked, implement only:

1. one canonical evaluation request boundary;
2. existing evaluation/attempt lifecycle usage;
3. existing acquisition integration exactly once;
4. existing evidence normalization;
5. governed coverage classification;
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

`STATUS: READY` may be restored only after the coverage classification implementation is merged and the orchestrator requirement has been reconciled against the resulting `main`.

Until then:

**STOPPING — BLOCKED BY GOVERNED COVERAGE CLASSIFICATION DEPENDENCY.**
