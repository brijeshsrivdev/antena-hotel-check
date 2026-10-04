# REQ-020 — Orchestrator Durable Context Foundation

**STATUS:** PR_READY  
**REQUIREMENT_ID:** REQ-020  
**TYPE:** Project/Process Foundation  
**BRANCH:** `docs/orchestrator-durable-context-foundation`  
**PR:** Pending creation

## Objective

Establish a small, durable repository-based context and handoff system so that a new orchestrator can continue the project without depending on the current ChatGPT session or accumulated conversation context.

The repository must remain the durable source of truth. Chat context is temporary working context only.

This requirement is intentionally a documentation/workflow foundation. It must not implement Hotel Check product functionality.

## Implementation

Created:

- `docs/context/ORCHESTRATOR-CONTEXT.md`
- `docs/context/CURRENT-STATE.md`
- `docs/context/DECISION-LOG.md`
- `docs/context/NEXT-STEPS.md`
- `docs/workflows/ORCHESTRATOR-HANDOFF.md`

The documents establish:

- compact stable orchestrator orientation;
- factual current-state tracking;
- durable decision capture without invented history;
- immediate next-step guidance;
- explicit orchestrator takeover procedure;
- ownership rules for durable context;
- repository-over-chat source-of-truth authority;
- separation between durable context and detailed requirements/specifications/architecture.

No Hotel Check application behavior was changed.

## Validation

- Created the implementation branch from the current `main`.
- Inspected the requirements lifecycle before implementation.
- Verified the context content against repository evidence available on `main`.
- Kept the context documents concise and complementary rather than duplicating specifications.
- Explicitly avoided describing future product work as implemented.
- No application test suite was required for this documentation/workflow-only change.
- CI/documentation validation will be reported from the PR once available.

## Ownership

The orchestrator owns project-level `CURRENT-STATE`, `NEXT-STEPS`, and `DECISION-LOG` maintenance and keeps the durable context aligned with repository reality. Implementation sessions update their requirement completion records and directly relevant implementation documentation only. Agents should not rewrite all durable context after every task.

## Source of Truth

Repository state, requirements, specifications, architecture, and tests are authoritative. Chat/session memory is temporary working context. When they conflict, repository evidence wins.

## Limitations

This foundation does not provide automatic context synchronization, automatic semantic validation of every context statement, external agent memory, or replacement of specifications/ADRs. Those remain future considerations only if a concrete need emerges.

## Requirement Status

`PR_READY`

STOPPING FOR ORCHESTRATOR REVIEW.
