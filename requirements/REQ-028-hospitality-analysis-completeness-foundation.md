# REQ-028 — Hospitality Analysis Completeness Foundation

STATUS: READY
REQUIREMENT_ID: REQ-028
TYPE: Implementation
BRANCH: `feature/hospitality-analysis-completeness-foundation`

## Objective

Expand the deterministic hospitality analysis capability so more of the nine intended SPEC-006 dimensions can be responsibly assessed from the evidence, observations, and signals already available to the system.

REQ-027 established conservative evidence-grounded deficiency analysis. REQ-028 must improve analysis completeness without inventing conclusions where evidence is insufficient.

The governing principle is:

```text
INTENDED DIMENSION
      ↓
CURRENTLY ASSESSABLE
      ↓
ACTUALLY ASSESSED FOR THIS EVALUATION
```

The existence of an intended dimension does not mean that dimension is currently assessable.

## Repository Baseline

REQ-027 is merged and establishes deterministic deficiency analysis over the existing REQ-026 pipeline.

Current pipeline boundary:

```text
StructuredEvidence
      ↓
HospitalityObservation
      ↓
HospitalityAnalysisSignal
      ↓
HospitalityFinding
      ↓
HospitalityDeficiency
      ↓
Coverage
      ↓
HospitalityAnalysisResult
```

REQ-028 builds on these existing contracts.

## Intended Dimensions

SPEC-006 defines these nine semantic dimensions:

1. `HOTEL_IDENTITY`
2. `DISCOVERABILITY`
3. `ROOMS`
4. `AMENITIES_AND_GUEST_FACING_INFORMATION`
5. `CONTACT_AND_LOCATION`
6. `BOOKING_AND_BOOKING_JOURNEY`
7. `TRUST_AND_CLARITY`
8. `MOBILE_AND_TECHNICAL_GUEST_EXPERIENCE`
9. `SEO_AND_STRUCTURED_DATA`

The implementation explicitly distinguishes dimensions that are intended from dimensions that are currently assessable and dimensions actually assessed for an evaluation.

## Scope

Implement the smallest deterministic expansion of hospitality analysis that the existing evidence/observation/signal contracts can support.

The implementation may add assessment capability for one or more existing dimensions where the repository already contains sufficient governed inputs.

The exact set of newly supported dimensions must be determined from the repository during implementation. It is valid and expected for some intended dimensions to remain unsupported.

A dimension must remain unsupported when its required evidence is not available through existing contracts.

## Assessment Semantics

For each newly supported dimension, the implementation preserves the distinction between:

### Positive/observed evidence

Evidence that directly supports the relevant hospitality assessment.

### Observed deficiency

Evidence that explicitly establishes a meaningful guest-facing problem.

### Insufficient evidence

Evidence exists but is not strong enough to establish the intended conclusion.

### Unable to verify / limitation

The relevant source or path could not be reliably observed.

### Not applicable

The analysis area does not meaningfully apply based on sufficient contextual evidence.

Missing evidence must not be converted into a negative hotel claim.

## Truth Boundary

The following rules are mandatory:

```text
Missing evidence              ≠ hotel deficiency
Unsupported dimension         ≠ assessed dimension
Acquisition failure           ≠ hotel deficiency
NOT_ATTEMPTED                 ≠ absence
Unable to verify              ≠ feature absent
Unrecognized observation      ≠ feature absent
Technical check failure       ≠ hospitality deficiency unless governed by evidence
```

REQ-027's booking correction remains authoritative:

```text
successful room evidence + no BOOKING signal → no booking deficiency
```

REQ-028 does not reintroduce that inference through another rule.

## Evidence and Provenance

Every new assessment/finding remains traceable through the existing provenance chain:

```text
Assessment / Finding
      ↓
Signal
      ↓
Observation
      ↓
StructuredEvidence
      ↓
Evaluation / Attempt
```

No new provenance model was introduced. Source evidence and acquisition results are not mutated, and evidence from different evaluations is not mixed.

## Coverage

REQ-028 provides richer inputs to the existing analysis coverage semantics without redesigning the coverage contract.

No new coverage formula, arbitrary threshold, or numerical score was introduced.

`TRUST_AND_CLARITY` becomes assessable only when the retained identity findings contain a deterministic, same-evaluation, cross-source material identity conflict. Coverage determines this from typed finding/observation data, not from human-readable finding text.

## Findings

The existing `HospitalityFinding` model and analysis contracts are reused.

