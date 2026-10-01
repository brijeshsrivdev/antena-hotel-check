# REQ-002 — Evaluation Input Contract

**Status:** READY

**Owner:** Implementation Session 2

**Type:** Specification

**Created by:** Antena Hotel Check Orchestrator

## Objective

Define the product-level contract for how a hotel evaluation is initiated before any discovery, crawling, analysis, scoring, or preview implementation begins.

The product supports two intended entry modes:

1. Hotel name + city
2. Hotel website URL

The governed flow is:

`User input → validation → canonical evaluation request → future discovery/analysis pipeline`

## Repository Context to Read

Before starting, read:

- `docs/context/`
- `docs/specs/README.md`
- `docs/specs/SPEC-001-hotel-check.md`
- relevant files under `docs/architecture/`, `docs/decisions/`, and `docs/workflows/`
- this requirement file in full

## Scope

Define a precise, implementation-neutral contract covering:

### Supported input modes

- hotel name + city
- hotel website URL

### Validation

Define expected product behavior for:

- missing hotel name
- missing city
- missing website URL
- blank values
- malformed website URLs
- unsupported URL forms
- invalid combinations
- whitespace and casing normalization
- obvious invalid input

### Input precedence

Define behavior when both hotel name + city and website URL are supplied.

Do not leave precedence implicit.

### Canonical representation

Define the conceptual representation passed to downstream discovery/analysis stages, distinguishing where appropriate between:

- user-provided input
- normalized input
- canonical evaluation target

Do not prematurely define implementation classes, database tables, or technology choices unless the specification requires them.

### Ambiguity

Define product behavior when:

- hotel name + city could identify multiple properties
- a supplied website does not appear to represent the named hotel
- a hotel cannot be confidently resolved

The system must not silently select an unrelated hotel.

### Error semantics

Define product-level categories for invalid input and unresolved/ambiguous evaluation targets.

### Boundaries

Explicitly identify what this requirement does NOT define.

## Non-Scope

Do not implement or specify implementation details for:

- crawling
- scraping
- search-engine APIs
- hotel discovery algorithms
- SEO analysis
- guest journey analysis
- scoring
- AI generation
- preview rendering
- preview hosting
- booking integration
- OTA integration
- application framework or technology stack
- database schema

These are governed by later requirements/specifications.

## Product Principles

Preserve the principles established by `SPEC-001`:

- hospitality-first
- no aggressive scraping dependency
- public-information boundaries
- provenance matters
- inferred/generated content must not silently become verified hotel facts
- interactive hotel preview is a core eventual product outcome
- do not turn the product into a generic SEO checker

## Deliverables

Create/update documentation required for this contract, including:

- `docs/specs/SPEC-002-evaluation-input.md`
- specification index updates if needed
- decision records under `docs/decisions/` only when a genuine decision is required

Do not create application feature code.

## Acceptance Criteria

The resulting specification must:

1. Define both supported input modes unambiguously.
2. Define validation behavior for the important invalid/blank/malformed cases.
3. Define precedence when multiple input modes are supplied.
4. Define the conceptual canonical evaluation request.
5. Define behavior for ambiguity and hotel/website mismatch.
6. Prevent silent selection of an unrelated hotel.
7. Clearly separate this input boundary from discovery, analysis, scoring, and preview work.
8. Provide testable acceptance criteria for a future implementation session.
9. Preserve the principles and terminology established by `SPEC-001`.
10. Avoid committing the project to a technology stack or implementation architecture prematurely.

## Session Completion Record

### Status

_To be completed by the implementation session._

### Branch

_To be completed._

### PR

_To be completed._

### Documents Changed

_To be completed._

### Decisions Made

_To be completed._

### Open Questions

_To be completed._

### Validation Performed

_To be completed._

## Orchestrator Review History

### Review Round 1

_Status: PENDING_

_The orchestrator will review the PR against this requirement and record findings here._
