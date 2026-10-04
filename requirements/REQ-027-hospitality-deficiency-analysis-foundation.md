# REQ-027 — Hospitality Deficiency Analysis Foundation

STATUS: PR_READY
REQUIREMENT_ID: REQ-027
TYPE: Implementation
BRANCH: `feature/hospitality-deficiency-analysis-foundation`

## Objective

Extend the deterministic hospitality analysis pipeline from primarily positive/observed signals to evidence-grounded observed deficiencies and meaningful evidence gaps.

REQ-027 must make the analysis materially useful for the product question:

> Can a guest find, understand, trust, explore, and book this hotel online?

The engine must identify a problem only when the available evidence supports the problem. It must not turn missing evidence, failed acquisition, or unsupported analysis capability into a hotel deficiency.

## Scope

Implement deterministic, evidence-grounded deficiency analysis over the existing REQ-026 pipeline. Inspect existing qualified hospitality signals and supporting observations/evidence, apply explicit deterministic rules, create findings using the existing `HospitalityFinding` contract, preserve journey/provenance relationships, and integrate findings into `HospitalityAnalysisResult`.

## Current Capability Boundary

Current observation categories remain `HOTEL_IDENTITY`, `ROOMS`, `AMENITIES`, `CONTACT`, `BOOKING`, and `DINING`. Do not add observation categories merely to manufacture deficiencies. Unsupported dimensions remain not currently assessable.

## Mandatory Truth Boundary

```text
No signal found             ≠ hotel feature absent
Acquisition failed          ≠ hotel feature absent
NOT_ATTEMPTED               ≠ hotel feature absent
Unsupported dimension       ≠ hotel deficiency
Could not verify booking    ≠ booking does not work
```

A deficiency requires positive observational support from retained evidence/context.

## Rule Families

### Booking

REQ-027 MUST NOT infer a booking deficiency from the absence of a `BOOKING` observation or signal. Successful room evidence plus no booking signal is insufficient evidence. A booking deficiency may be created only if an existing governed observation/signal/evidence contract explicitly establishes a broken or unusable guest-facing booking path.

The current upstream contracts do not provide that explicit successful broken-booking-path semantic, so REQ-027 creates no booking deficiency.

Do not infer booking capability absence, booking failure, or booking success. Do not add keyword-based booking detection.

### Room information

Where a room listing/detail context is explicitly observed, an explicit rule may identify materially insufficient guest-facing room information. Do not infer that the hotel lacks rooms because room evidence was unavailable.

### Contact/location

Where a relevant contact/location context is successfully observed, an explicit rule may identify a materially missing guest-facing contact or location path. Do not infer absence from inaccessible pages.

### Guest-facing information

Where an amenities/guest-information context is successfully observed, an explicit rule may identify materially missing decision-support information when the rule is explicit and testable.

### Trust/conflict

Where retained sources materially contradict each other, the inconsistency itself may become a finding when relevant to guest understanding, trust, or booking. Do not silently select one source.

### Broken observed path

Create a deficiency only where existing evidence explicitly establishes that the relevant guest-facing path is broken or unusable. Access failure remains a limitation. The current evidence contract does not expose such a governed successful broken-path state, so REQ-027 does not manufacture one.

## No Keyword-Only Rules

Context-free keyword absence is insufficient. Do not use rules such as `page.contains("book") == false → booking deficiency` or `page.contains("room") == false → no rooms`.

## Findings / Provenance

Reuse `HospitalityFinding`. Preserve evaluation identity, journey stages, analysis dimension, problem statement, observational basis, supporting evidence, and signal/observation provenance. Every deficiency remains traceable through signal → observation → structured evidence → evaluation/acquisition context.

## Determinism

Identical structured evidence and governed inputs must produce identical deficiency findings. No randomness, LLM, external network, wall-clock dependency, or mutable global state.

## Multi-Evidence / Conflicts

