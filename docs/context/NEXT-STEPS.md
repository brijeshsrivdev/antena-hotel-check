# Next Steps

## Current Position

REQ-033 — Hospitality Analysis Coverage Classification Contract is accepted and merged.

REQ-034 — Hospitality Analysis Coverage Classification Implementation is merged as PR #32.

REQ-035 — Hospitality Coverage Assessment Contract is implemented and merged as PR #34.

REQ-036 — Hospitality Coverage Assessment Derivation is implemented and merged as PR #36.

REQ-032 — Evaluation Execution Orchestration Foundation is now the active implementation slice on `feature/evaluation-execution-orchestration` and is **IN PROGRESS**.

## REQ-032 Current Direction

The active execution boundary is:

```text
Canonical Evaluation Request
        ↓
Evaluation / Attempt
        ↓
Existing acquisition integration
        ↓
Structured evidence
        ↓
Governed observations/signals/findings
        ↓
REQ-036 coverage assessment derivation
        ↓
Coverage classification
        ↓
Deterministic hospitality analysis
        ↓
Guest journey
        ↓
Recommendations
        ↓
Structured report
```

The orchestrator is sequencing existing services only. It must not absorb acquisition, evidence parsing, observation, signal, finding, limitation, coverage derivation, classifier, analysis, journey, recommendation, or report semantics.

## REQ-036 Implemented Boundary

REQ-036 defines and implements the governed derivation:

```text
Governed hospitality observations / qualified signals
                    ↓
       Coverage Assessment Derivation
                    ↓
      HospitalityCoverageAssessment
```

The existing mappings remain authoritative, including `DINING` → `AMENITIES_AND_GUEST_FACING_INFORMATION` and typed material same-evaluation identity conflict → `TRUST_AND_CLARITY`.

Explicit limitation category/journey context may create limited scope. Unscoped acquisition limitations do not receive guessed scope.

## REQ-032 Guardrails

- use one canonical request entry point;
- preserve one evaluation identity;
- call REQ-036 derivation before the classifier;
- pass the exact derivation result to the classifier;
- pass the exact classifier state to deterministic analysis;
- never default coverage state;
- preserve typed acquisition failures and inability-to-verify semantics;
- do not add preview generation, API, persistence, external integrations, queues, or workflow engines.

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
Coverage Assessment Derivation [REQ-036 IMPLEMENTED]
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

## Future Sequence

After REQ-032 implementation and review, reassess the actual repository state before defining the next slice. Likely future areas include:

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
