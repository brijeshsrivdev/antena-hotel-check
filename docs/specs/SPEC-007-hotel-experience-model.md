# SPEC-007 — Hotel Experience Model

**Status:** SPECIFIED  
**Version:** 1.0  
**Requirement:** REQ-007  
**Implementation:** Not started

## Purpose

Define the durable, implementation-neutral product/domain contract for the normalized hotel experience representation used between evidence/hospitality analysis and the eventual owner-facing report and interactive Antena-hosted hotel preview.

The governing product flow remains:

**Hotel input → Evaluation → Evidence → Hospitality analysis → Analysis result + interactive hotel preview**

The Hotel Experience Model represents the guest-facing hotel experience that can be responsibly established from an evaluation. It is not a generic website content model and is not a generic AI website-generation schema.

## Relationship to prior specifications

This specification depends on and refines the following contracts without changing them:

- `SPEC-001` — hospitality-first product boundary, guest journey, public-access boundary, evidence states, and truthful report + preview outcome.
- `SPEC-002` — canonical evaluation input and unresolved/mismatch semantics.
- `SPEC-003` — evaluations, attempts/runs, terminal outcomes, partial failure, and repeatability.
- `SPEC-004` — controlled public evidence acquisition, source relationships, acquisition timestamps, access limitations, and non-bypass behavior.
- `SPEC-005` — evidence identity, provenance, availability, conflicts, freshness, derivation, and traceability.
- `SPEC-006` — hospitality analysis dimensions, guest-journey findings, limitations, recommendations, coverage, and preview/report relationships.

The authoritative provenance vocabulary remains `DISCOVERED`, `NORMALIZED`, `INFERRED`, and `DEMONSTRATION`. This specification does not add another provenance state. Other status concepts below describe applicability, observation availability, qualification, and presentation safety; they are not replacements for provenance.

## Core product principles

1. The model represents the guest-facing hotel experience discovered and qualified during an evaluation.
2. Every material hotel-facing value remains traceable to supporting evidence or explicitly identified as inferred/demonstration content.
3. Provenance is preserved rather than overwritten by normalization, inference, conflict resolution, or presentation.
4. Missing, unavailable, failed, stale, insufficient, and conflicting information remain distinguishable from a verified negative hotel fact.
5. Optional hospitality domains are represented only when applicable or evidenced; their absence from the model does not imply that the hotel lacks that offering.
6. Analysis findings and recommendations are not hotel facts and must not be inserted into the model as discovered content.
7. Demonstration content may make a preview explorable, but it remains explicitly non-verified.
8. A downstream consumer can determine whether a value is safe to present as a verified public hotel fact.
9. The same governed representation supports both report assembly and interactive preview consumption.
10. The model does not silently select a truth when evidence conflicts.

## Model boundary

A **Hotel Experience Model** is the governed representation of one evaluated hotel's guest-facing experience for a specific evaluation attempt/run.

Conceptually it contains:

```text
Hotel Experience Model
├── evaluation / attempt identity
├── property identity/context
├── hospitality domains
│   ├── property
│   ├── location & contact
│   ├── rooms
│   ├── amenities & facilities
│   ├── dining (optional)
│   ├── policies & guest information
│   └── booking
├── facts / fields within those domains
├── evidence/provenance references
├── qualification / conflict / freshness context
├── applicability and observation state
└── presentation-safety determination
```

This is a semantic contract only. It does not prescribe Java classes, TypeScript interfaces, database tables, JSON schemas, API endpoints, persistence technology, or rendering technology.

## Semantic hospitality domains

The model defines semantic domains rather than requiring a fixed UI or persistence structure.

### Property

Identity and basic positioning where supportable: property name, description, positioning/property type where directly supported, guest-facing introductory information, and relevant identity context. Identity must not be silently substituted when the evaluation target is unresolved or mismatched.

### Location and Contact

Address, locality/city/region where observed, phone/contact methods, email/contact paths, map/directions references, and useful location context.

### Rooms

Room identity/name, description, occupancy, room attributes, room amenities, room-detail relationships, publicly observable images/media references, and other room information needed to understand the offering. Multiple room types must be representable without collapsing them into one generic room fact.

### Amenities and Facilities

Guest-facing facilities, services, and amenities. The model distinguishes an observed statement that a feature is absent from an inability to observe information about that feature.

### Dining (optional)

Restaurant, dining, bar, breakfast, or related hotel information where applicable and publicly observed. Absence of this domain does not establish that the property has no dining offering.

### Policies and Guest Information

Check-in/check-out information, relevant stay policies, guest instructions, cancellation/payment information where publicly observable, and other material guest information. The model represents what the evaluation can responsibly establish; it does not establish legal validity or completeness of a hotel's policies.

