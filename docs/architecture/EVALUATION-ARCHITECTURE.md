# Evaluation Architecture Contract

**Status:** SPECIFIED  
**Version:** 1.0  
**Requirement:** REQ-009  
**Implementation:** Not started

## Purpose

Define the first implementation-neutral architecture contract for Antena Hotel Check. It connects the approved product specifications without selecting programming language, framework, database, queue, browser, cloud, hosting, renderer, or AI provider.

Governing flow:

**Hotel input → Evaluation → Evidence acquisition → Evidence model → Hospitality analysis → Hotel Experience Model → analysis result + interactive hotel preview**

The architecture protects the central outcome: an owner submits a hotel name + city or website URL, receives a hospitality-specific evaluation, and can explore the resulting Antena-hosted hotel preview.

## Architectural principles

1. **Evaluation is the system-level unit.** Input, evidence, analysis, Hotel Experience Model, report, preview, limitations, and attempt/run history remain attributable to one evaluation context.
2. **Attempts are traceable.** A retry/re-run is a distinguishable attempt and never silently mutates historical outcomes.
3. **External content is untrusted.** User URLs, public hotel content, third-party content, and generated/demo content cross explicit trust boundaries.
4. **Provenance survives transformation.** Acquisition, normalization, inference, analysis, report assembly, and preview production preserve the evidence chain.
5. **Analysis is not hotel fact.** Findings and recommendations cannot silently become Hotel Experience Model facts.
6. **The Hotel Experience Model is the governed preview source.** Preview production does not execute or blindly reproduce arbitrary source content.
7. **Report and preview are distinct outcomes.** They may share governed data, but neither substitutes for or mutates the other.
8. **Partial failure is explicit.** Evidence/check/capability failure does not automatically become evaluation failure or a negative hotel fact.
9. **Security is architectural.** Intake, external acquisition, processing, persistence, and owner-facing serving each require explicit controls.
10. **Technology choices remain open.** A material choice that closes an intentional seam requires a later decision record.

## Conceptual components and responsibilities

### Evaluation Intake

Accepts the modes defined by `SPEC-002`, validates/normalizes them, creates or requests an evaluation context, and rejects invalid input before downstream work. User input is untrusted.

### Evaluation Orchestrator

Owns `SPEC-003` lifecycle state, attempt/run creation, coordination of bounded capabilities, aggregation of capability/check outcomes, terminal evaluation outcome, and retry/re-run relationships. It does not own the internal algorithm of each capability.

### Target Resolution Boundary

Establishes a sufficiently identified hotel/property after input validation and produces resolved, ambiguous, mismatch, or unresolved outcomes. It must never silently substitute another hotel.

### Public Evidence Acquisition

Per `SPEC-004`, acquires only bounded public evidence relevant to the hotel journey; preserves source relationship, timestamp, method, availability, limitations, and provenance; and never bypasses authentication, CAPTCHAs, access controls, paywalls, or anti-bot restrictions. This is an explicit untrusted external-network boundary, not an unrestricted crawler/client.

### Evidence Model / Normalization

Per `SPEC-005`, retains observations and evidence items, derivation relationships, `DISCOVERED`/`NORMALIZED`/`INFERRED`/`DEMONSTRATION` provenance, availability, conflicts, freshness, and attempt/run attribution. Normalization must not upgrade truth status.

### Hospitality Analysis

Per `SPEC-006`, evaluates **Discover → Understand → Explore → Trust → Book**, producing findings, limitations, coverage, and recommendations grounded in evidence. Technical/SEO/accessibility signals remain supporting dimensions.

### Hotel Experience Model

Per `SPEC-007`, constructs the governed hotel-facing representation for property, location/contact, rooms, amenities, dining, policies/guest information, and booking as applicable. It preserves provenance, qualification, conflict, freshness, applicability, and presentation safety.

### Report Assembly

