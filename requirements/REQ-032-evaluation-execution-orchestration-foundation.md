# REQ-032 — Evaluation Execution Orchestration Foundation

STATUS: PR_READY
REQUIREMENT_ID: REQ-032
TYPE: Implementation
BRANCH: `feature/evaluation-execution-orchestration`

## Dependency Resolution Status

REQ-032 is now unblocked.

- REQ-034 — Hospitality Analysis Coverage Classification Implementation — merged.
- REQ-035 — Hospitality Coverage Assessment Contract — merged.
- REQ-036 — Hospitality Coverage Assessment Derivation Contract and runtime implementation — merged as PR #36.

The governed dependency chain is now:

```text
Governed hospitality observations / signals
        ↓
REQ-036 Coverage Assessment Derivation
        ↓
HospitalityCoverageAssessment
        ↓
REQ-034 Coverage Classification
        ↓
HospitalityAnalysisCoverageState
        ↓
HospitalityAnalysisService
```

REQ-032 consumes these boundaries and does not move their semantics into orchestration.

## Objective

Introduce the smallest explicit end-to-end orchestration boundary for executing one canonical hotel evaluation through the existing Antena Hotel Check analysis pipeline.

The product objective remains:

> **Can a guest find, understand, trust, explore, and book this hotel online?**

The interactive Antena-hosted hotel experience remains downstream.

## Canonical Execution Flow

```text
Canonical Evaluation Request
        ↓
Evaluation / Attempt
        ↓
Public Web Acquisition
        ↓
Structured Evidence
        ↓
Governed Hospitality Observations / Signals
        ↓
REQ-036 Coverage Assessment Derivation
        ↓
HospitalityCoverageAssessment
        ↓
REQ-034 Coverage Classification
        ↓
HospitalityAnalysisCoverageState
        ↓
Deterministic Hospitality Analysis
        ↓
Guest Journey Analysis
        ↓
Recommendations
        ↓
Structured Hospitality Analysis Report
```

The orchestrator coordinates this sequence and does not redefine stage semantics.

## Scope

Implemented only:

1. one canonical evaluation request execution boundary;
2. existing evaluation/attempt lifecycle usage;
3. existing acquisition integration exactly once;
4. existing evidence normalization;
5. existing observation/signal preparation;
6. REQ-036 coverage assessment derivation;
7. existing coverage classification;
8. existing deterministic hospitality analysis;
9. existing guest-journey analysis;
10. existing recommendation generation;
11. existing report assembly;
12. one evaluation-attributed orchestration result.

## Explicit Non-Scope

Do not add:

- acquisition behavior;
- crawler/browser changes;
- retries;
- raw evidence parsing;
- analysis rules;
- coverage classification rules inside orchestration;
- coverage-assessment derivation rules inside orchestration;
- duplicate coverage rules;
- scoring;
- prioritization;
- AI;
- Google integrations;
- API;
- UI;
- persistence;
- Antena integration;
- preview generation;
- generic workflow engines;
- queues, schedulers, distributed locks, or new transaction boundaries.

## Coverage Contract Boundary

REQ-036 establishes the governed derivation from existing qualified signals, typed identity-conflict facts, and explicitly scoped limitations into `HospitalityCoverageAssessment`.

The orchestrator:

- calls `HospitalityCoverageAssessmentDerivationService` before classification;
- passes the exact assessment returned by derivation to `HospitalityAnalysisCoverageClassifier`;
- passes the exact `HospitalityAnalysisCoverageState` returned by the classifier to `HospitalityAnalysisService`;
- never constructs a `HospitalityCoverageAssessment` manually;
- never constructs or defaults a coverage state;
- never reproduces classifier or derivation rules.

## Evaluation Identity

One execution corresponds to one evaluation identity and one current attempt.

The orchestrator obtains the evaluation/attempt from the existing acquisition integration and propagates the same evaluation ID through evidence, assessment derivation, deterministic analysis, journey analysis, recommendations, and report assembly.

No outputs from another evaluation are combined.

## Lifecycle / Failure Semantics

Existing lifecycle states are reused; no new status is introduced.

A successful analysis/report pipeline completes this orchestration slice as `INCOMPLETE` because the current product lifecycle requires both an analysis report and an interactive preview for `COMPLETED`, while preview generation is explicitly outside REQ-032.

If a mandatory downstream orchestration stage throws a runtime failure after an evaluation has been created, the current attempt is marked `FAILED` using the existing lifecycle contract and the exception is rethrown. Later stages are not executed.

Typed acquisition failures remain governed acquisition outcomes. They are not converted into hospitality deficiencies or synthetic coverage. The existing deterministic analysis service remains responsible for its current unscoped acquisition-limitation lifecycle.

## Acquisition

Use `EvaluationAcquisitionIntegrationService` and its existing `PublicWebAcquisitionService` contract.

No scraping, browser automation, retry, proxy, bypass, or third-party integration behavior was added.

