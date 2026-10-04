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

REQ-028 must build on these existing contracts.

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

The implementation must explicitly distinguish dimensions that are intended from dimensions that are currently assessable and dimensions actually assessed for an evaluation.

## Scope

Implement the smallest deterministic expansion of hospitality analysis that the existing evidence/observation/signal contracts can support.

The implementation may add assessment capability for one or more existing dimensions where the repository already contains sufficient governed inputs.

The exact set of newly supported dimensions must be determined from the repository during implementation. It is valid and expected for some intended dimensions to remain unsupported.

A dimension must remain unsupported when its required evidence is not available through existing contracts.

## Assessment Semantics

For each newly supported dimension, the implementation must preserve the distinction between:

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

REQ-028 must not reintroduce that inference through another rule.

## Evidence and Provenance

Every new assessment/finding must remain traceable through the existing provenance chain:

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

Do not create a new provenance model.

Do not mutate source evidence or acquisition results.

Do not mix evidence from different evaluations.

## Coverage

REQ-028 may provide richer inputs to the existing analysis coverage semantics, but must not redesign the coverage contract.

Do not introduce new coverage formulas, arbitrary thresholds, or numerical scores.

Do not claim exhaustive verification merely because multiple dimensions are assessed.

Existing coverage states remain governed by the established analysis contract, including substantially assessed, partially assessed, and insufficient coverage semantics.

## Findings

Reuse the existing `HospitalityFinding` model and existing analysis contracts.

Do not introduce a second finding hierarchy, generic rule engine, or scoring framework.

Any new finding must preserve its affected dimension, guest-facing relevance where already supported, evidence basis, provenance, and evaluation attribution.

Do not create a finding solely because an expected observation is absent.

## Hospitality-First Boundary

The analysis remains organized around the hospitality guest journey and SPEC-006 dimensions.

Technical, SEO, structured-data, mobile, accessibility, or performance observations may be used only where existing evidence supports a meaningful guest-facing analysis.

Do not turn REQ-028 into a generic SEO or website auditing engine.

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

## Requirement Update

The implementation agent must update this same requirement file before `PR_READY` with:

- context reconciliation;
- dimensions inspected;
- newly supported dimensions;
- intentionally unsupported dimensions;
- implementation summary;
- files changed;
- tests and validation;
- truth-boundary decisions;
- architectural decisions;
- limitations;
- self-review;
- final CI/head information.

Only then may the requirement status change from `READY` to `PR_READY`.

## Expected Session Outcome

The session must:

- create branch `feature/hospitality-analysis-completeness-foundation` from current `main`;
- implement only REQ-028;
- add appropriate tests;
- update this requirement to `PR_READY` only after final validation;
- create a PR against `main`;
- stop for orchestrator review.

The session must not merge the PR or begin REQ-029.
