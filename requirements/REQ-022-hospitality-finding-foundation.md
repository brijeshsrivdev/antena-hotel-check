# REQ-022 — Hospitality Finding Foundation

**STATUS:** READY  
**REQUIREMENT_ID:** REQ-022  
**TYPE:** Implementation  
**BRANCH:** `feature/hospitality-finding-foundation`

## Objective

Establish the smallest deterministic analysis layer that turns a qualified `HospitalityAnalysisSignal` into a traceable, truth-preserving hospitality finding.

REQ-021 established a qualified interpretation of bounded hospitality observations. REQ-022 introduces the next semantic boundary required by `SPEC-006`: a finding that can explicitly distinguish an observed/verified condition from evidence limitations without prematurely implementing severity, scoring, recommendations, coverage, reporting, or AI.

The current focus remains trustworthy digital-presence analysis. Antena-hosted hotel experience integration remains downstream and is not part of this requirement.

## Scope

Implement an in-memory, deterministic hospitality finding boundary consuming `HospitalityAnalysisSignal` only.

The finding must preserve:

- the originating analysis signal;
- the relevant hospitality dimension/category already represented by the signal;
- guest-journey stage(s) already represented by the signal;
- concise factual/guest-facing finding text;
- evidence traceability through the originating signal/observation;
- evaluation/attempt attribution;
- an explicit finding status that does not overstate what the evidence proves.

The implementation may define only the minimum finding states required by this slice and by the existing `SPEC-006` contract.

## Truthfulness Boundary

A finding must never claim more than the signal and supporting evidence establish.

Examples:

- A booking entry point observation may support a finding that a booking entry point was observed. It must not establish that booking succeeds.
- Room-related information may support a finding that room information was observed. It must not establish room availability.
- Missing, failed, unavailable, or insufficient evidence must not become an observed hotel deficiency.
- A limitation must remain distinguishable from an observed deficiency.
- An inferred or demonstration value must not silently become a verified discovered hotel fact.

The finding layer must preserve the distinction required by `SPEC-006` between:

```text
verified / observed
limitation / unable to verify
insufficient evidence
not applicable
```

Do not add a new status merely because it is convenient for implementation if the existing specification does not justify it.

## Acceptance

1. The finding service accepts `HospitalityAnalysisSignal` as its sole domain input.
2. No external network access is introduced.
3. Processing is deterministic for the same signal input.
4. Every finding remains traceable to its originating signal and therefore to supporting observation/evidence.
5. Evaluation and attempt attribution are preserved.
6. Finding status explicitly distinguishes verified/observed conditions from inability to verify or insufficient evidence where applicable.
7. An unavailable or failed evidence condition cannot be converted into an observed deficiency.
8. Missing evidence cannot be treated as proof of absence.
9. Booking entry-point signals cannot become booking-success findings.
10. Room-information signals cannot become room-availability findings.
11. Inferred or demonstration information cannot become verified discovered hotel information.
12. No severity or importance classification is produced.
13. No numerical or qualitative score is produced.
14. No recommendation is produced.
15. No analysis coverage/completeness result is produced.
16. No report or preview output is produced.
17. No AI/LLM dependency is introduced.
18. Source signals are not mutated.
19. Deterministic tests cover positive findings and conservative negative cases.
20. Existing REQ-019 observation semantics and REQ-021 signal semantics remain unchanged.
21. Backend CI validation passes.

## Explicit Non-Scope

Do not implement:

- severity calibration;
- scoring;
- ranking;
- recommendations;
- report generation;
- analysis completeness/coverage calculation;
- conflict resolution;
- freshness policy;
- persistence schema;
- public API;
- UI;
- AI/LLM analysis;
- interactive hotel preview;
- Antena integration;
- booking or OTA integrations;
- generic SEO auditing;
- competitor analysis.

## Dependencies

- REQ-018 — Evidence Normalization Foundation
- REQ-019 — Hospitality Observation Foundation
- REQ-021 — Hospitality Analysis Signal Foundation
- SPEC-005 — Evidence Model
- SPEC-006 — Hospitality Analysis Contract
- existing backend engineering/testing conventions

## Required Tests

At minimum, deterministic tests must cover:

- a supported signal producing a verified/observed finding;
- correct preservation of journey stage and traceability;
- evaluation/attempt attribution;
- limitation/insufficient-evidence semantics where represented by the supported input;
- booking entry-point truthfulness;
- room-information truthfulness;
- missing evidence not becoming a deficiency;
- failed/unavailable evidence not becoming a deficiency;
- inferred/demo information not becoming verified fact;
- source signal immutability;
- deterministic repeatability;
- null/invalid input handling consistent with existing repository conventions.

No live hotel website or external network dependency is permitted in tests.

## Engineering Constraints

Prefer the smallest local domain types and deterministic mapping needed to satisfy this requirement.

Do not create a generic `FindingEngine`, `AnalysisEngine`, `RuleEngine`, or framework abstraction unless repository evidence demonstrates that it is required.

Reuse existing types where appropriate. Keep the output in-memory and immutable/value-oriented where practical.

## Required Requirement Update

The implementation session must update this same requirement file with:

- implementation summary;
- files changed;
- tests performed;
- CI result;
- PR number/status;
- limitations;
- self-review;
- any material architectural decision discovered during implementation.

Only set `STATUS: PR_READY` after actual validation and CI evidence.

## Governance

This requirement is intentionally the smallest next semantic slice after REQ-021. It must not be expanded into the complete `SPEC-006` findings/recommendations/scoring/report contract.

If repository evidence shows that any acceptance criterion cannot be implemented without architectural guessing, the implementation session must STOP and report the ambiguity rather than inventing a contract.
