# REQ-008 — Interactive Hotel Preview Contract

**Status:** READY  
**Owner:** Next implementation/specification session  
**Type:** Specification  
**Branch:** `spec/interactive-preview-contract`

## Objective

Define the implementation-neutral product contract for the interactive Antena-hosted hotel preview that is produced alongside an evaluation result.

The preview is a core product outcome, not a marketing add-on. A hotel owner must be able to open and explore a usable representation of the evaluated hotel's guest-facing digital experience, while clearly distinguishing verified public hotel information from inferred and demonstration content.

The governing product flow remains:

**Hotel input → Evaluation → Evidence → Hospitality analysis → Hotel Experience Model → Analysis result + interactive hotel preview**

## Read before execution

The session MUST read the complete contents of:

- `docs/context/project-context.md`
- `docs/specs/SPEC-001-hotel-check.md`
- `docs/specs/SPEC-002-evaluation-input.md`
- `docs/specs/SPEC-003-evaluation-lifecycle.md`
- `docs/specs/SPEC-004-public-evidence-acquisition.md`
- `docs/specs/SPEC-005-evidence-model.md`
- `docs/specs/SPEC-006-hospitality-analysis.md`
- `docs/specs/SPEC-007-hotel-experience-model.md`
- `docs/architecture/` relevant documents
- `docs/decisions/` relevant documents
- `requirements/` current workflow guidance and this requirement

If a referenced document does not exist or materially contradicts this requirement, STOP and report the ambiguity instead of inventing a resolution.

## Scope

Specify the semantic contract for:

1. Preview purpose and relationship to the completed evaluation.
2. Preview identity and relationship to a specific hotel/evaluation/attempt.
3. Guest-explorable page/flow capabilities, including where applicable:
   - homepage/property overview;
   - rooms and room details;
   - amenities/facilities;
   - dining where applicable;
   - location/contact;
   - policies/guest information;
   - booking/discovery journey where technically supported.
4. Navigation and guest exploration expectations.
5. How the preview consumes the Hotel Experience Model without silently changing provenance.
6. Verified, qualified/inferred, demonstration, unknown, unavailable, and other presentation states.
7. Explicit disclosure/truthfulness rules for demonstration or generated content.
8. What the preview may do when information is missing, unavailable, conflicting, or inferred.
9. Booking handoff semantics and third-party destination boundaries.
10. Relationship between preview content and analysis findings/recommendations.
11. Preview readiness/completeness semantics and relationship to evaluation completion.
12. Preview failure/partial capability semantics.
13. Preview security/trust boundaries, including unsafe external navigation and untrusted source content.
14. Requirements for stable preview identity/URL semantics at the product level without choosing an implementation technology.
15. Acceptance criteria for future implementation and tests.

## Explicit non-scope

Do NOT implement application code.

Do NOT select or lock:

- Next.js or another frontend framework;
- Java/Spring or another backend framework;
- database/persistence technology;
- hosting/cloud infrastructure;
- CDN or routing implementation;
- rendering engine;
- browser automation;
- AI provider/model;
- template/component library;
- domain/DNS implementation.

Do NOT define visual design, final UI copy, branding, or a detailed screen-by-screen design system.

Do NOT redefine evidence acquisition, evidence provenance, hospitality analysis, or the Hotel Experience Model. Reference the existing specifications instead.

Do NOT turn the preview into a generic AI website generator.

## Required product principles

The specification MUST preserve these invariants:

- A preview is tied to a specific evaluated hotel and evaluation attempt/run.
- The preview does not become a second source of truth that overwrites the Hotel Experience Model.
- Demonstration content remains explicitly demonstration content.
- Inferred information never becomes verified merely because it is rendered.
- Missing/unavailable information must not be silently fabricated.
- Analysis findings and recommendations are not hotel facts.
- Third-party booking or external destinations remain identifiable as third-party.
- A preview being technically renderable is not by itself proof that it is ready for an owner to explore.
- Evaluation completion requires the preview outcome required by SPEC-001/SPEC-003; the new contract must clarify what preview readiness means without contradicting those lifecycle rules.
- Partial preview capability must remain distinguishable from a fully ready preview.
- Preview content must be safe to expose to an owner even though source evidence is untrusted external content.

## Required output

Create/update the following documentation only:

1. `docs/specs/SPEC-008-interactive-preview.md`
2. `docs/specs/README.md` — add SPEC-008 to the index.
3. This requirement file — update the Session Completion Record when work is complete.

The specification should be implementation-neutral and use concrete acceptance criteria that a later implementation session can turn into tests.

## Acceptance criteria for this requirement

The resulting SPEC-008 MUST:

- define the preview as a core evaluation outcome;
- define its identity and relationship to evaluation/attempt/run;
- define the minimum guest exploration surface and optional hospitality surfaces;
- define navigation and flow semantics without prescribing UI technology;
- define how Hotel Experience Model facts are consumed;
- preserve provenance and presentation safety;
- explicitly prevent invented content from being presented as verified hotel information;
- define treatment of missing, unavailable, failed, conflicting, stale, inferred, and demonstration information;
- define booking handoff and third-party boundaries;
- distinguish preview readiness from mere technical rendering;
- define partial preview capability and preview failure semantics;
- define the relationship between preview readiness and evaluation completion;
- define owner-facing trust/disclosure expectations;
- define relevant security/trust boundaries for untrusted hotel/source content and external navigation;
- remain consistent with SPEC-001 through SPEC-007;
- avoid premature technology/architecture decisions;
- include implementation-oriented acceptance criteria and edge cases;
- update the specification index.

## Git / PR requirements

- Create the exact branch specified above: `spec/interactive-preview-contract`.
- Create the branch from the current `main`.
- Do not use another branch name.
- Documentation/specification changes only.
- Update this SAME requirement file with the completion record.
- Include exact branch, PR number, PR URL, changed files, validation performed, and open questions.
- Create the PR against `main`.
- Do not merge the PR.

## Session Completion Record

Not started.

## Orchestrator Review History

No orchestrator review has occurred yet.
