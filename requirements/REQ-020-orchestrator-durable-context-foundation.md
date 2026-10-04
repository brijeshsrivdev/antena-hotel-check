# REQ-020 — Orchestrator Durable Context Foundation

**STATUS:** READY  
**REQUIREMENT_ID:** REQ-020  
**TYPE:** Project/Process Foundation  
**BRANCH:** `docs/orchestrator-durable-context-foundation`

## Objective

Establish a small, durable repository-based context and handoff system so that a new orchestrator can continue the project without depending on the current ChatGPT session or accumulated conversation context.

The repository must remain the durable source of truth. Chat context is temporary working context only.

This requirement is intentionally a documentation/workflow foundation. It must not implement Hotel Check product functionality.

## READ

The implementation session MUST read:

- `requirements/README.md`
- `docs/workflows/development-lifecycle.md`
- `docs/engineering/AGENT-GUIDELINES.md`
- `docs/engineering/ENGINEERING-PRINCIPLES.md`
- `docs/engineering/TESTING-STANDARDS.md`
- existing `docs/context/` contents, if present
- relevant existing requirements and specifications needed to seed current durable state

The session must inspect the actual current `main` state before writing context.

## INSPECT

Determine and document, using repository evidence:

1. The stable product/orchestration principles a future orchestrator must know.
2. The actual current implementation state on `main`.
3. The latest merged requirement and immediate next governed step.
4. Important architectural/product decisions that must not be rediscovered or contradicted.
5. Existing workflows that a new orchestrator must follow.
6. Any existing context documentation that should be reused rather than duplicated.

Do not infer implementation status from chat memory.

## DEPENDENCIES

- `requirements/README.md`
- `docs/workflows/development-lifecycle.md`
- `docs/engineering/AGENT-GUIDELINES.md`
- existing requirements/specifications/architecture documents
- current `main` repository state

## REQUIRED DURABLE CONTEXT

Create or establish the following small set of repository documents under `docs/context/`:

### `ORCHESTRATOR-CONTEXT.md`

Stable project-level orchestration knowledge, including:

- product objective and boundaries
- specification-driven/TDD lifecycle
- orchestrator responsibilities
- implementation-session responsibilities
- review/merge authority
- truthfulness and no-hallucination principles
- hospitality-first product boundary
- relationship between requirements, specifications, architecture, code, tests and PRs
- GSD/agent-engineering role where already established

This file must remain concise and must not duplicate detailed specifications.

### `CURRENT-STATE.md`

The current factual project state, including:

- current `main` commit at the time of implementation
- completed/merged requirements relevant to the current product path
- current implementation boundary
- latest merged PR
- immediate next governed step
- known limitations/open questions
- explicitly unimplemented/planned areas when needed to prevent false assumptions

This file represents current repository reality, not historical conversation state.

### `DECISION-LOG.md`

A concise durable record of important decisions that future agents/orchestrators must not accidentally reverse. Each decision should contain, at minimum:

- decision
- rationale
- status
- date or originating requirement/ADR when available

Only decisions supported by repository evidence may be added. Do not manufacture historical decisions.

### `NEXT-STEPS.md`

A short continuation guide containing:

- current position in the governed lifecycle
- immediate next action
- why that action is next
- explicit non-actions / things not to implement yet
- expected review/merge sequence

It must be easy to replace/update as the project advances.

### `docs/workflows/ORCHESTRATOR-HANDOFF.md`

A takeover procedure for a new orchestrator. It must require, at minimum:

1. Read the durable context files.
2. Inspect `main`.
3. Inspect the latest requirements and their statuses.
4. Inspect the latest merged PR and relevant diff when necessary.
5. Read the assigned specification before implementation.
6. Treat repository evidence as authoritative over chat memory.
7. Stop on material ambiguity rather than inventing context.
8. Continue the established requirement → implementation → review → merge lifecycle.

## UPDATE OWNERSHIP

Define clearly which context is expected to be updated and by whom.

At minimum:

- implementation sessions update requirement completion records and any directly relevant implementation context;
- the orchestrator maintains project-level current state, decisions, and next-step direction;
- no agent should rewrite durable context merely to restate its own task completion;
- context updates must be factual and based on repository state.

The workflow must avoid requiring every agent to rewrite every context document.

## SOURCE-OF-TRUTH RULE

Explicitly establish:

```text
Repository state + requirements + specifications + architecture + tests
    ↓
authoritative project state

Chat/session memory
    ↓
temporary working context
```

If chat memory conflicts with repository evidence, repository evidence wins.

## CONTEXT DESIGN RULES

Keep the system simple.

Do NOT create:

- a giant project dump
- duplicated copies of specifications
- a transcript of previous conversations
- per-session context files for every session
- an artificial database of every implementation detail
- generated summaries that cannot be traced to repository evidence

Detailed information remains in its authoritative location:

- requirements → `requirements/`
- product specifications → `docs/specs/`
- architecture → `docs/architecture/` / `docs/decisions/`
- engineering guidance → `docs/engineering/`
- implementation → source code/tests
- historical review → PR/requirement records

## DO NOT TOUCH

Do not implement or modify:

- Hotel Check application functionality
- acquisition behavior
- evidence behavior
- hospitality observation behavior
- analysis/scoring
- AI/LLM behavior
- database/persistence
- APIs
- UI
- preview
- booking/OTA
- unrelated specifications
- unrelated architecture

Avoid broad documentation cleanup unrelated to this requirement.

## ACCEPTANCE

REQ-020 is complete only when:

1. A new orchestrator can identify the project's objective and boundaries without access to the current chat.
2. A new orchestrator can determine the actual current project state from `CURRENT-STATE.md` and verify it against `main`.
3. A new orchestrator can identify the immediate next governed step from `NEXT-STEPS.md`.
4. Important durable decisions are captured in `DECISION-LOG.md` without invented history.
5. `ORCHESTRATOR-HANDOFF.md` provides an explicit takeover procedure.
6. The documents are concise and complementary rather than duplicated.
7. The context explicitly distinguishes implemented, in progress, specified, planned, proposed, deferred and rejected work where relevant.
8. The context explicitly states that repository evidence overrides chat memory.
9. The workflow defines ownership for keeping durable context current.
10. No Hotel Check application behavior is changed.
11. The implementation is self-consistent with the existing requirements lifecycle and engineering guidance.
12. The same requirement file is updated with implementation details, validation, PR information and limitations before being marked `PR_READY`.

## VALIDATION

Because this is a documentation/workflow foundation:

- no application test suite is required solely because of this requirement;
- validate all referenced paths and links where practical;
- verify the context against the actual current `main` state;
- verify that no documented item is falsely described as implemented;
- run applicable repository CI/documentation validation if present.

## OUTPUT

The implementation session must:

- create the durable context documents;
- update this requirement with implementation details and validation;
- create branch `docs/orchestrator-durable-context-foundation` from current `main`;
- create a PR against `main`;
- leave the PR unmerged;
- report the exact files changed and validation performed.

The orchestrator will review the PR and the user controls final merge.

## NON-SCOPE / FUTURE

This foundation does not attempt to solve:

- automatic context synchronization
- automatic repository summarization
- agent memory outside the repository
- CI-enforced semantic validation of every context statement
- replacement of specifications or ADRs
- product feature implementation

Those may be considered later only if a concrete need emerges.
