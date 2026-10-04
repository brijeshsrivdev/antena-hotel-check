# REQ-032 — Evaluation Execution Orchestration Foundation

STATUS: READY
REQUIREMENT_ID: REQ-032
TYPE: Implementation
BRANCH: `feature/evaluation-execution-orchestration-foundation`

## Objective

Introduce the smallest explicit end-to-end orchestration boundary for executing one canonical hotel evaluation through the existing Antena Hotel Check analysis pipeline.

REQ-032 does not add new analysis capability. It coordinates existing contracts so that one `CanonicalEvaluationRequest` can flow through acquisition, evidence normalization, deterministic hospitality analysis, guest-journey analysis, recommendations, and structured report assembly.

The product objective remains:

> Can a guest find, understand, trust, explore, and book this hotel online?

The current product focus remains trustworthy digital-presence analysis. Antena-hosted hotel experience generation at `<hotel-name>.antenapro.com` remains downstream.

## Repository Baseline

REQ-032 consumes the completed pipeline:

```text
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
Hospitality Observation
        ↓
Qualified Hospitality Analysis Signal
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
```

Existing domain contracts remain authoritative. The orchestrator must coordinate them rather than redesigning or duplicating them.

## Scope

Implement a small deterministic orchestration service for one canonical evaluation execution.

The orchestration boundary must:

1. accept one canonical evaluation request;
2. establish/use the existing evaluation and attempt lifecycle;
3. invoke the existing evaluation-acquisition integration exactly once for the execution;
4. pass the successful retained acquisition output into the existing evidence normalization boundary;
5. execute the existing deterministic hospitality analysis pipeline;
6. execute the existing guest-journey analysis;
7. execute the existing recommendation layer;
8. assemble the existing structured hospitality analysis report;
9. return the resulting report/output together with the existing evaluation/attempt attribution needed by callers.

The orchestrator must not become a second implementation of any downstream stage.

## Existing Contract Reuse

Reuse existing services/types for:

- canonical evaluation request;
- evaluation/attempt lifecycle;
- acquisition integration;
- structured evidence normalization;
- hospitality observation/signal processing;
- deterministic analysis;
- guest journey analysis;
- recommendation generation;
- report assembly.

If a required downstream contract is not currently callable from an orchestration boundary, introduce only the smallest adapter/interface needed to invoke the existing behavior. Do not redesign the underlying domain contract merely for orchestration convenience.

## Execution Ordering

The order must be explicit and deterministic:

```text
Request
  ↓
Evaluation / Attempt
  ↓
Acquisition Integration
  ↓
Evidence Normalization
  ↓
Hospitality Analysis
  ↓
Guest Journey
  ↓
Recommendations
  ↓
Report
```

Do not execute downstream analysis before its required upstream input exists.

Do not parallelize stages unless the existing contracts explicitly guarantee independence and doing so does not alter lifecycle/provenance semantics. Sequential execution is the default.

## Acquisition Invocation

The existing REQ-017 contract remains authoritative.

For one orchestration execution:

- acquisition must be invoked exactly once;
- the existing typed acquisition result must be preserved;
- acquisition provenance/evaluation attribution must remain intact;
- acquisition failures must retain their existing capability-failure semantics;
- acquisition failure must not become a hotel-feature absence;
- acquisition failure must not be converted into a false successful report.

Do not add retries, crawler behavior, browser automation, target discovery, or new HTTP behavior.

## Evidence Boundary

Only the existing successful retained acquisition/evidence contract may enter evidence normalization.

Do not normalize raw HTTP/browser responses directly inside the orchestrator.

Do not duplicate the evidence model.

Do not mutate acquisition results.

## Analysis Boundary

The orchestrator must call the existing deterministic analysis implementation.

It must not:

- implement new observation rules;
- implement new signal qualification;
- classify evidence itself;
- create findings itself;
- calculate coverage itself;
- infer unsupported dimensions;
- add scoring;
- parse human-readable finding text.

All analysis truth semantics remain owned by the existing analysis contracts.

## Guest Journey Boundary

The orchestrator must reuse the existing guest-journey analysis:

```text
DISCOVER → UNDERSTAND → EXPLORE → TRUST → BOOK
```

Do not create a second journey model.

Do not infer journey failures in orchestration.

Missing evidence, acquisition failure, unsupported dimensions, `NOT_ATTEMPTED`, and `UNABLE_TO_VERIFY` must retain their existing semantics.

## Recommendation Boundary

The orchestrator must reuse the existing deterministic recommendation service.

Do not generate recommendations in orchestration.

Do not prioritize, score, rank, or rewrite recommendations.

The existing booking truth remains authoritative:

```text
successful room evidence
+
no BOOKING signal
        ↓
no booking deficiency
        ↓
no booking recommendation
```

## Report Boundary

The orchestrator must reuse the existing `HospitalityAnalysisReportService`.

It must not construct a second report model or generate narrative content.

The final report must belong to exactly the evaluation being executed.

## Lifecycle Semantics

Preserve existing evaluation/attempt lifecycle semantics.

In particular:

- successful acquisition does not automatically mean evaluation `COMPLETED` unless the existing lifecycle contract defines completion at the end of the complete orchestration;
- downstream capability failure must not be silently treated as hotel-feature absence;
- a successful end-to-end orchestration may become terminal success only through the existing lifecycle semantics;
- exceptions must not leave an apparently successful report attached to a failed or unrelated evaluation.

