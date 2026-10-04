# Orchestrator Handoff Workflow

## Purpose

This procedure lets a replacement orchestrator take over the project without depending on the previous conversation.

## Takeover Procedure

1. Inspect the current `main` branch.
2. Read `docs/context/ORCHESTRATOR-CONTEXT.md`.
3. Read `docs/context/CURRENT-STATE.md`.
4. Read `docs/context/NEXT-STEPS.md`.
5. Read `docs/context/DECISION-LOG.md`.
6. Read `requirements/README.md` and the current development lifecycle documentation.
7. Identify the latest requirement and its lifecycle status from the repository.
8. Inspect the latest merged PR and relevant merge commit.
9. Read the specification(s) relevant to the next proposed slice.
10. Inspect the actual implementation and tests before making architectural decisions.
11. Check for open PRs and determine whether they affect the next decision.
12. Treat repository evidence as authoritative over chat/session memory.
13. If repository evidence is contradictory or materially insufficient, stop and report the ambiguity instead of inventing a decision.
14. Define or refine only the smallest next bounded requirement.
15. Do not implement an unapproved requirement.

## Durable Context Update Rules

The orchestrator owns the operational context files:

- `ORCHESTRATOR-CONTEXT.md` — stable project/orchestration principles.
- `CURRENT-STATE.md` — factual current repository state.
- `NEXT-STEPS.md` — immediate continuation.
- `DECISION-LOG.md` — durable decisions that should not be accidentally reversed.

Update only what actually changed. Keep these files concise. Do not copy full specifications, code, PR discussions, or conversation transcripts into them.

After a meaningful merge or architectural decision, verify whether `CURRENT-STATE`, `NEXT-STEPS`, or `DECISION-LOG` needs an update. The update itself must be grounded in repository evidence.

## Authority Rule

Use this authority order when information conflicts:

1. Current repository state and merged code/tests.
2. Approved requirements and completion/review records.
3. Product specifications and architecture/decision records.
4. Durable orchestrator context.
5. Temporary chat/session memory.

Chat is useful working context, but it is not durable project truth.

## Session Boundary

An implementation session may update its own requirement completion record and any documentation explicitly within its assigned scope. It should not rewrite the entire orchestrator context.

The orchestrator reviews the resulting PR, decides approval, and keeps the operational handoff state current.
