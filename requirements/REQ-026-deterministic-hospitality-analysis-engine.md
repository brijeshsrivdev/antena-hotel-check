# REQ-026 — Deterministic Hospitality Analysis Engine

STATUS: PR_READY
REQUIREMENT_ID: REQ-026
TYPE: Implementation
BRANCH: `feature/deterministic-hospitality-analysis-engine`

## Objective

Move from isolated hospitality-analysis domain foundations to the first **usable deterministic analysis pipeline**.

REQ-026 consumes already-retained `StructuredEvidence` for one evaluation and deterministically produces analysis artifacts that can be assembled into a `HospitalityAnalysisResult`:

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

It does **not** introduce crawling, browser automation, scoring, AI, report UI, persistence, or Antena preview integration.

## Product Question

The analysis remains organized around:

**Can a guest find, understand, trust, explore, and book this hotel online?**

The implementation remains hospitality-first rather than becoming a generic website audit.

## Scope

Implement a deterministic analysis application/service that:

1. accepts the already-produced structured evidence for exactly one evaluation;
2. invokes the existing `HospitalityObservationService`;
3. qualifies observations through the existing `HospitalityAnalysisSignalService`;
4. converts supported qualified signals into verified hospitality findings;
5. converts supported unavailable/failed evidence conditions into truthful unable-to-verify limitations where existing REQ-023 rules permit it;
6. derives factual coverage representation inputs from what was actually assessable;
7. assembles the existing `HospitalityAnalysisResult`;
8. preserves evaluation identity and evidence/provenance traceability throughout.

The service supports the currently implemented hospitality observation categories:

- HOTEL_IDENTITY
- ROOMS
- AMENITIES
- CONTACT
- BOOKING
- DINING

These six observation categories are **not** the complete hospitality analysis ontology. They are the currently implemented observation capabilities.

## Finding Semantics

A verified finding is grounded in an existing qualified `HospitalityAnalysisSignal` and its supporting discovered evidence.

The implementation uses concise deterministic finding interpretations already present in the signal layer. It does not invent unsupported hotel-quality claims.

These remain observations of the public digital presence, not claims that the hotel is good, complete, available, or bookable.

Do not create negative findings merely because a positive observation was not found unless the evidence model and explicit deterministic rule establish that the relevant source was sufficiently observed and the missing content itself is a supported conclusion.

## Limitation Semantics

Reuse `HospitalityAnalysisLimitationService` and existing REQ-023 semantics.

A limitation may represent timeout, network failure, HTTP/public acquisition failure where the existing contract supports it, or another explicitly supported unable-to-verify condition.

A limitation must never become hotel capability absence, booking failure, room absence, amenity absence, or contact absence.

`NOT_ATTEMPTED` remains insufficient to establish a limitation unless the existing contract explicitly provides an inability-to-verify reason.

## Coverage Boundary

REQ-024 intentionally established **representation**, not classification.

REQ-026 does not invent a classifier or thresholds.

The engine distinguishes two separate concepts:

1. **Full intended hospitality analysis scope** — the complete nine-dimension scope governed by SPEC-006.
2. **Currently assessable scope** — the subset of those dimensions supported by findings and limitations produced by the currently implemented REQ-026 observation/signal capabilities.

The full intended dimension set is explicit and independent of `DIMENSION_BY_CATEGORY`:

- `HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING`
- `DISCOVERABILITY_AND_NAVIGATION`
- `ROOMS_AND_ROOM_INFORMATION`
- `AMENITIES_AND_GUEST_FACING_INFORMATION`
- `CONTACT_AND_LOCATION`
- `BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS`
- `TRUST_AND_CLARITY`
- `MOBILE_AND_TECHNICAL_GUEST_EXPERIENCE`
- `SEO_AND_STRUCTURED_DATA_SUPPORTING_SIGNALS`

The current observation categories contribute only to dimensions they genuinely support. In particular, `DINING` contributes to the existing `AMENITIES_AND_GUEST_FACING_INFORMATION` dimension and does not create a separate analysis dimension.

Unsupported dimensions remain part of the intended scope but are not marked assessable merely because the intended ontology contains them. They are not hotel deficiencies.

The final `HospitalityAnalysisCoverageState` remains a governed caller-supplied input. The engine does not derive it from page counts, finding counts, percentages, or thresholds.

## Analysis Result

Use the existing `HospitalityAnalysisResult` from REQ-025.

The returned result belongs to exactly one evaluation, preserves existing findings, limitations, and coverage objects, allows zero findings, retains provenance through existing objects, and remains immutable after creation.

## Deterministic Rule Boundary

All REQ-026 analysis rules are explicit and testable. Identical structured evidence produces equivalent analysis artifacts. There is no randomness, clock-dependent classification, external network access, LLM/model call, or mutable global state.

## Input Boundary

The engine consumes `StructuredEvidence` only. It does not invoke crawling, browser acquisition, HTTP clients, external services, or acquisition retries.

## Multi-Evidence Handling

The engine supports multiple `StructuredEvidence` items belonging to the same evaluation. It processes each eligible evidence item, preserves evidence identity and traceability, deterministically deduplicates equivalent generated findings, preserves materially distinct sources, and rejects cross-evaluation input.

No universal source hierarchy is introduced.

## Conflict Handling

Materially different observations are not silently overwritten. Multiple traceable findings are retained.

## Hospitality Journey

The current implemented signal mappings remain aligned with DISCOVER, UNDERSTAND, EXPLORE, TRUST, and BOOK. Existing multi-stage signal assignments are preserved.

## Deduplication

Deduplication occurs through deterministic generated-artifact equality so equivalent signals from the same evidence/source context may be represented once, while distinct source evidence remains traceable.

