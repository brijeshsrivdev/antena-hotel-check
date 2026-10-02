# SPEC-008 — Interactive Hotel Preview Contract

**Status:** SPECIFIED  
**Version:** 1.0  
**Requirement:** REQ-008  
**Implementation:** Not started

## Purpose

Define the implementation-neutral product/domain contract for the interactive Antena-hosted hotel preview produced as part of a hotel evaluation.

The preview is a core owner-facing outcome of a completed evaluation. It is an explorable representation of the evaluated hotel's guest-facing digital experience, not a screenshot, report-only artifact, generic website generator, or independent source of hotel truth.

The governing product flow remains:

**Hotel input → Evaluation → Evidence → Hospitality analysis → Hotel Experience Model → analysis result + interactive hotel preview**

## Relationship to prior specifications

`SPEC-001` establishes that a completed evaluation produces both an analysis result/report and an interactive Antena-hosted preview, and that verified, inferred, and demonstration information must remain distinguishable.

`SPEC-002` establishes the canonical evaluation request, target-resolution rules, and the prohibition on silently substituting another hotel.

`SPEC-003` establishes evaluations, attempts/runs, terminal outcomes, partial failure, retry semantics, and the rule that `COMPLETED` requires both owner-facing outcomes.

`SPEC-004` establishes controlled public-evidence acquisition, first-party/third-party boundaries, public-access restrictions, and non-bypass behavior.

`SPEC-005` establishes evidence provenance, availability, conflicts, freshness, derivation, source relationships, and traceability.

`SPEC-006` establishes the hospitality analysis and the Discover → Understand → Explore → Trust → Book journey, including findings, limitations, recommendations, and analysis coverage.

`SPEC-007` establishes the Hotel Experience Model as the governed semantic representation consumed by the preview, including presentation-safety requirements.

This specification refines only the interactive preview boundary. It does not redefine upstream evidence, analysis, or Hotel Experience Model semantics.

## Product principles

1. The preview is a product outcome of one specific evaluation attempt/run.
2. The Hotel Experience Model is the preview's governed content source; the preview does not become a second source of truth.
3. Preview rendering must preserve provenance, qualification, applicability, conflict, freshness, and availability semantics from the Hotel Experience Model.
4. Inferred and demonstration content never becomes verified merely because it is rendered.
5. Missing, unavailable, failed, stale, conflicting, or insufficient information must not be silently fabricated.
6. Analysis findings and recommendations are not hotel facts.
7. Third-party destinations remain identifiable as third-party and are never represented as Antena-owned hotel functionality.
8. A technically renderable preview is not necessarily a preview that is ready for owner exploration or sufficient for evaluation completion.
9. Partial preview capability must remain distinguishable from a fully ready preview.
10. The preview must be safe to expose despite untrusted external source content.
11. The preview exists to let an owner explore the guest-facing experience; it is not a replacement for the analysis report.
12. The preview must remain hospitality-specific and must not silently become a generic AI-generated website.

## Preview domain identity

A **preview** is the owner-facing interactive representation produced for a specific hotel evaluation attempt/run.

Conceptually, preview identity includes:

- the parent evaluation identity;
- the specific evaluation attempt/run identity;
- the resolved hotel/property target identity available for that attempt;
- the preview identity, distinct from the hotel identity;
- the preview lifecycle/readiness state;
- the preview capability/completeness state;
- the creation/production context needed to distinguish this preview from later attempts.

The preview must not be identified only by a hotel slug, hotel name, or website URL. Those values can collide across properties or across attempts and are insufficient to establish which evaluation outcome a preview represents.

A later attempt against the same hotel produces a distinct preview outcome. A materially changed target is a new evaluation request under `SPEC-002`, not a mutation of the existing preview's hotel identity.

### Preview identity and historical attempts

A preview identity is stable for its associated attempt/run once created. Re-running an evaluation must not silently mutate an earlier preview so that historical preview content appears to have been produced by the newer attempt.

