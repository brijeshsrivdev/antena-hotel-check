# Next Steps

## Current Position

REQ-021 — Hospitality Analysis Signal Foundation is the latest completed product capability.

REQ-020 — Orchestrator Durable Context Foundation is merged and establishes the durable handoff/context mechanism.

## Immediate Next Action

Inspect the updated `main` and define the next smallest product requirement for trustworthy digital-presence analysis.

Do not assume the next product requirement merely from this file. Read the relevant specifications and inspect the implementation before defining it.

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