## Explicit Non-Scope

Do NOT implement browser/crawler acquisition, scraping infrastructure, search APIs, AI/LLM/model confidence, numerical scoring, hotel quality score, severity calibration, ranking/grading, recommendation generation, report generation/UI/PDF/dashboard, persistence/database schema, REST API, interactive preview, booking/OTA integration, competitor analysis, generic SEO auditing, universal source hierarchy, full nine-dimension analysis capability, arbitrary coverage thresholds, arbitrary coverage classification, or new observation categories solely to populate unsupported analysis dimensions.

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

1. A single analysis service processes multiple `StructuredEvidence` items belonging to one evaluation.
2. Successful discovered evidence is passed through the existing observation service.
3. Eligible observations are passed through the existing signal qualification service.
4. Qualified signals become verified hospitality findings without unsupported hotel claims.
5. Supported acquisition/evidence failures become truthful unable-to-verify limitations through the existing REQ-023 contract.
6. Unsupported/not-attempted states do not silently become hotel-feature absence or unsupported limitations.
7. Cross-evaluation evidence is rejected and never mixed.
8. Findings preserve signal/evidence/provenance traceability.
9. Limitations preserve supporting evidence/acquisition provenance.
10. Multiple distinct evidence sources remain traceable; conflicts are not silently overwritten.
11. Equivalent duplicate signals are handled deterministically without losing distinct source context.
12. Zero findings is valid and does not imply absence of hotel capabilities.
13. The coverage's `intendedDimensions` contains the complete nine-dimension SPEC-006 hospitality scope independently of the currently implemented observation categories.
14. `assessableDimensions` contains only dimensions genuinely supported by current findings.
15. Unsupported dimensions remain in the intended scope and are not falsely marked assessable.
16. `DINING` does not create a new analysis dimension and only contributes to an existing governed dimension where supported by the current specification/domain semantics.
17. No arbitrary coverage threshold or classification rule is invented.
18. The existing `HospitalityAnalysisResult` is the final aggregation boundary.
19. The complete pipeline is deterministic and has no external/network/AI dependency.
20. Existing upstream contracts remain intact.
21. Focused REQ-026 coverage and complete backend CI pass.

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

Do not create a generic rule engine, plugin framework, strategy hierarchy, reflection-based pipeline, configuration registry, or metadata framework. Reuse existing services rather than duplicating their rules. The complete intended dimension scope should remain a small explicit governed constant/set.

## Testing Requirements

Tests cover the end-to-end deterministic pipeline, multiple evidence, mixed successful/failed evidence, zero eligible observations, repeated equivalent evidence, deterministic repeat execution, truthfulness of limitations, cross-evaluation rejection, output evaluation integrity, provenance, source conflicts, coverage representation, and regression of the existing suite.

The coverage tests additionally prove:

1. the intended dimension set contains all nine SPEC-006 dimensions;
2. assessable dimensions are derived only from currently supported findings;
3. discoverability/navigation, trust/clarity, mobile/technical, and SEO/structured-data remain intended but are not falsely assessed by unrelated current findings;
4. dining maps only to an existing governed dimension and does not redefine the analysis ontology;
5. the caller-supplied coverage state remains unchanged and no threshold/classifier is introduced.

No live hotel website or external network dependency is permitted.

## Validation

The implementation correction was validated through focused REQ-026 tests and the complete backend Maven suite. GitHub Actions Backend Validation must run against the final PR head; an earlier successful commit does not constitute final validation.

### Implementation Record — P1 Correction

#### Correction

The original implementation derived `intendedDimensions` from `DIMENSION_BY_CATEGORY.values()`. That incorrectly reduced the intended analysis scope to the six currently implemented observation categories.

The correction introduces an explicit `FULL_INTENDED_DIMENSIONS` set containing all nine dimensions governed by SPEC-006. `DIMENSION_BY_CATEGORY` remains only the mapping used to determine currently assessable/limited dimensions from actual observation artifacts.

This preserves the architectural distinction:

```text
SPEC-006 full intended dimensions
        │
        ├── dimensions supported by current findings
        │       ↓
        │   assessable dimensions
        │
        └── dimensions not yet supported
                ↓
            intended but not currently assessable
```

No new observation categories were added. `DINING` remains an observation category mapped to `AMENITIES_AND_GUEST_FACING_INFORMATION`.

#### Files changed for correction

- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisService.java`
- `backend/src/test/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisServiceTest.java`
- `requirements/REQ-026-deterministic-hospitality-analysis-engine.md`

#### Coverage-scope tests

Added tests proving:

- full nine-dimension intended scope;
- currently assessable dimensions are limited to supported findings;
- unsupported dimensions remain intended but not assessable;
- dining contributes to amenities without creating an invented dimension;
- existing caller-supplied coverage-state behavior remains intact.

#### Self-review

- Scope: only the P1 intended-vs-assessable coverage dimension issue was corrected.
- Specification: full intended dimension set matches SPEC-006.
- Current capability: only dimensions supported by current findings/limitations are assessable/limited.
- Dining: remains an observation category and does not redefine the analysis ontology.
- Truthfulness: unsupported dimensions are not represented as hotel deficiencies.
- Coverage: no thresholds, percentages, scoring, or automatic classification introduced.
- Regression: existing REQ-026 pipeline behavior remains covered by the complete test suite.
- Simplicity: one explicit dimension set; no framework or abstraction layer added.

## Requirement Governance

`STATUS: PR_READY` is set only after implementation, focused tests, complete backend validation, final-PR-head CI validation, and this requirement update.

**STOPPING FOR ORCHESTRATOR REVIEW.**
