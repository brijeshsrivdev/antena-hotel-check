# Antena Hotel Check — Product Roadmap

**Status:** DIRECTIONAL / LIVING ROADMAP  
**Owner:** Orchestrator  
**Last updated:** 2026-10-04

## Purpose

This document captures the current product direction beyond the immediate implementation slice. It is intentionally **directional**, not a fixed delivery schedule.

Implementation order, scope, and timing may change as repository evidence, completed analysis capabilities, product learning, and architectural constraints evolve.

Individual implementation requirements remain the authoritative source for committed work. This roadmap must not be treated as proof that a future requirement is already specified or implemented.

## Product North Star

Antena Hotel Check evaluates a hotel's public digital presence and answers:

> **Can a guest find, understand, trust, explore, and book this hotel online?**

The long-term product flow is:

```text
Hotel public digital presence
        ↓
Acquisition
        ↓
Structured Evidence
        ↓
Hospitality Observations
        ↓
Qualified Signals
        ↓
Deterministic Hospitality Analysis
        ↓
Evidence-grounded Deficiencies + Truthful Limitations
        ↓
Coverage Classification
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
Antena Opportunity
        ↓
Improved hotel experience
        ↓
<hotel-name>.antenapro.com
```

## Current Position

The repository has completed the core deterministic analysis/report domain foundation through REQ-031.

Current blocker:

- **REQ-032 — Evaluation Execution Orchestration Foundation — BLOCKED.**

Session 32 correctly stopped because the existing analysis service requires a caller-supplied `HospitalityAnalysisCoverageState`, while the canonical evaluation request does not provide one and REQ-024 intentionally established representation without classification/calibration rules.

The orchestrator has created:

- **REQ-033 — Hospitality Analysis Coverage Classification Contract — READY.**

The immediate sequence is therefore:

```text
REQ-033 contract
      ↓
coverage classification implementation
      ↓
REQ-032 orchestration
```

## Phase 1 — Trustworthy Hospitality Analysis

### Completed foundation

The repository now contains merged deterministic foundations for:

- public-web acquisition;
- evaluation/acquisition integration;
- structured evidence normalization;
- hospitality observations;
- analysis signals;
- findings and deficiencies;
- limitations;
- coverage representation;
- deterministic analysis;
- guest-journey analysis;
- recommendations;
- structured report aggregation.

The important truth boundary remains:

```text
Missing evidence ≠ hotel deficiency
Acquisition failure ≠ hotel deficiency
NOT_ATTEMPTED ≠ hotel deficiency
Unsupported dimension ≠ hotel deficiency
```

### REQ-033 — Coverage Classification Contract

**Status:** READY

Define and calibrate the three existing coverage states so the execution layer has a governed source of `HospitalityAnalysisCoverageState`.

Initial calibration:

- `SUBSTANTIALLY_ASSESSED`: all five journey stages covered; at least six of nine intended dimensions covered; at least three journey stages have assessable evidence.
- `PARTIALLY_ASSESSED`: at least one intended journey stage or dimension is covered, but substantial criteria are not satisfied.
- `INSUFFICIENT_COVERAGE`: zero intended journey stages and zero intended dimensions are covered.

These are analysis-capability classifications, not hotel-quality scores.

### Coverage Classification Implementation

**Status:** NOT YET SPECIFIED

Implement the deterministic classifier defined by REQ-033 after the contract is reviewed and accepted.

The implementation must remain separate from the end-to-end orchestrator so the orchestration layer does not become the owner of product calibration.

### REQ-032 — Evaluation Execution Orchestration

**Status:** BLOCKED

After coverage classification exists, introduce one thin end-to-end execution boundary:

```text
Canonical Evaluation Request
        ↓
Evaluation / Attempt
        ↓
Acquisition
        ↓
Evidence
        ↓
Coverage Classification
        ↓
Analysis
        ↓
Guest Journey
        ↓
Recommendations
        ↓
Report
```

The orchestrator must coordinate existing contracts rather than absorb their responsibilities.

### Real Hotel Evaluation

After REQ-032 is implemented, run the complete analysis pipeline against controlled real publicly accessible hotels:

```text
Hotel name + city OR hotel URL
        ↓
Acquire
        ↓
Normalize
        ↓
Observe
        ↓
Analyze
        ↓
Find deficiencies
        ↓
Generate report
```

This is a major product milestone: the system should produce a trustworthy result for a real hotel without manual intervention inside the analysis layer.

### Analysis Quality / Calibration

Use controlled real-hotel evaluations to determine where the deterministic model is genuinely useful and where additional evidence-backed capabilities are needed.

Prioritize hospitality dimensions that materially improve the guest journey. Do not expand into generic SEO auditing simply to increase the number of checks.

## Phase 2 — Customer-Facing Analysis Product

### Report API / Rendering Boundary

Expose the mature analysis/report through a stable API suitable for:

- Antena Admin;
- future public reports;
- internal tooling;
- automated hotel evaluation workflows.

### Customer-Facing Report

Render the structured analysis into a useful hotel-owner experience only after the underlying report semantics are sufficiently mature.

The report should distinguish:

- verified/discovered information;
- observed problems;
- limitations;
- recommendations;
- unavailable/unsupported areas.

Do not turn absence of evidence into negative hotel claims.

## Phase 3 — Connected Digital Performance (Paid / Plan-Gated)

This capability is intentionally **not part of the free/public-web-only baseline analysis**. It is a potential paid-plan capability for hotels that connect their own first-party Google properties.

