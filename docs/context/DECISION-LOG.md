# Decision Log

This is a compact record of durable decisions that a future orchestrator should not accidentally reverse. It is not a complete project history.

## Decision 001 — Hospitality-first product boundary

**Status:** Active

**Decision:** Antena Hotel Check is a hospitality-specific digital experience analyzer. Generic SEO, performance, accessibility, and technical health are supporting concerns, not the product center.

**Rationale:** The product question is whether a guest can find, understand, trust, explore, and book a hotel online.

**Source:** Project product direction and repository specifications/context.

## Decision 002 — Bounded public-web acquisition

**Status:** Active

**Decision:** Public-web acquisition is bounded and service-owned. The product must not depend on aggressive scraping or bypassing third-party protections.

**Rationale:** Acquisition must be predictable, secure, and respectful of public-web boundaries while providing evidence for evaluation.

**Source:** Public-web acquisition requirements/specifications and ADR/architecture records in the repository.

## Decision 003 — Acquisition and evidence are separate boundaries

**Status:** Active

**Decision:** Acquisition produces typed acquisition results; downstream normalization produces structured evidence. Acquisition failures must not silently become hospitality-feature absence.

**Rationale:** Separating acquisition from interpretation preserves lifecycle semantics, provenance, and truthful failure handling.

**Source:** REQ-017 and REQ-018 plus the repository evidence model.

## Decision 004 — Evidence is not automatically a hotel fact

**Status:** Active

**Decision:** Downstream representations must remain traceable to source evidence and must distinguish observed signals from inference, demonstration, and unavailable information.

**Rationale:** A public page signal such as a booking CTA is not proof that booking succeeds; absence of an observation is not proof of absence.

**Source:** Evidence-model requirements and REQ-018/REQ-019 contracts.

## Decision 005 — Hospitality observations are deterministic first

**Status:** Active

**Decision:** The initial hospitality observation layer is deterministic and bounded. AI/LLM interpretation is not a foundational dependency at this stage.

**Rationale:** Deterministic behavior is easier to test, trace, constrain, and review while the domain boundaries are being established.

**Source:** REQ-019 and current architecture.

## Decision 006 — Requirements govern implementation sessions

**Status:** Active

**Decision:** A requirement is created on `main` before implementation. Sessions work only within that requirement, update the same requirement with completion evidence, create a PR, and stop for orchestrator review. Only the orchestrator approves a requirement.

**Rationale:** This keeps scope explicit and makes the project recoverable across sessions.

**Source:** `requirements/README.md` and development lifecycle documentation.

## Decision 007 — Repository is durable truth

**Status:** Active

**Decision:** Chat/session memory is temporary working context. Repository state, requirements, specifications, architecture/decision records, tests, and merged code are authoritative.

**Rationale:** A long-running project must remain recoverable if an orchestrator or implementation session ends or changes.

**Source:** Orchestrator durable-context foundation.

## Decision 008 — Antena is the downstream product integration

**Status:** Active

**Decision:** Antena is the eventual downstream destination for Hotel Check conversion opportunities. Once digital-presence analysis is mature enough, Hotel Check may provide an Antena-hosted hotel experience at `<hotel-name>.antenapro.com` to address problems identified by the analysis.

**Rationale:** The analysis must first become trustworthy and useful. Premature integration would couple the early analysis foundation to downstream site generation and could distort scope.

**Source:** Product direction maintained by the orchestrator.

## Decision 009 — Analysis maturity precedes Antena integration

**Status:** Active

**Decision:** Current development focuses on digital-presence analysis only. Antena-hosted hotel experience integration is intentionally downstream and must not be treated as implemented, required in early analysis slices, or used to justify premature architecture.

**Rationale:** Establish a reliable analysis product before building the downstream solution that addresses the identified problems.

**Source:** Product sequencing decision maintained by the orchestrator.

## Decision 010 — Deficiency claims require observed support

