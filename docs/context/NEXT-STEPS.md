# Next Steps

## Current Position

REQ-027 — Hospitality Deficiency Analysis Foundation is merged as PR #27.

REQ-028 — Hospitality Analysis Completeness Foundation is merged as PR #28.

REQ-029 — Guest Journey Analysis Foundation is merged as PR #29.

REQ-030 — Hospitality Recommendation Foundation is merged as PR #30.

REQ-031 — Hospitality Analysis Report Foundation is merged as PR #31.

REQ-020 — Orchestrator Durable Context Foundation is merged and establishes the durable handoff/context mechanism.

## Immediate Next Slice

REQ-032 — Evaluation Execution Orchestration Foundation — READY.

Implementation branch:

`feature/evaluation-execution-orchestration-foundation`

REQ-032 is the next bounded vertical slice. It moves the system from a collection of individually implemented analysis components toward one explicit end-to-end evaluation execution flow.

The implementation must consume existing acquisition, evidence, observation, analysis, journey, recommendation, and report contracts. It must not redesign those contracts.

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

REQ-023 establishes the truthful distinction between **unable to verify** and an observed hotel deficiency.

REQ-024 establishes coverage representation only. Coverage state remains governed and must not be invented from page/finding counts.

REQ-025 establishes the immutable aggregation boundary for findings, limitations, and coverage belonging to one evaluation.

REQ-026 establishes the first executable deterministic analysis pipeline over already-retained evidence and explicitly separates the full nine-dimension intended scope from the currently assessable scope.

REQ-027 establishes conservative evidence-grounded observed deficiencies.

REQ-028 establishes the first conservative completeness expansion and makes `TRUST_AND_CLARITY` assessable only where existing typed evidence establishes a material same-evaluation cross-source identity conflict.

REQ-029 establishes the first explicit guest-journey representation:

```text
DISCOVER → UNDERSTAND → EXPLORE → TRUST → BOOK
```

REQ-030 establishes deterministic bounded recommendations derived from governed deficiencies/semantics without scoring, prioritization, AI, or speculative advice.

REQ-031 establishes the first structured report-domain boundary by aggregating existing governed outputs without committing the project to UI, API, persistence, PDF, or public-report presentation.

## REQ-032 Direction

REQ-032 should create a single explicit orchestration service for one canonical evaluation execution.

```text
Canonical Evaluation Request
        ↓
Evaluation Execution Orchestrator
        ↓
Existing acquisition integration
        ↓
Structured evidence
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

It should make the end-to-end sequence testable and explicit while preserving current failure/truth semantics.

## Future Sequence

After REQ-032, reassess the actual repository state before defining the next slice. Likely future areas include:

- API boundary for starting/retrieving an evaluation;
- persistence and evaluation history;
- real-hotel end-to-end validation against controlled public targets;
- analysis quality/calibration and additional supported dimensions;
- customer-facing report rendering;
- connected digital-performance signals such as Google Business Profile, Search Console, GA4, or similar plan-gated capabilities;
- eventually Antena integration and `<hotel-name>.antenapro.com` experience generation.

These are **not yet implementation requirements** unless separately specified and marked READY.

## Product Sequence

```text
Hotel public digital presence
        ↓
Canonical Evaluation Request
        ↓
End-to-End Evaluation Orchestration  ← REQ-032
        ↓
Acquisition
        ↓
Structured Evidence
        ↓
Bounded Hospitality Observations
        ↓
Qualified Hospitality Analysis Signals
        ↓
Executable Deterministic Analysis
        ↓
Evidence-grounded deficiencies + truthful limitations
        ↓
Coverage representation
        ↓
Hospitality Analysis Result
        ↓
Guest Journey Analysis
        ↓
Actionable Recommendations
        ↓
Structured Analysis Report
        ↓
Mature, trustworthy analysis
        ↓
Connected Digital Performance (optional paid capability)
        ↓
Antena integration opportunity
        ↓
<hotel-name>.antenapro.com
```

## Standard Continuation Lifecycle

```text
Inspect main
    ↓
Identify next bounded slice
    ↓
Create READY requirement on main
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

After every merged agent PR, update durable orchestrator context before starting the next implementation session.

## Guardrails

Do not jump directly to a complete scoring engine, arbitrary coverage classifier, generic AI analysis, full hospitality ontology, recommendation prioritization, report rendering/UI, interactive Antena-hosted experience, broad crawling infrastructure, or generic SEO auditing. Each requires an explicit bounded requirement.

REQ-032 is intentionally bounded to orchestration of existing trustworthy components. It must not expand into API design, persistence, UI, connected integrations, preview generation, or Antena integration.
