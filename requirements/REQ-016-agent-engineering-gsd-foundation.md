# REQ-016 — Agent Engineering & GSD Foundation

STATUS: READY
REQUIREMENT_ID: REQ-016
BRANCH: docs/req-016-agent-engineering-gsd-foundation

## Objective

Establish a small, durable engineering and agent-execution foundation for `antena-hotel-check` so future implementation sessions can use GSD or another compatible coding agent without losing repository governance, product context, engineering quality, or scope discipline.

This requirement is workflow/documentation infrastructure. It must not implement a product feature.

## Why This Exists

The repository is expected to grow into a production hospitality analysis platform with many interconnected capabilities. Future coding agents must not need the entire repository schema or every feature in context for every task. They should receive the smallest complete context needed for the current requirement and must stop rather than guess when repository evidence is insufficient.

GSD is an execution aid, not the product authority.

## Authority Model

The following order is authoritative:

1. Product direction and orchestrator decisions
2. Approved repository specifications and requirements
3. Architecture and ADRs
4. Existing implemented code and tests
5. Agent planning/execution tooling such as GSD
6. Agent assumptions — assumptions are never authoritative

An agent MUST NOT override a requirement, architecture decision, security boundary, or product decision because its planning system suggests another approach.

## In Scope

1. Document how GSD is used with this repository.
2. Establish a scoped-context protocol for implementation sessions.
3. Establish concise engineering principles and backend/frontend pattern guidance.
4. Establish testing, security, reliability, and production-readiness expectations.
5. Establish an agent self-review gate before `PR_READY`.
6. Establish an ambiguity/no-hallucination protocol.
7. Establish lightweight decision-record expectations for implementation decisions.
8. Define the standard implementation-session lifecycle.
9. Provide a reusable implementation-session prompt/template for future requirements.
10. Keep all guidance simple, discoverable, and repository-based.

## Explicit Non-Goals

This requirement MUST NOT:

- implement hotel analysis
- implement crawling/acquisition
- implement AI analysis
- implement scoring
- implement preview generation
- introduce a new agent platform
- build a custom task orchestration service
- force a specific coding-agent vendor
- require every project file to be loaded into every session
- prescribe design patterns where simpler code is clearly better
- introduce unnecessary abstractions or framework layers
- replace GitHub, CI, requirements, or repository documentation as the source of truth

## Required Repository Structure

Create or update only the minimum durable documentation required. The preferred structure is:

```text
docs/engineering/
├── ENGINEERING-PRINCIPLES.md
├── BACKEND-PATTERNS.md
├── FRONTEND-PATTERNS.md
├── TESTING-STANDARDS.md
└── AGENT-GUIDELINES.md

docs/workflows/
└── IMPLEMENTATION-SESSION.md

requirements/
└── REQ-016-agent-engineering-gsd-foundation.md
```

If an existing repository document already serves one of these purposes, extend it instead of creating a duplicate.

## GSD Integration Boundary

Document GSD as the implementation execution layer.

The intended relationship is:

```text
Antena Orchestrator
        |
        | product direction / requirements / architecture / review
        v
Requirement + scoped context
        |
        v
GSD / implementation agent
        |
        +--> plan
        +--> implement
        +--> test
        +--> self-review
        v
PR_READY
        |
        v
Orchestrator review
        |
        +--> changes required
        +--> approve
        v
Merge
```

GSD MUST NOT become the authority for product scope or architecture.

The implementation session must verify the current official GSD installation/use guidance rather than hardcoding obsolete package/repository details into Antena documentation.

The workflow should remain usable if the team later changes agent tooling.

## Scoped Context Protocol

Every future implementation requirement should provide, where applicable:

### READ

The requirement and the small set of authoritative specifications/ADRs needed to understand it.

### INSPECT

Existing classes, components, services, tests, APIs, or migrations that directly participate in the change.

### DEPENDENCIES

The contracts and implemented features that the change directly depends on.

### DO NOT TOUCH

Modules/features explicitly outside the current scope.

### ACCEPTANCE

The requirement's acceptance criteria. The agent must not invent additional product requirements.

### OUTPUT

Expected code, tests, documentation updates, and PR state.

Agents should follow references progressively. They should not load the entire database schema, entire repository, or unrelated feature documentation unless the current task genuinely requires it.

## No-Hallucination / Ambiguity Protocol

Before implementation, the agent must inspect existing code and relevant repository documentation.

If a required fact cannot be established from repository evidence:

> STOP — repository evidence is insufficient. Do not guess.

The agent should identify:

- what is unknown
- why it matters
- what repository evidence is missing
- the smallest clarification or decision required

The agent must not invent:

- classes that it assumes exist
- APIs that it assumes exist
- database fields that it assumes exist
- product behavior not present in the requirement
- architecture decisions not documented or justified

