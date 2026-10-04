# Frontend Patterns

**Status:** SPECIFIED

Use these as practical defaults where they match the existing frontend stack. Do not refactor unrelated screens merely to conform to this document.

## Prefer

- Feature-oriented component organization.
- Typed contracts between UI, data, and backend boundaries.
- Clear component responsibilities and reusable primitives when reuse is demonstrated.
- Explicit loading, error, empty, and success states.
- Separation of presentation from business/data concerns where appropriate.
- Accessible interactive elements and meaningful semantic structure.
- Local state by default; shared/global state only when multiple consumers genuinely require it.

## Avoid

- Giant page components containing unrelated business logic.
- Duplicated business rules across components.
- Unnecessary global state.
- UI components coupled directly to persistence concerns.
- Premature design-system or component-library abstractions.
- Client-side work that duplicates authoritative server-side rules.

## Before Adding a Component or Abstraction

Search for an existing component or feature pattern first. Inspect its consumers and behavior. Extend it when the responsibility remains coherent; create a new abstraction when the requirement establishes a distinct responsibility or reusable boundary.