The product may expose a current/owner-friendly preview URL or alias for the latest eligible preview, but that alias must not change the identity of the underlying preview. If historical previews remain available, each must remain attributable to its own evaluation attempt. If historical previews are not retained, the product must not imply that a current preview represents an earlier attempt.

Exact identifier formats, URL routing, persistence, retention, and hostname technology are implementation decisions and are intentionally not defined here.

## Preview URL semantics

The product must provide a stable owner-facing way to open an eligible preview, potentially using a hotel-oriented hostname such as `<hotel-slug>.antenapro.com`.

At product level:

- the preview URL must resolve to one unambiguous preview identity for the relevant owner-facing context;
- the URL must not silently switch to a different hotel because a slug collides or changes;
- URL/hostname values must not be treated as proof of hotel identity or ownership;
- a preview URL must not expose or authorize access to unrelated evaluations;
- if a human-readable alias changes, the underlying preview identity remains stable;
- URL availability, publication, and historical-retention behavior must follow the preview lifecycle and later hosting decisions.

This contract does not choose subdomains, paths, routing technology, DNS, custom domains, or hosting infrastructure.

## Preview content source and construction boundary

The preview consumes the Hotel Experience Model from `SPEC-007` plus the preview-specific interpretation needed to arrange that model into guest-explorable flows.

The preview may derive presentation structure from:

- hotel/property facts;
- rooms and room details;
- amenities/facilities;
- dining information where applicable;
- location/contact information;
- policies and guest information;
- booking-discovery information;
- evidence qualifications and presentation-safety state;
- analysis findings only where they are used as contextual/diagnostic information and not as hotel facts.

The preview must not directly treat raw external HTML, arbitrary third-party scripts, or untrusted source markup as the authoritative preview content model. Source evidence is input to the governed Hotel Experience Model; the preview consumes the normalized semantic representation and its provenance rather than blindly reproducing source execution/content.

The preview must not alter an upstream fact's provenance in order to make a page complete, attractive, or internally consistent.

## Minimum guest exploration surface

An eligible preview must provide a coherent guest-exploration path covering the core hotel journey. The exact visual design and page technology are intentionally deferred.

### 1. Homepage / property overview

The preview must provide an entry point that identifies the evaluated property using the resolved evaluation target and safe Hotel Experience Model content.

It should allow a guest to reach the main supported hotel information areas without requiring the guest to know internal preview identifiers.

If material property information is unavailable, the preview must communicate the limitation or use clearly marked demonstration content rather than inventing a verified property fact.

### 2. Rooms

Where room information is applicable and sufficiently represented, the preview must provide a room discovery surface and meaningful navigation to individual room details.

Each represented room type remains a distinct semantic entity. Room details must use the evidence-backed values available for that room.

If room information is unavailable, the preview may provide an explicit unavailable/unknown state. It must not invent room names, occupancy, amenities, rates, availability, or policies and present them as verified hotel facts.

### 3. Room details

Where a room is represented, its detail experience should expose the material room information available from the Hotel Experience Model, including applicable description, occupancy, attributes, amenities, and media references where safely available.

A missing field is not permission to fill it with a plausible value. Demonstration content may be used for non-verified presentation only when clearly identified according to the disclosure rules below.

### 4. Amenities and facilities

The preview must provide a discoverable surface for materially represented amenities/facilities where applicable.

The absence of a domain or field must not be presented as proof that the hotel lacks the offering unless the Hotel Experience Model contains an evidence-supported negative fact.

### 5. Location and contact

The preview must expose the location/contact information that can be responsibly established, including relevant address, contact channels, map/direction references, or explicit unknown/unavailable states.

External map/contact destinations must remain identifiable as external destinations when applicable.

### 6. Policies and guest information

The preview should make material observed guest information discoverable, such as check-in/out and relevant stay policies where represented by the Hotel Experience Model.

