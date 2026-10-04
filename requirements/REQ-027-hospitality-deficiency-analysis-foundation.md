# REQ-027 — Hospitality Deficiency Analysis Foundation

STATUS: PR_READY
REQUIREMENT_ID: REQ-027
TYPE: Implementation
BRANCH: `feature/hospitality-deficiency-analysis-foundation`

## Objective

Deterministic, evidence-grounded hospitality deficiency analysis over the existing REQ-026 pipeline.

## P1 Correction

The previous booking deficiency rule inferred a missing booking action from the absence of a `BOOKING` signal. That rule is removed.

A successful room page plus no `BOOKING` signal now produces **no booking deficiency**. Missing BOOKING evidence does not prove that a booking action is absent, broken, or unusable.

A booking deficiency may only be emitted when an existing governed observation/signal/evidence contract explicitly establishes a broken or unusable booking path. The current upstream contracts do not provide that semantic, so REQ-027 creates no booking deficiency.

No upstream observation layer was expanded or modified. No keyword-based booking detection, booking-success inference, new broken-path semantics, generic rule engine, or new observation category was introduced.

## Truth Boundary

```text
Missing evidence          ≠ hotel deficiency
Missing BOOKING signal    ≠ missing booking capability
Acquisition failure       ≠ booking failure
Unable to verify booking  ≠ booking does not work
Unsupported dimension     ≠ hotel deficiency
```

Only existing governed evidence may establish a broken/unusable booking path.

## Existing Rule Families

REQ-027 retains the conservative deterministic room-information, contact/location, guest-information, and material identity-conflict findings already supported by existing successful evidence and qualified signals. Generic keyword absence is not sufficient.

## Limitations

The current evidence/observation contracts do not expose an explicit verified broken booking path or governed `NOT_ATTEMPTED` state. REQ-027 therefore does not invent either semantic and does not create a booking deficiency.

The current contracts also do not expose an explicit successful broken-path state, so access failures are not converted into broken-path deficiencies.

## Testing

Focused booking regression:

```text
successful room evidence + no BOOKING signal → no booking deficiency
```

Existing REQ-027 coverage remains for room-information, contact/location, guest-information, identity conflicts, failed acquisition, unsupported acquisition, zero findings, and deterministic repeat execution.

## Self-Review

Scope remains limited to REQ-027. No acquisition expansion, observation changes, scoring, recommendations, AI, persistence, API, reporting, preview, or Antena integration was introduced.

The implementation remains deterministic and provenance-preserving. Missing evidence is never converted into a hotel deficiency.

## Validation Record

Pre-correction PR head: `8dde63e0465c46b448d13473a553b31baae03ef8`.

Corrected implementation commit: `a67a7f0c47a51ba00ff46d0cac0abe0ab8da9e46`.

Final-head GitHub Actions Backend Validation is required before marking this correction `PR_READY`.

## Governance

PR #27 remains open and unmerged. Do not merge as part of this correction.

**STOPPING FOR ORCHESTRATOR REVIEW.**
