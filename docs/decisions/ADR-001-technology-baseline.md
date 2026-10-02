# ADR-001 — Technology Baseline

**Status:** ACCEPTED FOR INITIAL IMPLEMENTATION  
**Date:** 2026-10-02  
**Decision type:** Technology baseline  
**Scope:** Initial implementation slices of Antena Hotel Check

## Context

Antena Hotel Check is a hospitality-specific digital experience analyzer. The approved product flow is:

**Hotel input → Evaluation → Evidence acquisition → Evidence model → Hospitality analysis → Hotel Experience Model → analysis result + interactive hotel preview**

A completed evaluation must provide both an owner-facing analysis result/report and an interactive Antena-hosted hotel preview. The repository contracts require bounded public-web evidence acquisition, explicit provenance, attempt/run traceability, partial-failure handling, and a strong separation between verified hotel facts, inferred information, and demonstration content.

The architecture deliberately leaves many operational choices open. This ADR closes only the material technology choices needed to begin implementation without locking the product into premature production infrastructure.

## Decision

### 1. Backend

Use **Java 21 LTS with Spring Boot 3.x** for the backend/application services.

The backend is the primary owner of evaluation lifecycle orchestration, domain logic, evidence/provenance handling, hospitality analysis, Hotel Experience Model construction, report/preview preparation, and security-sensitive server-side boundaries.

Spring Boot is the application framework baseline; individual capabilities should remain behind the architectural boundaries in `EVALUATION-ARCHITECTURE.md` rather than becoming framework-coupled monolithically.

### 2. Owner-facing application and preview

Use **TypeScript with Next.js** for the owner-facing web application and the interactive hotel preview surface.

Next.js is the baseline web framework for the initial owner-facing experience and preview serving/presentation layer. The preview consumes the governed Hotel Experience Model and must not execute arbitrary source-site HTML/scripts as its content model.

The frontend is a presentation/serving boundary, not the system of record for hotel facts or evaluation state.

### 3. Primary persistence

Use **PostgreSQL** as the primary durable relational datastore.

PostgreSQL will be the initial persistence baseline for evaluation context, attempts/runs, evidence metadata/provenance relationships, analysis outcomes, Hotel Experience Model data, preview metadata, and other transactional domain state required by implementation slices.

This ADR does not prescribe the final schema, retention policy, raw-content storage strategy, indexing strategy, or every persistence technology used for derived/static assets.

### 4. Initial execution model

Use a **hybrid execution model**:

- **Synchronous request path:** input validation/normalization, canonical request creation/acceptance, and immediate invalid-input responses.
- **Asynchronous execution path:** target resolution when external discovery is required, public evidence acquisition, normalization/derivation, analysis, report assembly, Hotel Experience Model construction, preview production, and preview publication preparation.

The first implementation slice may use a simple application-managed asynchronous execution mechanism where appropriate, provided the domain boundaries and lifecycle semantics do not depend on that mechanism. A production queue/workflow platform is intentionally deferred.

### 5. Public-evidence acquisition technology boundary

Adopt an **HTTP-first, browser-escalation** acquisition boundary:

- Ordinary public HTTP(S) retrieval is the default for relevant evidence.
- The backend may use a standards-compliant HTTP client for bounded retrieval.
- Browser-rendered observation is permitted only for a relevant public page where ordinary HTTP retrieval is insufficient and rendering does not bypass authentication, CAPTCHA, bot protection, paywalls, or other access controls.
- A browser automation implementation should use **Playwright** as the initial browser-automation baseline when browser rendering is required.
- Acquisition remains bounded by the approved public-evidence contract: target scope, request/concurrency/resource limits, redirect/security policy, provenance, timestamps, and explicit unavailable/failed outcomes.
- Search-engine scraping, unrestricted crawling, proxy/fingerprint rotation for evasion, credential use not explicitly authorized for the evaluation, and other bypass mechanisms are outside the baseline.

Playwright is an implementation choice for the browser-rendering seam, not permission to escalate aggressively after a block. A blocked HTTP request does not by itself justify browser rendering or repeated acquisition attempts.

### 6. Test and development tooling baseline

Use the following minimum tooling baseline:

- **Backend:** Maven, JUnit 5, and Mockito for unit/domain/service tests.
- **Persistence/integration:** Testcontainers with PostgreSQL for tests that require real database behavior.
- **Frontend:** TypeScript type-checking plus the project's Next.js-compatible unit/component test tooling; use Playwright for browser-level acceptance tests where an end-to-end guest journey is being validated.
- **Static quality:** compiler/type checks and repository-native formatting/lint checks where configured.
- **Documentation/decision changes:** Markdown review against the requirement acceptance criteria; no application test suite is required for documentation-only ADR work.

Tests should emphasize domain invariants and failure semantics, including provenance preservation, unresolved targets, partial acquisition, retries, preview readiness, and isolation boundaries, rather than only happy-path HTTP tests.

### 7. Local development and repository structure

The initial implementation should remain a small, separable repository structure aligned with the approved architecture. At minimum, implementation work should keep clear boundaries for:

- backend/application services;
- owner-facing web application/preview;
- test suites;
- durable specifications, architecture, and decision records.

The initial baseline should support local PostgreSQL through a reproducible development/test setup, preferably containerized, without requiring production cloud services for ordinary domain development.

Exact module names, build layout, Docker Compose usage, environment-variable conventions, and deployment topology are implementation details and may be established by the first implementation-slice specifications.

## Why this baseline fits the product

### Java 21 + Spring Boot

The product has a domain-heavy evaluation lifecycle with explicit state transitions, provenance relationships, security-sensitive external acquisition, and multiple capability boundaries. Java 21 provides a long-lived LTS runtime, while Spring Boot provides a mature service/application foundation without requiring the initial implementation to couple domain semantics to a particular cloud provider.

The choice also keeps orchestration, transactional persistence, validation, and security concerns in one strongly typed backend boundary while leaving acquisition, analysis, and preview capabilities replaceable behind interfaces.

### Next.js + TypeScript

The product requires an owner-facing web application and a genuinely interactive hotel preview. Next.js provides a single web framework for application UI and preview serving/presentation concerns, while TypeScript supports a shared, explicit representation of presentation state such as verified, qualified, inferred, demonstration, unavailable, and conflicted content.

The decision does not make Next.js the source of hotel truth; it remains downstream of the Hotel Experience Model.

### PostgreSQL

The approved architecture has strongly relational concepts: evaluations, attempts/runs, lifecycle states, evidence/provenance relationships, findings, conflicts, hotel-experience facts, and preview identities. PostgreSQL provides transactional consistency and relational querying without requiring a separate database technology for the initial implementation.

A relational baseline also supports explicit foreign-key relationships and historical attempt/run traceability. JSON/JSONB can be used selectively where a later specification establishes genuinely variable content, without turning the entire domain into an unstructured document store.

### Hybrid execution

A fully synchronous design conflicts with public network latency, browser rendering, retries, partial failure, and preview generation. A fully asynchronous intake path would unnecessarily complicate immediate validation and acceptance. Hybrid execution preserves a simple request boundary while allowing the evaluation itself to run independently of one request timeout.

### HTTP-first with bounded Playwright escalation

Most public hotel information should be obtainable without browser automation, making HTTP-first acquisition cheaper and easier to bound. Some hotel sites depend on client-side rendering, so the architecture needs a legitimate browser-rendering seam. Playwright provides a concrete implementation baseline for that seam while the public-evidence contract prevents it from becoming an evasion mechanism.

## Alternatives considered

### Backend alternatives

| Alternative | Consideration | Outcome |
| --- | --- | --- |
| Java 21 + Spring Boot | Strong domain typing, mature service ecosystem, good transactional/security support, practical for a small backend team | **Selected** |
| Node.js + TypeScript backend | Strong web ecosystem and language alignment with frontend; less attractive for the initial domain/service boundary given the repository's backend complexity and need for explicit domain separation | Rejected for baseline |
| Python + FastAPI | Strong scraping/data/AI ecosystem and fast experimentation; would introduce a different primary application runtime for the core domain and persistence lifecycle | Rejected for baseline; may still be used later for isolated specialized tooling if justified |
| Kotlin + Spring Boot | Good JVM/domain fit, but adds a second language decision without a material product requirement | Deferred/rejected for baseline |

### Frontend alternatives

| Alternative | Consideration | Outcome |
| --- | --- | --- |
| Next.js + TypeScript | Supports owner application plus interactive preview in one web framework and has a mature TypeScript ecosystem | **Selected** |
| React with a separate SPA framework | Flexible, but would require additional framework/routing/serving decisions without a current product need | Rejected for baseline |
| Angular | Capable enterprise framework, but would add a different frontend model without a requirement-driven advantage for this product | Rejected for baseline |

### Persistence alternatives

| Alternative | Consideration | Outcome |
| --- | --- | --- |
| PostgreSQL | Strong relational integrity, transactions, flexible JSON support, mature ecosystem, good fit for traceable evaluation/run relationships | **Selected** |
| MongoDB/document database | Flexible evidence documents, but weaker fit for the many explicit relationships and transactional lifecycle semantics in the approved domain | Rejected for primary persistence |
| Cloud-managed proprietary relational database | Could reduce operations later, but prematurely couples the foundation to a provider/service topology | Deferred; PostgreSQL remains the technology baseline |

### Execution alternatives

| Alternative | Consideration | Outcome |
| --- | --- | --- |
| Fully synchronous | Simple initially, but unsuitable for network acquisition, browser rendering, retries, and multi-stage evaluation completion | Rejected |
| Fully asynchronous from intake | Scales long-running work, but unnecessarily complicates immediate validation and acceptance | Rejected |
| Hybrid | Keeps intake simple and moves long-running work off the request path while preserving capability/lifecycle boundaries | **Selected** |

### Public-evidence acquisition alternatives

