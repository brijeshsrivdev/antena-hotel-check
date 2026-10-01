# Development Lifecycle

**Status:** SPECIFIED

Requirement → Specification → Architecture/design decision → Implementation prompt → Implementation session → Tests → Dedicated branch → Commit → Push → Pull Request → Orchestrator review → Fixes → Re-review → Approval → User merge.

## Implementation-session rules

Before changing code:
1. Read durable context.
2. Read the assigned specification.
3. Inspect repository state.
4. Confirm scope and non-scope.
5. Stop and report material ambiguity.

During implementation:
- implement only the assigned slice;
- add appropriate automated tests;
- preserve security/truthfulness constraints;
- avoid unrelated refactoring.

Before PR:
- run applicable validation;
- update context/specification;
- use a clear branch name;
- write an accurate PR description.

## Orchestrator review

Review specification compliance, architecture, correctness, edge cases, tests, security/privacy, UX/product behavior, documentation/context and CI/validation. Green CI is not sufficient for acceptance.

## Merge authority

Implementation sessions do not merge their own PRs. The orchestrator reviews and the user controls final merge.