The preview must not imply that observed public policy text is legally complete or independently validated unless a separate product contract establishes that capability.

### 7. Booking discovery / handoff

Where a booking destination or booking journey entry point is supported by the Hotel Experience Model, the preview must expose a clear booking path consistent with the evidence.

The preview may hand the guest to a first-party or third-party booking destination that is represented by the model. A third-party handoff must be clearly identifiable as external/third-party and must not be represented as an Antena-hosted transaction.

The preview does not guarantee booking completion, rate accuracy, availability, payment processing, or reservation success unless a future explicit product contract provides those capabilities.

Where no trustworthy booking destination is available, the preview must not invent a live booking URL, rate, availability, room inventory, or transaction flow. It should instead represent booking discovery as unavailable/unknown or provide clearly marked demonstration behavior that cannot be mistaken for a real booking capability.

### 8. Dining where applicable

Dining/restaurant/bar/breakfast content should be exposed when the Hotel Experience Model establishes that the domain is applicable and has usable information.

The absence of dining information does not establish that the hotel has no dining offering.

## Optional and conditional surfaces

The preview may include additional hospitality surfaces when supported by the Hotel Experience Model, such as richer media, services, guest instructions, or other relevant property information.

Additional surfaces must remain within the evaluated hotel's guest-facing experience and must not expand into unrelated brand, corporate, recruitment, competitor, or generic content merely because such content exists in source evidence.

A surface is optional because of applicability or evidence availability, not because the preview is allowed to invent a replacement.

## Navigation and guest exploration semantics

The preview should behave as a coherent guest-facing journey rather than a collection of disconnected generated pages.

At minimum:

- the owner can enter through the preview entry point and reach each supported core surface;
- navigation labels and destinations correspond to the actual preview capability available;
- links to unavailable capabilities must communicate the limitation rather than dead-ending silently;
- room listings lead to the correct room details where room details exist;
- booking actions lead either to the represented booking journey or to an explicit unavailable/unsupported state;
- external destinations are distinguishable from internal preview navigation;
- navigation does not expose internal evaluation identifiers or source-internal implementation details as guest-facing content;
- the preview does not silently navigate to a different hotel's preview.

A preview may have fewer optional surfaces than the conceptual full hotel journey when evidence or applicability does not support them. This is a completeness limitation, not permission to fill the missing surface with fabricated hotel facts.

## Provenance and presentation states

The preview must preserve the authoritative provenance vocabulary from `SPEC-005` and `SPEC-007`:

- `DISCOVERED` — directly observed public information;
- `NORMALIZED` — a representation directly supported by discovered evidence without adding unsupported meaning;
- `INFERRED` — interpretation derived from evidence;
- `DEMONSTRATION` — generated content used to make the preview explorable and explicitly non-verified.

Preview presentation may additionally need to communicate qualification/application states such as:

- verified/supported;
- qualified or stale;
- inferred;
- demonstration;
- unknown/not observed;
- unavailable;
- failed;
- not applicable;
- conflicted.

These presentation states do not replace or mutate provenance.

### Verified presentation

A value may be presented as a verified/public hotel fact only when the Hotel Experience Model's presentation-safety contract permits that use. A `DISCOVERED` value or safely derived `NORMALIZED` value may qualify when supporting evidence, source relationship, freshness, and conflict conditions permit.

### Qualified presentation

A value that is useful but materially limited may be shown with a clear qualification, such as stale/possibly stale, source-qualified, conflicting, or otherwise limited. The qualification must be understandable to an owner and must not be hidden by visual presentation.

### Inferred presentation

An inferred value may be shown only when its non-verified nature is clearly communicated. The preview must not use wording, placement, metadata, or visual treatment that reasonably makes an inferred value appear to be a confirmed hotel fact.

### Demonstration presentation

Demonstration content may be used to make an otherwise sparse preview explorable, including layout examples, illustrative descriptions, or other generated content. It must be explicitly and consistently identified as demonstration/generated content and must never be represented as verified hotel information.