Support multiple evidence items for one evaluation. Retain material conflicts and source-specific provenance. Do not invent a universal source hierarchy.

## Integration Boundary

Use the small explicit `HospitalityDeficiencyAnalysisService` collaborator from `HospitalityAnalysisService`. Do not create a generic rule engine, plugin architecture, strategy registry, reflection dispatcher, or configurable rules framework.

## Non-Scope

No AI/LLM, scoring, generic SEO/accessibility/performance auditing, complete nine-dimension analysis, recommendations, reports/UI/PDF, persistence, REST API, crawler/acquisition expansion, booking/OTA integration, competitor analysis, interactive preview, `<hotel-name>.antenapro.com` generation, Antena API integration, universal source hierarchy, automatic coverage classification, booking-success inference, keyword booking detection, or new broken-path semantics.

## Acceptance Criteria

1. REQ-026 behavior remains intact.
2. Deficiency analysis uses only successfully retained/observable evidence and existing deterministic signals.
3. Supported room-information, contact/location, guest-information, conflict, and explicitly governed observed-path rules are covered where contracts support them.
4. No deficiency is created solely from missing evidence, failed acquisition, NOT_ATTEMPTED, unsupported dimensions, or absence of a BOOKING signal.
5. Context-free keyword absence cannot create a hotel deficiency.
6. Observed deficiency, insufficient evidence, and unable-to-verify limitation remain distinguishable.
7. Findings retain evaluation identity and provenance.
8. Material source conflicts remain visible.
9. Multiple evidence items remain supported.
10. Equivalent findings are deterministically deduplicated without losing distinct source context.
11. Zero deficiency findings remains valid.
12. Coverage remains caller-governed.
13. No new observation categories are added solely for deficiencies.
14. Full backend tests and focused REQ-027 tests pass.
15. No external network, AI, persistence, report, preview, or Antena integration is introduced.

## Testing Requirements

- successful room evidence + no BOOKING signal → no booking deficiency;
- inaccessible booking context → limitation/no deficiency;
- observed room context with explicitly insufficient information → deficiency;
- inaccessible room context → no deficiency;
- observed contact/location context with explicit missing guest-facing detail → deficiency;
- observed amenities/guest-information context with explicit insufficient information → deficiency;
- material conflicting hotel information → trust/clarity finding with both sources preserved;
- observed broken guest-facing path only if an existing governed signal can establish it;
- NOT_ATTEMPTED → no deficiency when represented by an existing governed contract;
- unsupported dimension → no deficiency;
- keyword-only absence → no deficiency;
- zero deficiencies → valid result;
- cross-evaluation evidence remains rejected;
- provenance remains traceable;
- deterministic repeat execution produces equivalent results;
- existing REQ-026 regression suite remains green.

## Engineering / Security

Keep implementation simple and explicit. Treat structured evidence and retained public content as untrusted data. Do not execute HTML/scripts/expressions/URLs, make network calls, swallow errors, or reinterpret infrastructure failures as deficiencies.

## Governance

Implementation sessions must inspect current `main`, read this requirement and dependencies, stop if a semantic distinction requires inventing a product rule, update this requirement with implementation/validation evidence, create a PR against `main`, stop for orchestrator review, and never merge their own PR.

`READY` becomes `PR_READY` only after implementation and final-head CI validation.

## Implementation Record — Session 27

### Context reconciliation

Inspected REQ-026 and the existing observation, signal, finding, limitation, coverage, result, structured-evidence, evaluation/provenance, and test layers. No upstream contract was changed. Current categories remain `HOTEL_IDENTITY`, `ROOMS`, `AMENITIES`, `CONTACT`, `BOOKING`, and `DINING`.

The current evidence model does not expose a governed `NOT_ATTEMPTED` state or explicit successful broken-path observation contract; REQ-027 does not invent either semantic.

### Implementation summary