## Engineering Principles

Keep engineering guidance deliberately small.

### Simple First

Prefer the simplest design that satisfies the requirement and current architectural constraints.

Do not introduce an abstraction merely because a design pattern exists.

### Existing Code Before New Code

Before creating a new service, component, utility, repository, DTO, or abstraction:

1. Search for an existing equivalent.
2. Inspect its callers and tests.
3. Determine whether it should be extended.
4. Create something new only when the existing design cannot reasonably support the requirement.

### Composition Over Cleverness

Prefer cohesive components and clear boundaries over deep inheritance or generic framework layers.

### Explicit Contracts

Keep boundaries between input, domain behavior, persistence, external systems, and presentation clear.

### Design Patterns Are Tools

Patterns may be used when they solve a demonstrated problem. They are not mandatory checkboxes.

Examples of legitimate uses include:

- Strategy/adapter-style boundaries when multiple interchangeable implementations are genuinely expected.
- Repository abstractions where persistence boundaries already exist.
- Feature-oriented UI components when a feature has meaningful independent behavior.

Avoid pattern-driven over-engineering such as unnecessary factories, registries, wrappers, or abstraction layers with only one trivial consumer.

## Backend Guidance

Prefer, where consistent with the existing codebase:

- constructor dependency injection
- immutable request/response/value types where appropriate
- cohesive services with explicit responsibilities
- domain/service/persistence boundaries
- typed failures and meaningful error handling
- transactions at clear consistency boundaries
- bounded external calls with timeouts
- deterministic tests
- explicit validation at trust boundaries

Avoid:

- god services
- persistence models leaking everywhere as API contracts
- static state for business behavior
- hidden network calls
- unbounded loops, queries, queues, or response buffers
- speculative generic infrastructure

## Frontend Guidance

Prefer, where consistent with the existing frontend stack:

- feature-oriented components
- typed contracts
- reusable UI primitives where reuse is demonstrated
- explicit loading, error, empty, and success states
- clear separation between presentation and business/data concerns
- accessibility-conscious interactive elements

Avoid:

- giant page components
- duplicated business logic
- unnecessary global state
- components coupled directly to persistence concerns
- premature design-system abstraction

## Testing Standards

Tests should primarily verify behavior and contracts.

For each implementation, consider:

- happy path
- validation failures
- important boundary conditions
- dependency failures
- security-sensitive cases
- concurrency/idempotency where relevant
- deterministic behavior

Tests must not depend on live third-party systems unless a requirement explicitly and safely defines such an integration test.

An agent must not report a test as passed unless it actually executed.

CI is authoritative for repository-level validation when local infrastructure differs from CI.

## Production-Readiness Check

Agents should consider production implications without prematurely building enterprise infrastructure.

Before `PR_READY`, consider whether the change introduces:

- unbounded memory or processing
- N+1 queries
- unsafe external calls
- missing timeouts
- retry storms
- concurrency/race conditions
- duplicate processing/idempotency problems
- data loss or partial-failure risks
- authorization or input-validation gaps
- PII/secrets exposure
- unsafe logging
- missing resource cleanup

Fix genuine issues within scope. Record larger concerns as follow-up work rather than expanding the requirement without authorization.

## Security Minimum

For every feature, identify relevant trust boundaries.

At minimum consider:

- untrusted input
- authentication/authorization where applicable
- injection risks
- SSRF for server-side network access
- secret handling
- PII handling
- file/path access
- external service abuse

Security requirements must not be weakened to make tests or development easier.

## Agent Self-Review Gate

Before changing a requirement to `PR_READY`, the implementation session must perform a concise self-review:

### Scope

- Did I implement only this requirement?
- Did I modify unrelated behavior?

### Context

- Did I inspect existing implementations before adding new ones?
- Did I read the relevant requirement/spec/ADR documents?

### Design

- Is this the simplest reasonable design?
- Did I introduce an abstraction only because it is useful now or clearly justified by the requirement?
- Did I follow existing project conventions?

### Correctness

- What are the important failure paths?
- Are validation and boundary conditions handled?

### Testing

- Did meaningful behavior get tested?
- Did important failure cases get tested?
- Did the tests actually execute?

### Security / Production

- Did I inspect relevant trust boundaries?
- Did I consider timeouts, resource bounds, concurrency, retries, and data exposure where applicable?

### Repository Hygiene

- Is the requirement updated with the real implementation and validation status?
- Is documentation accurate?
- Are no unrelated files or generated artifacts included?

If a material answer is unknown, the agent must stop or record the blocker rather than fabricate confidence.

## Decision Protocol

Implementation decisions should be recorded at the smallest appropriate level:

- trivial/local decision: code comment or PR description only when useful
- requirement-specific decision: update the requirement's completion/decision section
- reusable architectural decision: create/update an ADR

