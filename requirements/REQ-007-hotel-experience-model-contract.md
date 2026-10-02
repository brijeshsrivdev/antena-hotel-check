# REQ-007 — Hotel Experience Model Contract

**Status:** READY  
**Owner:** Next implementation/specification session  
**Type:** Specification

## Objective

Define the durable product/domain contract for the normalized hotel experience representation that sits between evidence/analysis and the eventual owner-facing report + interactive hotel preview.

The contract must establish what hotel-facing information can be represented, how provenance and confidence/qualification are preserved, how missing/conflicting information is handled, and how the same governed representation can support both analysis/reporting and the interactive preview without inventing hotel facts.

The central product flow remains:

**Hotel input → Evaluation → Evidence → Hospitality analysis → Analysis result + interactive hotel preview**

## Repository Context to Read

Before starting, read:

- `docs/context/project-context.md`
- `docs/specs/SPEC-001-hotel-check.md`
- `docs/specs/SPEC-002-evaluation-input.md`
- `docs/specs/SPEC-003-evaluation-lifecycle.md`
- `docs/specs/SPEC-004-public-evidence-acquisition.md`
- `docs/specs/SPEC-005-evidence-model.md`
- `docs/specs/SPEC-006-hospitality-analysis.md`
- relevant architecture and decision documents
- this requirement file completely

## Scope

Define an implementation-neutral **Hotel Experience Model** contract covering at minimum:

1. The major guest-facing hotel information domains needed by the product, such as:
   - hotel/property identity
   - description/positioning
   - location/contact
   - rooms and room details
   - amenities/facilities
   - dining where applicable
   - policies/guest information where observable
   - booking entry points/journey references
2. How modelled information is linked to supporting evidence and provenance.
3. How `DISCOVERED`, `NORMALIZED`, `INFERRED`, and `DEMONSTRATION` information remains distinguishable.
4. How missing, unavailable, failed, stale, insufficient, and conflicting information is represented without silently inventing values.
5. How individual modelled facts/fields can retain source/evidence traceability.
6. How multiple observations of the same hotel fact are reconciled without destroying underlying evidence or conflicts.
7. How the model distinguishes verified public hotel information from qualified/inferred information and demonstration content.
8. How the model can be consumed by both:
   - the analysis/report path, and
   - the interactive hotel preview path.
9. How the model handles optional hospitality areas without implying that every hotel must have every feature.
10. How downstream consumers can determine whether a value is safe to present as verified hotel fact.

## Core Product Principle

The Hotel Experience Model is **not** a generic website content model and is not a generic AI website-generation schema.

It exists to represent the guest-facing hotel experience discovered and qualified during an evaluation.

The model must never silently convert:

- missing evidence → invented hotel fact
- inference → verified fact
- demonstration content → discovered hotel content
- conflicting observations → silently selected truth

## Provenance Contract

Use the evidence contract from `SPEC-005` and analysis contract from `SPEC-006`.

The model must preserve enough provenance for a downstream consumer to determine, for each material hotel-facing value:

- where it came from;
- whether it was directly discovered, normalized, inferred, or demonstration-only;
- whether the underlying evidence is currently available;
- whether the value is qualified because of conflict, staleness, or incomplete evidence;
- whether it is safe to present as verified public hotel information.

Do not invent a new provenance vocabulary that conflicts with the established evidence model.

## Fact and Field Semantics

Define product-level semantics for a modelled hotel fact/value, including as appropriate:

- value/content;
- semantic field/domain;
- provenance state;
- supporting evidence reference(s);
- qualification/limitations;
- conflict state where material;
- freshness/observation context where relevant;
- applicability/absence semantics.

This is a domain contract only. Do not prescribe Java classes, TypeScript interfaces, database tables, JSON schema, or persistence technology.

## Missing and Unavailable Information

The model must distinguish at least:

- known/observed information;
- information that is not applicable;
- information that is missing from observed public evidence;
- information that could not be verified because acquisition/access failed;
- information that is inferred or qualified;
- information intentionally created only as demonstration content.

A missing field must not automatically mean that the hotel does not have the underlying feature.

For example, inability to observe a room amenity must not become a statement that the room lacks that amenity.

## Conflicts and Multiple Sources

When multiple sources provide different values for the same conceptual hotel fact:

- preserve the underlying evidence relationships;
- do not silently discard conflicting observations;
- allow a downstream consumer to understand that a conflict exists;
- define product-level semantics for a selected/representative value only if the selection remains traceable and qualified.

Do not introduce a universal source-priority rule unless justified by an explicit later architecture/product decision.

## Hospitality Domains

The specification should define the semantic domains needed by the eventual preview and report, without requiring every hotel to populate every domain.

At minimum consider:

### Property

Identity, name, description, positioning, property type where supportable.

### Location and Contact

