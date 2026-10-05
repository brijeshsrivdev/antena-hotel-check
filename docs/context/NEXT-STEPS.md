# Next Steps

## Current Position

REQ-033 — Hospitality Analysis Coverage Classification Contract is accepted and merged.

REQ-034 — Hospitality Analysis Coverage Classification Implementation is merged as PR #32.

REQ-035 — Hospitality Coverage Assessment Contract is implemented and merged as PR #34.

REQ-036 — Hospitality Coverage Assessment Derivation Contract is now specified as the missing factual derivation boundary. Runtime implementation has not started.

REQ-032 — Evaluation Execution Orchestration Foundation remains **BLOCKED** as a separate implementation slice.

## REQ-035 Implemented Boundary

The runtime pre-classification boundary is:

```text
Evidence / governed upstream facts
        ↓
HospitalityCoverageAssessment
        ↓
HospitalityAnalysisCoverageClassifier
        ↓
HospitalityAnalysisCoverageState
        ↓
HospitalityAnalysisService
        ↓
HospitalityAnalysisCoverage
```

`HospitalityCoverageAssessment` is immutable and contains exactly one evaluation identity plus intended, assessable, and limited journey/dimension scope. Covered scope is derived as assessable ∪ limited scope.

The classifier consumes the assessment directly. REQ-033 calibration remains unchanged.

## REQ-036 — Current Specification Boundary

REQ-036 defines the governed derivation:

```text
Governed hospitality observations / qualified signals
                    ↓
       Coverage Assessment Derivation
                    ↓
      HospitalityCoverageAssessment
```

The contract preserves the existing mappings:

- `HOTEL_IDENTITY` → `DISCOVER`, `UNDERSTAND` and hotel-identity dimension;
- `ROOMS` → `EXPLORE` and rooms dimension;
- `AMENITIES` → `UNDERSTAND`, `EXPLORE` and amenities/guest-facing dimension;
- `CONTACT` → `DISCOVER` and contact/location dimension;
- `BOOKING` → `BOOK` and booking-discoverability dimension;
- `DINING` → `UNDERSTAND`, `EXPLORE` and amenities/guest-facing dimension;
- typed material same-evaluation cross-source hotel identity conflict → `TRUST_AND_CLARITY`.

The current unsupported dimensions remain unsupported: discoverability/navigation, mobile/technical guest experience, and SEO/structured-data supporting signals.

Explicit limitation category/journey context may create limited scope. Unscoped acquisition limitations do not receive guessed scope.

REQ-036 is specification only. No runtime implementation has started.

## REQ-032 Direction

REQ-032 remains blocked until REQ-036 is implemented and reviewed.

Once unblocked, it should create a single explicit orchestration service for one canonical evaluation execution:

```text
Canonical Evaluation Request
        ↓
Evaluation Execution Orchestrator
        ↓
Existing acquisition integration
        ↓
Structured evidence
        ↓
Governed observations/signals
        ↓
REQ-036 coverage assessment derivation
        ↓
Coverage classification
        ↓
Existing deterministic analysis pipeline
        ↓
Guest journey
        ↓
Recommendations
        ↓
Structured report
```

The orchestrator should coordinate existing contracts rather than absorb their responsibilities or duplicate coverage semantics.

## Existing Coverage Classification

REQ-033 remains authoritative:

- `SUBSTANTIALLY_ASSESSED`: all five journey stages covered; at least six of nine intended dimensions covered; at least three journey stages have assessable evidence.
- `PARTIALLY_ASSESSED`: at least one intended journey stage or dimension is covered, but substantial criteria are not satisfied.
- `INSUFFICIENT_COVERAGE`: zero intended journey stages and zero intended dimensions are covered.

These are analysis-capability classifications, not hotel-quality scores.

## Completed Analysis Foundation

```text
Acquisition
  ↓
Structured Evidence
  ↓
Hospitality Observation
  ↓
Qualified Analysis Signal
  ↓
Coverage Assessment Derivation [REQ-036 specified; implementation pending]
  ↓
Governed Coverage Assessment
  ↓
Governed Coverage Classification
  ↓
Hospitality Finding / Deficiency / Unable-to-Verify Limitation
  ↓
Hospitality Analysis Coverage
  ↓
Hospitality Analysis Result
  ↓
Guest Journey Analysis
  ↓
Actionable Recommendations
  ↓
Structured Analysis Report
```

REQ-023 establishes the truthful distinction between unable-to-verify and an observed hotel deficiency.

REQ-024 establishes final coverage representation. REQ-033 and REQ-034 establish and implement governed classification. REQ-035 provides the pre-classification representation. REQ-036 now defines how existing governed facts populate that representation.

## Future Sequence

After REQ-036 implementation and review, reassess the actual repository state before beginning REQ-032. Do not start REQ-032 until the assessment derivation is implemented and the complete dependency chain is validated.

After REQ-032, reassess the actual repository state before defining the next slice. Likely future areas include:

- real-hotel end-to-end validation against controlled public targets;
- API boundary for starting/retrieving an evaluation;
- persistence and evaluation history;
- analysis quality/calibration and additional supported dimensions;
- customer-facing report rendering;
- connected digital-performance signals;
- eventually Antena integration and `<hotel-name>.antenapro.com` experience generation.

These are not current implementation requirements unless separately specified and marked READY.

## Standard Continuation Lifecycle

```text
Inspect main
    ↓
Identify next bounded slice
    ↓
Create/refine READY requirement on main
    ↓
Implementation session
    ↓
Tests + CI
    ↓
PR
    ↓
Orchestrator review
    ↓
Fix/re-review if required
    ↓
Merge
    ↓
Update durable context
```

## Guardrails

Do not bypass the coverage-assessment derivation/classification contract by defaulting a state inside orchestration. Do not duplicate calibration rules in the orchestrator. Do not make the classifier inspect raw evidence. Do not guess limited scope from unscoped acquisition failures. Do not start REQ-032 until REQ-036 is implemented/reviewed and the separate orchestration requirement is ready.
