# REQ-031 — Hospitality Analysis Report Foundation

STATUS: PR_READY
REQUIREMENT_ID: REQ-031
TYPE: Implementation
BRANCH: `feature/hospitality-analysis-report-foundation`

## Objective

Introduce the first stable, structured hospitality analysis report domain over the completed deterministic analysis, guest-journey, coverage, limitation, and recommendation boundaries.

REQ-031 makes the mature analysis chain consumable as one report representation. It is a domain/model boundary, not a presentation or rendering layer.

## Repository Baseline

REQ-031 consumes the existing pipeline:

```text
StructuredEvidence
      ↓
HospitalityObservation
      ↓
Qualified HospitalityAnalysisSignal
      ↓
HospitalityFinding / Deficiency / Limitation
      ↓
HospitalityAnalysisCoverage
      ↓
HospitalityAnalysisResult
      ↓
GuestJourneyAnalysis
      ↓
HospitalityRecommendations
      ↓
Structured Hospitality Analysis Report
```

It must not redesign acquisition, evidence, observations, signals, findings, deficiencies, limitations, coverage, analysis result, guest journey, or recommendation contracts.

## Scope

Implement a small immutable report representation and deterministic assembly service that can represent, at minimum:

- evaluation identity and hotel target identity already available from governed evaluation inputs/results;
- analysis coverage and truthful limitations;
- an executive-level summary derived only from existing governed outputs;
- guest-journey stages and their governed impacts;
- observed strengths only where existing evidence/analysis explicitly supports them;
- observed deficiencies/problems;
- bounded recommendations;
- source/evaluation attribution needed to preserve existing provenance boundaries.

The report must aggregate existing domain outputs. It must not independently rediscover hotel facts or re-run analysis rules.

## Report Truth Boundary

The report is a representation of existing governed analysis, not a new evidence source.

```text
Missing evidence              ≠ positive hotel fact
No finding                    ≠ verified strength
Unsupported dimension         ≠ successful dimension
Acquisition failure           ≠ hotel deficiency
NOT_ATTEMPTED                 ≠ hotel deficiency
Unable to verify              ≠ observed deficiency
No recommendation             ≠ no problem exists
```

Do not fill empty report sections with invented content.

A report section may be empty, unavailable, or explicitly limited when the underlying governed contracts do not establish content.

## Evaluation Identity

A report must belong to exactly one evaluation boundary.

Evaluation A must never include findings, limitations, journey impacts, recommendations, or coverage from Evaluation B.

Use the existing evaluation identity semantics rather than creating a second evaluation model.

## Hotel / Target Identity

Use only hotel/target identity already established by the existing canonical evaluation and analysis contracts.

Do not perform target discovery or independently resolve hotel identity in REQ-031.

If a hotel identity is not sufficiently available from existing contracts, preserve the missing/unknown state rather than inventing it.

## Executive Summary

REQ-031 may provide a deterministic summary representation derived from existing report components.

The summary must not be an LLM-generated narrative.

It must not introduce:

- new findings;
- new scores;
- new severity;
- business-value estimates;
- conversion estimates;
- unsupported strengths;
- unsupported claims about booking success.

A summary can contain bounded counts or structured statements only when those values are already represented by governed report inputs.

Avoid turning the summary into a generic website-audit scorecard.

## Guest Journey

Reuse the existing `GuestJourneyAnalysis` and `GuestJourneyStage` semantics:

```text
DISCOVER → UNDERSTAND → EXPLORE → TRUST → BOOK
```

The report must preserve stage impacts and limitations already produced by the journey layer.

Do not create a second journey model.

Do not recalculate journey impacts from finding text.

Do not convert absence of a finding into a positive journey claim.

## Strengths

Strengths are allowed only when existing analysis contracts explicitly establish an observed positive/verified signal suitable for reporting.

The absence of a deficiency is not sufficient to create a strength.

If the current analysis model does not expose a governed positive signal for a proposed strength, the report must leave that strength unrepresented rather than invent it.

## Problems / Deficiencies

Reuse existing `HospitalityFinding` / deficiency semantics.

Do not create a second problem/finding hierarchy.

Preserve:

- source finding identity;
- evaluation identity;
- existing journey-stage impact where available;
- existing provenance chain.

## Limitations

Reuse existing limitation semantics, including `UNABLE_TO_VERIFY` where present.

Limitations must remain distinguishable from deficiencies.

