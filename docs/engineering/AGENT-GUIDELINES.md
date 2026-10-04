# Agent Guidelines

**Status:** SPECIFIED

This document defines how coding agents, including GSD-backed sessions, operate inside the repository. It does not replace requirements, specifications, architecture decisions, GitHub, CI, or orchestrator review.

## Authority

Use this hierarchy:

1. Product direction and orchestrator decisions.
2. Approved repository requirements and specifications.
3. Architecture and decision records.
4. Existing implemented code and tests.
5. Agent planning/execution tooling, including GSD.
6. Agent assumptions.

Lower levels must not override higher levels.

## GSD Boundary

GSD is an execution aid: it can help plan and carry out an assigned implementation. It is not product or architecture authority.

The governed flow is:

```text
Product direction / orchestrator
        ↓
Requirement + scoped context
        ↓
GSD or another implementation agent
        ↓
Plan → Implement → Test → Self-review
        ↓
PR_READY
        ↓
Orchestrator review
        ↓
Fix / Re-review / Approval
        ↓
Merge
```

Do not hardcode obsolete GSD package, command, or repository details into Antena guidance. When setup instructions are actually needed, verify the current official GSD guidance at that time. The repository workflow must remain usable if the team changes implementation tooling.

## Scoped Context Protocol

Each implementation requirement should provide a small context pack where applicable:

| Section | Agent action |
|---|---|
| **READ** | Read the requirement and only the authoritative specifications/ADRs needed to understand it. |
| **INSPECT** | Inspect existing classes, components, services, tests, APIs, migrations, or configuration directly involved. |
| **DEPENDENCIES** | Identify contracts and implemented features the change directly depends on. |
| **DO NOT TOUCH** | Treat explicitly excluded modules/features as out of scope. |
| **ACCEPTANCE** | Implement the stated acceptance criteria; do not invent additional product requirements. |
| **OUTPUT** | Produce the requested code/tests/docs and the required PR state. |

Follow references progressively. Start with the requirement, inspect direct dependencies, and read deeper context only when the task requires it. Do not load the entire repository, database schema, or unrelated architecture by default.

## Ambiguity / No-Hallucination Rule

Before creating a new abstraction, search for an existing implementation, inspect related callers/tests, and determine whether it should be extended.

If repository evidence is insufficient to make a required decision:

> **STOP — repository evidence is insufficient. Do not guess.**

Report what is unknown, why it matters, what evidence is missing, and the smallest clarification or decision needed.

Never invent classes, APIs, database fields, product behavior, architecture, or business rules merely because they seem plausible.

## Security and Production Check

Before `PR_READY`, consider risks relevant to the change, including:

- untrusted input and injection;
- authentication/authorization;
- SSRF and unsafe server-side network access;
- PII and secrets;
- file/path access;
- unsafe logging;
- external-service abuse;
- unbounded processing, memory, queries, or responses;
- timeouts, retries, concurrency, and idempotency;
- resource cleanup and partial-failure/data-loss risks.

Be production-aware without building enterprise infrastructure that the requirement does not need.

## Self-Review Gate

Before setting a requirement to `PR_READY`, answer:

### Scope
- Did I implement only the assigned requirement?
- Did I avoid unrelated behavior/refactoring?

### Context
- Did I inspect existing implementations first?
- Did I read the relevant requirement/specification/ADR?
- Are all material assumptions supported by repository evidence?

### Design
- Is this the simplest reasonable solution?
- Did I avoid unnecessary abstractions and pattern proliferation?
- Does the solution follow existing conventions?

### Correctness
- What happens for invalid input and important boundaries?
- What happens when dependencies fail?
- What happens on retries or duplicates where relevant?

### Testing
- Are important behaviors and failure paths covered?
- Are tests deterministic?
- Did I actually execute the reported tests?

### Security / Production
- Did I inspect relevant trust boundaries?
- Did I consider resource limits, timeouts, concurrency, retries, and data exposure where applicable?

### Repository Hygiene
- Is the same requirement file updated with real status and validation?
- Is documentation accurate?
- Are no unrelated or generated artifacts included?

If a material answer is unknown, stop or record the blocker instead of claiming confidence.