| Alternative | Consideration | Outcome |
| --- | --- | --- |
| HTTP-only | Simple and efficient, but cannot reliably observe some relevant client-rendered hotel experiences | Rejected as the complete baseline |
| Browser-first | Broad observation capability, but materially more expensive and operationally complex for routine public pages | Rejected |
| HTTP-first + justified browser escalation | Efficient default with a controlled rendering seam for genuinely relevant client-rendered pages | **Selected** |
| Unrestricted crawler/proxy/evasion approach | Conflicts directly with the public-access and non-bypass contracts | Rejected |

### Test tooling alternatives

| Alternative | Consideration | Outcome |
| --- | --- | --- |
| Unit tests only | Fast, but insufficient for database behavior and real guest-journey/browser integration | Rejected as the complete baseline |
| Heavy end-to-end-only testing | Expensive and slow; weak at pinpointing domain invariant failures | Rejected |
| Unit + integration/Testcontainers + targeted browser acceptance | Balances domain coverage, real persistence behavior, and critical guest-journey verification | **Selected** |

## Consequences

### Positive consequences

- The first implementation team has concrete language/framework/datastore choices without deciding the entire production topology.
- The backend and frontend boundaries map cleanly to the approved evaluation architecture.
- PostgreSQL supports traceable evaluation/attempt/evidence relationships from the start.
- Hybrid execution leaves room for retries and partial failures without making the initial request path complex.
- HTTP-first acquisition limits unnecessary browser cost while preserving a browser seam for JavaScript-heavy hotel sites.
- The technology choices are broadly portable and do not require a specific cloud vendor.
- Automated testing can cover domain behavior, persistence, and critical interactive journeys.

### Negative consequences / trade-offs

- Two primary application languages are required: Java and TypeScript.
- Playwright introduces browser binaries and additional local/CI resource requirements for the browser-rendered acquisition and end-to-end test seams.
- Spring Boot and Next.js create two application runtimes that need coordinated local development and deployment later.
- PostgreSQL schema evolution and evidence relationships require deliberate modeling; the team cannot rely on schemaless storage to postpone every domain decision.
- Asynchronous execution will eventually require durable job/queue/workflow behavior if evaluation volume or reliability requirements exceed a simple initial mechanism.

## Replaceability and migration implications

The baseline intentionally preserves seams so that technology can change without changing product semantics:

- Backend domain contracts must not expose Spring-specific types as the only representation of core product concepts.
- Acquisition must sit behind the public-evidence acquisition boundary so the HTTP client or Playwright can be replaced.
- Persistence access should keep domain semantics separate from PostgreSQL-specific persistence details where practical.
- Next.js components must consume governed preview data rather than own hotel truth.
- The asynchronous execution mechanism must implement the evaluation lifecycle rather than redefine it.
- Browser automation must remain an implementation of the `BROWSER_PUBLIC` acquisition category and must not leak into product semantics.

A future replacement of any selected technology should preserve the approved specifications and create a new ADR when the replacement materially changes architecture or operational assumptions.

## Explicitly deferred decisions

The following remain **OPEN / DEFERRED** and require later bounded decisions when implementation evidence makes them necessary:

- exact Spring Boot minor/patch version and dependency versions;
- exact Next.js/TypeScript and Node.js version policy;
- exact PostgreSQL version and schema design;
- exact HTTP client library and parsing libraries;
- exact Playwright browser/version and browser-isolation mechanism;
- queue/workflow/job technology and worker topology;
- production cloud provider/service topology;
- production CDN, DNS, hostname routing, and custom-domain behavior;
- object storage and static/media asset strategy;
- production browser isolation/sandboxing infrastructure;
- authentication and authorization provider;
- tenancy model and production access-control implementation;
- exact report rendering engine;
- exact preview rendering/serving deployment model;
- AI provider, model, prompts, and model-assisted analysis strategy;
- retention periods and numeric operational limits;
- production observability/logging/metrics/tracing vendor;
- production rate limiting and abuse-prevention infrastructure;
- deployment and CI/CD topology.

## Impacted repository contracts

This ADR implements the technology-decision boundary left open by:

- `docs/context/project-context.md`
- `docs/specs/SPEC-001-hotel-check.md`
- `docs/specs/SPEC-002-evaluation-input.md`
- `docs/specs/SPEC-003-evaluation-lifecycle.md`
- `docs/specs/SPEC-004-public-evidence-acquisition.md`
- `docs/specs/SPEC-005-evidence-model.md`
- `docs/specs/SPEC-006-hospitality-analysis.md`
- `docs/specs/SPEC-007-hotel-experience-model.md`
- `docs/specs/SPEC-008-interactive-preview.md`
- `docs/architecture/EVALUATION-ARCHITECTURE.md`
- `docs/architecture/architecture-concerns.md`

No approved product behavior in those contracts is changed by this ADR.

## Implementation handoff

Future implementation requirements may now select concrete libraries, module boundaries, schemas, queues, deployment components, and operational controls within this baseline. Any choice that materially closes a deferred seam or changes an approved product/architecture contract must be recorded in a later decision record and reviewed by the orchestrator.
