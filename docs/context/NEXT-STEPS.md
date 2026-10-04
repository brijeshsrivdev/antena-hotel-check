# Next Steps

## Current Position

REQ-022 — Hospitality Finding Foundation is merged into `main`.

REQ-020 — Orchestrator Durable Context Foundation is merged and establishes the durable handoff/context mechanism.

## Immediate Next Slice

REQ-023 — Hospitality Analysis Limitation Foundation.

Purpose: establish a truthful, deterministic representation of **unable to verify** conditions without turning them into hotel deficiencies.

Required distinction:

```text
Unable to verify booking
  ≠ Booking is broken

No usable evidence
  ≠ Hotel lacks the capability

NOT_ATTEMPTED
  ≠ Feature absent
```

The implementation must consume existing acquisition/evidence state and must not introduce new acquisition, network access, AI, scoring, severity, recommendations, coverage, reporting, persistence, or Antena integration.

Do not assume a limitation merely because evidence is absent. Use only repository-supported acquisition/evidence conditions. If the existing contracts cannot distinguish a limitation from `NOT_ATTEMPTED` or simple absence of evidence, stop rather than invent semantics.

## After REQ-023

Do not pre-commit to a feature beyond the next repository-grounded slice. After REQ-023 is merged, the orchestrator should inspect `main`, specifications, and implemented contracts again before defining the next requirement.

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
Hospitality Findings + truthful limitations
        ↓
Digital-presence / hospitality analysis
        ↓
Mature, trustworthy analysis result
        ↓
Antena integration opportunity
        ↓
<hotel-name>.antenapro.com
```

The current focus is the analysis portion of this sequence. The Antena-hosted hotel experience is a later downstream capability and must not be implemented prematurely.

## What Must Not Be Implemented Prematurely

Do not jump directly to:

- a complete hotel scoring engine
- generic AI analysis
- a full hospitality ontology
- recommendation generation
- report generation
- interactive Antena-hosted hotel experience
- broad crawling infrastructure
- generic SEO auditing as the product center
- downstream Antena integration before analysis is mature

Each must be introduced only through an explicit, bounded requirement.

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
