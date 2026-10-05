# REQ-032 — Evaluation Execution Orchestration Foundation

STATUS: BLOCKED
REQUIREMENT_ID: REQ-032
TYPE: Implementation
BRANCH: `feature/evaluation-execution-orchestration-foundation`

## Dependency Resolution Status

REQ-032 remains blocked by the coverage-assessment contract chain discovered while reconciling the intended execution sequence against the actual repository.

The deterministic analysis service requires a `HospitalityAnalysisCoverageState` before it creates final `HospitalityAnalysisCoverage`. The governed classifier now consumes the pre-classification `HospitalityCoverageAssessment` established by REQ-035, but a separate governed derivation contract is required to determine how upstream observations/signals populate that assessment.

The dependency chain is now explicit:

```text
Governed hospitality observations / signals
        ↓
REQ-036 — Hospitality Coverage Assessment Derivation Contract
        ↓
HospitalityCoverageAssessment
        ↓
REQ-034 — HospitalityAnalysisCoverageClassifier
        ↓
HospitalityAnalysisCoverageState
        ↓
HospitalityAnalysisService
```

REQ-035 solved the representation/boundary problem. REQ-036 now defines the missing factual derivation semantics. REQ-032 must not invent those semantics inside orchestration.

REQ-032 remains `BLOCKED` until REQ-036 is implemented and the complete assessment → classifier → analysis sequence is available for orchestration.

## Reconciliation Record

Current `main` after REQ-035 implementation contains:

- REQ-024 — Hospitality Analysis Coverage Foundation;
- REQ-026 — Deterministic Hospitality Analysis Engine;
- REQ-033 — Hospitality Analysis Coverage Classification Contract;
- REQ-034 — Hospitality Analysis Coverage Classification Implementation;
- REQ-035 — Hospitality Coverage Assessment Contract and runtime implementation;
- REQ-036 — Hospitality Coverage Assessment Derivation Contract (specification boundary).

The repository implementation was inspected directly. `HospitalityAnalysisService` still derives factual coverage scope internally while receiving the coverage state as caller-supplied input. `HospitalityCoverageAssessment` and the classifier boundary now exist, but the governed transformation from existing observations/signals into that assessment is the remaining contract/implementation gap.

## Objective

Introduce the smallest explicit end-to-end orchestration boundary for executing one canonical hotel evaluation through the existing Antena Hotel Check analysis pipeline, once the pre-classification coverage derivation contract is implemented.

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
Governed Hospitality Observations / Signals
        ↓
REQ-036 Coverage Assessment Derivation
        ↓
HospitalityCoverageAssessment
        ↓
REQ-034 Coverage Classification
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
→ governed observations/signals
→ coverage assessment derivation
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
5. governed pre-classification coverage assessment using the REQ-036 contract;
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
- coverage-assessment derivation rules inside orchestration;
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

REQ-035 establishes `HospitalityCoverageAssessment` as the pre-classification fact representation.

REQ-036 establishes the governed derivation semantics for populating that representation from existing observations/signals and explicitly scoped limitation/identity-conflict facts.

The conceptual relationship is:

```text
Governed observations/signals
        ↓
REQ-036 Coverage Assessment Derivation
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

The orchestrator must not construct a final `HospitalityAnalysisCoverage` merely to obtain the state required by analysis.

The orchestrator must not default, guess, or derive a state independently.

The classifier remains the sole owner of the REQ-033 coverage calibration.

The assessment derivation remains the sole governed owner of the mapping from existing analysis facts to assessment scope.

## Governance

REQ-032 is blocked by the REQ-036 derivation contract and its subsequent implementation, building on the REQ-035 representation/classifier boundary.

Required sequence before REQ-032 implementation:

```text
REQ-035 — Hospitality Coverage Assessment Contract — implemented
        ↓
REQ-036 — Hospitality Coverage Assessment Derivation Contract — READY
        ↓
REQ-036 implementation
        ↓
REQ-032 — Evaluation Execution Orchestration
```

The blocker is explicitly architectural/contractual, not an implementation invitation to move classification or derivation logic into the orchestrator.

## Final-Head Validation Rule

The requirement implementation record must reference validation performed against the exact final PR head. If the requirement file changes after a successful CI run, Backend Validation must be rerun against the resulting final head.

**Blocked by REQ-036 derivation contract and subsequent implementation.**