The product should preserve a useful core report without these connections. Connected data should make the report materially richer, not become a prerequisite for basic Hotel Check functionality.

### Google Business Profile

Potential connected metrics include:

- Google Search visibility/impressions;
- Google Maps visibility/impressions;
- website clicks;
- calls;
- direction requests;
- booking actions where available;
- other supported profile-performance interactions.

### Google Search Console

Potential connected metrics include:

- search queries;
- impressions;
- clicks;
- click-through rate;
- average position;
- landing-page/search performance.

### Google Analytics 4

Potential connected metrics include:

- users;
- sessions;
- landing pages;
- device mix;
- engagement;
- traffic sources;
- booking/key-event funnel signals where configured.

### Connected-data principle

Connected first-party data should complement public-web analysis:

```text
Public Web Analysis
        +
Connected First-Party Performance Data
        ↓
Richer Hospitality Analysis
```

These conclusions must remain grounded in actual connected metrics and configured measurement context. Do not infer conversion problems when required analytics events are not configured or available.

### Pricing / entitlement boundary

The eventual commercial model should allow plan-level entitlement for connected digital performance features.

Possible structure:

```text
Base Hotel Check
    → public-web analysis

Paid / Connected plan
    → Google Business Profile
    → Search Console
    → GA4
    → richer performance analysis
```

Exact pricing, limits, and plan names remain undecided and require a separate product/pricing decision before implementation.

### Privacy / authorization boundary

Connected metrics must be accessed only through properly authorized hotel-owned properties/accounts.

Never attempt to obtain private analytics data without explicit authorization.

A missing connection means:

```text
behavioral/performance analysis unavailable
```

not:

```text
poor performance
```

## Phase 4 — Production Evaluation Engine

### Evaluation Orchestration

REQ-032 is the first foundation for production-grade execution, but production concerns should be added only after the deterministic end-to-end path is proven.

Potential later concerns include:

- idempotency;
- retries;
- timeouts;
- partial failure handling;
- attempt tracking;
- observability.

### Hotel Target Discovery / Resolution

Support input such as:

```text
Hotel XYZ, Pune
```

or a hotel name + city and resolve it to the appropriate public digital presence.

Avoid turning the product into a generic scraping/search platform.

### Evaluation History

Support repeated evaluations of the same hotel:

```text
Hotel
 ├── Evaluation #1
 ├── Evaluation #2
 └── Evaluation #3
```

The eventual product should be able to answer whether the hotel's digital experience improved over time.

## Phase 5 — Antena Conversion Opportunity

This phase comes **after digital presence analysis is sufficiently mature**.

Antena is the product that can ultimately address the problems identified by Hotel Check.

### Antena Opportunity Mapping

Map analysis findings to capabilities Antena can provide:

```text
Analysis Finding
       ↓
Can Antena address this?
       ↓
Yes / No
       ↓
Relevant Antena capability
```

Examples:

| Analysis finding | Potential Antena opportunity |
|---|---|
| Poor room presentation | Antena hotel website |
| Weak booking CTA | Antena direct booking |
| Missing dining presentation | Antena dining capability |
| Weak contact/location journey | Antena hotel website |
| Poor mobile experience | Antena responsive experience |
| Weak information architecture | Antena hotel website |

### Hotel Preview Generation

Generate an Antena-hosted hotel preview:

```text
<hotel-slug>.antenapro.com
```

The generated experience must distinguish:

- verified/discovered hotel information;
- normalized/inferred information;
- demonstration/generated content.

Invented information must never silently appear as verified hotel facts.

### Interactive Guest Preview

The eventual preview should be genuinely explorable, potentially including:

```text
Home
 ├── Rooms
 │    └── Room Detail
 ├── Amenities
 ├── Dining
 ├── Location
 ├── Contact
 └── Booking
```

The interactive preview is a core product capability, not merely a marketing screenshot.

## Phase 6 — AI-Assisted Intelligence

AI should be introduced **after deterministic evidence and analysis are trustworthy**.

Potential uses:

- semantic interpretation;
- nuanced trust analysis;
- summarization;
- recommendation wording;
- multilingual analysis;
- advanced content interpretation.

Preferred architecture:

```text
Evidence
   ↓
Deterministic facts/signals
   ↓
AI interpretation
   ↓
Traceable conclusion
```

AI must not replace the evidence/provenance model or become an opaque source of hotel facts.

## What Is Explicitly Not the Roadmap Default

Do not jump directly to:

- a complete scoring engine;
- arbitrary coverage classification hidden inside orchestration;
- generic AI analysis;
- a giant hospitality ontology;
- a generic recommendation engine;
- report/UI work before the analysis model is mature;
- broad crawling infrastructure without a bounded requirement;
- generic SEO auditing;
- competitor analysis as the core product;
- Antena preview generation before analysis is trustworthy;
- connected Google analytics features in the free baseline without an explicit product/pricing decision.

Each of these requires an explicit bounded requirement and architectural justification.

## Planning Principle

The roadmap is intentionally flexible.

After each significant merged slice:

1. Inspect the actual repository state.
2. Review what the completed capability proves.
3. Identify the smallest high-value next boundary.
4. Create/refine a concrete READY requirement.
5. Implement through the normal SDD + TDD + PR lifecycle.
6. Update durable context after merge.

Therefore **REQ numbers after the current active requirement are directional placeholders, not a promise of exact ordering or implementation timing**.

The orchestrator may split, combine, reorder, defer, or replace roadmap items when repository evidence or product learning justifies it.
