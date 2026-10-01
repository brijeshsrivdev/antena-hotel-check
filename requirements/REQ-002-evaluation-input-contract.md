# REQ-002 — Evaluation Input Contract

**Status:** PR_READY

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

PR_READY

### Branch

`spec/req-002-evaluation-input-contract`

### PR

[#2 — docs: define REQ-002 evaluation input contract](https://github.com/brijeshsrivdev/antena-hotel-check/pull/2)

### Documents Changed

- `docs/specs/SPEC-002-evaluation-input.md` — created the implementation-neutral evaluation input contract.
- `docs/specs/README.md` — indexed SPEC-002.
- `requirements/REQ-002-evaluation-input-contract.md` — recorded this session completion state and PR metadata.

### Decisions Made

- Accepted input modes are hotel name + city, website URL, or all three fields.
- When all three fields are supplied, the website URL is the canonical evaluation target and hotel name/city remain supplemental identity context.
- Partial hotel identity combined with a website URL is rejected; the combined mode requires a complete name + city pair.
- Validation distinguishes syntactically valid input from later hotel resolution.
- Ambiguous, unresolved, and material hotel/website mismatch outcomes must never silently select or substitute an unrelated property.
- No separate decision record was created because these choices are part of the REQ-002 product contract and do not select implementation architecture or technology.

### Open Questions

No material open question remains within the bounded REQ-002 scope. Discovery/resolution algorithms, evidence thresholds, security/network policy, and implementation architecture remain future work as explicitly scoped.

### Validation Performed

- Read the complete READY requirement and all referenced repository context on `main` before making changes.
- Reviewed `SPEC-001`, specification index, architecture concerns, decision guidance, and development lifecycle.
- Created the specification without application feature code, crawling, discovery, analysis, scoring, AI generation, or preview implementation.
- Checked that the specification contains explicit supported modes, validation cases, precedence, canonical representation, ambiguity/mismatch behavior, error categories, boundaries, and future implementation acceptance tests.
- No automated application tests were applicable because this session changes documentation/specification only.

## Orchestrator Review History

### Review Round 1

_Status: PENDING_

_The orchestrator will review the PR against this requirement and record findings here._
