# Proposed Implementation Roadmap

**Status:** PLANNED

This is a proposal, not an implementation commitment.

## Slice 0 — Foundation
**Status:** IMPLEMENTED in this PR. Durable context, product specification, architecture decision areas and lifecycle are established.

## Slice 1 — Evaluation Input Contract
Define the smallest input boundary for hotel name + city or website URL without building the analyzer. Acceptance: validated input, canonical evaluation-target representation, explicit invalid-input behavior, tests, and no crawling/preview implementation.

## Slice 2 — Public Evidence Acquisition Boundary
Implement constrained public fetching after SSRF, limits, access and observability decisions.

## Slice 3 — Hospitality Evidence Model
Normalize acquired evidence into a source-aware hotel/guest-journey representation.

## Slice 4 — First Hospitality Analysis Checks
Implement a small explicitly specified set of guest-journey checks with evidence links.

## Slice 5 — Owner-facing Report
Assemble findings, limitations and provenance into a useful report contract.

## Slice 6 — Preview Model
Define the data contract for discovered/normalized/demonstration preview content.

## Slice 7 — Interactive Preview Runtime
Build the first actual navigable hotel preview.

## Slice 8 — Preview Hosting
Add Antena-hosted preview addressing/lifecycle.

## Sequencing principle

Prefer the smallest validated slice that moves toward:

**Hotel input → Analysis → Guest Journey Understanding → Interactive Hotel Preview**

Do not let the roadmap become a reason to prematurely build scraping infrastructure or a generic website generator.
