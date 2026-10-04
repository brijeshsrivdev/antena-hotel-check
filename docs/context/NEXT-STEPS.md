# Next Steps

## Current Position

REQ-019 — Hospitality Observation Foundation is the latest completed product foundation.

REQ-020 — Orchestrator Durable Context Foundation is the current documentation/workflow foundation being established so that future orchestration does not depend on temporary chat context.

## Immediate Next Action

Complete review and merge of REQ-020. After merge, inspect the updated `main` and define the next smallest product requirement from the repository specifications and implemented boundaries.

Do not assume the next product requirement merely from this file.

## Current Product Boundary

```text
Acquisition
    ↓
Structured Evidence
    ↓
Bounded Hospitality Observations
```

The next product layer is expected to move toward hospitality analysis, but its exact scope must be defined from current repository evidence.

## What Must Not Be Implemented Prematurely

Do not jump directly to:

- a complete hotel scoring engine
- generic AI analysis
- a full hospitality ontology
- recommendation generation
- report generation
- interactive preview generation
- broad crawling infrastructure
- generic SEO auditing

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
