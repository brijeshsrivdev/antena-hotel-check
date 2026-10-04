# Engineering Principles

**Status:** SPECIFIED

These principles guide implementation sessions. They are deliberately small and support the repository's requirements, specifications, architecture decisions, and existing code rather than replacing them.

## 1. Simplest Complete Solution

Prefer the simplest design that fully satisfies the requirement and existing architectural constraints. Do not add infrastructure, abstractions, or configuration merely because they may be useful later.

## 2. Existing Code Before New Code

Before creating a service, component, utility, repository, DTO, or other abstraction:

1. Search for an existing equivalent.
2. Inspect its callers and tests.
3. Decide whether the existing abstraction should be extended.
4. Create something new only when the existing design cannot reasonably support the requirement.

## 3. Clear Boundaries

Keep responsibilities cohesive and boundaries explicit between input, domain behavior, persistence, external systems, and presentation.

Prefer composition over deep inheritance and avoid god services/components.

## 4. Explicit Contracts

Validate untrusted input at trust boundaries. Keep API, domain, persistence, and external-system contracts understandable and typed where the stack supports it.

## 5. Patterns Are Tools

Design patterns are not mandatory decorations. Use a pattern only when it solves a demonstrated problem or makes an important boundary clearer.

Avoid speculative factories, registries, wrappers, generic framework layers, and one-consumer abstractions.

## 6. Evidence Over Assumption

Repository evidence is authoritative over agent assumptions. If a required fact cannot be established, stop and report the ambiguity rather than inventing behavior or architecture.

## 7. Production-Aware, Not Production-Overengineered

Consider security, reliability, resource limits, failure modes, and operational impact for the change. Fix genuine issues within scope; record larger concerns as follow-up work instead of expanding the requirement without authorization.

## 8. Small, Reviewable Changes

Keep changes bounded to the assigned requirement. Avoid unrelated refactoring. The resulting diff should be understandable to an orchestrator reviewing it against the requirement.