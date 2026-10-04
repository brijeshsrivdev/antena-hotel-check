# REQ-027 — Hospitality Deficiency Analysis Foundation

STATUS: PR_READY
REQUIREMENT_ID: REQ-027
TYPE: Implementation
BRANCH: `feature/hospitality-deficiency-analysis-foundation`

## P1 Correction Record

The booking deficiency rule that inferred a missing booking action from the absence of a `BOOKING` signal has been removed.

A successful room page plus no `BOOKING` signal now produces **no booking deficiency**. Missing BOOKING evidence is not proof that a booking action is absent, broken, or unusable.

A booking deficiency may only be emitted when an existing governed observation/signal/evidence contract explicitly establishes a broken or unusable booking path. The current upstream contracts do not provide that semantic, so REQ-027 makes no booking-deficiency claim.

No upstream observation layer was expanded or modified. No keyword-based booking detection, booking-success inference, new broken-path semantics, generic rule engine, or new observation category was introduced.

## Truth-Boundary Decisions

- Missing evidence ≠ hotel deficiency.
- Missing BOOKING signal ≠ missing booking capability.
- Acquisition/access failure ≠ booking failure.
- Unable to verify booking ≠ booking does not work.
- Unsupported dimensions remain unsupported.
- Only existing governed evidence may establish a broken/unusable booking path.

## Testing

The focused booking regression is:

```text
successful room evidence + no BOOKING signal → no booking deficiency
```

Existing REQ-027 coverage for room-information, contact/location, guest-information, identity conflicts, failed acquisition, unsupported acquisition, zero findings, and deterministic repeat execution remains in place.

## Limitations

The current evidence/observation contracts do not expose an explicit verified broken booking path or governed `NOT_ATTEMPTED` state. REQ-027 therefore does not invent either semantic and does not create a booking deficiency.

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
