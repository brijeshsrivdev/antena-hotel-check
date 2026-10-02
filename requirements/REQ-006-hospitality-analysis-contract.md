# REQ-006 — Hospitality Analysis Contract

**Status:** PR_READY  
**Owner:** Next implementation/specification session  
**Type:** Specification

## Objective

Define the durable product/domain contract for turning an evaluation's available evidence into a hospitality-specific analysis.

The contract must answer what the analyzer evaluates, how findings are expressed and traced to evidence, how unavailable evidence affects analysis completeness, and how the analysis remains centered on the guest journey rather than becoming a generic SEO or website audit.

The contract must remain compatible with the completed-evaluation requirement established by `SPEC-003`: a completed evaluation ultimately requires both a useful analysis/report and an interactive hotel preview.

## Repository Context to Read

Before starting, read:

- `docs/context/project-context.md`
- `docs/specs/SPEC-001-hotel-check.md`
- `docs/specs/SPEC-002-evaluation-input.md`
- `docs/specs/SPEC-003-evaluation-lifecycle.md`
- `docs/specs/SPEC-004-public-evidence-acquisition.md`
- `docs/specs/SPEC-005-evidence-model.md`
- relevant architecture and decision documents
- this requirement file completely

## Scope

Define an implementation-neutral hospitality analysis contract covering at minimum:

1. The major analysis dimensions needed to answer the product question:
   "Can a guest find, understand, trust, explore, and book this hotel online?"
2. Hospitality-specific evaluation areas, including as appropriate:
   - hotel identity and basic information
   - discoverability and navigation
   - rooms and room information
   - amenities and guest-facing information
   - contact/location information
   - booking discoverability and booking journey signals
   - trust and clarity signals
   - mobile/technical experience where it materially affects the guest journey
   - relevant SEO/structured-data/performance/accessibility observations as supporting dimensions, not as the product center
3. The distinction between an analysis finding, supporting evidence, and an unavailable/unverified observation.
4. Finding severity/importance semantics where product-level meaning is required; do not invent arbitrary numerical scores without justification.
5. How findings remain traceable to supporting evidence and provenance.
6. How conflicting or insufficient evidence affects a finding.
7. How the analyzer distinguishes an actual observed deficiency from an inability to verify something.
8. How analysis completeness is represented when important evidence is unavailable.
9. Requirements for hospitality-specific recommendations or explanations to remain grounded in observed evidence.
10. The relationship between analysis findings and the eventual interactive preview, without specifying preview UI/runtime.

## Hospitality-First Principle

The specification must make clear that Antena Hotel Check is not primarily:

- a generic SEO checker
- a generic website audit
- an OTA/competitor scraping product
- a generic AI website generator

Technical website health may contribute to the analysis, but hospitality and guest experience remain the center.

## Guest Journey

Use the journey established by `SPEC-001` as the organizing product lens:

- Discover
- Understand
- Explore
- Trust
- Book

Define what each stage means at analysis level and what kinds of observable evidence can support findings for that stage.

Do not prescribe implementation algorithms.

## Evidence and Provenance

Use the evidence contract from `SPEC-005`.

A finding must not be presented as established fact when the underlying evidence is unavailable, failed, inferred, or demonstration-only.

Where a conclusion depends on inference, the analysis contract must preserve that distinction.

The analyzer must not silently turn missing evidence into a negative finding.

## Completeness and Limitations

Define product-level semantics for at least:

- verified/observed finding
- limitation / unable to verify
- insufficient evidence
- not applicable where justified

A hotel should not receive a negative finding merely because the analyzer could not access or verify the relevant information.

## Recommendations

Define the contract for recommendations so that they:

- are tied to an observed finding or meaningful evidence gap
- remain hospitality-specific where applicable
- do not claim unsupported hotel facts
- distinguish factual observation from suggested improvement

Do not design the detailed recommendation UI.

## Preview Relationship

The analysis contract must preserve the eventual product flow:

Hotel input → Evaluation → Evidence → Hospitality analysis → Analysis result + interactive hotel preview

Define only the product relationship needed so that future preview generation can use analysis/evidence responsibly.

Do not specify preview rendering, hosting, generated-site technology, or visual design.

## Non-Scope

Do NOT implement or specify in detail:

- crawler/browser implementation
- scraping infrastructure
- search APIs
- AI providers or prompts
- numerical scoring formulas or final score calibration
- report UI/frontend
- interactive preview UI/runtime
- preview hosting/domain routing
- database schema or Java/TypeScript classes
- booking integrations
- OTA integrations
- pricing/monetization
- production infrastructure