### Booking

Booking call-to-action presence, booking destination/reference, booking-engine handoff, booking journey entry point, limitations on third-party booking observation, and other booking-discovery information. A third-party booking destination remains explicitly third-party and does not silently become first-party hotel content.

## Domain optionality and applicability

The model supports different hotel offerings without treating a fixed domain set as mandatory.

For each domain or field, downstream consumers must be able to distinguish at least:

- **applicable and observed/represented**;
- **not applicable** — does not meaningfully apply based on sufficient contextual evidence;
- **not observed / missing** — not found in observed public evidence, without establishing absence;
- **unavailable** — relevant evidence could not be reliably observed;
- **failed** — an acquisition/observation attempt failed;
- **inferred** — a value is an interpretation rather than a directly observed hotel statement;
- **demonstration** — content exists only to support an explorable demonstration.

`NOT_APPLICABLE` must not be used merely because information is missing. Where an explicit negative hotel fact is supported, the model may represent an observed absence, retaining evidence and provenance supporting the negative assertion. Failure to observe a feature is never sufficient by itself.

## Fact representation semantics

Each material fact/field conceptually supports these dimensions:

### Semantic identity

A stable semantic identity such as domain + field + relevant subject/entity. For example, a room amenity belongs to a particular room entity rather than to the hotel globally. Semantic identity is independent of source URLs so multiple observations can describe the same conceptual fact without losing source provenance.

### Value/content

A concrete value, structured value, reference, or explicit absence/unknown state as appropriate. A value must not be fabricated merely to make a field non-empty.

### Provenance state

One authoritative state from `SPEC-005`: `DISCOVERED`, `NORMALIZED`, `INFERRED`, or `DEMONSTRATION`.

### Supporting evidence

Every material evidence-dependent fact references one or more supporting evidence items/observations. A derived fact preserves the evidence chain. A demonstration fact may reference motivating evidence/context, but that does not make it verified.

### Qualification

Material limitations such as stale/possibly stale observation, incomplete evidence, source relationship limitations, material conflict, indirect/inferred interpretation, unavailable source portions, or other conditions affecting presentation. Qualification does not erase provenance.

### Conflict state

The fact can indicate that materially conflicting observations exist and retain links to the conflicting evidence rather than replacing them with a single unqualified value.

### Freshness/observation context

Relevant observation timestamp(s) from `SPEC-005`, distinct from later normalization/inference time. Where age is material, the fact remains qualified rather than silently presented as current.

### Applicability/absence semantics

The model distinguishes a verified value, a verified observed absence when supported, not applicable, not observed/missing, unavailable, failed, inferred, and demonstration.

## Provenance and derivation rules

The Hotel Experience Model inherits the derivation rules of `SPEC-005`.

```text
DISCOVERED observation
      ├──> NORMALIZED fact
      └──> INFERRED fact

DISCOVERED / NORMALIZED / INFERRED evidence
      ├──> INFERRED fact
      └──> DEMONSTRATION content
```

Rules:

1. Creating a normalized, inferred, or demonstration representation never mutates supporting evidence into that new provenance state.
2. Normalization must not add unsupported factual meaning.
3. An inferred value remains inferred even when later evidence happens to agree with it; later evidence remains separate evidence.
4. Demonstration content remains demonstration content after rendering in the interactive preview.
5. A fact may reference multiple evidence items.
6. A fact derived from conflicting evidence preserves the conflict and qualification.
7. A downstream consumer must not infer `DISCOVERED` merely because a value has a source URL; provenance and evidence relationships are authoritative.

## Source relationships

Downstream consumers must distinguish, where relevant:

- first-party hotel source;
- third-party public source;
- user-provided information, if later supported;
- derived information;
- demonstration/generated information.

A third-party source may establish a fact about the existence or behavior of that third-party destination, but it does not automatically establish a first-party hotel fact. User-provided information is not public discovery merely because it is useful or supplied by the hotel owner.

## Missing, unavailable, failed, and insufficient information

The model preserves these cases distinctly.

### Known/observed

Relevant evidence was successfully observed and supports the represented value or explicit observed absence.

### Not applicable

The field/domain does not meaningfully apply based on sufficient contextual evidence; retain the reason when needed to prevent ambiguity.

### Missing / not observed

The relevant information was not found in public evidence observed within scope. This is an unknown/coverage state, not proof of absence. For example, if no room amenity information was found, the model must not create `air_conditioning = false` merely because the field is empty.

### Unavailable

The relevant source or flow could not be reliably observed because of an access restriction, unsupported behavior, public-web limitation, or other acquisition limitation.

