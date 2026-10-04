# REQ-026 — Deterministic Hospitality Analysis Engine

STATUS: READY
REQUIREMENT_ID: REQ-026
TYPE: Implementation
BRANCH: `feature/deterministic-hospitality-analysis-engine`

## Objective

Move from isolated hospitality-analysis domain foundations to the first **usable deterministic analysis pipeline**.

REQ-026 should consume already-retained `StructuredEvidence` for one evaluation and deterministically produce the analysis artifacts that can be assembled into a `HospitalityAnalysisResult`:

```text
Structured Evidence
      ↓
Hospitality Observations
      ↓
Qualified Analysis Signals
      ↓
Verified Hospitality Findings
      +
Unable-to-Verify Limitations
      ↓
Coverage Representation
      ↓
Hospitality Analysis Result
```

This is intentionally a larger vertical slice than the preceding foundation requirements. It is the first requirement that should make the analysis pipeline executable end-to-end over already-acquired evidence.

It does **not** introduce crawling, browser automation, scoring, AI, report UI, persistence, or Antena preview integration.

## Product Question

The analysis remains organized around:

**Can a guest find, understand, trust, explore, and book this hotel online?**

The implementation must remain hospitality-first rather than becoming a generic website audit.

## Scope

Implement a deterministic analysis application/service that:

1. accepts the already-produced structured evidence for exactly one evaluation;
2. invokes the existing `HospitalityObservationService`;
3. qualifies observations through the existing `HospitalityAnalysisSignalService`;
4. converts supported qualified signals into verified hospitality findings;
5. converts supported unavailable/failed evidence conditions into truthful unable-to-verify limitations where existing REQ-023 rules permit it;
6. derives the coverage **representation inputs** from what was actually assessable;
7. assembles the existing `HospitalityAnalysisResult`;
8. preserves evaluation identity and evidence/provenance traceability throughout.

The service should support the currently implemented hospitality observation categories:

- HOTEL_IDENTITY
- ROOMS
- AMENITIES
- CONTACT
- BOOKING
- DINING

Do not invent additional observation categories in this requirement merely to match all nine SPEC-006 dimensions.

## Finding Semantics

A verified finding must be grounded in an existing qualified `HospitalityAnalysisSignal` and its supporting discovered evidence.

The first implementation may use concise deterministic finding interpretations already present in the signal layer. It must not invent unsupported hotel-quality claims.

Examples of acceptable verified findings:

- a hotel/property identity signal was observed;
- a room-related entry point or room information was observed;
- guest-facing amenity information was observed;
- a guest contact/location path was observed;
- a booking entry point was observed;
- guest-facing dining information was observed.

These are observations of the public digital presence, not claims that the hotel is good, complete, available, or bookable.

Do not create negative findings merely because a positive observation was not found unless the evidence model and explicit deterministic rule establish that the relevant source was sufficiently observed and the missing content itself is a supported conclusion.

## Limitation Semantics

Reuse `HospitalityAnalysisLimitationService` and existing REQ-023 semantics.

A limitation may represent:

- timeout;
- network failure;
- HTTP/public acquisition failure where the existing contract supports it;
- another explicitly supported unable-to-verify condition.

A limitation must never become:

- hotel capability absence;
- booking failure;
- room absence;
- amenity absence;
- contact absence.

`NOT_ATTEMPTED` remains insufficient to establish a limitation unless the existing contract explicitly provides an inability-to-verify reason.

## Coverage Boundary

REQ-024 intentionally established **representation**, not classification.

REQ-026 must therefore NOT invent a classifier or thresholds.

The engine may construct the factual coverage inputs needed by `HospitalityAnalysisCoverage`, such as:

- intended journey scope;
- intended analysis dimensions supported by the current deterministic checks;
- assessable journey stages/dimensions;
- limited journey stages/dimensions;
- supporting findings;
- supporting limitations.

However, the final `HospitalityAnalysisCoverageState` must remain a governed input rather than an invented score/threshold calculation unless an explicit repository contract has been added before implementation.

If the existing contracts do not provide a truthful way to select the final coverage state, the implementation must stop at the representation boundary and report the gap rather than inventing a rule.

## Analysis Result

Use the existing `HospitalityAnalysisResult` from REQ-025.

The engine must not introduce another result type or duplicate the finding/limitation/coverage graphs.

The returned result must:

- belong to exactly one evaluation;
- preserve existing findings, limitations, and coverage objects;
- allow zero findings;
- retain provenance through existing objects;
- remain immutable after creation.

## Deterministic Rule Boundary

All analysis rules in REQ-026 must be explicit and testable.

For the same structured evidence input:

```text
same input → same analysis artifacts
```

No:

- random behavior;
- clock-dependent classification;
- external network access;
- LLM/model calls;
- mutable global state.

## Input Boundary

The engine consumes `StructuredEvidence` only.

It must not invoke:

- the crawler;
- browser acquisition;
- HTTP clients;
- external services;
- acquisition retries.

Acquisition is already completed upstream.

## Multi-Evidence Handling

The engine must support multiple `StructuredEvidence` items belonging to the same evaluation.

It must:

- process each eligible successful discovered evidence item;
- preserve evidence identity and traceability;
- avoid duplicate equivalent findings where the same signal is encountered repeatedly;
- preserve materially distinct evidence rather than silently overwriting it;
- reject or fail clearly on cross-evaluation input rather than mixing evaluations.

Do not invent a universal first-party-over-third-party hierarchy.

## Conflict Handling

If evidence produces materially different observations, the engine must not silently overwrite one with another.

