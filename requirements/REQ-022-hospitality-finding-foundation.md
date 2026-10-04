# REQ-022 — Hospitality Finding Foundation

**STATUS:** PR_READY  
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

## Implementation Record — Session 22

### Implementation summary

Implemented the smallest deterministic in-memory boundary from `HospitalityAnalysisSignal` to `HospitalityFinding`.

The finding retains the originating signal by reference. Journey stages, hospitality category, finding text, supporting observation/evidence, evaluation identity, and attempt attribution are exposed from that retained signal chain rather than duplicating the complete evidence object.

The finding service emits `VERIFIED_OBSERVED` only when the supplied signal is `QUALIFIED` and its originating observation remains `DISCOVERED` and backed by successful retained evidence content. A malformed/manually-constructed signal whose nested provenance/evidence cannot support a verified observation is rejected as a finding rather than strengthened into a hotel deficiency or verified fact.

`HospitalityFindingStatus` uses only the four status concepts explicitly established by `SPEC-006`: `VERIFIED_OBSERVED`, `LIMITATION`, `INSUFFICIENT_EVIDENCE`, and `NOT_APPLICABLE`. REQ-021 currently produces only successful, discovered, content-backed qualified signals, so the latter three states are not synthesized from unsupported input in this slice. This preserves the status vocabulary without inventing limitation/absence semantics that the current signal contract cannot legitimately provide.

No severity, score, recommendation, coverage, report, persistence, API, UI, AI, acquisition, observation, or signal behavior was added or changed.

### Files changed

- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityFindingStatus.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityFinding.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityFindingService.java`
- `backend/src/test/java/com/antenapro/hotelcheck/analysis/HospitalityFindingServiceTest.java`
- this requirement file

### Tests

Added deterministic unit coverage for:

1. supported signal → `VERIFIED_OBSERVED` finding;
2. journey-stage preservation;
3. hospitality category preservation;
4. originating signal/observation/evidence traceability;
5. evaluation ID and attempt ID/number attribution;
6. booking entry-point truthfulness without booking-success inference;
7. room-information truthfulness without availability/inventory inference;
8. failed evidence not becoming a verified finding or deficiency;
9. inferred provenance not becoming a verified finding;
10. demonstration provenance not becoming a verified finding;
11. source signal/observation immutability;
12. deterministic repeatability;
13. exact four-state finding vocabulary from `SPEC-006`;
14. null input handling consistent with existing service conventions.

No live hotel website or external network dependency is used by the tests.

### Validation

Local Maven validation was not available in the session environment because repository cloning could not resolve `github.com`. The repository's GitHub Actions validation was used as the authoritative executed validation.

GitHub Actions **Backend Validation** run **#116** (run ID `37175424942`) executed:

`mvn --batch-mode --no-transfer-progress test`

Result:

- **BUILD SUCCESS**
- **106 tests**
- **0 failures**
- **0 errors**
- `HospitalityFindingServiceTest`: **11 tests**, all passing
- `HospitalityAnalysisSignalServiceTest`: **10 tests**, all passing
- `HospitalityObservationServiceTest`: **8 tests**, all passing

The run completed successfully. Existing non-blocking Java/Actions deprecation warnings were unrelated to REQ-022.

### CI

**PASS** — Backend Validation run #116 / run ID `37175424942`.

### PR

- **PR:** #22 — `REQ-022: Hospitality Finding Foundation`
- **Base:** `main`
- **Head:** `feature/hospitality-finding-foundation`
- **Validated head:** `ad0eccb5a3be430fe0c0239d0bf6956d7a760afe`
- **Status:** OPEN
- **Merged:** No

### Limitations / open questions

- REQ-021's current input contract can only produce qualified signals from successful `DISCOVERED` evidence with retained content. Therefore this slice does not manufacture `LIMITATION`, `INSUFFICIENT_EVIDENCE`, or `NOT_APPLICABLE` findings from unsupported signal states.
- Limitation/insufficient-evidence finding generation for actual analysis inputs requires a later governed signal/input contract that can represent those conditions without ambiguity.
- Finding persistence, aggregation, conflict handling, freshness, severity/importance, scoring, recommendations, coverage, reporting, and preview consumption remain later requirements.

### Material architectural decision

The finding retains the `HospitalityAnalysisSignal` rather than copying the full evidence graph. This keeps traceability intact through `signal → observation → structured evidence` while preventing duplicate evidence state and preserving source immutability. Finding-level accessors expose the hospitality category, journey stages, evidence, and evaluation/attempt attribution from that retained source chain.

### Self-review

#### Scope

Only REQ-022 was implemented. No acquisition, evaluation, evidence-normalization, observation, or signal behavior was modified.

#### Truthfulness

The service never upgrades booking entry points to booking success, room information to availability, failed/unavailable evidence to deficiency, or inferred/demonstration provenance to verified fact.

#### Status

Only status concepts defined by `SPEC-006` were introduced. The current REQ-021 signal contract does not support legitimate limitation/insufficient/not-applicable inputs, so those states are represented in the vocabulary but not fabricated by this boundary.

#### Traceability

The finding retains the originating signal by reference, preserving the complete existing chain to observation, structured evidence, provenance, evaluation, and attempt attribution.

#### Architecture

No generic finding engine, rule engine, framework, persistence model, or speculative abstraction was introduced.

#### Determinism

The mapping is a pure deterministic validation/wrapping operation with no network, clock, randomness, or AI dependency.

#### Tests

Focused REQ-022 tests and the complete backend Maven suite passed in GitHub Actions with zero failures/errors. Tests contain no live hotel website or external-network dependency.

#### Security / production

The new boundary performs no network access, persistence, external service calls, or unbounded processing. It operates only on the supplied in-memory signal graph.

## Governance

`STATUS: PR_READY` — implementation is complete, requirement documentation is updated with validation and PR evidence, Backend Validation run #116 passed, and PR #22 remains open and unmerged for orchestrator review.

**STOPPING FOR ORCHESTRATOR REVIEW.**
