# REQ-004 — Public Evidence Acquisition Contract

**Status:** PR_READY

**Owner:** Next implementation/specification session

**Requirement Type:** Product specification

## Objective

Define the product-level contract for how Antena Hotel Check may acquire publicly accessible hotel evidence after an evaluation has a valid `CanonicalEvaluationRequest` and evaluation lifecycle semantics are established.

This requirement must establish a controlled, hospitality-focused evidence acquisition boundary without turning the product into an aggressive scraping system.

The governed flow is:

`CanonicalEvaluationRequest → controlled public evidence acquisition → evidence with provenance → future analysis`

## Repository Context to Read First

The session MUST read:

- `docs/context/project-context.md`
- `docs/specs/SPEC-001-hotel-check.md`
- `docs/specs/SPEC-002-evaluation-input.md`
- `docs/specs/SPEC-003-evaluation-lifecycle.md`
- `docs/specs/README.md`
- `docs/architecture/architecture-concerns.md`
- `docs/decisions/` relevant files
- `requirements/README.md`
- this requirement file in full

## Scope

Define the product/domain contract for public evidence acquisition, including:

1. What qualifies as public evidence.
2. Permitted first-party hotel website acquisition.
3. Controlled discovery of relevant public pages within the hotel website.
4. When browser-rendered acquisition may conceptually be necessary.
5. Boundaries for third-party/public sources.
6. Evidence provenance and source attribution.
7. Access restrictions, unavailable pages, and partial acquisition.
8. Rate/concurrency/crawl-boundary principles at a product level.
9. Respect for robots/access restrictions and non-bypass behavior.
10. Evidence freshness and acquisition-time semantics at a conceptual level.
11. Duplicate/canonical URL handling at a product level.
12. Security boundaries for externally supplied URLs and public web access.
13. Relationship between acquired evidence and the evaluation lifecycle.

## Hospitality Focus

Evidence acquisition should prioritize information useful for the guest journey, such as:

- hotel identity
- location/contact
- rooms and room details
- amenities
- dining where applicable
- policies where publicly available
- booking/discovery paths
- guest-facing trust and informational content

Do not expand this into unrestricted website or internet crawling.

## Public-Web Safety Principles

The specification MUST preserve these principles:

- The product must not depend on aggressive scraping.
- Do not bypass authentication, access controls, CAPTCHAs, bot protections, paywalls, or other technical restrictions.
- Do not attempt to evade blocking or blocklists.
- Do not treat failure to access a page as permission to use increasingly aggressive acquisition techniques.
- Acquisition must remain bounded to what is necessary for the hotel evaluation.
- Public availability does not mean unlimited acquisition rights; the product should minimize unnecessary requests.
- Third-party content must not be represented as first-party hotel facts without provenance.

## Acquisition Model

Define an implementation-neutral acquisition model that distinguishes, where useful, between:

- directly fetched public content
- browser-rendered public content
- publicly linked third-party content
- unavailable/restricted content

The specification may establish an escalation principle such as using ordinary HTTP acquisition before browser rendering when sufficient, but must not prematurely lock the project to a specific library or infrastructure.

## Evidence Provenance

Every retained evidence item must conceptually preserve enough provenance to understand:

- source URL
- source/domain relationship where known
- acquisition timestamp
- acquisition method/category
- whether content was directly observed or derived later
- access/unavailability status

Do not silently transform evidence into verified hotel facts without preserving provenance.

## Access and Failure Semantics

Define product behavior for:

- successful acquisition
- timeout
- unavailable page
- robots/access restriction
- server error
- unsupported content
- JavaScript-dependent content
- blocked/bot-protected content
- partial acquisition
- duplicate content/URLs

Acquisition failure must remain distinguishable from evidence indicating that a hotel feature does not exist.

## Security Boundaries

Define product-level safeguards for externally supplied website URLs, including the requirement that future implementation must prevent the evaluation system from becoming an uncontrolled network request mechanism.

The specification must identify relevant concerns such as:

- unsafe URL targets
- internal/private network access
- redirect abuse
- excessive request amplification
- unbounded depth or page count
- unsafe downloaded content

Do not prescribe a specific implementation mechanism yet.

## Non-Scope

Do NOT implement or specify detailed implementation for:

- crawler code
- Playwright code
- HTTP client libraries
- queue infrastructure
- proxy services
- CAPTCHA solving
- anti-bot bypass
- search engine scraping
- OTA scraping
- hotel identity resolution algorithms
- analysis rules
- scoring
- AI/model architecture
- report UI
- preview rendering
- preview hosting
- database schema
- cloud deployment

These require later requirements/specifications/architecture decisions.

## Acceptance Criteria

The resulting specification must:

1. Define what public evidence acquisition means for this product.
2. Establish a controlled hotel-site acquisition boundary.
3. Define hospitality-focused acquisition priorities without unrestricted crawling.
4. Explicitly prohibit bypassing authentication, CAPTCHAs, access controls, paywalls, or anti-bot protections.
5. Define product-level handling of unavailable, restricted, blocked, and partial evidence.
6. Preserve evidence provenance and acquisition-time semantics.
7. Distinguish acquisition failure from evidence that a hotel feature is absent.
8. Define conceptual HTTP-first/browser-rendered escalation without locking a technology choice.
9. Define conceptual crawl/request boundaries and minimization principles.
10. Define product-level security boundaries for user-supplied URLs and redirects.
11. Preserve the provenance/truthfulness principles from SPEC-001 and lifecycle semantics from SPEC-003.
12. Provide testable acceptance criteria for a future implementation session.
13. Avoid introducing unapproved application technology or infrastructure decisions.

## Deliverables

Create/update documentation only.

Expected primary deliverable:

`docs/specs/SPEC-004-public-evidence-acquisition.md`

Update `docs/specs/README.md` if required.

Create/update architecture or decision documents only if the requirement reveals a genuine durable decision that belongs there.

## Completion Protocol

When the session completes the work:

1. Update this SAME requirement file.
2. Change status to `PR_READY`.
3. Record branch name.
4. Record PR number/link.
5. Record documents changed.
6. Record decisions made.
7. Record validation performed.
8. Record open questions.
9. Preserve all orchestrator review history added later.

Create branch:

`spec/req-004-public-evidence-acquisition`

Create a PR against `main`.

Do NOT merge.

The orchestrator will review the PR against this exact requirement and may require multiple review rounds.

## Non-Goal

Do not move from specification into crawler or acquisition implementation merely because this specification is complete.

## Session Completion Record

### Status

PR_READY

### Branch

`spec/req-004-public-evidence-acquisition`

### PR

Pending creation on this branch; this section will be completed with the PR number and link before the session stops.

### Documents Changed

- `docs/specs/SPEC-004-public-evidence-acquisition.md` — created the implementation-neutral public evidence acquisition contract, including hospitality-focused scope, first-party acquisition, controlled discovery, HTTP-first/browser-rendered escalation, third-party source boundaries, provenance, access/failure semantics, freshness, URL duplication/canonical handling, security boundaries, minimization, lifecycle integration, and future implementation acceptance criteria.
- `docs/specs/README.md` — indexed SPEC-004.
- `requirements/REQ-004-public-evidence-acquisition-contract.md` — records this session completion and will record the PR details.

### Decisions Made

- Public evidence is limited to information ordinarily observable from public sources without authentication, authorization, circumvention, or evasion of technical restrictions.
- First-party hotel website acquisition is the primary source, with discovery bounded to guest-journey-relevant pages rather than exhaustive domain crawling.
- Ordinary HTTP acquisition is the default when sufficient; browser-rendered observation is a justified fallback for relevant public pages only, under the same access and security boundaries.
- Access restrictions are terminal acquisition boundaries, not triggers for increasingly aggressive retries or bypass techniques.
- Third-party public evidence may be observed when relevant, but its source relationship must remain explicit and it must not silently become first-party hotel fact.
- Retained evidence must preserve source URL, source relationship, acquisition timestamp, method/category, availability status, and observation-versus-derivation semantics.
- Acquisition failure/unavailability must never be interpreted as evidence that a hotel feature is absent.
- Acquisition must be bounded by evaluation scope, request/concurrency/resource controls, redirects, traversal, and minimization principles; exact numeric limits remain future architecture/implementation decisions.
- Externally supplied URLs are an untrusted network boundary; future implementation must prevent private/internal network access, redirect abuse, request amplification, unbounded traversal, and unsafe content execution.
- No separate architecture decision record was created because the work establishes product/domain constraints without selecting implementation technology or infrastructure.

### Open Questions

No material open question remains within the bounded REQ-004 scope. Exact crawl/request limits, robots precedence details, URL canonicalization mechanics, browser technology, network isolation mechanism, retention policy, and implementation architecture remain intentionally deferred to later requirements/decision records.

### Validation Performed

- Inspected the repository and confirmed `main` is the default branch.
- Located and read the current `READY` requirement under `requirements/` and read it completely before making changes.
- Read the referenced `docs/context/project-context.md`, SPEC-001, SPEC-002, SPEC-003, specifications index, architecture concerns, decisions README, requirements workflow, and the requirement itself.
- Confirmed `docs/decisions/` currently contains only its README and no additional decision records requiring review.
- Created only documentation/specification changes; no application, crawler, browser, infrastructure, or acquisition implementation was added.
- Confirmed SPEC-004 addresses all thirteen requirement acceptance criteria, including controlled hotel-site boundaries, hospitality priorities, explicit non-bypass behavior, partial/unavailable evidence, provenance/time semantics, HTTP-first/browser escalation, crawl/request minimization, URL/redirect security, lifecycle integration, and future testability.
- Indexed SPEC-004 in `docs/specs/README.md`.
- No automated application tests were applicable because this session is documentation/specification-only.
- Branch was created from `main` as `spec/req-004-public-evidence-acquisition` and all changes are confined to the required documentation scope.

### Orchestrator Review History

<!-- Orchestrator review rounds are appended below without deleting prior history. -->
