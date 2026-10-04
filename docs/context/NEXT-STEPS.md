# Next Steps

## Current Position

REQ-025 — Hospitality Analysis Result Foundation is merged into `main` as PR #25.

REQ-020 — Orchestrator Durable Context Foundation is merged and establishes the durable handoff/context mechanism.

## Immediate Next Slice

REQ-026 — Deterministic Hospitality Analysis Engine — READY.

Implementation branch:

`feature/deterministic-hospitality-analysis-engine`

REQ-026 is intentionally the first **larger vertical implementation slice** after the analysis-domain foundations. It should connect the existing observation, signal, finding, limitation, coverage, and result boundaries into an executable deterministic pipeline over already-acquired `StructuredEvidence`.

The implementation must inspect the actual current `main` before coding and reuse the existing services/contracts rather than duplicating their rules.

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

REQ-024 establishes coverage representation only. It intentionally does not classify coverage, invent thresholds, or calculate coverage from page/finding counts.

REQ-025 establishes the immutable aggregation boundary for findings, limitations, and coverage belonging to one evaluation.

## REQ-026 Direction

REQ-026 is the first larger implementation slice and should provide an executable deterministic analysis path:

```text
StructuredEvidence[]
      ↓
ObservationService
      ↓
SignalService
      ↓
Verified Findings + truthful Limitations
      ↓
Coverage representation inputs
      ↓
HospitalityAnalysisResult
```

It currently supports the implemented observation categories:

- HOTEL_IDENTITY
- ROOMS
- AMENITIES
- CONTACT
- BOOKING
- DINING

Do not invent a full nine-dimension ontology or arbitrary coverage classifier in this slice.

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
Executable Deterministic Analysis  ← REQ-026
        ↓
Hospitality Findings + truthful limitations
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

Current focus remains the digital-presence analysis portion. Antena-hosted integration is downstream and must not be implemented prematurely.

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

Do not jump directly to a complete scoring engine, arbitrary coverage classifier, generic AI analysis, full hospitality ontology, recommendations, report generation, interactive Antena-hosted experience, broad crawling infrastructure, or generic SEO auditing. Each requires an explicit bounded requirement.

REQ-026 is intentionally larger than the previous foundation slices, but it remains bounded to deterministic analysis over already-acquired evidence. It must not expand into acquisition, reporting, preview generation, or Antena integration.