At this stage it is acceptable to retain multiple traceable findings/signals rather than resolving the conflict.

Conflict resolution and source-quality ranking remain future work unless already explicitly governed.

## Hospitality Journey

The current implemented signal mappings must remain aligned with:

- DISCOVER
- UNDERSTAND
- EXPLORE
- TRUST
- BOOK

Do not force an observation into exactly one stage when the existing signal contract already assigns multiple stages.

## Deduplication

Deduplicate only when the repository can establish that two generated findings represent the same deterministic signal for the same source/evidence context.

Do not deduplicate merely because two different pages mention the same word.

Preserve distinct source references.

## Explicit Non-Scope

Do NOT implement:

- browser/crawler acquisition;
- scraping infrastructure;
- search APIs;
- AI/LLM/model confidence;
- numerical scoring;
- hotel quality score;
- severity calibration;
- ranking/grading;
- recommendation generation;
- report generation/UI/PDF/dashboard;
- persistence/database schema;
- REST API;
- interactive preview;
- `<hotel-name>.antenapro.com` integration;
- booking/OTA integration;
- competitor analysis;
- generic SEO auditing;
- universal source hierarchy;
- full nine-dimension ontology expansion;
- arbitrary coverage thresholds;
- arbitrary coverage classification.

## Dependencies

- `SPEC-005` — Evidence Model
- `SPEC-006` — Hospitality Analysis Contract
- REQ-019 — Hospitality Observation Foundation
- REQ-021 — Hospitality Analysis Signal Foundation
- REQ-022 — Hospitality Finding Foundation
- REQ-023 — Hospitality Analysis Limitation Foundation
- REQ-024 — Hospitality Analysis Coverage Foundation
- REQ-025 — Hospitality Analysis Result Foundation
- existing evaluation/evidence identity conventions
- existing backend engineering/testing standards

## Acceptance Criteria

1. A single analysis service can process multiple `StructuredEvidence` items belonging to one evaluation.
2. Successful discovered evidence is passed through the existing observation service.
3. Eligible observations are passed through the existing signal qualification service.
4. Qualified signals can become verified hospitality findings without inventing unsupported hotel claims.
5. Supported acquisition/evidence failures can become truthful unable-to-verify limitations through the existing REQ-023 contract.
6. `NOT_ATTEMPTED` cannot silently become hotel-feature absence or an inability-to-verify limitation without an explicit supported reason.
7. Cross-evaluation evidence is rejected and never mixed into an analysis result.
8. Findings preserve their originating signal/evidence/provenance chain.
9. Limitations preserve their supporting evidence/acquisition provenance.
10. Multiple distinct evidence sources remain traceable; no silent conflict overwrite occurs.
11. Equivalent duplicate signals are handled deterministically without losing distinct source context.
12. Zero findings is valid and does not imply absence of hotel capabilities.
13. Coverage inputs are derived only from actual assessable evidence and existing analysis artifacts.
14. No arbitrary coverage threshold or classification rule is invented.
15. The existing `HospitalityAnalysisResult` is used as the final aggregation boundary.
16. The complete pipeline is deterministic and has no external/network/AI dependency.
17. Existing upstream acquisition, evidence, observation, signal, finding, limitation, coverage, and result contracts remain intact.
18. Focused tests and complete backend CI pass.

## Engineering Expectations

Prefer a small application service/orchestrator with explicit collaborators:

```text
HospitalityAnalysisService
    ↓
ObservationService
    ↓
SignalService
    ↓
Finding creation
    ↓
Limitation creation
    ↓
Coverage representation
    ↓
HospitalityAnalysisResult
```

This is guidance, not a mandate to create unnecessary classes. Reuse existing services rather than duplicating their rules.

Do not create a generic rule engine, plugin framework, strategy hierarchy, or reflection-based pipeline.

Keep individual deterministic rules small, named, and unit-testable.

## Testing Requirements

Tests must include:

### End-to-end deterministic pipeline

- one successful evidence item producing observation → signal → finding;
- multiple successful evidence items;
- mixed successful and failed evidence;
- zero eligible observations;
- repeated equivalent evidence;
- deterministic repeat execution.

### Truthfulness

- failed evidence does not become a negative hotel finding;
- timeout does not become booking failure;
- missing room signal does not become no rooms;
- `NOT_ATTEMPTED` does not become absence;
- limitations remain limitations.

### Evaluation integrity

- cross-evaluation evidence is rejected;
- output artifacts all belong to the same evaluation.

### Provenance

- finding → signal → observation → evidence remains traceable;
- limitation → evidence/acquisition remains traceable;
- multiple sources remain distinct.

### Coverage

- assessable/limited scope is derived from actual artifacts;
- no page-count or finding-count scoring;
- no arbitrary percentage threshold;
- if final state cannot be governed, the implementation does not invent one.

### Regression

- existing REQ-019/021/022/023/024/025 tests continue to pass unchanged.

No live hotel website or external network dependency is permitted.

## Validation

The implementation session must:

1. run focused REQ-026 tests;
2. run the complete backend Maven test suite;
3. run GitHub Actions Backend Validation against the final PR head;
4. update this requirement with the implementation/validation record;
5. document any coverage-classification gap explicitly rather than inventing semantics.

## Requirement Governance

Only move this requirement to `PR_READY` after:

- implementation is complete;
- focused tests pass;
- full backend CI passes;
- the same requirement file contains the implementation record;
- the PR is open against `main`.

**STOPPING FOR ORCHESTRATOR REVIEW.**
