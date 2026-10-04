# REQ-025 — Hospitality Analysis Result Foundation

STATUS: READY
REQUIREMENT_ID: REQ-025
TYPE: Implementation
BRANCH: `feature/hospitality-analysis-result-foundation`

## Objective

Establish the smallest immutable domain boundary that represents the result of the bounded hospitality analysis work completed so far.

The result aggregates already-produced analysis artifacts without inventing scoring, classification, recommendations, or report semantics.

## Product Boundary

The current analysis pipeline is:

```text
Public Web Acquisition
  ↓
Structured Evidence
  ↓
Hospitality Observation
  ↓
Qualified Analysis Signal
  ↓
Finding / Unable-to-Verify Limitation
  ↓
Coverage Representation
  ↓
Hospitality Analysis Result
```

REQ-025 establishes only the final aggregation boundary for these existing analysis artifacts.

It does not decide whether the hotel is good or bad. It does not calculate a score. It does not generate recommendations.

## Scope

Implement a small immutable `HospitalityAnalysisResult` representation that preserves:

- evaluation identity;
- analysis findings;
- analysis limitations;
- coverage representation;
- hospitality/guest-journey context already carried by those objects;
- deterministic result identity/metadata only where existing repository conventions require it.

Existing domain objects must remain referenced rather than duplicated.

## Truthfulness Boundary

The result must preserve the distinction between:

- observed finding;
- inability to verify;
- analysis coverage;
- hotel capability.

The result must not convert limitations into deficiencies or coverage into hotel quality.

An analysis result containing no verified finding must not imply that the hotel has no corresponding capability.

## Explicit Non-Scope

Do not implement:

- scoring;
- severity;
- ranking;
- grading;
- recommendation generation;
- coverage classification;
- numerical thresholds;
- percentage calculations;
- report generation;
- persistence;
- REST/API/UI;
- PDF/dashboard;
- AI/LLM/model confidence;
- crawling/browser/acquisition;
- competitor analysis;
- generic SEO auditing;
- interactive Antena preview;
- `<hotel-name>.antenapro.com` integration;
- booking/OTA behavior.

## Dependencies

- `SPEC-005` — Evidence Model
- `SPEC-006` — Hospitality Analysis Contract
- REQ-021 — Hospitality Analysis Signal Foundation
- REQ-022 — Hospitality Finding Foundation
- REQ-023 — Hospitality Analysis Limitation Foundation
- REQ-024 — Hospitality Analysis Coverage Foundation
- existing evaluation identity conventions
- existing backend engineering/testing conventions

## Acceptance Criteria

1. A hospitality analysis result can aggregate findings, limitations, and coverage for exactly one evaluation.
2. Evaluation identity is retained and cross-evaluation artifacts are rejected.
3. Existing findings, limitations, and coverage remain traceable through their existing objects.
4. Source domain objects are not mutated.
5. Collections are immutable/defensively copied according to repository conventions.
6. A limitation remains an inability-to-verify semantic and cannot become a hotel deficiency through aggregation.
7. Coverage remains an explicit representation and is not recalculated by REQ-025.
8. No finding/limitation/coverage score is derived from counts.
9. Empty findings are allowed where the existing analysis context legitimately contains no findings; absence of findings must not imply absence of hotel capabilities.
10. The result is deterministic and contains no external/network dependency.
11. Existing upstream contracts remain unchanged.
12. Backend Maven validation and GitHub Actions Backend Validation pass.

## Implementation Guidance

Inspect the actual merged implementations on `main` before coding.

Prefer a small immutable value object over a service or framework.

Do not create a generic `AnalysisResult` abstraction unless existing repository evidence requires one.

Do not introduce a new status lifecycle for the analysis result unless an existing specification explicitly requires it.

If a result-level status is not already governed, do not invent one. The result may simply be the immutable aggregation boundary.

## Governance

REQ-025 is intentionally an aggregation boundary. Future requirements may add:

- governed coverage classification;
- scoring/severity;
- recommendations;
- report presentation;
- persistence/API;
- Antena integration.

Those capabilities must be separately specified.

**STOPPING FOR IMPLEMENTATION SESSION REVIEW.**
