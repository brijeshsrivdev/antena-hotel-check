# Antena Hotel Check — Product Roadmap

**Status:** DIRECTIONAL / LIVING ROADMAP  
**Owner:** Orchestrator  
**Last updated:** 2026-10-04

## Purpose

This document captures current product direction beyond the immediate implementation slice. It is intentionally directional, not a fixed delivery schedule.

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
Governed Coverage Assessment
        ↓
Coverage Classification
        ↓
Deterministic Hospitality Analysis
        ↓
Evidence-grounded Deficiencies + Truthful Limitations
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

The repository has completed the deterministic analysis/report foundation through REQ-034.

Current blocker:

- **REQ-032 — Evaluation Execution Orchestration Foundation — BLOCKED.**

The blocker is a circular contract dependency: `HospitalityAnalysisService` requires `HospitalityAnalysisCoverageState` before creating `HospitalityAnalysisCoverage`, while `HospitalityAnalysisCoverageClassifier` currently consumes that final `HospitalityAnalysisCoverage` object.

Session 35 established:

- **REQ-035 — Hospitality Coverage Assessment Contract — READY.**
- REQ-035 defines the pre-classification coverage fact boundary needed to remove the circular dependency.
- REQ-035 is specification-only and is not implemented.

The immediate sequence is therefore:

```text
REQ-035 contract
      ↓
REQ-035 implementation / classifier-boundary integration
      ↓
REQ-032 orchestration
```

## Phase 1 — Trustworthy Hospitality Analysis

### Completed foundation

The repository contains merged deterministic foundations for:

- public-web acquisition;
- evaluation/acquisition integration;
- structured evidence normalization;
- hospitality observations;
- analysis signals;
- findings and deficiencies;
- limitations;
- coverage representation;
- coverage classification;
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

### REQ-035 — Hospitality Coverage Assessment Contract

**Status:** READY

Define the smallest immutable pre-classification fact contract using the existing evaluation identity, five guest-journey stages, and nine hospitality dimensions.

The assessment represents:

- intended journey stages;
- intended dimensions;
- assessable journey stages/dimensions;
- limited journey stages/dimensions.

Covered scope is assessable ∪ limited scope. Limited scope counts as covered but does not count as assessable evidence.

The assessment does not contain the final coverage state and does not replace `HospitalityAnalysisCoverage`.

### REQ-032 — Evaluation Execution Orchestration

**Status:** BLOCKED

After REQ-035 is implemented and the classifier boundary is reconciled, introduce one thin end-to-end execution boundary:

```text
Canonical Evaluation Request
        ↓
Evaluation / Attempt
        ↓
Acquisition
        ↓
Evidence
        ↓
Governed Coverage Assessment
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

The orchestrator must coordinate existing contracts rather than absorb their responsibilities or duplicate coverage calibration.

### Real Hotel Evaluation

After REQ-032 is implemented, run the complete analysis pipeline against controlled real publicly accessible hotels.

This is a major product milestone: the system should produce a trustworthy result for a real hotel without manual intervention inside the analysis layer.

### Analysis Quality / Calibration

Use controlled real-hotel evaluations to determine where the deterministic model is genuinely useful and where additional evidence-backed capabilities are needed.

Prioritize hospitality dimensions that materially improve the guest journey. Do not expand into generic SEO auditing simply to increase the number of checks.

## Phase 2 — Customer-Facing Analysis Product

### Report API / Rendering Boundary

Expose the mature analysis/report through a stable API suitable for Antena Admin, future public reports, internal tooling, and automated hotel evaluation workflows.

### Customer-Facing Report

Render the structured analysis into a useful hotel-owner experience only after the underlying report semantics are sufficiently mature.

The report should distinguish verified/discovered information, observed problems, limitations, recommendations, and unavailable/unsupported areas.

## Phase 3 — Connected Digital Performance (Paid / Plan-Gated)

This capability is intentionally not part of the free/public-web-only baseline analysis. It is a potential paid-plan capability for hotels that connect their own first-party Google properties.

Connected first-party data should complement public-web analysis and must remain grounded in actual authorized metrics and measurement context.

## Phase 4 — Production Evaluation Engine

REQ-032 is the first foundation for production-grade execution, but production concerns should be added only after the deterministic end-to-end path is proven.

Potential later concerns include idempotency, retries, timeouts, partial failure handling, attempt tracking, observability, hotel target resolution, and evaluation history.

## Phase 5 — Antena Conversion Opportunity

This phase comes after digital presence analysis is sufficiently mature.

Antena is the product that can ultimately address problems identified by Hotel Check. The eventual hotel preview at `<hotel-name>.antenapro.com` must distinguish verified/discovered hotel information, normalized/inferred information, and demonstration/generated content.

The interactive preview is a core product capability, not merely a marketing screenshot.

## Phase 6 — AI-Assisted Intelligence

AI should be introduced after deterministic evidence and analysis are trustworthy. AI must not replace the evidence/provenance model or become an opaque source of hotel facts.

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

## Planning Principle

After each significant merged slice:

1. Inspect the actual repository state.
2. Review what the completed capability proves.
3. Identify the smallest high-value next boundary.
4. Create/refine a concrete READY requirement.
5. Implement through the normal SDD + TDD + PR lifecycle.
6. Update durable context after merge.

Therefore requirement numbers after the current active requirement are directional placeholders, not a promise of exact ordering or implementation timing.
