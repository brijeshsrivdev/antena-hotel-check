# Next Steps

## Current Position

REQ-026 — Deterministic Hospitality Analysis Engine is merged into `main` as PR #26.

REQ-020 — Orchestrator Durable Context Foundation is merged and establishes the durable handoff/context mechanism.

## Immediate Next Slice

REQ-027 — Hospitality Deficiency Analysis Foundation — READY.

Implementation branch:

`feature/hospitality-deficiency-analysis-foundation`

REQ-027 is the next larger vertical analysis slice. It extends the deterministic analysis pipeline from primarily positive/observed signals to **evidence-grounded observed deficiencies and meaningful evidence gaps**.

The implementation must inspect the actual current `main` before coding and reuse existing observation, signal, finding, limitation, coverage, and result contracts rather than duplicating their rules.

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
Hospitality Finding / Unable-to-Verify Limitation
  ↓
Hospitality Analysis Coverage
  ↓
Hospitality Analysis Result
```

REQ-023 establishes the truthful distinction between **unable to verify** and an observed hotel deficiency.

REQ-024 establishes coverage representation only. Coverage state remains governed and must not be invented from page/finding counts.

REQ-025 establishes the immutable aggregation boundary for findings, limitations, and coverage belonging to one evaluation.

REQ-026 establishes the first executable deterministic analysis pipeline over already-retained evidence and explicitly separates the full nine-dimension intended scope from the currently assessable scope.

## REQ-027 Direction

REQ-027 should make the analysis materially useful by detecting problems only when the retained evidence establishes an observed guest-facing deficiency or meaningful observed evidence gap.

```text
StructuredEvidence[]
      ↓
ObservationService
      ↓
SignalService
      ↓
Deterministic deficiency rules
      ↓
Observed deficiencies / insufficient evidence / truthful limitations
      ↓
Coverage representation
      ↓
HospitalityAnalysisResult
```

Initial rule families are deliberately conservative:

- booking discoverability deficiencies where the relevant context explicitly establishes the expected booking action;
- materially insufficient observed room information;
- materially missing observed contact/location paths;
- materially insufficient guest-facing information;
- material cross-source conflicts relevant to trust/clarity;
- explicitly observed broken guest-facing paths.

Do not turn missing evidence, failed acquisition, `NOT_ATTEMPTED`, unsupported dimensions, or context-free keyword absence into hotel deficiencies.

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
Evidence-grounded deficiencies + truthful limitations  ← REQ-027
        ↓
Coverage representation
        ↓
Hospitality Analysis Result
        ↓
Mature, trustworthy analysis
        ↓
Antena integration opportunity
        ↓
<hotel-name>.antenapro.com
```

Current focus remains the digital-presence analysis portion. Antena-hosted integration is downstream and must not be implemented as part of REQ-027.

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

Do not jump directly to a complete scoring engine, arbitrary coverage classifier, generic AI analysis, full hospitality ontology, recommendation engine, report generation, interactive Antena-hosted experience, broad crawling infrastructure, or generic SEO auditing. Each requires an explicit bounded requirement.

REQ-027 is intentionally larger than the foundation slices, but remains bounded to deterministic deficiency analysis over already-retained evidence. It must not expand into acquisition, reporting, preview generation, or Antena integration.
