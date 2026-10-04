# REQ-023 — Hospitality Analysis Limitation Foundation

**STATUS:** PR_READY  
**REQUIREMENT_ID:** REQ-023  
**TYPE:** Implementation  
**BRANCH:** `feature/hospitality-analysis-limitation-foundation`

## Objective

Establish the smallest deterministic boundary for representing meaningful analysis limitations from existing evidence/acquisition conditions so downstream hospitality findings can distinguish **unable to verify** from an observed deficiency.

REQ-022 established the finding boundary but intentionally did not fabricate `LIMITATION`, `INSUFFICIENT_EVIDENCE`, or `NOT_APPLICABLE` states because REQ-021 currently produces only successful, content-backed qualified signals. REQ-023 introduces only the missing limitation signal needed to make the existing SPEC-006 truthfulness contract operational.

The current focus remains trustworthy digital-presence analysis. Antena-hosted hotel experience integration remains downstream and is not part of this requirement.

## Scope

Consume only already-produced acquisition/evidence lifecycle information. Produce a small immutable deterministic analysis-limitation representation that can be consumed by the existing finding boundary later.

The limitation must preserve:

- relevant hospitality dimension/category where deterministically known;
- relevant guest-journey stage(s) where deterministically known;
- limitation reason/type;
- supporting acquisition/evidence traceability;
- evaluation/attempt attribution;
- provenance/context needed to explain why the condition could not be verified.

## Truthfulness Boundary

A limitation means the analyzer could not reliably establish a condition.

Examples:

- booking engine could not be observed because access failed → limitation;
- source was unavailable or blocked → limitation;
- evidence was insufficient for a conclusion → insufficient evidence where justified;
- no evidence was attempted → must not become proof of absence;
- failed acquisition must not become a negative hotel capability finding.

REQ-023 must never create an observed deficiency merely because acquisition/evidence was unavailable.

## Acceptance

1. The boundary consumes existing repository acquisition/evidence state only; it does not perform network access.
2. Processing is deterministic.
3. Every limitation is traceable to the originating evaluation/attempt and supporting acquisition/evidence context.
4. Limitation semantics remain distinct from observed deficiency.
5. `NOT_ATTEMPTED` or missing evidence does not automatically become a limitation asserting a hotel problem.
6. Failed/unavailable acquisition can be represented as inability to verify where the existing contract supports that interpretation.
7. No unsupported hotel fact is produced.
8. No severity, score, recommendation, coverage, report, preview, or AI output is produced.
9. Existing REQ-018/019/021/022 semantics remain unchanged.
10. Deterministic tests cover supported limitation conditions and conservative negative cases.
11. Backend CI validation passes.

## Explicit Non-Scope

Do not implement:

- generic error taxonomy;
- crawler/browser behavior;
- new acquisition mechanisms;
- severity;
- scoring;
- recommendations;
- analysis coverage;
- report generation;
- persistence;
- public API;
- UI;
- AI/LLM;
- preview;
- Antena integration;
- booking or OTA integrations;
- generic SEO auditing;
- competitor analysis;
- NOT_APPLICABLE decision logic unless existing repository evidence already provides an explicit deterministic contract for it.

## Dependencies

- REQ-017 — Evaluation Acquisition Integration
- REQ-018 — Evidence Normalization Foundation
- REQ-019 — Hospitality Observation Foundation
- REQ-021 — Hospitality Analysis Signal Foundation
- REQ-022 — Hospitality Finding Foundation
- SPEC-005 — Evidence Model
- SPEC-006 — Hospitality Analysis Contract
- existing backend engineering/testing conventions

## Required Tests

At minimum:

- supported unavailable/failed evidence condition → limitation;
- limitation preserves evaluation/attempt attribution;
- limitation preserves source/acquisition traceability;
- limitation does not become an observed deficiency;
- `NOT_ATTEMPTED` does not become a negative hotel finding;
- missing evidence does not become proof of absence;
- successful evidence does not become limitation;
- deterministic repeatability;
- source objects are not mutated;
- null/invalid input follows existing repository conventions.

No live hotel website or external network dependency is permitted.

## Engineering Constraints

Prefer the smallest local immutable/value-oriented types and deterministic mapping needed for this requirement.

Do not create a generic `ErrorEngine`, `LimitationEngine`, `AnalysisEngine`, or framework abstraction unless repository evidence demonstrates that it is required.

Reuse existing acquisition/evidence status types where appropriate. Do not duplicate status vocabularies unnecessarily.

If the repository does not provide enough information to distinguish an inability to verify from a condition that must remain unresolved, STOP and report the ambiguity rather than inventing semantics.

## Implementation Record — Session 23

### Implementation summary

Implemented a small deterministic limitation boundary directly from `StructuredEvidence` to an immutable `HospitalityAnalysisLimitation`.

The boundary:

- accepts only existing normalized evidence and performs no acquisition or network access;
- represents only `UNABLE_TO_VERIFY` at this slice;
- preserves the exact originating `AcquisitionOutcome` as the source condition instead of creating a duplicate acquisition-status vocabulary;
- supports hospitality categories and guest-journey stages when the caller can deterministically associate them with the inaccessible source/flow;
- retains the complete `StructuredEvidence` by reference, preserving source observation, evaluation/attempt attribution, acquisition provenance, URLs, and limitation detail;
- supports only repository acquisition outcomes that unambiguously mean the requested source could not be reliably observed: `HTTP_ERROR`, `TIMEOUT`, `REDIRECT_LIMIT_EXCEEDED`, `RESPONSE_TOO_LARGE`, and `NETWORK_ERROR`;
- deliberately rejects `SUCCESS`, including successful evidence without retained content, so successful evidence is never converted into a limitation;
- deliberately rejects `INVALID_TARGET` and `UNSUPPORTED_SCHEME` because the current contracts do not establish those request-validation conditions as hotel-source inability-to-verify semantics;
- requires `DISCOVERED` source provenance so inferred/derived representations cannot manufacture a source limitation.

No finding behavior, acquisition behavior, evidence normalization behavior, signal behavior, scoring, severity, recommendation, coverage, report, persistence, API, UI, AI, or preview behavior was changed.

### Files changed

- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisLimitationType.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisLimitation.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisLimitationService.java`
- `backend/src/test/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisLimitationServiceTest.java`
- this requirement file

### Tests

Added deterministic unit coverage for:

- timeout → `UNABLE_TO_VERIFY` limitation;
- HTTP error → limitation with exact source condition;
- network failure → limitation;
- booking category / `BOOK` journey context preservation;
- evaluation/attempt attribution;
- supporting evidence and source-observation traceability;
- deterministic repeatability;
- successful evidence not becoming limitation;
- successful evidence with no retained content not becoming limitation;
- invalid target not being interpreted as inability to verify;
- unsupported scheme not being interpreted as inability to verify;
- non-`DISCOVERED` evidence not producing a limitation;
- source evidence immutability;
- null input handling.

No live hotel website or external network dependency is used by the tests.

### Validation

Local Maven validation is not available in the session environment because direct repository cloning cannot resolve `github.com`.

GitHub Actions Backend Validation run **#129** / run ID **`37176816747`** executed against final branch head **`b45662f4e9466f508652d8c9e2d68d655bf75d74`**:

- Workflow: **Backend Validation**
- Job: **Java 21 / Maven tests**
- Command: `mvn --batch-mode --no-transfer-progress test`
- Result: **BUILD SUCCESS**
- Tests: **117**
- Failures: **0**
- Errors: **0**
- REQ-023 tests: **11**, all passing

The final-head CI run is the authoritative repository-level validation for this requirement.

### CI

**PASS** — Backend Validation run #129 / run ID `37176816747`.

### PR

- **PR:** #23 — `REQ-023: Hospitality Analysis Limitation Foundation`
- **Base:** `main`
- **Head:** `feature/hospitality-analysis-limitation-foundation`
- **Validated head:** `b45662f4e9466f508652d8c9e2d68d655bf75d74`
- **Status:** OPEN
- **Merged:** No

### Limitations / open questions

- The current acquisition/evidence contracts do not contain a `NOT_ATTEMPTED` acquisition outcome. REQ-023 therefore does not invent one or map any existing condition to `NOT_ATTEMPTED`.
- The current acquisition contract has no separate `UNAVAILABLE` `AcquisitionOutcome`; `UNAVAILABLE` exists as an acquisition method. REQ-023 uses only explicit non-success outcomes that establish failure to reliably observe the requested source.
- Category/journey context is optional because the current `StructuredEvidence` model does not itself carry hospitality meaning. The limitation boundary preserves those fields only when a caller already knows them deterministically.
- `INSUFFICIENT_EVIDENCE` and `NOT_APPLICABLE` decision logic remain unimplemented because the current repository does not provide an explicit deterministic input contract for either state.

### Material architectural decision

The limitation retains `StructuredEvidence` by reference rather than copying the acquisition/evidence graph. This preserves traceability through `StructuredEvidence → EvaluationAcquisitionResult → EvaluationAttempt → AcquisitionResult` while avoiding duplicated mutable state. The exact `AcquisitionOutcome` is retained as the limitation source condition instead of introducing a parallel failure taxonomy.

### Self-review

#### Scope

Only REQ-023 was implemented. No acquisition, evaluation lifecycle, evidence normalization, hospitality observation, analysis signal, or finding behavior was modified.

#### Truthfulness

The limitation represents inability to verify the requested source/flow. It never asserts that a hotel lacks booking, rooms, room information, or another capability. Successful evidence and unsupported request-validation outcomes are rejected rather than over-interpreted.

#### NOT_ATTEMPTED / missing evidence

The repository does not provide a `NOT_ATTEMPTED` acquisition outcome, so no invented mapping was added. Successful evidence with no retained content is also rejected as a limitation rather than converted into a deficiency or inability-to-verify conclusion.

#### Traceability

Every created limitation retains the complete supporting `StructuredEvidence`, including source observation, acquisition outcome, URLs, provenance, evaluation ID, attempt ID/number, and limitation detail.

#### Architecture

No generic limitation engine, error framework, rule engine, factory, persistence model, or acquisition infrastructure was introduced.

#### Determinism

The mapping is a pure in-memory operation with no clock, randomness, external network, or AI dependency.

#### Security / production

The new boundary performs no network access, persistence, external service calls, or unbounded processing. It only consumes already-normalized evidence.

## Governance

`STATUS: PR_READY` — implementation complete, final-head Backend Validation passed, PR #23 is open against `main`, and the PR remains unmerged for orchestrator review.

**STOPPING FOR ORCHESTRATOR REVIEW.**
