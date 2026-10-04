# Next Steps

## Current Position

REQ-027 — Hospitality Deficiency Analysis Foundation is merged as PR #27.

REQ-028 — Hospitality Analysis Completeness Foundation is merged as PR #28.

REQ-029 — Guest Journey Analysis Foundation is merged as PR #29.

REQ-030 — Hospitality Recommendation Foundation is merged as PR #30.

REQ-031 — Hospitality Analysis Report Foundation is merged as PR #31.

REQ-020 — Orchestrator Durable Context Foundation is merged and establishes the durable handoff/context mechanism.

## Current Blocker

REQ-032 — Evaluation Execution Orchestration Foundation — BLOCKED.

Session 32 correctly stopped during repository reconciliation because the existing deterministic analysis service requires a caller-supplied `HospitalityAnalysisCoverageState`, but the canonical evaluation request has no coverage classification input and REQ-024 intentionally provides representation rather than classification.

The orchestrator must not silently default the state or invent thresholds inside the execution layer.

## Immediate Next Slice

REQ-033 — Hospitality Analysis Coverage Classification Contract — READY.

Requirement:

`requirements/REQ-033-hospitality-analysis-coverage-classification-contract.md`

REQ-033 defines the product semantics/calibration for the three already-governed coverage states. It is a specification boundary, not the classifier implementation itself.

The next implementation slice after REQ-033 review should implement the classifier, then REQ-032 can be reconciled and implemented.

## Required Sequence

```text
REQ-033 — Coverage Classification Contract
        ↓
Coverage Classification Implementation
        ↓
REQ-032 — Evaluation Execution Orchestration
        ↓
End-to-End Real Evaluation Validation
```

## Coverage Classification Direction

The initial governed calibration in REQ-033 is:

- `SUBSTANTIALLY_ASSESSED`: all five journey stages covered; at least six of nine intended dimensions covered; at least three journey stages have assessable evidence.
- `PARTIALLY_ASSESSED`: at least one intended journey stage or dimension is covered, but substantial criteria are not satisfied.
- `INSUFFICIENT_COVERAGE`: zero intended journey stages and zero intended dimensions are covered.

Covered means explicitly assessable or explicitly limited. Limited scope contributes to accounted-for coverage but does not count toward the three assessable journey stages required for substantial assessment.

These are product calibration rules, not quality scores, and may be revised later using real evaluation data.

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

REQ-024 establishes coverage representation only. Coverage state must be governed and must not be invented from page/finding counts.

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

## REQ-032 Direction After Unblocking

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
Governed coverage classification
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

- real-hotel end-to-end validation against controlled public targets;
- API boundary for starting/retrieving an evaluation;
- persistence and evaluation history;
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
Governed Coverage Classification
        ↓
End-to-End Evaluation Orchestration
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
Coverage
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

Do not jump directly to a complete scoring engine, generic AI analysis, full hospitality ontology, recommendation prioritization, report rendering/UI, interactive Antena-hosted experience, broad crawling infrastructure, or generic SEO auditing. Each requires an explicit bounded requirement.

Do not bypass the coverage-classification dependency by defaulting a state inside the orchestration layer.
