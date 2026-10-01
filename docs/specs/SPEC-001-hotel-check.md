# SPEC-001 — Hotel Check Product Specification

**Status:** SPECIFIED  
**Version:** 1.1  
**Implementation:** Not started

## Purpose

Define the first coherent product contract without prematurely selecting implementation technologies.

## Problem

Hotel owners need to understand whether their public digital presence supports a real guest journey from discovery through booking. A technical audit alone does not answer this hospitality question.

## Product outcome

A **completed hotel evaluation** produces two owner-facing outcomes together:

1. an analysis result/report; and
2. an interactive Antena-hosted hotel preview that the owner can explore as a guest.

The intended product flow is:

**Hotel input → analysis → analysis result + interactive hotel preview → owner explores preview**

The preview is therefore a core outcome of an evaluation, not a separate marketing artifact or a later optional product concept. The detailed preview generation, content model, runtime, and hosting behavior remain future specifications and are not implemented by SPEC-001.

## Inputs

The product must support:

### Hotel identity
- hotel name;
- city.

### Website URL
- hotel website URL.

Identity resolution, source strategy, and conflict resolution require later decisions.

## Evidence model

Each finding should eventually be traceable to observable evidence. Conceptual states:

- **DISCOVERED** — directly observed in a public source.
- **NORMALIZED** — transformed into a consistent internal representation.
- **INFERRED** — derived interpretation based on evidence.
- **DEMONSTRATION** — generated content used to make a preview explorable and clearly not verified.

Exact schemas and confidence/provenance rules are later decisions.

## Hotel analysis

Hospitality-first analysis may cover:
- website reachability and critical guest-facing paths;
- hotel identity, location and contact discovery;
- property description and information clarity;
- room discovery and room information;
- amenities and important guest information;
- booking-action discoverability and reachable booking journey where technically possible;
- supporting SEO, performance, mobile, accessibility, and structured-data signals.

These technical signals support the product; they do not define it.

## Guest journey

Evaluate the representative sequence:

**Discover → Understand → Explore → Trust → Book**

At minimum, later specifications must cover discoverability, property understanding, room exploration, relevant information, contact/discovery, booking discoverability, and booking-journey reachability where technically possible.

If evidence is unavailable or a third-party flow prevents reliable observation, the result must record that limitation rather than imply success or failure.

## Report

The owner-facing report should communicate:
- what was observed;
- what it means for the guest journey;
- material gaps;
- evidence/provenance;
- areas requiring owner verification;
- public-web limitations.

A universal scoring model is intentionally not defined yet.

## Interactive preview

The completed evaluation must provide an interactive Antena-hosted hotel preview, for example `<hotel-slug>.antenapro.com`, that the owner can actually explore as a guest.

The preview must be an interactive web experience rather than a screenshot, PDF, or static mockup.

Candidate pages/flows:
- homepage;
- rooms;
- room details;
- amenities;
- dining where applicable;
- location;
- contact;
- booking journey where supportable.

The actual minimum set, preview content model, rendering/runtime behavior, hosting lifecycle, and detailed interaction requirements are future specifications.

## Preview truthfulness and inspectable provenance

The interactive preview must not silently present inferred, generated, normalized-from-uncertain evidence, or demonstration information as verified hotel facts.

At product level, preview information must have a **clear, user-visible distinction between verified/publicly discovered information and information that is inferred, generated, or demonstration-only**. Where a guest-facing element contains non-verified information, the preview must provide clear labeling and/or an inspectable provenance mechanism sufficient to understand its status.

The product must preserve the provenance/verification state needed to support this behavior. Exact UI treatment, schemas, labels, confidence semantics, and inspection interaction are intentionally deferred to later preview/provenance specifications.

Where evidence is missing, the system should prefer an explicit unknown/missing state or clearly marked demonstration content over silent invention.

## Public-access boundary

The product analyzes publicly accessible digital presence. No implementation is authorized to bypass access controls, defeat anti-bot protections, use unauthorized authentication, access private data, or perform aggressive/prohibited scraping. Later acquisition architecture must explicitly address access constraints, rate limits, privacy, terms, and applicable law.

## Capability boundaries

Candidate bounded capabilities:
1. Input & identity.
2. Public evidence acquisition.
3. Hospitality evidence normalization.
4. Analysis engine.
5. Report assembly.
6. Preview model.
7. Preview runtime.
8. Preview hosting.
9. Evaluation/observability.

These are capability boundaries, not technology commitments.

## Security baseline

Later implementation must address URL validation, SSRF, resource/time limits, fetched-content isolation, untrusted HTML/script handling, tenant/evaluation isolation if persistence exists, secrets, abuse/rate limiting, and preview lifecycle/access controls.

## Reliability baseline

Later implementation must account for network failures, timeouts, JavaScript-heavy sites, access restrictions, third-party booking engines, partial analysis, repeatability, and evidence traceability. Failure of one sub-check should not automatically invalidate an entire evaluation.

## Explicit non-goals

- generic SEO-only auditing;
- generic website auditing detached from hospitality;
- competitor intelligence as the core product;
- OTA scraping as the primary purpose;
- bypassing third-party protections;
- silently publishing invented hotel facts;
- guaranteeing successful third-party booking;
- final AI-first architecture.

## Future implementation-slice acceptance criteria

Any future slice must have an explicit specification, bounded scope, automated tests for normal and key failure paths, no unapproved architecture, preserved provenance/truthfulness rules, security/access constraints, updated documentation, and reported validation.
