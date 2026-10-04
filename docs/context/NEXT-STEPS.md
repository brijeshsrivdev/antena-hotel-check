# Next Steps

## Current Position

REQ-027 — Hospitality Deficiency Analysis Foundation is merged as PR #27.

REQ-028 — Hospitality Analysis Completeness Foundation is merged as PR #28.

REQ-029 — Guest Journey Analysis Foundation is merged as PR #29.

REQ-020 — Orchestrator Durable Context Foundation is merged and establishes the durable handoff/context mechanism.

## Immediate Next Slice

REQ-030 — Hospitality Recommendation Foundation — READY.

Implementation branch:

`feature/hospitality-recommendation-foundation`

REQ-030 is the next bounded vertical slice. It moves the system from explaining observed problems and guest-journey impact toward **evidence-grounded actionable recommendations**.

The implementation must consume existing analysis and journey outputs. It must not redesign acquisition, evidence, observations, signals, findings, deficiencies, limitations, coverage, result, or journey contracts.

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
Actionable Recommendations  ← REQ-030
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

## REQ-030 Direction

REQ-030 should make the analysis actionable without turning recommendations into a generic AI or scoring system.

```text
Existing findings / deficiencies / governed limitations
                    ↓
          deterministic mapping
                    ↓
      bounded hospitality recommendation
                    ↓
      source finding + journey traceability
```

Initial recommendation families should remain small and evidence-grounded, for example:

- improve booking discoverability;
- improve room information;
- improve guest-facing information;
- improve contact/location information;
- resolve conflicting hotel identity information;
- improve trust/clarity.

Do not create recommendations from missing evidence alone. Do not create a booking recommendation merely because a BOOKING observation is absent. Do not rank recommendations or estimate ROI/conversion impact.

## Future Sequence

After REQ-030, the orchestrator should reassess the actual repository state before defining the next slice. Likely future areas include:

- recommendation quality/completeness expansion;
- connected digital-performance signals such as Google Business Profile, Search Console, GA4, or similar plan-gated capabilities;
- customer-facing report generation;
- persistence and evaluation history;
- analysis maturity/calibration;
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

Do not jump directly to a complete scoring engine, arbitrary coverage classifier, generic AI analysis, full hospitality ontology, recommendation prioritization, report generation, interactive Antena-hosted experience, broad crawling infrastructure, or generic SEO auditing. Each requires an explicit bounded requirement.

REQ-030 is intentionally bounded to deterministic recommendations derived from already-retained evidence-grounded analysis. It must not expand into acquisition, reporting, connected integrations, preview generation, or Antena integration.