Produces the owner-facing analysis result from governed findings/evidence/model context while preserving observation, inference, limitation, and recommendation distinctions. It does not become the source of hotel truth.

### Interactive Preview Production

Per `SPEC-008`, creates the owner-explorable preview from the Hotel Experience Model, preserves presentation safety, supports the required guest journey surfaces and external handoffs, and reports preview readiness and surface outcomes.

### Preview Serving Boundary

Exposes an eligible preview through a stable owner-facing identity/URL, isolates one evaluation from another, and enforces safe external navigation. Exact hosting, routing, DNS, CDN, and rendering technology remain open.

## Conceptual data/control flow

```text
User
  |
  v
Evaluation Intake
  |
  v
Evaluation Orchestrator
  |
  +--> Target Resolution --> RESOLVED ------------------+
  |                         AMBIGUOUS/MISMATCH/UNRESOLVED |
  |                                                      |
  +------------------------------------------------------+ 
                                                         v
                                          Public Evidence Acquisition
                                                         |
                                                         v
                                          Evidence Model / Normalization
                                                         |
                                  +----------------------+------------------+
                                  |                                         |
                                  v                                         v
                         Hospitality Analysis                    Hotel Experience Model
                                  |                                         |
                                  +----------------------+------------------+
                                                         |
                                  +----------------------+------------------+
                                  |                                         |
                                  v                                         v
                           Report Assembly                    Preview Production
                                  |                                         |
                                  v                                         v
                          Owner-facing report                Preview Serving Boundary
                                                                        |
                                                                        v
                                                                  Owner explores
```

The diagram is conceptual. Components may be combined or split later provided the semantic boundaries and traceability remain intact.

## Evaluation and attempt/run ownership

One accepted `CanonicalEvaluationRequest` establishes the evaluation context. An evaluation may contain multiple attempts/runs. Each attempt owns its acquisition observations, capability/check outcomes, report outcome, and preview outcome. Downstream artifacts retain the attempt/run relationship. A retry does not erase prior evidence, limitations, or terminal outcomes. A material change to hotel identity or canonical website target is a new evaluation request under `SPEC-002`.

## Responsibility boundaries

| Boundary | Owns | Must not own |
| --- | --- | --- |
| Intake | validation and canonical request | resolution, crawling, analysis, preview |
| Orchestrator | lifecycle and attempts | capability internals |
| Target resolution | property identity outcome | silent substitution |
| Acquisition | bounded public observations | analysis conclusions, unrestricted crawling |
| Evidence model | provenance, conflicts, freshness | hotel quality judgments |
| Analysis | guest-journey findings/recommendations | mutation of hotel facts |
| Experience model | governed hotel-facing facts | UI rendering, recommendations as facts |
| Report | owner-facing explanation | rewriting provenance/facts |
| Preview | guest-explorable representation/readiness | second source of truth |
| Preview serving | safe exposure/isolation | access to unrelated evaluations |

## Execution model: synchronous versus asynchronous

The architecture does not choose a queue or workflow technology, but it establishes a conceptual split.

### Request-path work

Input validation/normalization, construction of an accepted canonical request, immediate invalid-input rejection, and evaluation acceptance/acknowledgement should be suitable for an immediate request/response boundary.

### Potentially asynchronous work

Target resolution when external discovery is required, public evidence acquisition, evidence normalization/derivation, analysis, report assembly, Hotel Experience Model construction, preview production, and preview publication/serving preparation must be architecturally separable from the initiating request because they may involve network latency, browser rendering, multiple pages, retries, and partial failures.

The complete evaluation must not depend on fitting within one request timeout or transaction. Progress/capability outcomes remain observable, while `SPEC-003` lifecycle state remains authoritative. Cancellation, worker allocation, queue, workflow, and delivery mechanisms are later decisions.

## Failure, partial completion, retry, and idempotency

