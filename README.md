# Antena Hotel Check

Antena Hotel Check is a hospitality-specific digital experience analyzer for hotels.

## Project status

**Current status: SPECIFIED / FOUNDATION**

This repository begins with durable product context and specifications. Product feature implementation has not started.

## Core objective

Answer the hospitality question:

> Can a guest find, understand, trust, explore, and book this hotel online?

The eventual product accepts a hotel name + city or a hotel website URL, analyzes publicly accessible digital presence and guest journey, produces a hospitality-specific report, and provides an interactive Antena-hosted hotel preview.

## Specification-first development

Requirement → Specification → Architecture/design decision → Implementation prompt → Implementation → Tests → Branch/PR → Orchestrator review → Approval → User merge.

See:
- `docs/context/project-context.md`
- `docs/specs/SPEC-001-hotel-check.md`
- `docs/architecture/architecture-concerns.md`
- `docs/decisions/README.md`
- `docs/workflows/development-lifecycle.md`
- `docs/plans/hotel-site-check-plan.md` — the ranking-signal site check (local-only, REQ-038 … REQ-040) and its implementation record; the owner-facing page lives in `frontend/` (see its README)