Those belong to later bounded requirements/specifications/architecture decisions.

## Acceptance Criteria

The specification must make it possible for a future implementation session to test that:

1. Analysis is organized around the hospitality guest journey rather than generic website auditing.
2. The major analysis dimensions and their product purpose are explicitly defined.
3. Findings can be distinguished from evidence and from evidence limitations.
4. Findings remain traceable to supporting evidence where applicable.
5. Missing/unavailable evidence is not silently converted into a negative hotel finding.
6. Conflicting or insufficient evidence is represented explicitly.
7. Inferred or demonstration information cannot silently become a verified finding.
8. Recommendations are grounded in findings or meaningful evidence gaps.
9. Technical/SEO/accessibility observations remain supporting dimensions rather than replacing the hospitality focus.
10. Analysis can represent partial/incomplete coverage without falsely claiming full verification.
11. The contract remains compatible with the eventual report + interactive preview product outcome.

## Deliverables

Create/update documentation only.

At minimum create:

`docs/specs/SPEC-006-hospitality-analysis.md`

Update the specification index if required.

If a genuine architectural/product decision is discovered, document it separately rather than hiding it inside the specification.

## Git Workflow

**BRANCH:**

`spec/hospitality-analysis-contract`

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

### Session

Session 6 — Hospitality Analysis Contract

### Branch

`spec/hospitality-analysis-contract`

### PR

[#6 — docs: define REQ-006 hospitality analysis contract](https://github.com/brijeshsrivdev/antena-hotel-check/pull/6)

### Files Changed

- `docs/specs/SPEC-006-hospitality-analysis.md` — created the implementation-neutral hospitality analysis contract covering the guest journey, hospitality analysis dimensions, finding semantics, evidence limitations, conflicts, qualitative importance, analysis coverage, grounded recommendations, technical supporting signals, and the relationship to the interactive preview.
- `docs/specs/README.md` — indexed SPEC-006.
- `requirements/REQ-006-hospitality-analysis-contract.md` — records this Session 6 completion record and PR details.

### Summary

Defined the durable hospitality analysis contract around **Discover → Understand → Explore → Trust → Book**. The specification establishes hotel identity/property understanding, discoverability/navigation, rooms, amenities/guest information, contact/location, booking discoverability/journey signals, trust/clarity, mobile/technical experience, and SEO/structured-data supporting signals as analysis dimensions. It distinguishes findings from evidence and limitations; separates observed deficiencies from inability to verify; preserves provenance, conflicts, freshness, and evidence traceability; defines qualitative importance and coverage semantics without introducing numerical scoring; and requires recommendations to be grounded in findings or meaningful evidence gaps.

The specification also preserves the eventual report + interactive preview outcome and prevents unavailable, inferred, or demonstration information from silently becoming verified hotel fact. It intentionally avoids crawler/acquisition implementation, AI providers/prompts, scoring formulas, report/preview UI, persistence schema, booking/OTA integrations, pricing, infrastructure, and other non-scope areas.

No separate architecture/product decision record was created because no technology or material architectural choice was required to establish this product/domain contract.

### Validation

- Inspected the repository and confirmed `main` was the current base before creating the branch.
- Located the current `READY` requirement under `requirements/` and read REQ-006 completely.
- Read all referenced context/specification documents: `project-context`, SPEC-001, SPEC-002, SPEC-003, SPEC-004, and SPEC-005.
- Read relevant architecture and decision documents: `docs/architecture/architecture-concerns.md` and `docs/decisions/README.md`.
- Created `docs/specs/SPEC-006-hospitality-analysis.md` and checked it against every REQ-006 scope area and acceptance criterion.
- Updated `docs/specs/README.md` to index SPEC-006.
- Confirmed the changes are documentation/specification-only; no application implementation was added.
- No automated application tests were applicable because no application code was changed.
- Created the exact required branch `spec/hospitality-analysis-contract` from current `main`.
- Created PR #6 against `main`; PR remains open and unmerged.

### Open Questions

No material open question remains within the bounded REQ-006 specification scope. Detailed analysis check catalog, implementation algorithms, qualitative-importance calibration, coverage thresholds, source-selection rules, persistence/schema representation, report presentation, and preview consumption/UI remain intentionally deferred to later bounded requirements/specifications/architecture decisions.

## Orchestrator Review History

<!-- Orchestrator review rounds are appended below without deleting prior history. -->
