# REQ-024 — Hospitality Analysis Coverage Foundation

STATUS: PR_READY
REQUIREMENT_ID: REQ-024
TYPE: Implementation
BRANCH: `feature/hospitality-analysis-coverage-foundation`

## Objective

Implement the smallest deterministic domain boundary for representing **analysis coverage**.

Coverage answers:

> How much of the intended hospitality evaluation could be responsibly assessed?

Coverage is not page-count coverage, website verification percentage, hotel quality, scoring, ranking, grading, severity, recommendation, report output, or preview output.

## Product Contract

The product-level coverage states are exactly:

- `SUBSTANTIALLY_ASSESSED`
- `PARTIALLY_ASSESSED`
- `INSUFFICIENT_COVERAGE`

`SPEC-006` intentionally leaves exact thresholds for future implementation/product calibration. REQ-024 therefore does **not** calculate coverage from numerical thresholds and does not invent calibration rules.

The implementation represents an explicit, already-governed coverage assessment and preserves the context needed to explain it. It does not derive a state from page count, finding count, or arbitrary percentages.

## Hospitality Scope

Coverage is organized around the existing guest journey:

`DISCOVER → UNDERSTAND → EXPLORE → TRUST → BOOK`

and the hospitality analysis dimensions defined by `SPEC-006`:

1. Hotel identity and property understanding
2. Discoverability and navigation
3. Rooms and room information
4. Amenities and guest-facing information
5. Contact and location
6. Booking discoverability and journey signals
7. Trust and clarity
8. Mobile and technical guest experience
9. SEO and structured-data supporting signals

The representation records intended scope, assessable scope, and limited scope for journey stages and dimensions.

## Truthfulness Boundary

Coverage describes assessment capability, not hotel capability or quality.

- Unable to verify is not absence.
- Failed/unavailable acquisition is not a hotel deficiency.
- Missing evidence is not proof of absence.
- Few findings do not imply low coverage.
- Many findings do not imply high coverage.
- Many pages do not imply substantially assessed.
- A booking engine that cannot be observed may limit coverage; it must not become a claim that booking is unavailable.
- Room information that cannot be observed may limit coverage; it must not become a claim that the hotel has no room information.

Existing findings and limitations are retained by reference so their evidence/provenance chains remain traceable rather than being duplicated.

## Scope

Implement only small immutable domain types for coverage representation:

- qualitative coverage-state enum;
- hospitality analysis-dimension enum mapped directly to `SPEC-006` terminology;
- immutable coverage value object preserving evaluation identity, intended/assessable/limited journey and dimension scope, supporting findings/limitations, coverage state, and rationale.

No coverage engine or generic rule framework is introduced.

## Explicit Non-Scope

Do not implement:

- numerical thresholds or percentages;
- score, grade, rating, ranking, or weighted score;
- severity;
- recommendations;
- report assembly, DTO, API, UI, PDF, dashboard;
- interactive hotel preview or Antena integration;
- AI/LLM/model confidence;
- crawler, browser, HTTP client, acquisition, retry, scraping, or network access;
- changes to acquisition, evaluation lifecycle, evidence normalization, observations, analysis signals, findings, or limitations;
- persistence;
- generic `CoverageEngine`, `AssessmentEngine`, rule framework, strategy/plugin system, factory, or speculative abstraction.

## Dependencies

- `SPEC-005` — Evidence Model
- `SPEC-006` — Hospitality Analysis Contract
- REQ-019 — Hospitality Observation Foundation
- REQ-021 — Hospitality Analysis Signal Foundation
- REQ-022 — Hospitality Finding Foundation
- REQ-023 — Hospitality Analysis Limitation Foundation
- existing backend engineering/testing conventions

## Acceptance Criteria

1. Coverage can represent all three governed qualitative states.
2. Coverage retains evaluation identity.
3. Coverage explicitly represents intended hospitality scope.
4. Coverage retains the Discover/Understand/Explore/Trust/Book journey lens.
5. Coverage represents assessable and limited journey/dimension areas.
6. Relevant findings and limitations remain traceable through existing domain objects.
7. Inability to verify can contribute to limited coverage without becoming a hotel deficiency.
8. Missing/failed evidence does not imply absence of a hotel capability.
9. Finding count does not determine coverage.
10. Page count does not determine coverage.
11. Coverage contains no score, percentage, grade, ranking, severity, or recommendation.
12. Same input produces the same coverage representation.
13. No external network or live hotel website is required by tests.
14. Existing upstream contracts remain unchanged.
15. Backend Maven validation and GitHub Actions Backend Validation pass.

## Implementation Record — Session 24

### Context reconciliation

The actual current `main` was inspected before implementation. Repository history shows `main` at:

`ff00182ac5403afb8279f2560207aa2d9641636f` — `docs: advance orchestrator next step after REQ-023`.

`docs/context/CURRENT-STATE.md` still records an older commit (`0bfd78fdc823ef31111b0302a732cf43ad5dc26a`), so that durable snapshot was stale. The actual repository state and merged code were treated as authoritative, consistent with the orchestrator authority rule.

The supplied READY REQ-024 text was not present at the expected path on actual `main`; the repository lookup returned `404`. The session-provided requirement was therefore used as the governing task input and this durable requirement file was created on the feature branch. No product decision was invented to resolve that repository/documentation mismatch.

### Existing contracts inspected

Inspected the implemented REQ-019/021/022/023 boundaries, including:

- `HospitalityObservationCategory`
- `GuestJourneyStage`
- `HospitalityAnalysisSignal`
- `HospitalityFinding`
- `HospitalityAnalysisLimitation`
- `StructuredEvidence`

The existing contracts provide traceable findings and limitations but do not provide a governed numerical coverage calculation or threshold. Therefore REQ-024 implements representation only; coverage state is explicit input rather than calculated by an ungoverned rule.

### Implementation summary

Added:

- `HospitalityAnalysisCoverageState` — exactly the three `SPEC-006` coverage states.
- `HospitalityAnalysisDimension` — the nine hospitality analysis dimensions defined by `SPEC-006`.
- `HospitalityAnalysisCoverage` — immutable coverage representation retaining evaluation identity, intended/assessable/limited journey and dimension scope, supporting findings/limitations, qualitative state, and rationale.

The coverage value object defensively copies collections, requires non-empty intended hospitality scope, prevents assessable/limited areas from escaping the intended scope, requires an explicit rationale, and validates that supporting findings/limitations belong to the same evaluation.

No coverage calculation engine, threshold, page-count input, finding-count input, score, severity, recommendation, report, preview, AI, network, or upstream behavior was introduced.

### Files changed

- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisCoverageState.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisDimension.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisCoverage.java`
- `backend/src/test/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisCoverageTest.java`
- `requirements/REQ-024-hospitality-analysis-coverage-foundation.md`

### Tests

Added deterministic tests covering:

- exact three coverage states;
- all nine hospitality analysis dimensions;
- Discover/Understand/Explore/Trust/Book scope;
- intended, assessable, and limited scope representation;
- explicit state representation without numerical thresholds;
- inability-to-verify represented as limitation context rather than hotel deficiency;
- finding/limitation evaluation traceability;
- cross-evaluation reference rejection;
- coverage not derived from finding count or page count;
- defensive copying of collections.

No live hotel website or external network dependency is used by the tests.

### Validation

Local Maven execution was attempted but repository cloning could not resolve `github.com` in the execution environment. Therefore local Maven success cannot be claimed.

GitHub Actions Backend Validation is required before this requirement can be considered validated. The requirement will remain `PR_READY` only after the actual CI run for the branch completes successfully.

### CI

PENDING at implementation handoff; do not interpret this record as CI success until the GitHub Actions run for the PR head is observed and confirmed.

### PR

PR will target `main` from:

`feature/hospitality-analysis-coverage-foundation`

Do not merge. The PR is for orchestrator review.

### Limitations

- Exact coverage calibration/thresholds remain intentionally unspecified by `SPEC-006` and are not invented here.
- The current implementation does not calculate coverage from upstream evidence; it represents an explicitly supplied governed coverage assessment.
- `NOT_APPLICABLE` coverage semantics are not introduced because the requirement defines only the three coverage states.
- No persistence or report-level aggregation is included.

### Self-review

#### Scope

Only REQ-024 behavior was added. No acquisition, lifecycle, evidence, observation, signal, finding, or limitation behavior was changed.

#### Truthfulness

Coverage describes assessment completeness and preserves inability-to-verify as limitation context. It does not infer hotel capability, quality, or booking/room availability.

#### Thresholds

No numerical or arbitrary threshold was introduced because `SPEC-006` explicitly leaves calibration for later governed work.

#### Hospitality

Coverage is explicitly organized around the five guest-journey stages and nine hospitality analysis dimensions from `SPEC-006`.

#### Traceability

Supporting findings and limitations are retained as existing domain objects, preserving their existing traceability to observations/evidence. Evaluation identity is explicitly retained and cross-evaluation references are rejected.

#### Simplicity

No generic coverage engine, rule framework, factory, strategy system, persistence model, API, or external dependency was introduced.

## Governance

`STATUS: PR_READY` is permitted only after the PR is created and the actual GitHub Actions Backend Validation run for the final PR head passes. Until that point, CI is explicitly pending.

**STOPPING FOR ORCHESTRATOR REVIEW.**