## Evidence / Observations / Signals

Use the existing evidence normalization, observation, signal, and finding services.

The orchestrator prepares the governed qualified signals and typed findings needed by REQ-036 without manufacturing observations or signals.

Current acquisition limitations are unscoped. REQ-036 explicitly forbids assigning arbitrary journey/dimension scope to an unscoped failure, so REQ-032 does not synthesize limited scope before derivation. The deterministic analysis service continues to own the existing acquisition-limitation lifecycle.

## Analysis / Journey / Recommendations / Report

The orchestrator delegates directly to the existing services in this order:

1. `HospitalityAnalysisService`
2. `GuestJourneyAnalysisService`
3. `HospitalityRecommendationService`
4. `HospitalityAnalysisReportService`

No analysis, journey, recommendation, or report rules are implemented inside the orchestrator.

## Implementation Record — Session 38

### Repository reconciliation

Current `main` was inspected before implementation. Confirmed:

- REQ-034 is merged;
- REQ-035 is merged;
- REQ-036 is merged as PR #36;
- `HospitalityCoverageAssessmentDerivationService` exists;
- `HospitalityAnalysisCoverageClassifier` exists and consumes `HospitalityCoverageAssessment`;
- `HospitalityAnalysisService` consumes caller-supplied `HospitalityAnalysisCoverageState`;
- acquisition/evidence/observation/signal/finding/limitation contracts exist;
- guest journey analysis exists;
- recommendation generation exists;
- report generation exists;
- the canonical `CanonicalEvaluationRequest` exists;
- evaluation/attempt lifecycle and acquisition integration exist.

No material repository mismatch was found that blocks REQ-032.

### Implementation mapping

Added:

- `backend/src/main/java/com/antenapro/hotelcheck/evaluation/EvaluationExecutionOrchestrator.java`
- `backend/src/main/java/com/antenapro/hotelcheck/evaluation/EvaluationExecutionResult.java`
- `backend/src/test/java/com/antenapro/hotelcheck/evaluation/EvaluationExecutionOrchestratorTest.java`

The orchestrator uses the existing service contracts and keeps the execution order explicit.

### Orchestration sequence implemented

```text
acquisition
→ evidence normalization
→ observation/signal preparation
→ coverage assessment derivation
→ coverage classification
→ deterministic hospitality analysis
→ guest journey analysis
→ recommendations
→ report
```

### Coverage assessment

`HospitalityCoverageAssessmentDerivationService` is invoked directly before classification. The assessment is not constructed by the orchestrator.

### Coverage classification

`HospitalityAnalysisCoverageClassifier` receives the exact assessment returned by REQ-036 derivation. Its returned `HospitalityAnalysisCoverageState` is passed unchanged to `HospitalityAnalysisService`.

### Evaluation identity

The execution result and all downstream governed artifacts are validated against the evaluation ID and attempt attribution. The existing acquisition integration creates the evaluation once; the orchestrator does not create a second evaluation identity.

### Failure handling

A mandatory downstream runtime failure marks the existing running attempt as `FAILED`, preserves the exception, and prevents later stages from executing.

### Lifecycle result

When the analysis/report pipeline succeeds, the existing attempt is transitioned to `INCOMPLETE` with `analysisReportAvailable=true` and `interactivePreviewAvailable=false`. No new lifecycle status is introduced and preview generation remains out of scope.

## Tests

Focused REQ-032 tests cover:

- one canonical request starts one evaluation execution;
- required execution ordering;
- REQ-036 derivation occurs before classification;
- exact assessment propagation to the classifier;
- exact classifier state propagation to deterministic analysis;
- evaluation identity propagation;
- no invented coverage state when classification has not completed;
- mandatory downstream failure propagation;
- no downstream execution after mandatory failure;
- final evaluation-attributed execution result and lifecycle state.

## Validation

### Backend Validation — passed

Backend Validation run **#384** / ID **`37252772701`** passed against head `f25a83ae5b78b56ac32ee2dcbe31cf43fc5fff84`.

- Workflow: `Backend Validation`
- Job: `Java 21 / Maven tests`
- Command: `mvn --batch-mode --no-transfer-progress test`
- Result: success

The requirement was then updated with this validation record. Per the final-head rule, Backend Validation must pass again against the resulting final PR head before this PR is considered fully ready for orchestrator approval.

## Known Limitations

- The current acquisition integration exposes a single acquisition result/evidence item, so this orchestration slice coordinates the existing single-source execution boundary.
- Current acquisition-generated limitations are unscoped; REQ-032 deliberately does not invent scope for them.
- Interactive preview generation is not part of this requirement, so successful analysis/report execution ends in the existing `INCOMPLETE` lifecycle state.

## Requirement Status

`STATUS: PR_READY`

PR: #37

Base: `main`

Branch: `feature/evaluation-execution-orchestration`

Do not merge. Stop for orchestrator review.
