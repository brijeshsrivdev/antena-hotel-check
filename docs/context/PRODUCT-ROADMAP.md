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
Coverage
        ↓
Hospitality Analysis Result
        ↓
Mature, trustworthy analysis
        ↓
Actionable Recommendations
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

The repository has completed the acquisition/evidence/analysis foundation through REQ-026.

Current active work:

- **REQ-027 — Hospitality Deficiency Analysis Foundation** — implementation in progress.

The current product focus remains **digital presence analysis**. Antena-hosted website generation is downstream and must not be pulled forward prematurely.

## Phase 1 — Trustworthy Hospitality Analysis

### REQ-027 — Hospitality Deficiency Analysis Foundation

**Status:** IN PROGRESS

Extend the deterministic analysis pipeline so it can identify guest-facing deficiencies only when retained evidence establishes them.

Initial rule families include:

- booking discoverability deficiencies;
- materially insufficient room information;
- materially missing contact/location paths;
- materially insufficient guest-facing information;
- material cross-source conflicts;
- explicitly observed broken guest-facing paths.

Core rule:

```text
Missing evidence ≠ hotel deficiency
Acquisition failure ≠ hotel deficiency
NOT_ATTEMPTED ≠ hotel deficiency
Unsupported dimension ≠ hotel deficiency
```

### Next likely slice — Hospitality Analysis Completeness

Expand deterministic analysis coverage across the nine intended SPEC-006 dimensions, one capability at a time.

The nine intended dimensions are:

1. Hotel identity / property understanding
2. Discoverability / navigation
3. Rooms / room information
4. Amenities / guest-facing information
5. Contact / location
6. Booking / booking journey
7. Trust / clarity
8. Mobile / technical guest experience
9. SEO / structured-data supporting signals

Important: the existence of a dimension in the intended scope does not mean it is currently assessable. Each capability must have an explicit evidence-backed implementation.

### Guest Journey Model

Introduce a first-class hospitality guest-journey representation once the underlying analysis signals are mature enough:

```text
DISCOVER
   ↓
UNDERSTAND
   ↓
EXPLORE
   ↓
TRUST
   ↓
CHOOSE
   ↓
BOOK
```

The goal is to explain **where the guest journey breaks**, rather than merely reporting a count of findings.

### Finding Prioritization

After findings are reliable, introduce explainable deterministic prioritization based on governed concepts such as:

- guest journey impact;
- evidence confidence;
- business relevance.

Avoid opaque or unexplained scores.

### Hospitality Recommendations

Transform evidence-grounded findings into actionable recommendations.

Every recommendation should be traceable to the finding/evidence that caused it.

Avoid generic advice such as undifferentiated "improve SEO" recommendations.

## Phase 2 — Customer-Facing Analysis Product

### Hospitality Analysis Report Model

Create a structured report model containing concepts such as:

```text
Hotel Overview
      ↓
Executive Summary
      ↓
Guest Journey
      ↓
Strengths
      ↓
Problems
      ↓
Limitations
      ↓
Priorities
      ↓
Recommendations
```

Keep the report model separate from presentation/UI concerns.

### Report API / Rendering Boundary

Expose the mature analysis/report through a stable API suitable for:

- Antena Admin;
- future public reports;
- internal tooling;
- automated hotel evaluation workflows.

### Real Hotel Evaluation

Run the complete analysis pipeline against a real publicly accessible hotel:

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

This is a major product milestone: the system should produce a trustworthy result for a real hotel without requiring manual intervention inside the analysis layer.

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

Examples of future insights:

```text
High Google visibility
        ↓
Low website click-through
        ↓
Discovery-to-website opportunity
```

or:

```text
Strong room-page traffic
        ↓
Low booking-journey progression
        ↓
Potential booking-friction opportunity
```

These conclusions must remain grounded in the actual connected metrics and configured measurement context. Do not infer conversion problems when the required analytics events are not configured or available.

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

Exact pricing, limits, and plan names are intentionally **not decided here** and require a separate product/pricing decision before implementation.

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

Introduce production-grade evaluation orchestration after the analysis contracts are mature:

```text
REQUESTED
   ↓
ACQUIRING
   ↓
NORMALIZING
   ↓
ANALYZING
   ↓
REPORT_READY
```

Consider:

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

This creates the bridge from analysis to commercial value.

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
- arbitrary coverage classifiers;
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