**Status:** Active

**Decision:** Hospitality analysis may report an observed deficiency only when retained evidence and an explicit deterministic rule establish the guest-facing problem. Missing evidence, acquisition failure, `NOT_ATTEMPTED`, unsupported dimensions, and context-free keyword absence are not deficiencies.

**Rationale:** The product must distinguish an observed problem from inability to verify.

**Source:** SPEC-006 analysis semantics and REQ-027.

## Decision 011 — Coverage classification must be governed separately from coverage representation

**Status:** Active

**Decision:** REQ-024 represents an explicitly governed coverage state but does not classify it. An end-to-end orchestrator must not invent a default coverage state or derive one from page counts, finding counts, arbitrary percentages, or other ungoverned heuristics.

A dedicated coverage-classification contract must define and calibrate the semantics before orchestration can supply `HospitalityAnalysisCoverageState` to deterministic analysis.

**Rationale:** Session 32 exposed a real contract gap: `HospitalityAnalysisService` requires a coverage state, while the canonical evaluation request does not provide one and REQ-024 intentionally leaves classification unspecified. Hiding this decision inside REQ-032 would create an accidental product rule at the wrong architectural boundary.

**Source:** REQ-024, REQ-033, and Session 32 repository reconciliation.

## Decision 012 — Pre-classification coverage assessment is a separate boundary

**Status:** Active

**Decision:** Introduce a governed `HospitalityCoverageAssessment` contract between upstream evidence-derived facts and coverage classification.

The intended relationship is:

```text
HospitalityCoverageAssessment
        ↓
HospitalityAnalysisCoverageClassifier
        ↓
HospitalityAnalysisCoverageState
        ↓
HospitalityAnalysisService
        ↓
HospitalityAnalysisCoverage
```

The assessment represents one evaluation's intended, assessable, and limited journey/dimension scope. Covered scope is assessable ∪ limited scope. The assessment does not contain the final classification state and does not replace `HospitalityAnalysisCoverage`.

**Rationale:** The current runtime contracts formed a circular dependency: analysis required a state before creating final coverage, while the classifier required final coverage to produce that state. A pre-classification fact boundary removed the cycle without moving product calibration into orchestration.

**Source:** Session 35 reconciliation and REQ-035.

## Decision 013 — Coverage assessment derivation is a separate governed boundary

**Status:** Active

**Decision:** The transformation from existing governed hospitality observations/signals into `HospitalityCoverageAssessment` is owned by REQ-036. The derivation reuses the existing six observation categories, five journey stages, nine dimensions, current signal-to-journey mappings, current category-to-dimension mappings, and typed identity-conflict semantics. Explicit limitation category/journey context may establish limited scope; unscoped acquisition failures must not receive guessed scope.

**Rationale:** REQ-035 solved the representation/circular-dependency problem but deliberately did not define how assessable/limited sets are populated. REQ-036 made this mapping explicit before orchestration.

**Source:** Session 36 repository reconciliation and REQ-036.

## Decision 014 — Evaluation execution orchestration is a thin synchronous boundary

**Status:** Active

**Decision:** REQ-032 uses one synchronous `EvaluationExecutionOrchestrator` entry point accepting the existing `CanonicalEvaluationRequest`. It coordinates the existing service contracts in the fixed order acquisition → evidence → observations/signals → REQ-036 derivation → classifier → deterministic analysis → journey → recommendations → report. It does not introduce a workflow engine, queue, scheduler, retry system, persistence boundary, or new domain semantics.

The existing acquisition integration owns creation of the evaluation/attempt identity. The orchestrator propagates that identity rather than creating a second one.

**Rationale:** The product needs a readable execution boundary now that all governed analysis stages exist. Keeping orchestration thin preserves the semantic ownership established by REQ-019 through REQ-036 and makes ordering/identity/failure behavior directly testable.

**Source:** REQ-032 and Session 38 repository reconciliation.
