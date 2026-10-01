# Antena Hotel Check — Project Context

**Status:** SPECIFIED / FOUNDATION  
**Repository:** brijeshsrivdev/antena-hotel-check

## Product objective

Antena Hotel Check is a hospitality-specific digital experience analyzer for hotels.

The eventual owner-facing flow is:
1. Enter hotel name + city, or enter hotel website URL.
2. Analyze publicly accessible digital presence and guest-facing journey.
3. Produce a useful hospitality-specific analysis/report.
4. Produce an interactive Antena-hosted preview such as `<hotel-slug>.antenapro.com`.
5. Allow the owner to explore the preview as an actual guest would.

Central question:

> Can a guest find, understand, trust, explore, and book this hotel online?

## Product boundary

This is not primarily:
- a generic SEO checker;
- a generic website audit;
- a competitor/OTA scraping product;
- a generic AI website generator.

Technical website health may contribute evidence, but hospitality and guest experience remain the center.

## Core product areas

### Hotel Analysis
Potential dimensions include website health, SEO fundamentals, performance, mobile experience, accessibility, structured data, hotel information, contact/discovery, guest-facing content, and booking discoverability.

### Guest Journey Analysis
Assess whether a guest can discover the hotel, understand it, find and understand rooms, find important amenities/information, find contact options, find booking functionality, and navigate the booking journey where technically possible.

### Interactive Hotel Preview
The eventual preview is a real interactive web experience, not a screenshot, PDF, or static mockup. Candidate flows include homepage, rooms, room details, amenities, dining where applicable, location, contact, and booking where supportable.

Preview content must distinguish publicly discovered, normalized/inferred, and demonstration/generated information. Invented information must never silently appear as verified hotel facts.

## Current state

- **IMPLEMENTED:** repository/specification foundation only.
- **SPECIFIED:** overall product intent and major capability boundaries.
- **PLANNED:** capability implementations described by SPEC-001.
- **PROPOSED:** technology choices and detailed architecture until explicit decision records exist.
- **DEFERRED:** all feature implementation beyond the foundation.
- **REJECTED:** generic SEO-only positioning, generic website-audit positioning, competitor/OTA scraping as the core product, and generic AI site generation as the product definition.

## Session-1 non-goals

No crawling/scraping, analysis engine, scoring algorithm, AI generation, preview rendering, custom-domain provisioning, booking/OTA integration, tenant persistence, authentication, or final technology-stack decision is made here.

## Durable-state rule

The repository is the durable source of truth. Material product and architecture decisions belong in repository documentation. Implementation sessions are bounded contributors; the orchestrator owns direction, sequencing, specifications, and PR acceptance.