Demonstration content must not create a misleading impression that the hotel offers a room, amenity, service, policy, rate, facility, or booking capability that the evaluation did not establish.

### Unknown / unavailable / failed presentation

When the product cannot establish a value, the preview should communicate an explicit unknown, unavailable, or failed state as appropriate. It must not turn inability to observe into a negative hotel fact.

### Conflict presentation

When materially conflicting evidence exists, the preview must not silently present a selected value as universally verified if the conflict makes that presentation misleading. The value may be qualified or the conflict may be surfaced through an appropriate owner-facing disclosure mechanism.

### Not applicable

A domain may be omitted or marked not applicable when the Hotel Experience Model establishes that it does not meaningfully apply. Missing evidence must not be represented as not applicable merely for convenience.

## Disclosure and trust requirements

The preview must give the owner enough information to understand what is verified versus qualified, inferred, demonstration, or unavailable without requiring access to raw source material.

The disclosure mechanism may be a visible label, status indicator, detail/inspection affordance, or another clear owner-facing mechanism. The implementation may choose the interaction, but it must satisfy these semantic requirements:

1. non-verified content is distinguishable from verified content;
2. demonstration content is explicitly identified as demonstration/generated;
3. inferred content is explicitly identified as inferred/estimated/derived rather than confirmed;
4. material qualifications such as conflict or staleness are not hidden when they affect interpretation;
5. the owner can understand why a major content area is unavailable or unknown;
6. analysis recommendations are not presented as hotel facts;
7. third-party booking/contact/map destinations are identifiable as external/third-party;
8. disclosures do not falsely imply that Antena independently verified every statement on the hotel website.

The exact labels, visual design, and interaction patterns are outside this specification.

## Missing, unavailable, failed, stale, and conflicting information

The preview must preserve the semantic distinctions established by `SPEC-005` and `SPEC-007`.

- **Missing/not observed:** the information was not found in the public evidence observed within scope. Do not infer absence.
- **Unavailable:** the relevant source or flow could not be reliably observed. Do not present absence as a fact.
- **Failed:** an acquisition/observation operation failed. Do not turn the operational failure into a hotel deficiency.
- **Stale:** the observation age is material. Do not imply current truth without an appropriate qualification.
- **Conflicting:** sources materially disagree. Preserve the conflict/qualification rather than silently overwriting it.
- **Insufficient:** evidence exists but does not support an unqualified assertion.
- **Not applicable:** the domain does not meaningfully apply based on sufficient contextual evidence.

A preview may use a demonstration representation to keep a page explorable when factual evidence is insufficient, but the demonstration state must remain explicit and the generated value must not be presented as the hotel's verified fact.

## Relationship to analysis findings and recommendations

Analysis findings, severity/importance, coverage statements, and recommendations from `SPEC-006` are separate from Hotel Experience Model facts.

The preview may surface analysis-related context when the product later chooses to do so, but:

- a finding that a page is unclear is not a hotel fact;
- a recommendation to add an amenity description is not evidence that the hotel lacks that amenity;
- an analysis severity is not a hotel attribute;
- analysis coverage is not proof that unobserved content does not exist.

The preview must never convert these artifacts into guest-facing verified hotel content.

## Booking handoff and third-party boundary

Booking is a discovery and handoff capability unless a later explicit specification adds a real booking transaction capability.

For a supported booking handoff:

1. the destination must be traceable to the Hotel Experience Model/evidence;
2. the destination's first-party or third-party relationship must remain known where relevant;
3. the preview must not claim Antena processed or guaranteed the booking when it only redirected/handoff to another service;
4. external navigation must be treated as an explicit trust boundary;
5. the product must not fabricate rates, availability, inventory, payment steps, confirmation numbers, or reservation success;
6. if the destination cannot be established safely, the booking action must be unavailable/qualified rather than invented.