- A capability may succeed, be partial, unavailable, or fail without automatically failing the evaluation.
- `FAILED` is reserved for an evaluation-level failure that prevents a trustworthy outcome or meaningful continuation.
- Ambiguous, mismatched, or unresolved targets stop downstream processing rather than permitting guessed hotel substitution.
- Partial evidence/analysis remains usable when trustworthy; limitations remain visible.
- A retry creates a distinguishable attempt/run and preserves historical outcomes.
- Duplicate initiation must not combine unrelated evaluations or mutate a completed historical result.
- The semantic idempotency boundary is the accepted canonical request/evaluation context. Exact idempotency keys, locking, concurrency, deduplication, and persistence mechanisms remain implementation decisions.

## Provenance and transformation boundary

The architecture preserves:

```text
source observation
      ↓
evidence item
      ↓
normalized / inferred representation
      ↓
analysis finding and/or Hotel Experience Model fact
      ↓
report content and/or preview content
```

Transformations create related representations rather than silently rewriting observations. The four authoritative provenance states remain distinguishable. Unavailable/failed acquisition is a limitation, not absence. Recommendations are not hotel facts. Preview rendering cannot upgrade provenance. Historical observations remain attributable to their original attempt/run.

## Security and trust boundaries

### User input → intake

Validate untrusted fields, URLs, and abusive requests. Later authentication/authorization and abuse controls must not be bypassed by the architecture.

### Resolution/acquisition → public network

Treat evaluation URLs as untrusted network targets. Enforce `SPEC-004` public-web boundaries: no private/internal/loopback/link-local/metadata access, redirect escape, unbounded traversal, request amplification, or unsafe resource consumption.

### External source → acquisition/evidence

Remote HTML, scripts, documents, images, metadata, and third-party content are data, not trusted application authority. Retrieved scripts must not execute with product privileges.

### Evidence → processing

Parsing/normalization/analysis must treat evidence as data. Source text or markup must never become an instruction to expand scope, access secrets, or bypass security policy.

### Model → report/preview

Only governed semantic representations cross into owner-facing outputs. Raw external source execution is outside the preview trust model.

### Preview → external destination

Booking/map/dining/contact/reservation destinations must be validated, remain identifiable as external, and never provide a path to private/internal resources or unrelated evaluations.

### Evaluation/preview isolation

One evaluation/attempt's artifacts must not be addressable or mutable through another context. Human-readable preview URLs are identifiers, not unrestricted authorization credentials unless a later security decision explicitly establishes and protects that model.

Authentication, authorization, tenant isolation, secrets, sandboxing, content-security controls, network isolation, and abuse/rate-limit mechanisms are implementation concerns, not selected here.

## Data ownership and lifecycle

- Intake owns accepted input and normalization context.
- Orchestrator owns lifecycle and attempt/run state.
- Acquisition owns source-observation and acquisition-outcome semantics for its operation.
- Evidence Model owns provenance relationships and evidence semantics.
- Analysis owns findings, coverage, limitations, and recommendations.
- Hotel Experience Model owns the governed hotel-facing semantic representation.
- Report owns its owner-facing representation derived from governed evaluation data.
- Preview owns preview-specific presentation/readiness state and serving identity, not the truth of hotel facts.

Historical evidence, attempt outcomes, and provenance must survive long enough to explain the owner-facing result under a later retention policy. No component becomes authoritative merely because it persists or renders data.

## Observability and audit requirements

An evaluation must be explainable after execution. Traceability must cover at least:

- evaluation identity and canonical target context;
- attempt/run identity and timestamps;
- lifecycle transitions;
- capability/check outcomes;
- acquisition source references, methods, timestamps, and limitations;
- evidence derivation/provenance relationships;
- material conflicts and freshness qualifications;
- analysis findings and supporting evidence;
- report outcome;
- preview outcome/readiness and relevant external handoffs;
- terminal failure reasons and retry relationships.

Auditability must allow the system to answer: what target was processed, which attempt produced the result, what was observed/blocked, which findings came from which evidence, which report/preview outcomes were produced, and why the evaluation became completed/incomplete/unresolved/failed. Exact logging/metrics/tracing technology remains open.