### Failed

An acquisition or observation operation encountered an operational failure. The failure is evidence about the observation process, not evidence that the hotel lacks the feature.

### Insufficient / qualified

Some evidence exists but is not sufficient for an unqualified assertion because it is indirect, stale, incomplete, conflicting, or otherwise materially limited.

### Inferred

The value is derived from evidence rather than directly stated/observed.

### Demonstration

The value was created for demonstration or preview exploration and is not verified hotel information.

These conditions may coexist with the authoritative provenance state where appropriate. For example, a `NORMALIZED` value can be qualified because its supporting observations conflict, while `DEMONSTRATION` is inherently non-verified.

## Negative facts and absence

A negative hotel fact is stronger than “not observed.” The model may represent a negative fact only when supporting evidence actually establishes the absence or non-availability being asserted, such as an explicit first-party statement that a service is not offered.

The following must not be converted into a negative fact:

- missing page;
- unavailable page;
- failed request;
- not-attempted source;
- incomplete acquisition;
- absence of a field from the normalized model;
- absence of an expected navigation link when the relevant source could not be reliably observed.

## Conflicts and representative values

Conflicting observations are first-class model context.

When multiple sources provide different values for the same conceptual fact:

1. retain each material supporting observation/evidence item;
2. represent the conceptual fact as conflicted/qualified when the disagreement matters;
3. do not silently discard the conflict;
4. permit a representative/selected value only when a downstream use genuinely requires one;
5. make the selected value traceable to its supporting evidence;
6. preserve references to material conflicting evidence;
7. record enough qualification/rationale for a consumer to understand that the selection is not an unqualified universal truth.

This specification does not establish a universal source-priority rule. Source relationship, recency, directness, and relevance may be considered by a later analysis decision, but the underlying evidence and conflict remain preserved. If no justified representative value can be selected, consumers should use an explicit conflict/unknown state rather than inventing a value.

## Freshness and staleness

A fact must not be presented as current merely because it has a value. Where freshness is material, consumers must be able to see the observation timestamp, whether evidence is sufficiently current when such a determination exists, whether it is stale/possibly stale, and whether freshness is unknown.

No universal numeric freshness threshold is established here. A later observation may be preferred for a current view without deleting or rewriting the historical observation.

## Presentation safety contract

The model must expose enough information for a downstream consumer to determine whether a fact is safe to present as a **verified public hotel fact**. Presentation safety is a governed determination based on provenance, evidence, source relationship, availability, qualification, and conflict state; it is not equivalent to “has a value.”

A fact is eligible for unqualified verified presentation only when all applicable conditions are satisfied, including:

1. it is `DISCOVERED`, or is a `NORMALIZED` representation whose factual meaning is directly and traceably supported by discovered evidence;
2. supporting evidence is available/usable for the claim;
3. the source relationship is acceptable for the particular claim;
4. no material unresolved conflict or other qualification makes the assertion misleading;
5. freshness is adequate where freshness materially matters, or the fact is explicitly qualified rather than presented as current;
6. the value does not depend on unsupported inference or demonstration content.

An `INFERRED` fact is not eligible for unqualified verified presentation. A `DEMONSTRATION` fact is never eligible for verified presentation. A `NORMALIZED` fact with unresolved conflict or material uncertainty is not eligible merely because it is `NORMALIZED`.

When a value is not safe for verified presentation, the consumer must receive a reason/qualification such as inference, demonstration, unavailable evidence, conflict, staleness, or insufficient evidence.

This is a product-level safety contract, not a prescribed boolean field or implementation algorithm. An implementation may represent the determination differently provided the same semantic decision is possible.

## Relationship to hospitality analysis

`SPEC-006` findings, limitations, coverage states, and recommendations remain analysis artifacts. They are not silently converted into Hotel Experience Model facts.

Examples:

- an observed room description can become a modelled hotel fact;
- a finding that the room description is unclear remains an analysis finding;
- a recommendation to improve the room page remains a recommendation;
- an inability to verify a room amenity remains a limitation/unknown condition;
- an inferred interpretation remains explicitly inferred.

The model may reference analysis findings for traceability, but a finding or recommendation never acquires hotel-fact status merely because it is linked to a modelled field.

## Relationship to report consumers

The report may consume the model to communicate verified/observed hotel information, normalized representations and their evidence, missing/unavailable information, conflicts and qualifications, freshness limitations, and relationships between hotel information and guest-journey findings.

Report consumers must distinguish facts from findings, limitations, and recommendations. Demonstration content must not be reported as verified hotel information.

## Relationship to interactive preview consumers

