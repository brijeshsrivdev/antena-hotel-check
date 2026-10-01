# SPEC-004 — Public Evidence Acquisition Contract

**Status:** SPECIFIED  
**Version:** 1.0  
**Requirement:** REQ-004  
**Implementation:** Not started

## Purpose

Define the product/domain contract for acquiring publicly accessible evidence for a hotel evaluation after a valid `CanonicalEvaluationRequest` exists and evaluation lifecycle semantics are established.

The governed boundary is:

**CanonicalEvaluationRequest → controlled public evidence acquisition → evidence with provenance → future analysis**

The acquisition capability exists to gather the minimum useful public evidence needed for a hospitality-focused evaluation. It is not a general-purpose crawler, unrestricted internet client, competitor intelligence system, OTA scraper, or mechanism for bypassing access restrictions.

This specification is implementation-neutral. It defines product behavior, boundaries, outcomes, provenance, and testable expectations without selecting HTTP libraries, browser technology, queues, proxy services, cloud infrastructure, storage technology, or other implementation mechanisms.

## Relationship to prior specifications

`SPEC-001` establishes the hospitality-first product boundary, public-access boundary, evidence provenance states, truthfulness requirements, security baseline, and eventual evaluation outcome of an analysis result plus interactive Antena-hosted hotel preview.

`SPEC-002` establishes the accepted `CanonicalEvaluationRequest` and makes clear that a valid website URL is a target candidate rather than proof of hotel identity.

`SPEC-003` establishes the evaluation lifecycle and distinguishes evidence outcomes from capability outcomes and evaluation-level outcomes. Acquisition failures therefore do not automatically fail an evaluation.

This specification refines the public-evidence acquisition capability only. It does not change the prior product outcome or authorize downstream analysis, scoring, AI generation, or preview implementation.

## Product definition of public evidence acquisition

Public evidence is information that the product can lawfully and ordinarily observe from a publicly accessible source without authentication, authorization, circumvention, or evasion of a technical restriction.

For this product, acquisition means a bounded observation operation against an evaluation target or a public source reached through an allowed discovery path, followed by preservation of the observation's provenance and availability status.

Public evidence may include:

- HTML and other publicly retrievable page content;
- publicly accessible metadata useful to the guest journey;
- publicly accessible structured information;
- publicly accessible images or other page assets when they are necessary for the bounded evaluation and can be acquired without bypassing restrictions;
- publicly linked third-party booking, dining, map, or contact destinations, subject to the third-party boundaries below.

Public accessibility is an observation condition, not a statement that Antena has unlimited rights to copy, retain, republish, or redistribute content. Acquisition must minimize unnecessary requests and retain only what is needed for the evaluation and its traceability requirements, subject to later retention and legal/compliance decisions.

A source is not public evidence for this capability merely because a URL is known. Authentication, private-network access, paywalled content, access-control restrictions, CAPTCHA challenges, bot protections, or other technical restrictions remain boundaries that acquisition must not bypass.

## Hospitality acquisition priorities

Acquisition must be driven by the guest journey and evaluation scope rather than by a generic site crawl.

The default evidence priorities are:

1. **Hotel identity and property context** — name, property description, location, address, contact details, and other directly relevant identity information.
2. **Rooms** — room listing pages, room names, descriptions, occupancy information, visible room attributes, and relevant room-detail paths.
3. **Amenities and guest information** — amenities, facilities, services, policies, check-in/out information, and other guest-facing information where publicly available.
4. **Booking discovery** — paths and public links that expose booking actions or booking destinations, without requiring successful completion of a third-party transaction.
5. **Dining** — restaurant, dining, bar, breakfast, or related hotel information where applicable.
6. **Location and contact** — location, directions, contact channels, and publicly linked map/location information.
7. **Guest-facing trust and informational content** — relevant content that helps a guest understand the property and decide whether to continue toward booking.
8. **Supporting technical signals** — only where later analysis specifications identify them as necessary; technical signals must not cause the acquisition capability to become a generic audit crawl.

The acquisition scope should normally stop once the required evidence for the evaluation has been obtained or the bounded discovery budget has been reached. A page is not acquired merely because it is linked if it has no material relationship to the hotel guest journey or a defined evaluation need.

## First-party hotel website acquisition

When the canonical evaluation target is a hotel website, first-party acquisition is the primary evidence source.

The product may conceptually:

- fetch the supplied public website target;
- follow permitted public links that remain within the evaluation's bounded first-party site scope;
- discover relevant pages through normal site navigation, relevant links, and public page metadata;
- identify canonical or equivalent URLs to avoid unnecessary duplicate acquisition;
- use ordinary public HTTP acquisition when it is sufficient;
- use browser-rendered observation only when a relevant public page cannot be meaningfully observed through ordinary acquisition and the page is otherwise within scope.

A website URL passing input validation does not guarantee that it is reachable, belongs to the named hotel, or can be fully observed. Those facts remain acquisition or downstream resolution outcomes.

The acquisition boundary is the hotel/property evaluation, not the whole domain. Brand-wide, corporate, recruitment, unrelated blog, account, or unrelated commercial content should not be traversed simply because it is technically reachable.

## Controlled public-page discovery

Discovery must be relevance- and boundary-driven.

A future implementation must conceptually maintain at least these constraints:

- a starting target derived from the accepted evaluation request;
- an allowed domain/site boundary based on the evaluation target and later identity/resolution rules;
- a bounded set of pages or observations;
- a bounded request rate and concurrency policy;
- a bounded traversal depth or equivalent scope control;
- a termination condition when sufficient evidence has been acquired or the acquisition budget is exhausted;
- duplicate/canonical URL suppression;
- a record of attempted and intentionally skipped relevant sources where that distinction matters to the evaluation.

These are product-level guardrails, not fixed numeric limits. Exact limits require later architecture and operational decisions based on measured behavior, resource constraints, and abuse risk.

The system should prefer targeted discovery of representative guest-facing pages over exhaustive enumeration. For example, after finding a rooms index, it may inspect relevant room-detail pages needed to understand room discoverability and information quality, but it should not recursively crawl every unrelated page on the domain.

Links that lead outside the first-party site are not automatically prohibited, but they enter the third-party/public-source boundary described below and must not silently expand the first-party crawl scope.

## Acquisition methods

Acquisition is conceptually categorized as follows:

### `HTTP_PUBLIC`

Ordinary public HTTP(S) retrieval is used when the relevant content is sufficiently observable without client-side rendering.

### `BROWSER_PUBLIC`

A browser-rendered observation is used for a relevant public page when ordinary HTTP retrieval is insufficient because meaningful guest-facing content or navigation depends on client-side rendering.

Browser rendering does not grant additional access rights. It remains subject to the same public-access, scope, rate, security, and non-bypass constraints as ordinary acquisition.

### `THIRD_PARTY_PUBLIC`

A publicly linked third-party destination is observed only when it is relevant to the hotel evaluation and permitted by the product's source boundary. Its evidence remains attributable to that third party.

### `UNAVAILABLE`

The relevant content could not be observed because it was unavailable, restricted, unsupported, failed, or otherwise outside the allowed acquisition boundary. The reason should be represented using a suitable product-level status/detail.

The exact method names are conceptual categories. Future implementation may use different internal representations while preserving the semantic distinction.

## HTTP-first and browser-rendered escalation

The product principle is **ordinary HTTP acquisition first when sufficient; browser-rendered acquisition only when justified by a relevant public page**.

Browser rendering is justified conceptually when all of the following hold:

1. the page or navigation path is relevant to the hotel evaluation;
2. ordinary public retrieval is insufficient to observe the required guest-facing content or navigation;
3. rendering can occur without bypassing authentication, CAPTCHA, bot protection, paywalls, or other access controls;
4. the additional resource cost remains within the evaluation's bounded acquisition policy.

A browser-rendered attempt must not become an automatic escalation ladder after a block. A blocked HTTP request does not itself authorize browser rendering, repeated retries, alternate clients, proxying, fingerprint changes, or other increasingly aggressive acquisition.

If browser rendering is also unavailable or restricted, the result remains unavailable/limited evidence. The product must not convert the acquisition failure into an assertion that the underlying hotel feature is absent.

## Third-party and publicly linked sources

Third-party public sources may be relevant to a guest journey, for example:

- an externally hosted booking engine;
- a publicly linked restaurant/dining service;
- a map or location destination;
- a publicly linked contact or reservation service.

The product may observe such a source only when it is relevant to the evaluation and within the later-defined acquisition boundary. Third-party observation must remain clearly attributable to the third-party source.

Third-party evidence must never silently become a first-party hotel fact. For example, an externally hosted room or rate page may be evidence that a public booking destination exists, but it does not by itself establish that every displayed rate, availability value, policy, or room attribute is a verified first-party hotel fact.

The acquisition capability does not authorize:

- scraping search engines as a discovery strategy;
- unrestricted OTA crawling;
- bulk collection of competitor properties;
- bypassing third-party restrictions;
- harvesting unrelated third-party content.

Later specifications may define narrowly bounded integrations or source-specific behavior where there is a clear product need.

## Evidence provenance contract

Every retained evidence observation must preserve enough provenance to reconstruct what was observed and under what acquisition conditions.

At minimum, the conceptual provenance record includes:

- **source URL** — the URL actually observed or requested;
- **source/domain relationship** — first-party hotel site, publicly linked third party, or other explicitly categorized relationship when known;
- **acquisition timestamp** — when the observation occurred;
- **acquisition method/category** — such as ordinary public HTTP, browser-rendered public observation, third-party public observation, or unavailable;
- **availability status** — whether the requested observation was available, unavailable, failed, or not attempted;
- **observation/derivation distinction** — whether a later value was directly observed or derived/normalized after acquisition;
- **access limitation** — material restriction or reason that affected what could be observed, when applicable.

The acquisition layer may preserve additional metadata later, but downstream analysis must not lose the distinction between source observation and derived information.

The evidence provenance model remains compatible with `SPEC-001`'s product-level evidence states:

- `DISCOVERED` — directly observed in a public source;
- `NORMALIZED` — transformed into a consistent internal representation;
- `INFERRED` — derived interpretation based on evidence;
- `DEMONSTRATION` — generated content used to make a preview explorable and clearly not verified.

Acquisition primarily produces `DISCOVERED` observations and availability limitations. It must not label a missing observation as `DISCOVERED`, and it must not silently create `INFERRED` or `DEMONSTRATION` content as though acquisition observed it.

## Acquisition time and freshness

Public-web evidence is time-dependent.

Every retained observation must have an acquisition timestamp sufficient to establish when the product observed it. The timestamp describes the observation event; it does not imply that the content remains current afterward.

A future implementation may expose freshness windows or re-acquisition policies, but this specification does not define a numeric freshness threshold. The product must avoid language that implies current truth when the evidence is materially stale or when freshness is unknown.

When multiple observations of the same source are retained, their acquisition times must remain distinguishable. A later observation may supersede a prior observation for a current evaluation view without silently erasing the historical provenance needed to understand what was previously observed.

## URL canonicalization and duplicate handling

The product must conceptually distinguish URL identity from content identity.

For public acquisition:

- URL normalization should remove clearly non-content navigation state where appropriate, consistent with the input contract and later URL policy;
- canonical URL signals published by the source may be used to identify equivalent pages;
- obvious duplicate URLs should not cause repeated acquisition when they resolve to the same relevant public resource;
- redirects must be followed only within the allowed redirect/security policy and must not silently expand the evaluation scope;
- two URLs must not be treated as duplicates solely because they look similar if their content or source relationship may differ.

A source-provided canonical URL is evidence about URL equivalence, not proof that all other URLs or domains in the redirect chain are trusted first-party sources.

Exact canonicalization rules, redirect limits, and storage representations are implementation concerns and must be specified later.

## Access restrictions and non-bypass policy

The product must treat access restrictions as boundaries, not challenges to overcome.

The acquisition system MUST NOT:

- bypass authentication or authorization;
- solve or circumvent CAPTCHAs;
- evade bot-management or anti-automation protections;
- defeat access controls;
- bypass paywalls;
- rotate identities, proxies, fingerprints, or request behavior for the purpose of evading blocking;
- use credentials not supplied and authorized for the evaluation;
- exploit alternate endpoints to obtain content that the public-facing endpoint intentionally restricts;
- continue escalating acquisition merely because an ordinary request was blocked.

If a relevant page is blocked or restricted, the product records the evidence limitation and continues with other permitted evidence where possible.

Robots directives and comparable access signals must be respected as part of the product's public-web access policy. Exact precedence and handling of conflicting signals require later implementation/architecture decisions, but the implementation must not treat a robots restriction as an invitation to circumvent it.

Failure to acquire a page is not evidence that the page, feature, room, amenity, booking option, or other hotel capability does not exist.

## Acquisition outcomes

At the evidence-observation level, the product should distinguish at least:

- **`AVAILABLE`** — the relevant public evidence was successfully observed and retained with provenance;
- **`UNAVAILABLE`** — the evidence could not be obtained or reliably observed because of an access, availability, unsupported-content, or product-boundary limitation;
- **`FAILED`** — an acquisition operation encountered an operational error and could not produce the requested observation;
- **`NOT_ATTEMPTED`** — the product intentionally did not acquire the source because it was outside scope, a prerequisite was missing, or the acquisition budget/boundary prevented it.

