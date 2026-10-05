# Current State

Last updated: 2026-10-05

## Repository

Repository: `brijeshsrivdev/antena-hotel-check`

`main` remains the source of truth. This file is a compact operational snapshot and must not replace detailed requirements/specifications.

## Product Objective

Antena Hotel Check analyzes a hotel's publicly accessible digital presence and guest journey with hospitality-specific analysis. The long-term product outcome is to identify problems/opportunities and, once the analysis is mature enough, provide an Antena-hosted hotel experience at `<hotel-name>.antenapro.com` that can address those problems.

**Current focus:** make the digital-presence analysis trustworthy and mature. Antena-hosted integration is downstream.

## Product Stage

The deterministic analysis/report foundation, governed coverage derivation/classification chain, and evaluation execution orchestration foundation are implemented/merged. REQ-037 is the current implementation slice for exposing that governed pipeline through a thin synchronous HTTP boundary.

The intended pipeline is now:

```text
HTTP Evaluation Request [REQ-037 IN PROGRESS]
        ↓
Canonical Evaluation Request
        ↓
Evaluation / Attempt
        ↓
Public Web Acquisition
        ↓
Acquisition Result
        ↓
Structured Evidence
        ↓
Governed Hospitality Observations / Signals
        ↓
Coverage Assessment Derivation [REQ-036 IMPLEMENTED]
        ↓
HospitalityCoverageAssessment
        ↓
Coverage Classification [REQ-034 IMPLEMENTED]
        ↓
HospitalityAnalysisCoverageState
        ↓
Deterministic Hospitality Analysis
        ↓
Hospitality Finding / Deficiency / Limitation
        ↓
Hospitality Analysis Coverage
        ↓
Hospitality Analysis Result
        ↓
Guest Journey Analysis
        ↓
Hospitality Recommendations
        ↓
Structured Hospitality Analysis Report
        ↓
HTTP Evaluation Response [REQ-037 IN PROGRESS]
```

## Completed / Merged Foundation

- REQ-015 — Public Web Acquisition Foundation — IMPLEMENTED / MERGED.
- REQ-017 — Evaluation Acquisition Integration — IMPLEMENTED / MERGED.
- REQ-018 — Evidence Normalization Foundation — IMPLEMENTED / MERGED.
- REQ-019 — Hospitality Observation Foundation — IMPLEMENTED / MERGED.
- REQ-020 — Orchestrator Durable Context Foundation — IMPLEMENTED / MERGED.
- REQ-021 — Hospitality Analysis Signal Foundation — IMPLEMENTED / MERGED.
- REQ-022 — Hospitality Finding Foundation — IMPLEMENTED / MERGED.
- REQ-023 — Hospitality Analysis Limitation Foundation — IMPLEMENTED / MERGED.
- REQ-024 — Hospitality Analysis Coverage Foundation — IMPLEMENTED / MERGED.
- REQ-025 — Hospitality Analysis Result Foundation — IMPLEMENTED / MERGED.
- REQ-026 — Deterministic Hospitality Analysis Engine — IMPLEMENTED / MERGED as PR #26.
- REQ-027 — Hospitality Deficiency Analysis Foundation — IMPLEMENTED / MERGED as PR #27.
- REQ-028 — Hospitality Analysis Completeness Foundation — IMPLEMENTED / MERGED as PR #28.
- REQ-029 — Guest Journey Analysis Foundation — IMPLEMENTED / MERGED as PR #29.
- REQ-030 — Hospitality Recommendation Foundation — IMPLEMENTED / MERGED as PR #30.
- REQ-031 — Hospitality Analysis Report Foundation — IMPLEMENTED / MERGED as PR #31.
- REQ-033 — Hospitality Analysis Coverage Classification Contract — ACCEPTED / MERGED.
- REQ-034 — Hospitality Analysis Coverage Classification Implementation — IMPLEMENTED / MERGED as PR #32.
- REQ-035 — Hospitality Coverage Assessment Contract — IMPLEMENTED / MERGED as PR #34.
- REQ-036 — Hospitality Coverage Assessment Derivation — IMPLEMENTED / MERGED as PR #36.
- REQ-032 — Evaluation Execution Orchestration Foundation — IMPLEMENTED / MERGED as PR #37.

## REQ-032 — Implemented Boundary

`EvaluationExecutionOrchestrator` is the governed synchronous execution boundary for one canonical evaluation.

It uses the existing acquisition integration exactly once, normalizes evidence, prepares governed observations/signals/findings, invokes REQ-036 derivation, invokes the classifier, passes the exact classifier state into deterministic analysis, then runs journey analysis, recommendations, and report assembly.

The execution result preserves one evaluation/attempt identity across the pipeline.

Successful analysis/report execution uses the existing lifecycle `INCOMPLETE` state because preview generation is outside REQ-032 and `COMPLETED` requires both report and preview outcomes.

A mandatory downstream runtime failure is recorded as the existing `FAILED` lifecycle state and rethrown; later stages do not execute.

## REQ-037 — Current Implementation

**Status: IN PROGRESS.**

Branch: `feature/evaluation-execution-api`

The current slice adds a thin Spring MVC boundary:

- `POST /api/evaluations`;
- existing `HotelEvaluationInput` accepted at the transport boundary;
- existing `HotelEvaluationInputValidator` produces the governed `CanonicalEvaluationRequest`;
- existing `EvaluationExecutionOrchestrator` executes synchronously;
- `EvaluationExecutionResponse` exposes the existing execution artifacts without new domain semantics;
- minimal machine-readable 4xx/5xx error handling;
- Spring MVC integration-style tests without a real external crawl.

No change has been made to the REQ-032 orchestrator or its hospitality semantics.

## Coverage Classification

REQ-033 remains authoritative:

- `SUBSTANTIALLY_ASSESSED`: all five journey stages covered; at least six of nine intended dimensions covered; at least three journey stages have assessable evidence.
- `PARTIALLY_ASSESSED`: at least one intended journey stage or dimension is covered, but substantial criteria are not satisfied.
- `INSUFFICIENT_COVERAGE`: zero intended journey stages and zero intended dimensions are covered.

Covered means explicitly assessable or explicitly limited. Limited scope contributes to covered scope but does not count toward the assessable-stage threshold.

## Explicitly Not Implemented

The following remain outside the completed foundation unless a merged requirement explicitly says otherwise:

- customer-facing report API/rendering/UI;
- Google Business Profile / Search Console / GA4 connected performance integrations;
- AI/LLM analysis or recommendation generation;
- interactive Antena-hosted hotel preview integration;
- booking/OTA analysis beyond bounded observed entry-point signals;
- persistence for the evidence/analysis pipeline;
- unrestricted crawling/browser acquisition;
- generic SEO auditing as the product center;
- competitor analysis;
- Antena integration.

## Operating Rule

Never describe a `READY`, `PLANNED`, `PROPOSED`, `BLOCKED`, or future requirement as implemented. Verify status against `main`, requirement files, merged PRs, and code before updating this snapshot.
