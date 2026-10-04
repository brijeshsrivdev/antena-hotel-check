# REQ-023 — Hospitality Analysis Limitation Foundation

**STATUS:** READY  
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

## Governance

This requirement is intentionally the smallest next slice after REQ-022 because SPEC-006 requires downstream analysis to distinguish observed conditions from evidence limitations. It must not be expanded into full coverage, severity, recommendations, reporting, or preview behavior.

Implementation must follow the established lifecycle:

`Requirement → inspect → implement → tests → CI → PR_READY → orchestrator review → merge → durable context update`.