Address, locality, contact methods, map/directions references, and useful location context where observed.

### Rooms

Room names, descriptions, occupancy/attributes, amenities, images/media references where publicly observable, and room-detail relationships.

### Amenities and Facilities

Guest-facing facilities and services with provenance and qualification.

### Dining

Restaurant/dining information where applicable and publicly observed.

### Policies and Guest Information

Check-in/check-out, relevant policies, and other guest-facing operational information where observable.

### Booking

Booking calls to action, booking destinations, relevant booking journey references, and limitations on third-party flows.

These domains are semantic categories, not a required UI or persistence shape.

## Optionality and Applicability

The model must support properties with different hospitality offerings.

Examples:

- a hotel may have no publicly discoverable restaurant;
- a property may have multiple room types;
- a hotel may expose booking through a third-party engine;
- some information may simply be unavailable during evaluation.

The absence of a domain or value must not be represented in a way that implies a verified negative fact unless the evidence supports that conclusion.

## Relationship to Analysis

`SPEC-006` findings may reference modelled hotel information, evidence limitations, or guest-journey observations.

The model must support the analysis/report path without allowing analysis recommendations to become hotel facts.

For example:

- an observed room description may be represented as hotel information;
- a finding that the room information is unclear remains an analysis finding;
- a recommendation to improve the room page remains a recommendation;
- none of those recommendations may be inserted into the model as discovered hotel content.

## Relationship to Interactive Preview

The model is intended to provide governed input to the future interactive hotel preview.

The preview must be able to distinguish at least:

- verified/discovered hotel information;
- qualified or inferred information;
- demonstration/generated information;
- unavailable information.

The preview must not fill missing hotel facts with invented content and present them as verified hotel information.

Demonstration content may exist to make the preview explorable, but its demonstration status must remain explicit and distinguishable from public hotel facts.

This requirement does not define preview pages, rendering technology, hosting, domain routing, visual design, or interaction mechanics.

## Relationship to Report

The report may consume the same Hotel Experience Model to explain:

- what information was discovered;
- what information could not be verified;
- what conflicts or qualifications exist;
- how the guest journey is represented;
- which findings/recommendations relate to particular hotel information.

The report must not reinterpret demonstration content as verified hotel facts.

Report UI and presentation are outside this requirement.

## Non-Scope

Do NOT implement or specify in detail:

- crawler/browser implementation;
- scraping infrastructure;
- acquisition algorithms;
- AI providers/prompts/model architecture;
- database schema or Java/TypeScript classes;
- API contracts or endpoint design;
- report UI/frontend;
- interactive preview UI/runtime/hosting;
- domain routing;
- booking/OTA integrations;
- pricing/monetization;
- scoring formulas;
- production infrastructure;
- visual design system.

Those belong to later bounded requirements/specifications/architecture decisions.

## Acceptance Criteria

The specification must make it possible for a future implementation session to test that:

1. A normalized hotel experience representation has clearly defined hospitality domains.
2. Material modelled values can retain provenance and supporting evidence references.
3. `DISCOVERED`, `NORMALIZED`, `INFERRED`, and `DEMONSTRATION` remain distinguishable.
4. Missing or unavailable information is not silently converted into a negative hotel fact.
5. Conflicting observations can be retained and traced without silently destroying the conflict.
6. Stale/qualified information can remain qualified for downstream consumers.
7. Optional hospitality domains do not imply that every hotel must provide every feature.
8. Downstream consumers can determine whether a value is safe to present as verified hotel information.
9. Analysis findings and recommendations remain distinguishable from hotel facts represented by the model.
10. The same governed representation can support both report and interactive-preview consumers.
11. Demonstration/generated information cannot silently appear as discovered hotel information.
12. The model remains compatible with the completed-evaluation requirement that the eventual outcome includes both useful analysis/report and interactive preview.
13. No implementation requires a particular programming language, framework, database, AI provider, browser, cloud provider, or hosting technology.

## Deliverables

Create/update documentation only.

At minimum create:

`docs/specs/SPEC-007-hotel-experience-model.md`

Update the specification index if required.

If a genuine architectural/product decision is discovered, document it separately rather than hiding it inside the specification.

## Git Workflow

**BRANCH:**

`spec/hotel-experience-model`

Create the branch from the current `main`.

Create a PR against `main`.

Do not merge.

Before stopping, update this SAME requirement file with:

- session name/identifier
- branch
- PR number/link
- files changed
- summary of work
- validation performed
- open questions

The orchestrator will review the PR against this requirement.

## Review Protocol

If the orchestrator records review findings in this file or the PR conversation, address those findings in the same requirement file and preserve the review history.

Do not mark the requirement `APPROVED`; only the orchestrator can do that.

## Session Completion Record

Not started.

## Orchestrator Review History

<!-- Orchestrator review rounds are appended below without deleting prior history. -->
