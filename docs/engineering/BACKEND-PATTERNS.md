# Backend Patterns

**Status:** SPECIFIED

Use these as practical defaults where they match the existing backend design. They are guidance, not a mandate to refactor unrelated code.

## Prefer

- Constructor dependency injection.
- Cohesive services with explicit responsibilities.
- Clear domain, service, persistence, and external-system boundaries.
- Immutable request/response/value types where appropriate.
- Explicit validation at trust boundaries.
- Meaningful, typed failures and consistent error handling.
- Transactions at clear consistency boundaries.
- Bounded external calls with appropriate timeouts.
- Deterministic tests for business behavior.
- Existing project conventions over introducing a new framework style.

## Avoid

- God services with unrelated responsibilities.
- Persistence models used indiscriminately as public API contracts.
- Static mutable state for business behavior.
- Hidden network calls from otherwise local-looking operations.
- Unbounded loops, queries, queues, response buffers, or recursive processing.
- Speculative generic infrastructure.
- Abstractions with only one trivial consumer unless they establish a meaningful boundary.

## Before Adding a Backend Abstraction

Search the repository first. Inspect callers and tests. Prefer extending an existing abstraction when that preserves its responsibility and contract. Introduce a new abstraction only when the requirement and existing design justify it.

## External Calls

Treat external systems as failure-prone boundaries. Consider timeout, response-size/resource limits, validation of returned data, retry behavior, and idempotency where relevant. Server-side access to user-controlled URLs must also consider SSRF and network-boundary controls.