A status must not be interpreted as a hotel finding by itself.

Examples:

- `UNAVAILABLE` because a booking engine requires authentication does not mean booking is unavailable to guests;
- `FAILED` because a server timed out does not mean the hotel has no rooms page;
- `NOT_ATTEMPTED` because an unrelated page was outside scope does not mean the page does not exist;
- `AVAILABLE` means the source was observed, not that every fact within it has been independently verified.

## Partial acquisition semantics

An evaluation may acquire some relevant evidence while other evidence remains unavailable or failed.

The acquisition capability should preserve:

- successful observations;
- attempted-but-unavailable observations where material;
- relevant failures and their limitations;
- sources intentionally not attempted when that affects interpretation.

Partial acquisition is not itself an evaluation failure. `SPEC-003` determines how capability-level outcomes contribute to the overall evaluation lifecycle.

Where acquisition coverage is incomplete, downstream analysis must retain enough limitation information to avoid presenting the observed subset as exhaustive evidence about the hotel.

## Security boundaries for externally supplied URLs

The acquisition capability consumes externally supplied or externally derived URLs and therefore must be treated as an untrusted network-access boundary.

Future implementation must prevent the system from becoming an uncontrolled network request mechanism. At product level, the required safeguards include:

### Unsafe URL targets

The product must reject or prevent acquisition of targets that are outside the permitted public-web scope. A syntactically valid HTTP(S) URL is not sufficient authorization to request an arbitrary network destination.

### Internal/private network access

Acquisition must not reach private, local, loopback, link-local, metadata-service, administrative, or otherwise non-public network destinations. This rule applies to the original URL and to destinations reached through redirects or other navigation.

### Redirect abuse

Redirects must remain within the security and acquisition scope. A public hotel URL must not become a mechanism for reaching an unrelated private or restricted destination through a redirect chain.

### Request amplification

A single evaluation request must not expand into an unbounded number of network requests. Page count, traversal, concurrency, redirects, retries, response size, and execution time must be bounded by later implementation policies.

### Unbounded depth/page acquisition

The system must not recursively traverse the internet or a hotel domain without a bounded evaluation scope. Discovery must have explicit stopping conditions.

### Unsafe downloaded content

Externally retrieved content is untrusted input. Future implementation must isolate or safely process HTML, scripts, documents, images, and other content so retrieved material cannot execute with unintended product privileges or compromise evaluation/tenant boundaries.

### Resource exhaustion

Future implementation must constrain response sizes, rendering time, execution time, retries, and other resource consumption so hostile or pathological public content cannot exhaust shared resources.

These are product-level security requirements. Specific mechanisms, network controls, sandboxing, and infrastructure remain future architecture decisions.

## Relationship to evaluation lifecycle

Public evidence acquisition is a bounded capability within the `RUNNING` evaluation state defined by `SPEC-003`.

Conceptually:

```text
ACCEPTED
   |
   v
RUNNING
   |
   +--> target resolution
   |
   +--> controlled public evidence acquisition
   |       |
   |       +--> AVAILABLE observations
   |       +--> UNAVAILABLE observations
   |       +--> FAILED observations
   |       +--> NOT_ATTEMPTED observations
   |
   +--> later normalization / analysis / report / preview capabilities
```

Acquisition may succeed partially while the evaluation continues. An acquisition capability outcome may be `SUCCEEDED`, `PARTIAL`, `UNAVAILABLE`, or `FAILED` according to the lifecycle contract, without automatically deciding the evaluation-level terminal state.

If the target is unresolved or materially mismatched, acquisition must not silently continue against a different property. If the evaluation later becomes `INCOMPLETE`, `UNRESOLVED`, `FAILED`, or `COMPLETED`, the acquisition evidence and limitations remain associated with the relevant evaluation attempt.

A completed evaluation may contain unavailable acquisition results if the overall completion criteria in `SPEC-003` are satisfied and the limitations are truthfully represented.

## Acquisition scope and minimization principles

The product follows these minimization principles:

1. Acquire only evidence relevant to the hotel evaluation.
2. Prefer first-party hotel content when it is sufficient.
3. Prefer direct page retrieval before browser rendering when direct retrieval is sufficient.
4. Prefer representative guest-facing pages over exhaustive traversal.
5. Avoid repeated acquisition of equivalent resources.
6. Stop when required evidence has been obtained or the bounded acquisition policy is exhausted.
7. Do not retry solely to overcome a deliberate access restriction.
8. Keep third-party observations separate from first-party hotel evidence.
9. Retain material limitations so partial acquisition is not misrepresented as exhaustive acquisition.
10. Do not acquire private, authenticated, or otherwise restricted content merely because it would improve analysis completeness.

## Truthfulness and downstream use

Acquired evidence is not automatically a verified hotel fact in every downstream context.

A directly observed first-party statement may become `DISCOVERED` evidence, while normalization, interpretation, or generation remains separately represented. Downstream specifications must preserve the chain from observation to normalized data to finding to preview content.

Where acquisition is incomplete, downstream components must prefer:

- explicit unknown/unavailable states; or
- clearly labeled inference or demonstration content,

over silent invention.

This requirement is especially important because the eventual preview is a guest-explorable product outcome. Public acquisition limitations must not be hidden in a way that makes generated preview content appear verified.

## Non-goals

This specification does not define or implement:

- crawler code;
- Playwright or browser automation code;
- HTTP client libraries;
- queue, workflow, or scheduler infrastructure;
- proxy services;
- CAPTCHA solving or anti-bot bypass;
- search-engine scraping;
- OTA scraping;
- hotel identity resolution algorithms;
- analysis rules or scoring;
- AI/model architecture;
- report UI;
- preview rendering, preview hosting, or preview lifecycle;
- database schema or persistence implementation;
- cloud deployment;
- legal advice or a complete legal/terms-of-service policy.

Those areas require later bounded requirements, specifications, or decision records.

## Testable acceptance criteria for future implementation

A future implementation of this contract is conformant only if automated tests demonstrate at least:

1. public first-party hotel content can be represented as acquired evidence with source URL and acquisition timestamp;
2. first-party and third-party source relationships remain distinguishable;
3. acquisition is bounded to the evaluation target and does not perform unrestricted domain or internet crawling;
4. relevant guest-journey pages are prioritized over unrelated pages according to the defined acquisition boundary;
5. ordinary HTTP acquisition is preferred when it is sufficient;
6. browser-rendered acquisition is used only for a relevant public page when ordinary acquisition is insufficient and the same access restrictions remain enforced;
7. a blocked or restricted page is represented as unavailable/limited rather than triggering bypass behavior;
8. authentication, CAPTCHA, paywall, anti-bot, and access-control restrictions are never bypassed;
9. failure to acquire a page is not converted into evidence that the corresponding hotel feature is absent;
10. successful, unavailable, failed, and not-attempted evidence outcomes are distinguishable;
11. partial acquisition retains successful evidence and material acquisition limitations;
12. each retained observation preserves source URL, source relationship, acquisition timestamp, method/category, availability status, and observation-vs-derivation distinction;
13. acquisition timestamps distinguish observations made at different times;
14. equivalent/canonical URLs are not needlessly acquired repeatedly while materially distinct resources remain distinguishable;
15. redirects cannot escape the permitted public-web/security scope;
16. an externally supplied URL cannot cause access to private, local, loopback, link-local, metadata, or other non-public network targets;
17. request count, traversal, redirect, retry, response-size, and execution-time behavior is bounded by explicit implementation policy;
18. retrieved content is treated as untrusted and cannot execute with unintended product privileges;
19. third-party public evidence is not silently represented as first-party hotel evidence;
20. acquisition does not silently replace an unresolved or mismatched hotel target with another property;
21. acquisition outcomes integrate with `SPEC-003` without making an individual evidence or capability failure automatically equal an evaluation-level failure;
22. acquisition preserves the provenance states and truthfulness requirements established by `SPEC-001`;
23. no test requires a particular programming language, HTTP library, browser, queue, proxy, cloud provider, or datastore;
24. no acquisition test depends on bypassing a real website's access restrictions.

## Future implementation handoff

A later implementation requirement may define the concrete acquisition capability and its tests. That requirement must map concrete operations to the product-level outcomes in this specification and `SPEC-003` while preserving:

- hospitality-focused bounded discovery;
- public-access-only behavior;
- HTTP-first/browser-rendered escalation where justified;
- non-bypass behavior;
- source attribution and acquisition timestamps;
- partial/unavailable evidence semantics;
- URL and network security boundaries;
- truthfulness and provenance requirements.

No crawler or acquisition implementation is authorized merely by the existence of this specification.