The preview may consume the same model. It must be able to distinguish at least:

- verified/publicly observed information;
- qualified or inferred information;
- demonstration/generated information;
- unavailable or unknown information.

The preview must not fill an unknown hotel fact with invented content and render it as verified. Where demonstration content makes a page explorable, its demonstration status remains explicit and distinguishable according to later preview specifications.

## Relationship between report and preview

The report and preview consume the same governed hotel experience representation but serve different purposes.

```text
                    ┌──> Owner-facing report
Evidence → Experience Model
                    └──> Interactive hotel preview
```

The model is the shared semantic boundary. Report presentation and preview rendering must not mutate or upgrade hotel fact provenance.

## Model integrity across evaluation attempts

The model is associated with the relevant evaluation attempt/run. A later attempt must not silently reuse an earlier observation as though it occurred during the later attempt. Reused historical evidence remains attributable to its original observation context.

If a re-run produces a different room description, address, amenity, policy, or booking destination, the new observation and resulting modelled representation preserve the new observation context while retaining earlier provenance history.

A materially changed hotel target is a new evaluation request under `SPEC-002`, not a silent mutation of the existing hotel's experience model.

## Traceability requirements

For every material modelled value, a downstream consumer must be able to traverse, directly or through explicit relationships, a chain equivalent to:

```text
Hotel Experience fact
      ↓
provenance / qualification
      ↓
supporting evidence item(s)
      ↓
source observation(s)
      ↓
source + acquisition context
```

For derived values, the chain also exposes the derivation relationship. For conflicts, it exposes material alternatives. For demonstration content, it preserves demonstration status and may expose motivating context without treating it as proof.

A value that cannot maintain a meaningful evidence/provenance relationship must not be represented as verified evidence-backed hotel fact.

## Non-scope

This specification does not define or implement:

- crawler/browser implementation;
- scraping or public-evidence acquisition algorithms;
- AI providers, prompts, or model architecture;
- database schema or persistence technology;
- Java/TypeScript classes or framework choices;
- API contracts/endpoints;
- report UI/frontend;
- interactive preview UI, rendering, runtime, hosting, or domain routing;
- booking or OTA integrations;
- scoring formulas or rankings;
- pricing/monetization;
- production infrastructure;
- universal source-priority rules;
- universal freshness thresholds;
- hotel quality/reputation judgments beyond supported evidence.

## Testable acceptance criteria for future implementation

A future implementation is conformant only if automated tests demonstrate at least:

1. the model represents the defined hospitality domains without requiring every domain for every hotel;
2. property, location/contact, rooms, amenities/facilities, optional dining, policies/guest information, and booking information can be represented semantically;
3. multiple room entities/types can be represented without collapsing their distinct facts;
4. material facts retain provenance and supporting evidence references;
5. `DISCOVERED`, `NORMALIZED`, `INFERRED`, and `DEMONSTRATION` remain distinguishable;
6. normalized, inferred, and demonstration representations do not mutate supporting evidence into another provenance state;
7. first-party, third-party, user-provided, derived, and demonstration source relationships remain distinguishable where relevant;
8. missing/not-observed information is distinct from an observed negative fact;
9. unavailable and failed evidence is distinct from verified absence;
10. `NOT_APPLICABLE` is not used merely because evidence is missing;
11. a supported negative fact retains evidence establishing the absence rather than relying on an empty field;
12. conflicting observations can coexist and remain traceable without silent overwriting;
13. a representative value selected despite conflict retains supporting evidence, conflicting alternatives, and qualification/rationale;
14. no universal source-priority rule is required by the model;
15. stale or freshness-unknown information can remain qualified without being silently presented as current;
16. downstream consumers can determine whether a value is safe to present as a verified public hotel fact;
17. `INFERRED` and `DEMONSTRATION` information cannot be presented as unqualified verified hotel fact;
18. material unavailable, insufficient, conflict, inference, demonstration, and freshness limitations remain visible to consumers;
19. analysis findings and recommendations remain distinct from hotel facts represented by the model;
20. the same governed representation can support report and interactive-preview consumers;
21. demonstration content cannot become verified merely because it is rendered in the preview;
22. material facts remain traceable to source observations and acquisition context through explicit evidence relationships;
23. evidence and modelled facts from different evaluation attempts do not silently merge into one observation context;
24. no implementation requires a particular programming language, framework, database, API shape, AI provider, browser, cloud provider, or hosting technology.

## Future implementation handoff

Later requirements may define concrete normalization, model persistence, report assembly, preview generation, and preview rendering. Those implementations must preserve the domain semantics in this specification and the upstream contracts in `SPEC-001` through `SPEC-006`.