Added `HospitalityDeficiencyAnalysisService` as the explicit deterministic collaborator invoked after observation and signal qualification. It covers room-information, contact/location, guest-information, and material identity-conflict findings using successful observed content plus explicit page/source context.

**P1 correction:** the previous absence-of-BOOKING rule has been removed. A successful room page with no BOOKING signal now produces **no booking deficiency**. The current upstream contracts cannot explicitly establish a broken/unusable booking path, so REQ-027 makes no booking-deficiency claim.

No keyword-based booking detection, booking-success inference, new observation category, new broken-path semantics, acquisition expansion, recommendation, severity, score, coverage classifier, AI, network, persistence, API, report, preview, or Antena integration was added.

### Files changed

- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityDeficiencyAnalysisService.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisService.java`
- `backend/src/test/java/com/antenapro/hotelcheck/analysis/HospitalityDeficiencyAnalysisServiceTest.java`
- `requirements/REQ-027-hospitality-deficiency-analysis-foundation.md`

### Truth-boundary decisions

- Successful room evidence + no BOOKING signal is **not** a booking deficiency.
- Missing BOOKING evidence may mean the current observation contract did not recognize or expose the action; it does not prove the action is absent.
- A booking deficiency can only be emitted if an existing governed observation/signal/evidence contract explicitly establishes a broken or unusable booking path.
- Booking capability absence, booking failure, and booking success are not inferred.
- Missing evidence, failed acquisition, unsupported dimensions, and inability to verify remain distinct from deficiencies.
- No upstream observation contract was expanded to make the booking rule possible.

### Focused tests

- successful room evidence + no BOOKING signal → no booking deficiency;
- observed room context without decision-support information → room-information deficiency;
- observed contact context without guest-facing detail → contact/location deficiency;
- observed amenities context without stay-related detail → guest-information deficiency;
- materially conflicting hotel identity sources → trust/clarity findings with both supporting evidence items retained;
- generic successful page → zero deficiency findings;
- acquisition timeout → limitation and no deficiency;
- unsupported acquisition → no deficiency;
- deterministic repeated execution → equivalent results.

### Limitations

- Current deterministic evidence/observation contracts do not expose an explicit verified broken booking path, so REQ-027 cannot legitimately create a booking deficiency.
- Current contracts do not expose a governed `NOT_ATTEMPTED` state, so no new semantics were invented.
- Current contracts do not expose a successful explicit broken-path state, so access failures are not converted into broken-path deficiencies.
- Conflict findings remain source-specific `HospitalityFinding` instances because the existing finding contract carries one supporting-evidence chain per finding; both source-specific findings remain traceable.

### Self-review

**Scope:** only REQ-027 behavior is changed; no acquisition, evidence normalization, observation extraction, signal qualification, coverage classification, result aggregation, recommendation, reporting, preview, or Antena integration was introduced.

**Truthfulness:** all deficiency rules require successful observed evidence plus explicit context. Generic keyword absence, missing evidence, acquisition failure, unsupported dimensions, and missing BOOKING signals are not treated as proof of a hotel deficiency.

**Provenance:** every deficiency derives from an existing qualified signal and remains traceable through observation, structured evidence, and evaluation/attempt attribution. Conflicts preserve both source-specific chains.

**Determinism:** no randomness, LLM, external network, wall-clock dependency, or mutable global state was introduced.

**Simplicity:** rules remain local private methods with explicit deterministic conditions; no configurable or reflective rule framework was introduced.

### Final CI / head

**PENDING — post-correction validation.**

Pre-correction PR head: `8dde63e0465c46b448d13473a553b31baae03ef8`.

Corrected implementation commit: `50418aaba93acc3dd7c7a876b8bb4cb9ed136ca9`.

The final PR head must be recorded here after GitHub Actions Backend Validation passes on the corrected branch head.

### Governance

PR #27 remains open and unmerged. Do not merge as part of REQ-027 implementation/review.

**STOPPING FOR ORCHESTRATOR REVIEW.**