## State ownership

- Evaluation state: orchestrator/lifecycle boundary.
- Capability state: owning capability plus orchestrator-visible outcome.
- Evidence observation state: acquisition/evidence boundary.
- Evidence provenance: evidence model.
- Analysis coverage/finding state: analysis boundary.
- Hotel-fact presentation safety: Hotel Experience Model.
- Preview readiness/surface outcomes: preview boundary.
- Owner-facing publication/access state: preview serving boundary.

Downstream presentation components must not mutate upstream lifecycle state because their own output failed.

## Extension seams

1. Intake/API transport and authentication.
2. Orchestration mode (synchronous/asynchronous/workflow/job).
3. Acquisition implementation (HTTP/browser behind the public-evidence contract).
4. Evidence persistence/content retention.
5. Analysis implementation (deterministic rules/heuristics or later model-assisted interpretation within the analysis contract).
6. Report rendering.
7. Preview model assembly/rendering/serving.
8. Preview hostname/routing.
9. Observability implementation.

Closing a seam with a material technology choice requires a later decision record with alternatives, consequences, and impacted specifications.

## Architecture acceptance criteria

A future implementation must demonstrate that:

1. invalid input can be rejected without full evaluation execution;
2. an accepted request has a traceable evaluation context;
3. lifecycle state is distinct from capability/check outcomes;
4. attempts/runs are distinguishable and downstream artifacts retain their relationship;
5. unresolved/mismatched targets cannot continue against a substitute hotel;
6. acquisition is bounded and cannot become unrestricted crawling;
7. external URLs cannot reach private/internal network targets directly or through redirects;
8. untrusted source content cannot execute with product privileges;
9. provenance survives normalization, analysis, report assembly, and preview production;
10. `DISCOVERED`, `NORMALIZED`, `INFERRED`, and `DEMONSTRATION` remain distinguishable;
11. unavailable/failed evidence is not transformed into a negative hotel fact;
12. findings/recommendations remain distinct from Hotel Experience Model facts;
13. report and preview are independent outcomes of the same evaluation;
14. preview content comes from the governed Hotel Experience Model rather than raw source execution;
15. preview readiness is distinct from technical renderability;
16. external preview destinations are validated and identifiable as external;
17. retries preserve prior attempt history;
18. observability can explain target, attempt, evidence, limitations, findings, report, preview, and terminal outcome;
19. ownership boundaries prevent downstream mutation of upstream truth/lifecycle state;
20. component technologies can be replaced without changing approved product semantics;
21. no acceptance test requires a specific language, framework, database, queue, browser, cloud, renderer, hosting platform, or AI provider.

## Important edge cases

The architecture must explicitly handle: invalid input; valid URL with unresolved identity; multiple plausible hotels; hotel/website mismatch; blocked important pages; inaccessible third-party booking; partial acquisition with useful analysis; conflicting source facts; stale observations across attempts; transient retry; duplicate initiation; partially generated preview; renderable-but-not-ready preview; unsafe source links/redirects/scripts; multiple previews for one hotel; and preview slug collision/change.

## Technology decision boundary

No programming language/runtime, frontend/backend framework, database, queue/workflow system, browser automation technology, HTTP client, cloud topology, object storage, CDN/DNS, authentication provider, renderer, AI provider/model, or deployment topology is selected here. These remain **PROPOSED / OPEN** until a later bounded decision is supported by implementation constraints and operational evidence.

No ADR is created by REQ-009 because no material technology choice needs to be closed to satisfy this architecture contract.

## Future implementation handoff

Each later implementation requirement must identify the architecture boundaries it implements, preserve attempt/run and provenance traceability, preserve public-web/security constraints, provide tests for normal and key failure paths, avoid unapproved technology choices, and update durable repository context when an architecture boundary or decision materially changes.
