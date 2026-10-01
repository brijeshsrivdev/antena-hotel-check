# SPEC-001 — Hotel Check Product Specification

**Status:** SPECIFIED  
**Version:** 1.0  
**Implementation:** Not started

## Purpose

Define the first coherent product contract without prematurely selecting implementation technologies.

## Problem

Hotel owners need to understand whether their public digital presence supports a real guest journey from discovery through booking. A technical audit alone does not answer this hospitality question.

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

The eventual preview must be an interactive Antena-hosted experience, for example `<hotel-slug>.antenapro.com`, rather than a screenshot, PDF, or static mockup.

Candidate pages/flows:
- homepage;
- rooms;
- room details;
- amenities;
- dining where applicable;
- location;
- contact;
- booking journey where supportable.

The actual minimum set is a later preview specification.

## Truthfulness and provenance

The report and preview must not represent generated content as verified hotel facts. The product model needs source/provenance, transformation state, verification status, and limitations. Missing evidence should remain unknown or be clearly marked as demonstration content.

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
