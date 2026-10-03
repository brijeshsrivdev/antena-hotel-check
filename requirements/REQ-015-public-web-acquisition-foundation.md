# REQ-015 — Public Web Acquisition Foundation

STATUS: READY
REQUIREMENT_ID: REQ-015
BRANCH: feature/public-web-acquisition-foundation

## Objective

Establish the first bounded acquisition capability for publicly accessible hotel web evidence.

The capability must accept the canonical evaluation target produced by REQ-011 and provide a deterministic acquisition contract that can retrieve publicly accessible web content without becoming an aggressive scraper or attempting to bypass third-party protections.

This is an acquisition foundation only. It must create trustworthy acquisition outputs that later analysis capabilities can consume.

## Context

The product objective is hospitality-specific digital experience analysis. The system must eventually evaluate whether a guest can find, understand, trust, explore, and book a hotel online.

The approved architecture uses an HTTP-first acquisition strategy with browser escalation only when justified. Acquisition must preserve evidence provenance and must not silently convert unavailable or inferred information into verified hotel facts.

Read completely before implementation:

- `docs/context/PROJECT-CONTEXT.md`
- `docs/specs/SPEC-001-hotel-check.md`
- `docs/specs/SPEC-002-hospitality-analysis.md`
- `docs/specs/SPEC-003-guest-journey.md`
- `docs/specs/SPEC-004-evidence-provenance.md`
- `docs/specs/SPEC-005-interactive-preview.md`
- `docs/architecture/ARCH-001-evaluation-architecture.md`
- `docs/decisions/ADR-001-technology-baseline.md`
- `requirements/REQ-011-evaluation-input-foundation-implementation.md`
- `requirements/REQ-013-evaluation-persistence-foundation-implementation.md`

## In Scope

1. Define an acquisition request/result contract for a canonical hotel website target.
2. Implement HTTP-first acquisition for a single public URL.
3. Normalize the requested URL without changing its semantic target.
4. Capture response metadata required for provenance, including final URL, status, content type, retrieval timestamp, and relevant redirect information.
5. Capture the retrieved HTML/body in a bounded representation suitable for downstream analysis.
6. Enforce bounded request behavior such as connect/read timeout, response-size limit, and redirect limit.
7. Restrict acquisition to public HTTP(S) resources and reject unsupported schemes.
8. Make acquisition failures explicit and typed rather than silently returning empty evidence.
9. Provide deterministic tests using a local/mock HTTP server; tests must not depend on third-party hotel websites.
10. Keep the acquisition boundary replaceable so browser-based acquisition can be added later without changing the analysis contract.
11. Document the public-web acquisition safety boundary.

## Explicit Safety / Scope Boundaries

The implementation MUST NOT:

- bypass robots.txt or other published access restrictions
- evade rate limits, bot protection, CAPTCHA, authentication, or access controls
- use credential stuffing or authenticated access
- implement proxy rotation or stealth browser behavior
- perform broad-domain crawling
- recursively crawl arbitrary external sites
- scrape OTAs or competitor systems
- attempt to bypass third-party protections
- introduce an uncontrolled crawler queue

REQ-015 is a **single-target, bounded acquisition capability**. Multi-page discovery/crawling is deferred.

## HTTP-First Contract

The initial acquisition path should use a standard HTTP client rather than a browser.

For a successful response, the result must preserve enough information to establish:

- requested URL
- final URL after allowed redirects
- HTTP status
- content type
- retrieval timestamp
- response headers required by provenance policy
- bounded response body/HTML
- acquisition method

The result must distinguish at minimum:

- SUCCESS
- HTTP_ERROR
- TIMEOUT
- REDIRECT_LIMIT_EXCEEDED
- RESPONSE_TOO_LARGE
- UNSUPPORTED_SCHEME
- NETWORK_ERROR
- INVALID_TARGET

Exact class/type names may follow the existing project naming conventions.

## Provenance Boundary

Acquisition output is evidence, not analysis.

The acquisition layer must not:

- claim that extracted text is factually correct about the hotel
- infer hotel amenities
- infer room attributes
- assign scores
- generate recommendations
- rewrite hotel content
- classify guest-journey quality

Downstream analysis is responsible for interpreting acquired evidence.

## Acceptance Criteria

### AC-1 — Single public URL acquisition

Given a valid canonical website URL, the acquisition service can retrieve a public HTTP(S) response and return a typed acquisition result.

### AC-2 — Provenance metadata

A successful acquisition records the requested URL, final URL, status, content type, retrieval timestamp, acquisition method, and bounded response content.

### AC-3 — Redirect handling

Redirects are followed only up to the configured bounded limit. Exceeding the limit produces a typed `REDIRECT_LIMIT_EXCEEDED` outcome.

### AC-4 — Response size protection

Responses exceeding the configured maximum size are rejected with a typed `RESPONSE_TOO_LARGE` outcome.

### AC-5 — Timeout protection

Connection/read timeout behavior produces a typed `TIMEOUT` outcome and does not hang the evaluation indefinitely.

### AC-6 — Scheme safety

Non-HTTP(S) schemes are rejected before network access.

### AC-7 — No uncontrolled crawling

REQ-015 performs acquisition for one requested URL only. It does not discover or recursively fetch additional pages.

### AC-8 — No bypass behavior

The implementation contains no proxy rotation, CAPTCHA bypass, authentication bypass, stealth browser behavior, or rate-limit evasion.

### AC-9 — Deterministic tests

Tests use a local/mock HTTP server and cover success, redirects, HTTP errors, timeout, oversized response, unsupported scheme, and network failure behavior as applicable.

### AC-10 — Replaceable acquisition boundary

Downstream code depends on an acquisition interface/contract rather than directly coupling analysis to a specific HTTP client implementation.

### AC-11 — CI validation

The repository's backend CI workflow executes the REQ-015 test suite successfully.

### AC-12 — Scope discipline

No analysis engine, scoring engine, AI provider, browser escalation, preview generation, booking integration, OTA integration, or multi-page crawler is introduced.

## Validation Required Before PR

The implementation session must:

1. Run the relevant Maven tests.
2. Confirm tests execute in the repository CI workflow.
3. Confirm the tests use deterministic local/mock HTTP infrastructure rather than live hotel websites.
4. Verify no unsupported network-bypass behavior was introduced.
5. Update this SAME requirement file with:
   - `STATUS: PR_READY`
   - implementation summary
   - branch used
   - PR number and URL
   - files changed
   - validation performed and actual CI result
   - open questions/blockers

## Governance

- Create the exact branch named in `BRANCH` from current `main`.
- Execute ONLY REQ-015.
- Do not merge the PR.
- Stop after creating/updating the PR and wait for orchestrator review.