The same boundary applies to other external contact, map, dining, or reservation destinations.

## Preview readiness and completeness

Preview lifecycle/readiness is distinct from technical renderability.

A preview may be technically renderable while still being unsuitable for owner exploration because its target is unresolved, required identity is unsafe, core navigation is broken, provenance disclosures are missing, or the preview does not satisfy the minimum guest-exploration contract.

The product should distinguish at least these preview-level states:

### `PREPARING`

Preview production is still in progress. The preview is not yet an eligible completed-evaluation outcome.

### `READY`

The preview satisfies the minimum guest-exploration contract, has a sufficiently established target, preserves required provenance/truthfulness semantics, has usable navigation across supported core surfaces, and is safe for owner exploration.

`READY` does not mean every hotel fact or every optional page was verified.

### `PARTIAL`

The preview is explorable and materially useful but one or more non-fatal core/optional capabilities are unavailable, incomplete, or qualified. The missing capabilities and limitations are explicit. `PARTIAL` is not equivalent to `READY` for lifecycle completion unless the applicable evaluation completion rules and minimum preview contract establish that the owner-facing preview outcome is still sufficient.

### `UNAVAILABLE`

The preview cannot currently be produced or made safely explorable because a known product/access limitation prevents the required preview outcome.

### `FAILED`

Preview production encountered an evaluation-level or preview-level failure that prevents a trustworthy preview outcome. The failure reason and any useful partial result should be retained according to `SPEC-003`.

A preview may also have a separate completeness description (for example, substantially complete, partially complete, or insufficient coverage) so that readiness is not confused with content coverage. Exact thresholds are not defined here.

## Relationship to evaluation completion

`SPEC-003` remains authoritative for evaluation terminal state.

The preview contributes to completion as follows:

- A `READY` preview can satisfy the preview side of the `COMPLETED` evaluation contract when the other lifecycle conditions are satisfied.
- A `PARTIAL` preview may satisfy the preview side only when it still meets the required owner-facing preview outcome and all material limitations are truthfully surfaced. A later implementation must not assume that any `PARTIAL` preview automatically satisfies completion.
- `PREPARING`, `UNAVAILABLE`, or `FAILED` preview states cannot satisfy the required preview outcome for `COMPLETED`.
- A technically renderable preview that fails the readiness/truthfulness/navigation requirements cannot be treated as the completed preview outcome merely because a URL opens.
- If the report exists but the required preview outcome does not, the evaluation is `INCOMPLETE` unless `SPEC-003` requires `UNRESOLVED` or `FAILED` for the specific cause.
- If the preview exists but the required report outcome does not, the evaluation is likewise not `COMPLETED`.

This contract intentionally does not define a numeric or universal threshold for when a `PARTIAL` preview is sufficient. That threshold must be established by later product/implementation validation without weakening the minimum contract or truthfulness rules.

## Preview failure and partial-capability semantics

Failure of one preview surface does not automatically invalidate all other preview surfaces.

For example, an unavailable third-party booking engine may leave booking handoff unavailable while homepage, rooms, amenities, location, and contact remain explorable. The preview should preserve that limitation rather than failing the entire preview solely because the external booking flow could not be observed.

Conversely, a failure that makes the preview identity unsafe, causes cross-hotel navigation, breaks the core entry/navigation path, or makes provenance/truthfulness disclosures unreliable prevents the preview from being considered `READY`.

The preview should retain capability-level outcomes for its surfaces so owner-facing readiness can be distinguished from individual surface availability.

## Security and trust boundaries

The preview is a security boundary because it exposes content derived from untrusted public sources.

Future implementations must at minimum ensure:

- source HTML, scripts, styles, documents, images, and other external content are treated as untrusted input;
- retrieved source scripts are not executed with Antena/product privileges;
- untrusted source markup cannot inject arbitrary controls, navigation, or content into the preview in a way that bypasses preview safety rules;
- preview content is generated from the governed semantic model rather than blindly embedding arbitrary source pages as trusted application UI;
- external links use only safely validated destinations and supported schemes;
- preview navigation cannot escape its intended evaluation/hotel context through manipulated source content;
- external destinations cannot be used as a path to private/internal product resources;
- owner access to one preview cannot authorize access to another evaluation or hotel;
- preview identifiers and URLs do not act as unrestricted authorization credentials unless a later security decision explicitly establishes and protects that model;
- generated/demo content cannot override provenance or disclosure state;
- resource-heavy or malicious source content cannot exhaust the preview runtime or compromise another evaluation/tenant.

The preview must not execute arbitrary hotel-source JavaScript merely to reproduce a source website. Browser/rendering implementation details are deferred, but the trust boundary is not.

## External navigation safety

All external destinations presented by the preview must be subject to product-level validation before navigation.

At minimum, external navigation must:

- use supported web schemes only;
- preserve the intended destination relationship from the Hotel Experience Model;
- reject or neutralize unsafe schemes or malformed destinations;
- not silently rewrite a destination to an unrelated property or service;
- clearly indicate that the guest is leaving the Antena-hosted preview where applicable;
- avoid treating arbitrary user/source-controlled text as a trusted destination without validation.

Exact allowlisting, redirect handling, interstitials, browser policy, and security mechanisms are implementation decisions.

## Preview content truthfulness rules

The following are prohibited:

- presenting an inferred room, amenity, policy, facility, rate, availability, or booking capability as verified;
- inventing hotel facts solely to fill missing fields;
- presenting recommendations as facts;
- converting a third-party statement into an unqualified first-party hotel fact;
- hiding material evidence conflicts to make the preview appear more complete;
- treating acquisition failure as proof of hotel absence;
- implying a successful booking when only a handoff was possible;
- claiming that Antena independently verified information that it only reproduced from public evidence;
- silently changing the evaluated hotel because a source or URL suggests another property.

## Preview/report relationship

The report and preview are two owner-facing views of the same evaluation, with different purposes:

- the **report** explains evidence, findings, limitations, and recommendations;
- the **preview** lets the owner explore a guest-facing representation of the hotel experience.

They may reference common evidence/model entities, but neither may mutate the other's provenance or completion state.

A preview can make a finding tangible by exposing a related guest-facing flow, but the finding remains an analysis artifact. A report recommendation must not be implemented in the preview as though the recommendation were an observed hotel fact.

## Preview capability outcome model

For each core/optional preview surface, the product should distinguish at least:

- **AVAILABLE** — the surface can be explored using supported preview content;
- **PARTIAL** — some relevant content/interaction is available but material limitations remain;
- **UNAVAILABLE** — the surface cannot be safely or reliably provided because the necessary evidence/capability is unavailable;
- **FAILED** — production of the surface encountered an operational failure;
- **NOT_APPLICABLE** — the surface does not meaningfully apply to the evaluated property based on sufficient context.

These surface outcomes must not be confused with provenance states or evaluation-level lifecycle states.

## Implementation-oriented acceptance criteria

A future implementation is conformant only if automated tests demonstrate at least:

1. a preview is associated with exactly one evaluation attempt/run and cannot silently switch to another target;
2. re-running the same target creates a distinguishable preview outcome rather than mutating the previous attempt's preview;
3. a preview URL/alias resolves unambiguously to its intended preview context and does not permit cross-hotel/evaluation confusion;
4. the minimum guest exploration surface includes property overview, rooms/room details where applicable, amenities/facilities, location/contact, policies/guest information, and booking discovery/handoff semantics, with dining available when applicable;
5. optional surfaces can be omitted because they are not applicable or not responsibly supported, without implying hotel absence;
6. supported navigation connects the entry point to the available core surfaces and does not silently dead-end or switch hotels;
7. room types remain distinct and room details are tied to the correct room entity;
8. preview content is sourced from the Hotel Experience Model and preserves provenance/qualification rather than inventing or upgrading facts;
9. `DISCOVERED`, `NORMALIZED`, `INFERRED`, and `DEMONSTRATION` remain distinguishable in preview content decisions;
10. inferred content cannot be rendered as an unqualified verified hotel fact;
11. demonstration content is explicitly disclosed and cannot be mistaken for verified hotel information;
12. missing, unavailable, failed, stale, insufficient, and conflicting information remain distinguishable in preview behavior;
13. an unavailable/failed source is not converted into a negative hotel fact;
14. analysis findings and recommendations cannot be rendered as hotel facts;
15. third-party booking, map, dining, contact, or reservation destinations remain identifiable as third-party/external where relevant;
16. booking behavior never fabricates rates, availability, inventory, payment, confirmation, or success when only a handoff is supported;
17. external navigation rejects unsafe or unsupported destination schemes and cannot be manipulated into private/internal product destinations;
18. untrusted source scripts/markup are not executed or treated as trusted Antena UI content;
19. preview content cannot cross evaluation/hotel boundaries through source-controlled links or content;
20. preview readiness is not inferred solely from technical renderability;
21. `PREPARING`, `READY`, `PARTIAL`, `UNAVAILABLE`, and `FAILED` preview states remain distinguishable;
22. a preview that lacks required identity, navigation, provenance/disclosure safety, or other minimum readiness conditions is not treated as `READY` merely because its URL responds;
23. a `READY` preview can participate in the `COMPLETED` evaluation contract, while `PREPARING`, `UNAVAILABLE`, or `FAILED` cannot;
24. `PARTIAL` preview semantics are explicit and do not automatically imply evaluation completion;
25. preview surface outcomes are distinguishable from preview lifecycle state and evaluation lifecycle state;
26. preview history remains attributable to the correct attempt/run when later attempts are produced;
27. the implementation does not require a particular frontend framework, backend framework, database, browser, renderer, hosting platform, domain/DNS solution, AI provider, or component library.

## Edge cases

The implementation must have defined behavior for at least these cases:

- hotel target is unresolved or identity-mismatched before preview production;
- two evaluation attempts exist for the same hotel;
- a room listing is available but individual room details are unavailable;
- room details conflict across public sources;
- a booking link is present but the third-party destination is inaccessible;
- a booking destination is available but rates/availability cannot be verified;
- a hotel has no observed dining information;
- an amenity is explicitly stated as unavailable versus simply not observed;
- source content contains unsafe scripts, markup, or external links;
- an external destination redirects outside the expected third-party context;
- evidence is materially stale;
- a demonstration page contains generated content that could be mistaken for a real hotel offering;
- preview rendering succeeds while provenance/disclosure checks fail;
- one preview surface fails while the rest remain usable;
- a human-readable preview slug collides or changes;
- a source suggests a different hotel than the resolved evaluation target.

## Explicit non-scope

This specification does not implement application code and does not select or lock:

- frontend/backend frameworks;
- programming languages/runtime;
- database/persistence technology;
- hosting/cloud/CDN infrastructure;
- domain/DNS/routing implementation;
- rendering/browser technology;
- AI provider/model or generation architecture;
- template/component library;
- booking/OTA integration technology;
- authentication/tenancy implementation.

It also does not define:

- visual design or a final design system;
- final UI copy or branding;
- a detailed screen-by-screen design;
- public-evidence acquisition behavior beyond the upstream contracts;
- evidence provenance semantics beyond the upstream contracts;
- hospitality analysis rules beyond the upstream contracts;
- the Hotel Experience Model semantics beyond `SPEC-007`.

## Future implementation handoff

A later implementation requirement may define preview-model assembly, rendering, hosting, navigation, disclosure UI, and preview lifecycle mechanics. That implementation must satisfy this contract and preserve the upstream invariants in `SPEC-001` through `SPEC-007`.