A small explicit domain helper, `HospitalityFinding.isIdentityConflictWith(...)`, determines a material identity conflict from the existing typed observation category, evaluation attribution, source reference, and observed identity value. This avoids using presentation-oriented finding text as a domain discriminator and does not introduce a generic rule engine or new finding hierarchy.

No upstream acquisition or observation contract was expanded.

## Hospitality-First Boundary

The analysis remains organized around the hospitality guest journey and SPEC-006 dimensions.

Technical, SEO, structured-data, mobile, accessibility, or performance observations are used only where existing evidence supports a meaningful guest-facing analysis.

REQ-028 is not a generic SEO or website auditing engine.

## Context Reconciliation

Before correction, PR #28 contained the REQ-028 service and test changes but did not update this requirement file. The repository review also identified a P1 implementation issue: coverage used the human-readable identity-conflict finding text as its discriminator.

The correction keeps REQ-027 behavior intact and addresses both P1 issues before PR readiness.

## Dimensions Inspected

All nine SPEC-006 dimensions were inspected against the existing observation, signal, finding, limitation, and coverage contracts:

- `HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING` — assessable through existing hotel identity observations.
- `DISCOVERABILITY_AND_NAVIGATION` — inspected; unsupported because the current governed observation contracts do not provide sufficient evidence.
- `ROOMS_AND_ROOM_INFORMATION` — already assessable through existing room observations and retained REQ-027 deficiency behavior.
- `AMENITIES_AND_GUEST_FACING_INFORMATION` — already assessable through existing amenities/dining observations.
- `CONTACT_AND_LOCATION` — already assessable through existing contact observations.
- `BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS` — existing booking observation support remains unchanged and conservative.
- `TRUST_AND_CLARITY` — newly supported when a material cross-source hotel identity conflict is deterministically established.
- `MOBILE_AND_TECHNICAL_GUEST_EXPERIENCE` — inspected; intentionally unsupported because sufficient governed evidence is not available.
- `SEO_AND_STRUCTURED_DATA_SUPPORTING_SIGNALS` — inspected; intentionally unsupported because sufficient governed evidence is not available.

## Newly Supported Dimensions

### `TRUST_AND_CLARITY`

The dimension is assessable when two retained `HOTEL_IDENTITY` findings from the same evaluation represent materially different identity values from different sources. The discriminator is typed and deterministic:

1. both findings are `HOTEL_IDENTITY`;
2. both belong to the same evaluation;
3. their source references differ;
4. their normalized observed identity values are materially different.

This reuses the existing REQ-027 identity-conflict behavior rather than creating a second conflict rule.

## Intentionally Unsupported Dimensions

The following remain unsupported because the current repository does not expose sufficient governed evidence to make responsible guest-facing claims:

- `DISCOVERABILITY_AND_NAVIGATION`
- `MOBILE_AND_TECHNICAL_GUEST_EXPERIENCE`
- `SEO_AND_STRUCTURED_DATA_SUPPORTING_SIGNALS`

No new acquisition, crawler, browser, SEO, mobile, or technical observation contract was added merely to make these dimensions appear assessable.

## Implementation Summary

1. Added a typed `HospitalityFinding.isIdentityConflictWith(...)` helper based on existing observation/category, evaluation, source, and observed-value data.
2. Changed `HospitalityAnalysisService` coverage derivation to use that typed helper instead of `findingText().startsWith(...)`.
3. Added/retained REQ-028 regression coverage for cross-source identity conflict and unsupported dimensions.
4. Preserved the REQ-027 booking truth boundary: room evidence without a BOOKING observation does not create a booking deficiency.
5. Added no AI, scoring, network access, acquisition expansion, persistence changes, or generic rule engine.

## Files Changed

- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisService.java`
- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityFinding.java`
- `backend/src/test/java/com/antenapro/hotelcheck/analysis/HospitalityAnalysisCompletenessFoundationTest.java`
- `requirements/REQ-028-hospitality-analysis-completeness-foundation.md`

## Tests

The REQ-028 focused test class covers:

- cross-source identity conflict makes `TRUST_AND_CLARITY` assessable;
- identity conflict discrimination is based on typed finding/observation data rather than finding text;
- trust remains unsupported without governed conflict evidence;
- mobile and SEO remain unsupported;
- room evidence without a BOOKING observation does not create a booking deficiency.

The complete backend Maven suite is the repository's CI validation gate and includes these focused tests.

## Architectural Decisions