Do not create an ADR for every implementation detail.

## Standard Implementation Session

The reusable session lifecycle is:

```text
1. Inspect current main
2. Read assigned requirement
3. Read scoped context
4. Inspect existing implementation/dependencies/tests
5. Confirm scope and non-scope
6. Plan the smallest implementation
7. Implement with TDD where appropriate
8. Run relevant tests
9. Perform agent self-review
10. Update the SAME requirement file
11. Set STATUS to PR_READY only when genuinely ready
12. Create/update PR
13. Report exact validation results
14. STOP for orchestrator review
```

The agent must not merge its own PR unless the orchestrator explicitly assigns that responsibility.

## Requirement Authoring Guidance for Future Work

Future requirements should, where useful, contain:

- objective
- context
- in scope
- explicit non-scope
- READ
- INSPECT
- DEPENDENCIES
- DO NOT TOUCH
- acceptance criteria
- validation required
- branch name
- governance/stop condition

This creates a small feature context pack without duplicating the whole architecture.

## Acceptance Criteria

### AC-1 — Durable engineering guidance

Repository contains concise, discoverable engineering guidance covering simplicity, backend/frontend patterns, testing, security, and production-readiness.

### AC-2 — GSD execution boundary

Repository documents GSD as an implementation aid while preserving requirements, architecture, GitHub, CI, and orchestrator review as authoritative governance.

### AC-3 — Scoped context protocol

Repository defines a repeatable READ / INSPECT / DEPENDENCIES / DO NOT TOUCH / ACCEPTANCE / OUTPUT context model.

### AC-4 — No-hallucination protocol

Repository explicitly instructs agents to stop and report ambiguity when repository evidence is insufficient rather than inventing facts or architecture.

### AC-5 — Agent self-review

Repository contains a concise pre-PR self-review checklist covering scope, context, design, correctness, testing, security, production readiness, and repository hygiene.

### AC-6 — Session lifecycle

Repository documents the governed implementation-session lifecycle from requirement through PR and orchestrator review.

### AC-7 — Progressive context

Guidance explicitly discourages dumping the entire repository/schema into every agent session and favors scope-oriented progressive context.

### AC-8 — Pattern discipline

Guidance explains that design patterns are tools, not mandatory requirements, and that simple solutions are preferred when they satisfy the contract.

### AC-9 — Tool independence

The workflow remains usable if GSD or another implementation agent/tool changes in the future.

### AC-10 — No product implementation

The PR contains only workflow/engineering documentation and related minimal configuration required for the agent workflow. No hotel-check product feature is implemented.

## Validation Required Before PR

The implementation session must:

1. Inspect the current repository guidance before adding duplicates.
2. Verify every new document is concise and internally consistent.
3. Verify the workflow does not contradict existing SDD/TDD or requirement lifecycle documentation.
4. Verify no product code is changed.
5. If GSD configuration is added, verify it is minimal, tool-version-aware, and does not become the source of product truth.

## Completion Record

The implementation session must update this SAME file before PR creation:

- `STATUS: PR_READY`
- implementation summary
- branch
- PR number/URL
- files changed
- validation performed
- open questions/blockers

## Governance

- Create/use the exact branch named in `BRANCH`.
- Execute ONLY REQ-016.
- Do not implement a product feature.
- Do not modify unrelated requirements.
- Do not merge the PR.
- Stop after creating/updating the PR and wait for orchestrator review.

## Implementation Prompt

Use this prompt to open the governed implementation session:

> You are Implementation Session for `antena-hotel-check`.
>
> Work ONLY on `requirements/REQ-016-agent-engineering-gsd-foundation.md`.
>
> Branch: `docs/req-016-agent-engineering-gsd-foundation`
>
> First inspect current `main`, read REQ-016 completely, inspect existing `requirements/README.md`, SDD/TDD workflow documentation, architecture/context guidance, and any existing agent/GSD-related files. Do not assume they are absent.
>
> Implement only the engineering/agent workflow foundation described by REQ-016. Keep it simple. Reuse existing documentation where appropriate instead of creating duplicates.
>
> Establish scoped context, engineering patterns, testing/security/production guidance, self-review, ambiguity/no-hallucination behavior, GSD integration boundaries, and the standard implementation-session lifecycle.
>
> GSD is an execution aid, not the product or architecture authority. Do not hardcode obsolete GSD installation details; verify the current official guidance if configuration is required.
>
> Do not implement hotel analysis, acquisition, crawling, AI, scoring, preview, booking, OTA, or other product features.
>
> Before PR_READY, perform the self-review defined by REQ-016, validate documentation consistency, update this SAME requirement file with the real completion record, create/update the PR, and STOP for orchestrator review.