Do not turn limitations into hotel defects merely because the report needs a "problems" section.

Do not convert limitations into recommendations unless the existing recommendation layer already produced one.

## Recommendations

Reuse `HospitalityRecommendation` outputs from REQ-030.

Do not generate recommendations inside the report service.

The report is an aggregation boundary, not a second recommendation engine.

Preserve recommendation provenance and journey-stage information already present in the recommendation model.

## Coverage

Reuse `HospitalityAnalysisCoverage` / existing coverage semantics.

Do not introduce:

- report completeness percentages;
- new coverage formulas;
- scores;
- weighted dimensions;
- page-count-based coverage;
- finding-count-based coverage.

Coverage must remain exactly the governed analysis coverage representation.

## Provenance

The report must preserve enough references to trace report sections back to their existing governed source objects.

At minimum, report entries derived from findings/recommendations must retain source identifiers and evaluation identity where those fields already exist.

Do not duplicate `StructuredEvidence` into the report model.

Do not create a second provenance framework.

## Determinism

For identical existing analysis inputs:

```text
HospitalityAnalysisResult
+
GuestJourneyAnalysis
+
HospitalityRecommendations
        ↓
identical Structured Hospitality Analysis Report
```

No randomness, current-time dependency, mutable global state, network access, or AI.

## No Scoring / Prioritization

Do not introduce:

- overall score;
- dimension score;
- journey score;
- severity score;
- recommendation priority;
- ROI/conversion estimates;
- business-value ranking.

If stable ordering is needed, use deterministic technical ordering and document that it is not business priority.

## No AI / Network / External Integrations

No LLM, model provider, prompt, generative summary, network access, crawling, browser automation, target discovery, Google Business Profile, Search Console, GA4, OTA APIs, booking APIs, or external analytics.

Connected first-party digital performance remains a future plan-gated capability.

## No Presentation Layer

Do not implement:

- frontend;
- report pages;
- dashboard UI;
- PDF generation;
- email rendering;
- public report rendering;
- HTML templates;
- CSS/design system;
- charting/visualization.

The output is a backend/domain report model only.

## No Persistence

Do not introduce report database tables, repositories, migrations, storage schemas, or evaluation-history persistence.

Persistence is a later requirement.

## No Antena Integration

Do not implement:

- `<hotel-name>.antenapro.com`;
- Antena opportunity mapping;
- preview generation;
- Antena package mapping;
- conversion CTAs.

Antena integration remains downstream after analysis maturity.

## Hospitality-First Boundary

The report must present hospitality analysis concepts rather than becoming a generic SEO/website audit report.

Technical/SEO/structured-data/mobile information may appear only through existing governed analysis outputs. REQ-031 must not add new technical auditing logic.

## Architectural Shape

Prefer:

```text
Immutable report record/model
+
small deterministic assembly service
+
focused tests
```

Do not introduce:

- generic report engines;
- template engines;
- reflection-based aggregation;
- dynamic section registries;
- workflow engines;
- generic document frameworks;
- scoring engines;
- AI abstractions.

Keep the report domain explicit and understandable from existing contracts.

## Testing Acceptance Criteria

Automated tests must demonstrate:

1. A report can be assembled from one evaluation's existing analysis result, journey analysis, and recommendations.
2. Evaluation identity is preserved.
3. Evaluation A cannot leak data into Evaluation B.
4. Existing coverage is preserved without recalculation.
5. Existing limitations remain limitations.
6. Existing deficiencies remain deficiencies.
7. Existing recommendations are reused rather than regenerated.
8. Missing evidence does not create positive hotel facts.
9. No finding does not automatically create a strength.
10. Unsupported dimensions remain unsupported.
11. Booking truth remains unchanged.
12. Journey-stage impacts are preserved without text parsing.
13. Source/provenance references remain traceable.
14. Identical input produces identical report output.
15. No scoring/prioritization exists.
16. No AI/network dependency exists.
17. No persistence or UI dependency exists.
18. REQ-027 through REQ-030 behavior remains unchanged.
19. Complete backend tests pass.

## Completion Requirements

Before `PR_READY`:

- inspect current `main` and referenced requirements/specifications;
- implement only REQ-031;
- add focused deterministic tests;
- run complete backend Maven tests;
- run Backend Validation against the exact final PR head;
- update this same requirement file with implementation, tests, validation, architectural decisions, truth-boundary decisions, limitations, and self-review;
- ensure the final requirement-file update is included in the successfully validated final PR head.

