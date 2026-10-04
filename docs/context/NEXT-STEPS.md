# Next Steps

## Current Position

REQ-027 — Hospitality Deficiency Analysis Foundation is merged as PR #27.

REQ-028 — Hospitality Analysis Completeness Foundation is merged as PR #28.

REQ-029 — Guest Journey Analysis Foundation is merged as PR #29.

REQ-030 — Hospitality Recommendation Foundation is merged as PR #30.

REQ-020 — Orchestrator Durable Context Foundation is merged and establishes the durable handoff/context mechanism.

## Immediate Next Slice

REQ-031 — Hospitality Analysis Report Foundation — READY.

Implementation branch:

`feature/hospitality-analysis-report-foundation`

REQ-031 is the next bounded vertical slice. It moves the system from internal analysis outputs toward a stable structured report model that can later support APIs, UI, PDFs, or public reports.

The implementation must consume existing analysis, journey, recommendation, coverage, and limitation outputs. It must not redesign those contracts.

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
Structured Analysis Report  ← REQ-031
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

## REQ-031 Direction

REQ-031 should create a stable domain-level report representation, not a presentation layer.

```text
Hospitality Analysis Result
        +
Guest Journey Analysis
        +
Hospitality Recommendations
        ↓
Structured Hospitality Analysis Report
```

The report should make it possible to represent, at minimum:

- hotel/evaluation identity;
- analysis coverage and truthful limitations;
- executive-level analysis summary derived from existing governed outputs;
- guest journey stages and their governed impacts;
- observed strengths only where existing evidence supports them;
- deficiencies/problems;
- recommendations;
- provenance/evaluation attribution where required by existing contracts.

Do not invent positive hotel facts merely because a report section expects content.

## Future Sequence

After REQ-031, reassess the actual repository state before defining the next slice. Likely future areas include:

- report API/rendering boundary;
- real-hotel end-to-end evaluation orchestration;
- analysis quality/calibration and additional supported dimensions;
- persistence and evaluation history;
- connected digital-performance signals such as Google Business Profile, Search Console, GA4, or similar plan-gated capabilities;
- eventually Antena integration and `<hotel-name>.antenapro.com` experience generation.

These are **not yet implementation requirements** unless separately specified and marked READY.

## Product Sequence

```text
Hotel public digital presence
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

REQ-031 is intentionally bounded to a structured domain report assembled from existing trustworthy analysis outputs. It must not expand into UI, rendering, persistence, connected integrations, preview generation, or Antena integration.