Do not invent a new lifecycle state machine.

## Failure Semantics

REQ-032 must distinguish at least:

### Acquisition failure

Preserve the existing acquisition capability failure. Do not continue into analysis as though the hotel had no feature.

### Evidence normalization failure

Preserve the existing evidence-layer failure semantics. Do not manufacture analysis findings from failed normalization.

### Analysis capability failure

Preserve existing analysis semantics. Do not convert technical execution failure into a hotel deficiency.

### Journey/recommendation/report failure

Preserve the existing downstream capability failure semantics and evaluation attribution. Do not return a misleading successful report.

Do not introduce a generic exception-to-hotel-finding mapping.

## Evaluation Isolation

One execution must operate within exactly one evaluation identity.

Evaluation A must never consume:

- acquisition results from B;
- structured evidence from B;
- findings from B;
- journey analysis from B;
- recommendations from B;
- report objects from B.

Add explicit regression coverage for cross-evaluation mismatch rejection where existing contracts support it.

## Determinism

For identical canonical request and identical deterministic upstream inputs, orchestration order and downstream output must be identical.

No:

- randomness;
- current-time-dependent business logic;
- mutable global state;
- AI;
- external calls beyond the existing acquisition boundary.

## No New Acquisition

REQ-032 does not add:

- crawler implementation;
- browser automation;
- target discovery;
- HTTP client behavior;
- retry strategy;
- redirect policy;
- acquisition limits;
- external API calls.

Use the existing REQ-015/017 acquisition contracts.

## No Persistence

Do not add:

- evaluation database tables;
- repositories;
- migrations;
- report storage;
- evidence storage;
- evaluation history.

The orchestration result may remain in-memory for this requirement.

Persistence is a later requirement.

## No API / UI

Do not implement:

- REST endpoints;
- controllers;
- Next.js pages;
- dashboard UI;
- report rendering;
- PDF generation;
- email rendering.

REQ-032 is a backend/domain orchestration boundary only.

## No Google / Connected Performance

Do not implement:

- Google Business Profile;
- Search Console;
- GA4;
- Google Places;
- OTA APIs;
- booking APIs;
- external analytics.

Connected digital-performance capabilities remain future plan-gated capabilities and may be added after the core analysis is mature.

## No AI

Do not add:

- LLMs;
- model providers;
- prompts;
- semantic classifiers;
- generated recommendations;
- generated report prose.

## No Antena Integration

Do not implement:

- `<hotel-name>.antenapro.com`;
- preview generation;
- Antena package mapping;
- commercial CTAs;
- automatic conversion of findings into Antena configuration.

The Antena-hosted experience remains a downstream product outcome after the analysis is mature enough.

## Architectural Shape

Prefer:

```text
CanonicalEvaluationRequest
        ↓
EvaluationExecutionOrchestrator
        ↓
Existing domain services in explicit order
        ↓
Structured Hospitality Analysis Report
```

The orchestrator should be thin.

Do not introduce:

- workflow engines;
- generic pipeline frameworks;
- reflection-based dispatch;
- plugin registries;
- dynamic stage discovery;
- generic command buses;
- duplicated domain logic.

Each stage should retain ownership of its existing domain rules.

## Testing Acceptance Criteria

Automated tests must demonstrate:

1. One canonical evaluation request produces one explicit end-to-end orchestration.
2. Existing evaluation/attempt attribution is preserved.
3. Acquisition integration is invoked exactly once.
4. Successful acquisition reaches evidence normalization.
5. Evidence normalization output reaches deterministic analysis.
6. Analysis output reaches guest-journey analysis.
7. Journey output reaches recommendation generation.
8. Analysis + journey + recommendations reach report assembly.
9. Acquisition failure does not become a hotel-feature absence.
10. Acquisition failure does not produce a false successful report.
11. Downstream capability failure does not become a hotel deficiency.
12. Evaluation A cannot leak data into Evaluation B.
13. Existing booking truth remains unchanged.
14. Existing limitation/unsupported semantics remain unchanged.
15. The orchestrator does not perform network access itself.
16. No duplicate analysis/recommendation/report logic is introduced.
17. Same deterministic inputs produce the same orchestration result.
18. Existing REQ-027 through REQ-031 behavior remains unchanged.
19. Complete backend tests pass.

## Completion Requirements

Before `PR_READY`:

- inspect current `main` and referenced requirements/specifications;
- implement only REQ-032;
- add focused deterministic orchestration tests;
- run complete backend Maven tests;
- run Backend Validation against the exact final PR head;
- update this same requirement file with implementation, tests, validation, architectural decisions, failure/lifecycle decisions, limitations, and self-review;
- ensure the final requirement-file update is included in the successfully validated final PR head.

Do not merge. Do not start REQ-033.

## Orchestrator Intent

REQ-032 is the first execution-level boundary after the analysis/report domain chain is complete. Its purpose is to prove that the existing pieces can be coordinated as one evaluation without prematurely introducing API, persistence, UI, integrations, or Antena preview generation.

The next implementation decision after REQ-032 must be based on actual end-to-end maturity and product value, not automatic REQ-number progression.