Do not merge. Do not start REQ-032.

## Orchestrator Intent

REQ-031 is deliberately the first report-model boundary after the analysis chain becomes actionable.

The report model should make the existing analysis understandable and consumable without prematurely committing the project to a UI, API, persistence model, PDF format, or public-report presentation.

The next implementation decision after REQ-031 must be based on actual repository maturity and product value, not automatic REQ-number progression.

## Session 31 Implementation Record

### Repository/context reconciliation

- Current `main` at implementation start: `d06cca42550e612daeb8fc2cc940492e32e8cb4f`, the requirement commit that added REQ-031.
- REQ-027 was verified merged as PR #27.
- REQ-028 was verified merged as PR #28.
- REQ-029 was verified merged as PR #29.
- REQ-030 was verified merged as PR #30.
- REQ-031 was verified present on `main` with `STATUS: READY` before implementation.
- Existing REQ-027 through REQ-030 contracts were inspected on `main`; no material mismatch was found that required stopping the session.

### Contracts inspected

Inspected the required evidence, analysis, and engineering context plus the implemented contracts for:

- `CanonicalEvaluationRequest.EvaluationTarget` as the existing hotel/target identity source;
- `HospitalityAnalysisResult` as the immutable analysis aggregation boundary;
- `GuestJourneyAnalysis` / `GuestJourneyStageAnalysis` as the existing journey model;
- `HospitalityAnalysisCoverage` as the existing governed coverage representation;
- `HospitalityAnalysisLimitation` as the existing limitation/provenance boundary;
- `HospitalityFinding` / `HospitalityFindingKind` as the existing deficiency/observation distinction;
- `HospitalityRecommendation` as the existing bounded recommendation output.

The report implementation does not create a second evaluation, evidence, journey, finding, limitation, coverage, or recommendation model.

### Implementation summary

Added the smallest explicit backend report boundary:

- `HospitalityAnalysisReport` — immutable report representation retaining the existing analysis result, guest-journey analysis, hotel target identity when available, and existing recommendations.
- `HospitalityAnalysisReportSummary` — deterministic executive summary containing only bounded counts and the existing journey stages with observed impact; it is not a score, priority, or coverage calculation.
- `HospitalityAnalysisReportService` — deterministic in-memory aggregation service that validates one evaluation boundary and reuses existing objects rather than re-running analysis or recommendation rules.

The report exposes deficiencies by filtering the existing typed `HospitalityFindingKind.DEFICIENCY`; ordinary observations are not promoted to problems or strengths. Because the current governed finding model exposes no explicit positive/verified strength contract, `strengths()` is intentionally empty and does not infer strengths from missing deficiencies.

Hotel identity is accepted only from the existing `CanonicalEvaluationRequest.EvaluationTarget`. The assembly service permits a missing target and preserves it as `null`; it performs no discovery or resolution.

The report summary derives only:

- deficiency count from existing deficiency findings;
- limitation count from existing limitations;
- recommendation count from supplied recommendation outputs;
- journey stages whose existing journey analysis state is `OBSERVED_IMPACT`.

No coverage score, finding-count coverage, severity, prioritization, ROI, conversion estimate, booking-success claim, AI, network access, persistence, UI, PDF, or Antena integration was added.

### Truth-boundary decisions

- Missing hotel identity remains `null`; no target is invented.
- Existing `HospitalityAnalysisCoverage` is retained by reference and not recalculated.
- Existing limitations remain limitations; they are not converted into deficiencies or recommendations.
- Existing deficiency semantics remain typed `HospitalityFindingKind.DEFICIENCY`; ordinary observations remain observations.
- No observed strength is synthesized from the absence of a deficiency. The current report therefore exposes an empty strength set.
- Recommendations are supplied to the report service and reused by reference; an empty recommendation input remains empty.
- Guest-journey impacts are taken from `GuestJourneyAnalysis` state and existing stage impacts; no finding text is parsed.
- Booking truth is unchanged: the report cannot create a booking deficiency or recommendation that the analysis/recommendation layers did not already produce.

### Provenance / evaluation isolation

The report retains the existing `HospitalityAnalysisResult`, `GuestJourneyAnalysis`, `HospitalityFinding`, `HospitalityAnalysisLimitation`, and `HospitalityRecommendation` objects rather than copying evidence or creating new provenance identifiers. Their existing source/evaluation chains remain inspectable.