- Reuse the existing `HospitalityFinding` domain model rather than introducing a second finding hierarchy.
- Keep identity-conflict semantics deterministic and local to the existing domain model.
- Use typed observation/category, evaluation, source, and observed-value information for the conflict discriminator.
- Do not infer trust problems from generic identity findings or from finding presentation text.
- Do not expand upstream evidence/acquisition/observation contracts for unsupported dimensions.
- Keep coverage as factual derived scope; do not introduce scoring or new coverage formulas.

## Limitations

- `TRUST_AND_CLARITY` currently gains only the identity-conflict assessment supported by existing evidence; it is not a general trust audit.
- Discoverability, mobile/technical, and SEO/structured-data dimensions remain unsupported until future requirements provide governed evidence sufficient for those assessments.
- No browser rendering, performance measurement, structured-data parser, or external platform integration was introduced.
- Focused test execution is represented by the REQ-028 test class and is covered by the complete Maven validation; the available repository tooling does not provide a separate local Maven execution environment in this session.

## Self-Review

- P1 requirement-update gap: corrected by updating this requirement on the PR branch.
- P1 finding-text discriminator: corrected; `HospitalityAnalysisService` no longer uses human-readable finding text to determine trust coverage.
- REQ-027 booking regression: explicitly retained and tested.
- Provenance/evaluation isolation: preserved through the existing finding and observation contracts.
- Scope: no REQ-029 work, no unrelated architecture, AI, acquisition, or integration changes.
- Final CI must pass against the exact final PR head before changing `STATUS` to `PR_READY`.

## Validation State

Current validation is **in progress** against final implementation head `a36538b7c3357d3bc62707d8f35a503a9a8eefa9`.

GitHub Actions Backend Validation has started for that exact head. `STATUS` remains `READY` until the final-head CI run completes successfully.

## Explicit Non-Scope

REQ-028 does not implement or introduce:

- guest-journey first-class modeling;
- `Discover → Understand → Explore → Trust → Book` workflow/domain model;
- recommendation generation;
- prioritization or scoring;
- AI/LLM analysis;
- Google Business Profile integration;
- Google Search Console integration;
- GA4 integration;
- new crawling/browser acquisition;
- new network access;
- new observation categories unless an existing requirement explicitly authorizes them;
- acquisition changes;
- evidence-model redesign;
- provenance redesign;
- persistence schema redesign;
- report UI;
- interactive preview;
- Antena integration;
- booking/OTA integrations;
- competitor analysis.

Connected Google performance data is a future, plan-gated capability documented in `docs/context/PRODUCT-ROADMAP.md` and is not part of REQ-028.

## Implementation Constraints

1. Inspect the actual `main` repository before implementation.
2. Read SPEC-006 and relevant existing requirements completely.
3. Reuse existing observations, signals, findings, limitations, coverage, and result contracts.
4. Add only deterministic logic justified by available evidence.
5. Do not expand upstream acquisition/observation contracts merely to make a dimension appear assessable.
6. If a dimension cannot be responsibly assessed, leave it unsupported and document the limitation.
7. Preserve all REQ-027 behavior.
8. Keep implementation explicit and understandable.

Avoid generic abstractions such as:

- rule engines;
- reflection-based analyzers;
- dynamic plugin registries;
- metadata-driven ontology frameworks;
- configurable scoring engines.

## Testing Acceptance Criteria

Automated tests must demonstrate:

1. Every newly supported dimension has deterministic evidence-backed assessment behavior.
2. Insufficient evidence does not become a negative conclusion.
3. Unable-to-verify evidence remains distinguishable from observed deficiency.
4. Unsupported dimensions are not falsely reported as assessed.
5. Missing BOOKING evidence does not become a booking deficiency.
6. Acquisition failure and `NOT_ATTEMPTED` semantics do not become hotel deficiencies.
7. Every new finding retains provenance to supporting evidence.
8. Evidence from evaluation A cannot create a finding for evaluation B.
9. Existing REQ-027 deficiency behavior remains unchanged.
10. Identical input produces identical analysis output.
11. No external network or AI dependency is required.
12. Existing backend tests continue to pass.

## Required Validation

Before marking `PR_READY`:

- run focused REQ-028 tests;
- run the complete backend Maven test suite;
- validate GitHub Actions Backend Validation against the final PR head;
- record the final validation result in this requirement.

Do not claim final CI success until it has passed for the final PR head.

## Expected Session Outcome

The session must:

- create branch `feature/hospitality-analysis-completeness-foundation` from current `main`;
- implement only REQ-028;
- add appropriate tests;
- update this requirement to `PR_READY` only after final validation;
- create a PR against `main`;
- stop for orchestrator review.

The session must not merge the PR or begin REQ-029.