The report model and assembly service require the report evaluation ID to match the analysis result and journey analysis, and require every recommendation to belong to the same evaluation. Cross-evaluation recommendations or journey objects are rejected rather than discarded or reassigned.

### Files changed

- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisReport.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisReportSummary.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisReportService.java`
- `backend/src/test/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisReportServiceTest.java`
- `requirements/REQ-031-hospitality-analysis-report-foundation.md`

### Tests

Focused REQ-031 tests were added for:

- single-evaluation report assembly and object/provenance preservation;
- ordinary observation not becoming a strength;
- missing hotel identity remaining missing;
- recommendation reuse rather than regeneration;
- evaluation A/B isolation for recommendations and journey analysis;
- booking truth preservation;
- deterministic repeat assembly.

The first final-head validation run was GitHub Actions Backend Validation **#296** / run ID **`37218759163`**, against PR head **`388520b1a48c776b1e3f4e6c4d215e7d5f7bbf54`**.

Result:

- Workflow: `Backend Validation`
- Job: `Java 21 / Maven tests`
- Command: `mvn --batch-mode --no-transfer-progress test`
- Result: **BUILD SUCCESS**
- Tests: **192**
- Failures: **0**
- Errors: **0**
- Skipped: **0**
- Focused `HospitalityAnalysisReportServiceTest`: **8 tests**, all passing.

Local Maven execution is not available in this session environment because direct repository cloning cannot resolve `github.com`; local test success is therefore not claimed. The GitHub Actions run is the authoritative repository-level validation.

### Architectural decisions

1. Use one explicit immutable report record plus one small assembly service and one bounded summary record; no generic report framework was introduced.
2. Retain existing domain objects by reference so evidence/provenance chains are not duplicated.
3. Use `CanonicalEvaluationRequest.EvaluationTarget` as the only hotel identity source and permit it to be absent rather than inventing identity.
4. Keep strengths conservative: no positive signal contract currently exists, so the report exposes no synthesized strengths.
5. Keep the summary descriptive and bounded; it contains counts and stage-state information only and has no scoring/prioritization semantics.

### Security / production review

The new code is pure in-memory domain aggregation. It performs no network access, URL fetching, persistence, external calls, file access, dynamic execution, or mutable global state. Inputs are validated for evaluation ownership and null collections at the domain boundary. The implementation performs only bounded collection filtering and set copying over already-produced analysis objects.

### Limitations

- The current analysis model does not expose a governed positive/verified strength signal distinct from ordinary observations, so strengths cannot responsibly be populated yet.
- Hotel target identity is nullable at the report boundary because REQ-031 does not define target discovery or identity resolution.
- The report exists only as an in-memory domain representation; API, rendering, persistence, and customer-facing delivery remain downstream requirements.
- Local Maven execution remains unavailable because the session environment cannot resolve `github.com`; CI is the authoritative repository-level validation path.

### Self-review

#### Scope
Only REQ-031 report model, deterministic assembly, focused tests, and this requirement record were changed. No acquisition, evidence, observation, signal, finding, limitation, coverage, journey, recommendation, UI, integration, persistence, or Antena behavior was changed.

#### Correctness
The report reuses typed deficiency semantics, preserves existing coverage/limitation/journey/recommendation objects, validates evaluation ownership, and never parses finding text or generates recommendations.

#### Truthfulness
Missing evidence, absent findings, unsupported dimensions, acquisition limitations, and no recommendations remain non-positive/non-defect claims. Booking truth is inherited unchanged from upstream governed outputs.

#### Determinism
The assembly uses no clock, randomness, network, AI, or mutable global state. The same existing objects and inputs produce equal report values.

#### Testing
Focused REQ-031 tests were added and the complete backend suite passed in Backend Validation run #296 against the exact pre-finalization PR head. This requirement-file update changes the branch head, so a new Backend Validation run is required and will be the authoritative final-head validation.

#### Repository hygiene
The branch diff contains only the four REQ-031 implementation/test files plus this requirement update. No unrelated generated artifacts or application modules were changed.

### Final-head validation rule

The requirement-file update containing this record changed the PR head after run #296. Therefore run #296 is intentionally **not** treated as final-head validation for `PR_READY`.

Backend Validation must pass against the exact new final PR head. Only after that run succeeds is `STATUS: PR_READY` considered final.

**STOPPING FOR ORCHESTRATOR REVIEW.**
