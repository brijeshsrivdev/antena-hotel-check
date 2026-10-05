# REQ-036 — Hospitality Coverage Assessment Derivation Contract

STATUS: PR_READY
REQUIREMENT_ID: REQ-036
TYPE: Implementation
BRANCH: `feature/hospitality-coverage-assessment-derivation`

## 1. Problem statement

`HospitalityCoverageAssessment` now provides the pre-classification representation required by REQ-035, but the repository does not yet define the governed derivation that populates its intended, assessable, and limited journey/dimension scope.

Without that contract, a future implementation could invent coverage semantics inside an orchestrator, classifier, or analysis service. REQ-036 defines the missing factual boundary:

```text
Governed hospitality observations / qualified signals
                    ↓
       Coverage Assessment Derivation
                    ↓
      HospitalityCoverageAssessment
                    ↓
 HospitalityAnalysisCoverageClassifier
```

The purpose is to answer, for exactly one evaluation, which existing hospitality journey stages and dimensions are responsibly assessable, which are explicitly limited, and which remain unsupported/uncovered.

REQ-036 does not classify the final coverage state.

## 2. Architectural context

The current runtime model contains:

```text
StructuredEvidence
      ↓
HospitalityObservation
      ↓
HospitalityAnalysisSignal
      ↓
HospitalityFinding / Limitation
      ↓
HospitalityCoverageAssessment   ← REQ-036 derivation boundary
      ↓
HospitalityAnalysisCoverageClassifier
      ↓
HospitalityAnalysisCoverageState
      ↓
HospitalityAnalysisService
      ↓
HospitalityAnalysisCoverage
      ↓
HospitalityAnalysisResult
```

REQ-035 established the immutable assessment representation. It intentionally does not determine how upstream facts populate it.

REQ-033 owns product calibration and REQ-034 owns classification. REQ-036 owns only the factual derivation of scope.

## 3. Current gap

Current `HospitalityAnalysisService` still derives assessable/limited scope internally while receiving a caller-supplied `HospitalityAnalysisCoverageState`. Repository behavior and mappings were inspected during reconciliation as evidence for the existing implementation boundary; they are not, by themselves, the durable semantic authority for future behavior. REQ-036 is the durable governed semantic contract for the next implementation slice, and future runtime implementation must follow REQ-036 rather than infer semantics from incidental current service behavior:

- all five journey stages are intended;
- all nine hospitality dimensions are intended;
- qualified signal journey stages establish assessable journey scope;
- observation categories map to supported hospitality dimensions;
- `DINING` maps to `AMENITIES_AND_GUEST_FACING_INFORMATION`;
- a typed material same-evaluation cross-source hotel-identity conflict establishes `TRUST_AND_CLARITY` assessability;
- explicit limitation categories/journey stages establish limited scope when supplied by the governed limitation contract.

REQ-036 converts those reconciled semantics into an explicit contract rather than creating a new taxonomy or silently expanding coverage.

## 4. Scope

REQ-036 defines a deterministic derivation contract that accepts already-governed upstream analysis facts for one evaluation and produces one `HospitalityCoverageAssessment`.

The derivation may consume:

- `HospitalityObservation` records;
- qualified `HospitalityAnalysisSignal` records derived from those observations;
- existing typed identity-conflict facts already established by the analysis domain;
- `HospitalityAnalysisLimitation` records when their category/journey scope is explicitly supplied.

The implementation must reuse the existing observation categories, journey stages, dimensions, provenance, evaluation identity, and limitation semantics.

For normal successful observation processing, a qualified signal is the governed source of journey assessability and its originating observation category is the governed source of dimension assessability.

The output is exactly one assessment for exactly one evaluation.

## 5. Non-scope

REQ-036 does not implement or redefine:

- acquisition or crawling;
- HTML parsing or raw evidence inspection;
- evidence normalization;
- observation extraction;
- signal qualification;
- finding creation;
- deficiency analysis;
- limitation creation;
- guest-journey analysis;
- recommendation generation;
- report generation;
- persistence;
- APIs/UI;
- booking/OTA integrations;
- Google integrations;
- AI/LLM/model calls;
- coverage classification;
- scoring, percentages, ranking, severity, or prioritization;
- new observation categories;
- a second journey taxonomy;
- a second dimension taxonomy.

REQ-036 must not move REQ-033 calibration into derivation.

## 6. Existing upstream facts

### 6.1 Observation categories

The repository has exactly six hospitality observation categories:

- `HOTEL_IDENTITY`
- `ROOMS`
- `AMENITIES`
- `CONTACT`
- `BOOKING`
- `DINING`

REQ-019 establishes that these are direct, discovered observations only. Missing observation is not a negative hotel fact.

### 6.2 Qualified signal journey mappings

REQ-021 and the current signal implementation establish these mappings:

| Observation category | Qualified journey stages | Coverage meaning |
|---|---|---|
| `HOTEL_IDENTITY` | `DISCOVER`, `UNDERSTAND` | Those stages have directly observed identity information and are assessable. |
| `ROOMS` | `EXPLORE` | Explore has directly observed room information/entry-point evidence and is assessable. |
| `AMENITIES` | `UNDERSTAND`, `EXPLORE` | Those stages have directly observed guest-facing amenity information and are assessable. |
| `CONTACT` | `DISCOVER` | Discover has a directly observed guest contact/location path and is assessable. |
| `BOOKING` | `BOOK` | Book has a directly observed booking entry point and is assessable. |
| `DINING` | `UNDERSTAND`, `EXPLORE` | Those stages have directly observed guest-facing dining information and are assessable. |

These mappings are reused; REQ-036 does not create a second journey taxonomy.

### 6.3 Existing nine dimensions

The repository's authoritative dimension enum contains exactly:

1. `HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING`
2. `DISCOVERABILITY_AND_NAVIGATION`
3. `ROOMS_AND_ROOM_INFORMATION`
4. `AMENITIES_AND_GUEST_FACING_INFORMATION`
5. `CONTACT_AND_LOCATION`
6. `BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS`
7. `TRUST_AND_CLARITY`
8. `MOBILE_AND_TECHNICAL_GUEST_EXPERIENCE`
9. `SEO_AND_STRUCTURED_DATA_SUPPORTING_SIGNALS`

All nine are intended dimensions for every evaluation.

### 6.4 Existing category-to-dimension mappings

The current governed mapping is:

| Observation category | Dimension | Meaning |
|---|---|---|
| `HOTEL_IDENTITY` | `HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING` | Direct hotel/property identity information can be assessed. |
| `ROOMS` | `ROOMS_AND_ROOM_INFORMATION` | Direct room-related information can be assessed. |
| `AMENITIES` | `AMENITIES_AND_GUEST_FACING_INFORMATION` | Direct amenity/facility information can be assessed. |
| `CONTACT` | `CONTACT_AND_LOCATION` | Direct contact/location information can be assessed. |
| `BOOKING` | `BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS` | A booking entry point can be assessed as a discoverability/journey signal. |
| `DINING` | `AMENITIES_AND_GUEST_FACING_INFORMATION` | Dining is guest-facing information under the existing amenities dimension. |

There is no separate Dining dimension.

### 6.5 Existing trust semantics

`TRUST_AND_CLARITY` is not established by a generic identity signal, contact signal, or absence of information.

REQ-028 establishes one currently governed trust-assessment fact: a material cross-source hotel identity conflict within the same evaluation. The conflict is established from typed domain data (`HOTEL_IDENTITY` category, same evaluation, different source references, materially different normalized observed identity values), not from human-readable finding text.

REQ-036 may therefore mark `TRUST_AND_CLARITY` assessable only when that existing typed conflict contract is satisfied.

No other trust semantics are invented here.

### 6.6 Existing limitations

`HospitalityAnalysisLimitationService` currently supports `UNABLE_TO_VERIFY` only for:

- `HTTP_ERROR`
- `TIMEOUT`
- `REDIRECT_LIMIT_EXCEEDED`
- `RESPONSE_TOO_LARGE`
- `NETWORK_ERROR`

The limitation preserves optional hospitality categories and journey stages supplied by an existing caller. The current analysis pipeline creates acquisition limitations with empty category/journey sets, so those limitations currently do not establish any specific limited journey or dimension scope.

REQ-036 must preserve this distinction: a limitation without explicit governed category/journey scope is still a limitation, but it cannot be assigned to a journey stage or dimension merely because the acquisition failed.

`UNSUPPORTED_SCHEME` and `INVALID_TARGET` are not currently limitation-producing conditions under REQ-023 and must not be reclassified by REQ-036.

## 7. Journey-stage derivation

The intended journey scope is always the complete existing five-stage set:

```text
DISCOVER
UNDERSTAND
EXPLORE
TRUST
BOOK
```

### 7.1 Assessable journey stages

A journey stage is **assessable** for an evaluation when at least one qualified, governed `HospitalityAnalysisSignal` belonging to that evaluation explicitly contains that stage.

The signal must satisfy the existing signal qualification boundary. REQ-036 does not qualify observations itself.

Therefore:

```text
qualified signal
    ↓
explicit journey stage on signal
    ↓
assessable journey stage
```

Multiple signals mapping to the same stage do not create duplicate scope.

### 7.2 Limited journey stages

A journey stage is **limited** only when an existing `HospitalityAnalysisLimitation` explicitly carries that journey stage for the same evaluation.

A limitation's acquisition outcome alone is insufficient to choose a journey stage.

Therefore:

```text
explicit stage-scoped limitation
    ↓
limited journey stage
```

An unscoped limitation contributes no limited journey stage.

### 7.3 No inference

The derivation must not mark a stage assessable because:

- another stage is assessable;
- another dimension is assessable;
- a page was acquired;
- a finding exists elsewhere;
- a hotel identity is known;
- a recommendation exists;
- the stage name exists in the taxonomy.

The derivation must not mark a stage limited because an unrelated source failed.

## 8. Nine-dimension derivation

The intended dimension scope is always all nine existing dimensions.

### 8.1 Assessable dimensions

For each qualified signal, inspect its originating observation category and apply only the existing category-to-dimension mapping in Section 6.4.

A dimension becomes assessable when at least one qualified signal from the same evaluation maps to that dimension.

The resulting rules are:

| Source observation/signal | Dimension | Assessable meaning |
|---|---|---|
| `HOTEL_IDENTITY` signal | `HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING` | Identity/property understanding has directly observed evidence. |
| `ROOMS` signal | `ROOMS_AND_ROOM_INFORMATION` | Room information has directly observed evidence. |
| `AMENITIES` signal | `AMENITIES_AND_GUEST_FACING_INFORMATION` | Guest-facing amenities/facilities have directly observed evidence. |
| `CONTACT` signal | `CONTACT_AND_LOCATION` | Contact/location information has directly observed evidence. |
| `BOOKING` signal | `BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS` | A booking entry point has directly observed evidence. |
| `DINING` signal | `AMENITIES_AND_GUEST_FACING_INFORMATION` | Dining information is assessed within the existing amenities/guest-facing dimension. |

### 8.2 Trust dimension

`TRUST_AND_CLARITY` becomes assessable only when the existing typed material identity-conflict contract is satisfied for the same evaluation.

A normal `HOTEL_IDENTITY` signal alone does not establish trust coverage.

A `CONTACT` signal does not establish trust coverage.

A `BOOKING` signal does not establish trust coverage.

### 8.3 Unsupported dimensions

The following remain intended but not assessable unless a future governed observation/signal contract explicitly supports them:

- `DISCOVERABILITY_AND_NAVIGATION`
- `MOBILE_AND_TECHNICAL_GUEST_EXPERIENCE`
- `SEO_AND_STRUCTURED_DATA_SUPPORTING_SIGNALS`

Their presence in the intended taxonomy is not evidence of assessment.

REQ-036 must not create synthetic mappings to these dimensions merely to increase coverage.

### 8.4 Dimension mapping is many-to-one where governed

`DINING` intentionally maps to `AMENITIES_AND_GUEST_FACING_INFORMATION`.

The derivation must not create a `DINING` dimension.

A future source category may map to more than one dimension only when a future governed requirement explicitly establishes that mapping. REQ-036 does not invent additional multi-dimension mappings.

## 9. Assessable semantics

`ASSESSABLE` means the current governed analysis has sufficient direct, evaluation-attributed information to responsibly perform analysis in that scope area.

For REQ-036, sufficient information is established only through the explicit mappings above:

- a qualified signal explicitly maps to the journey stage; and/or
- a qualified signal's observation category explicitly maps to the dimension; and/or
- the existing typed identity-conflict contract establishes `TRUST_AND_CLARITY`.

Assessability is therefore a factual capability boundary, not a quality judgment.

Do not use:

- page count;
- word count;
- HTTP status alone;
- finding count;
- recommendation count;
- number of observations alone;
- arbitrary thresholds;
- absence of evidence;
- human-readable finding text;
- current time;
- external metrics.

## 10. Limited semantics

`LIMITED` means the analysis contract explicitly records that a scope area could not be reliably verified, without asserting that the hotel lacks the capability.

For REQ-036, limited scope can only come from an existing limitation carrying explicit category and/or journey-stage context.

Rules:

1. An explicit limitation journey stage becomes a limited journey stage.
2. An explicit limitation observation category maps to a limited dimension using the same category-to-dimension mapping as Section 8.1.
3. An unscoped limitation creates no limited journey or dimension scope.
4. A successful evidence item creates no limitation.
5. `NOT_ATTEMPTED` is not invented or mapped because the current acquisition model has no such `AcquisitionOutcome`.
6. `UNSUPPORTED_SCHEME` and `INVALID_TARGET` do not become limitations under the current contract.
7. A limited area remains distinct from assessable evidence.

REQ-036 must not infer a limitation from merely missing observations.

## 11. Covered semantics

For both journey stages and dimensions:

```text
COVERED = ASSESSABLE ∪ LIMITED
```

This is representation semantics inherited from REQ-035 and classification semantics inherited from REQ-033/034.

However:

```text
ASSESSABLE ≠ LIMITED
```

Limited scope contributes to `coveredJourneyStages()` / `coveredDimensions()` but does not become assessable evidence.

REQ-036 must never collapse the two sets.

## 12. Absence / non-inference rules

The following are mandatory:

```text
absence of evidence ≠ evidence of absence
```

Therefore:

- no signal does not mean a feature is absent;
- no signal does not automatically mean the scope is limited;
- no signal does not automatically mean the scope is assessable;
- no finding does not mean no hotel capability exists;
- acquisition failure does not mean a hotel feature failed;
- inability to verify does not mean the hotel lacks the feature;
- unsupported dimension does not mean the hotel has a deficiency;
- missing BOOKING signal does not mean booking is unavailable, broken, or unusable.

In particular:

```text
successful room evidence
+
no BOOKING signal
        ↓
no automatic booking limitation
no automatic booking deficiency
no automatic booking failure
```

REQ-036 must preserve the distinction between “not observed” and “unable to verify” where the current domain supports it.

## 13. Dining semantics

`DINING` remains an observation category only.

The authoritative derivation is:

```text
DINING observation/signal
        ↓
AMENITIES_AND_GUEST_FACING_INFORMATION dimension
```

Its current qualified journey mapping remains:

```text
DINING
  ↓
UNDERSTAND + EXPLORE
```

No separate Dining dimension may be introduced.

Observed dining information does not establish dining quality, availability, operating hours, reservation success, or service quality unless another governed contract explicitly establishes those facts.

## 14. Booking semantics

`BOOKING` establishes only the existing qualified signal semantics: a booking entry point was observed.

It contributes to:

```text
BOOK journey stage
BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS dimension
```

It does not establish:

- booking transaction success;
- inventory availability;
- rate availability;
- reservation acceptance;
- payment success.

Conversely, absence of a `BOOKING` signal does not establish a booking deficiency or limitation.

REQ-027's booking non-inference rule remains authoritative.

## 15. Evaluation identity

The derivation is strictly evaluation-local.

Exactly one assessment is produced for exactly one evaluation:

```text
evaluation E
   ↓
one HospitalityCoverageAssessment(E)
```

All observations, signals, identity-conflict facts, and limitations contributing to the assessment must belong to that same evaluation.

Cross-evaluation mixing is invalid and must fail rather than silently discard, merge, or reassign data.

No new evaluation identity model is introduced. The existing `UUID evaluationId` is retained.

## 16. Determinism

For identical:

```text
evaluation identity
+
set of governed observations/signals/typed scope limitations
```

the derived `HospitalityCoverageAssessment` must be identical.

The derivation must contain no:

- randomness;
- current-time dependency;
- external API/network access;
- AI/LLM/model call;
- mutable global state;
- ordering-dependent classification.

Set membership, not iteration order, determines scope.

## 17. Failure / validation expectations

A later implementation must validate at least:

1. non-null evaluation identity;
2. every contributing observation/signal belongs to the requested evaluation;
3. every contributing limitation belongs to the requested evaluation;
4. no cross-evaluation identity-conflict facts are accepted;
5. intended journey stages are exactly the five existing stages;
6. intended dimensions are exactly the nine existing dimensions;
7. assessable and limited scope are disjoint before constructing the REQ-035 assessment;
8. derived assessable/limited scope remains within intended scope;
9. no unsupported dimension is introduced;
10. no second journey taxonomy is introduced.

An invalid cross-evaluation input must fail rather than produce a mixed assessment.

An empty assessable/limited scope is valid and must produce a structurally valid assessment capable of being classified as `INSUFFICIENT_COVERAGE` by REQ-034.

The derivation itself must not catch an invalid condition and convert it into a negative hotel conclusion.

## 18. Relationship to REQ-035

REQ-035 is the representation contract.

REQ-036 is the derivation contract.

```text
REQ-036
facts → assessment fields

REQ-035
assessment fields → immutable valid assessment
```

REQ-036 must construct or supply the existing `HospitalityCoverageAssessment`; it must not add fields to that model.

REQ-035 remains responsible for structural invariants such as intended-scope containment and assessable/limited disjointness.

REQ-036 is responsible for the semantic rules that determine which scope items are populated.

## 19. Relationship to REQ-033/034

REQ-033 defines product classification calibration.

REQ-034 implements that calibration.

REQ-036 must not duplicate or alter those thresholds.

The downstream sequence is:

```text
REQ-036 derivation
        ↓
HospitalityCoverageAssessment
        ↓
REQ-034 classifier
        ↓
HospitalityAnalysisCoverageState
```

REQ-034 must remain the sole owner of:

- `SUBSTANTIALLY_ASSESSED`;
- `PARTIALLY_ASSESSED`;
- `INSUFFICIENT_COVERAGE`.

REQ-036 must not decide those states.

## 20. Relationship to REQ-032

REQ-032 remains `BLOCKED` until the governed derivation boundary is available for orchestration.

Once REQ-036 is implemented, the intended sequence becomes:

```text
acquisition
→ evidence
→ governed observations/signals
→ REQ-036 coverage assessment derivation
→ HospitalityCoverageAssessment
→ REQ-034 coverage classification
→ HospitalityAnalysisCoverageState
→ deterministic analysis
→ journey
→ recommendations
→ report
```

REQ-032 must consume the assessment/classifier boundary rather than recreate the derivation rules.

REQ-032 must remain a thin coordinator and must not infer coverage itself.

## 21. Acceptance criteria

1. All five existing journey stages are explicitly addressed.
2. All nine existing hospitality dimensions are explicitly addressed.
3. Existing six observation categories are preserved without taxonomy changes.
4. Existing signal-to-journey mappings are reused exactly.
5. Existing category-to-dimension mappings are reused exactly.
6. `DINING` maps to `AMENITIES_AND_GUEST_FACING_INFORMATION` and does not create a new dimension.
7. `TRUST_AND_CLARITY` is assessable only through the existing typed material identity-conflict contract.
8. `DISCOVERABILITY_AND_NAVIGATION`, `MOBILE_AND_TECHNICAL_GUEST_EXPERIENCE`, and `SEO_AND_STRUCTURED_DATA_SUPPORTING_SIGNALS` remain unsupported unless a future governed source explicitly supports them.
9. A qualified signal is sufficient to establish assessability only for the stage/dimension explicitly governed by its mapping.
10. A limitation establishes limited scope only when it explicitly carries category and/or journey-stage context.
11. Unscoped limitations do not become arbitrary limited dimensions/stages.
12. Missing observations do not become limitations or deficiencies.
13. Missing BOOKING evidence does not become booking deficiency or booking failure.
14. Covered scope is assessable ∪ limited.
15. Limited scope never counts as assessable evidence.
16. Cross-evaluation inputs are rejected.
17. Empty scope is valid.
18. The assessment retains exactly the existing evaluation identity.
19. Derivation is deterministic and contains no AI, network, time, or mutable global state.
20. REQ-033/034 classification states and thresholds remain outside REQ-036.
21. No scoring, percentages, ranking, severity, recommendations, findings, or deficiencies are introduced.
22. The later implementation can be completed without inventing product semantics beyond this contract.

## 22. Explicit non-goals

REQ-036 is not:

- a coverage classifier;
- a quality score;
- a hotel-quality assessment;
- a deficiency engine;
- a recommendation engine;
- a journey-success evaluator;
- a generic website audit;
- a crawler/parser;
- an evidence processor;
- an AI interpretation layer;
- an acquisition failure policy beyond existing limitation semantics.

## 23. Open issues

### OPEN ISSUE — Limitation attribution remains upstream-context dependent

The current `HospitalityAnalysisLimitationService` permits explicit category/journey context, but the current acquisition-created limitations pass empty category/journey sets. Therefore REQ-036 cannot responsibly derive limited journey/dimension scope for those failures today.

This is not a missing REQ-036 mapping. It is an existing upstream limitation-context gap. REQ-036 deliberately records the limitation as unscoped rather than guessing which hotel capability was inaccessible.

A future requirement may define deterministic limitation attribution if product evidence justifies it.

### OPEN ISSUE — Unsupported dimensions remain unsupported

The current governed observation/signal model does not support responsible assessability for `DISCOVERABILITY_AND_NAVIGATION`, `MOBILE_AND_TECHNICAL_GUEST_EXPERIENCE`, or `SEO_AND_STRUCTURED_DATA_SUPPORTING_SIGNALS`.

REQ-036 intentionally does not invent mappings for them. A future requirement may expand upstream evidence/observation capability if real product evaluations show that expansion is valuable.

## 24. Runtime implementation record

### Implementation mapping

The runtime derivation is implemented by `HospitalityCoverageAssessmentDerivationService` in the analysis domain. It consumes the existing qualified `HospitalityAnalysisSignal` model, existing typed `HospitalityFinding` identity-conflict representation, and existing `HospitalityAnalysisLimitation` model, and constructs the existing immutable `HospitalityCoverageAssessment`.

### Classes added/changed

Added:

- `backend/src/main/java/com/antenapro/hotelcheck/analysis/HospitalityCoverageAssessmentDerivationService.java`
- `backend/src/test/java/com/antenapro/hotelcheck/analysis/HospitalityCoverageAssessmentDerivationServiceTest.java`

Changed:

- this requirement implementation record only.

No classifier, analysis orchestration, acquisition, persistence, API, UI, or taxonomy classes were changed.

### Journey derivation

The service reuses the qualified signal's explicit journey stages. Therefore the governed mappings remain:

- `HOTEL_IDENTITY` → `DISCOVER`, `UNDERSTAND`
- `ROOMS` → `EXPLORE`
- `AMENITIES` → `UNDERSTAND`, `EXPLORE`
- `CONTACT` → `DISCOVER`
- `BOOKING` → `BOOK`
- `DINING` → `UNDERSTAND`, `EXPLORE`

### Dimension derivation

The service reuses the governed category-to-dimension mapping:

- `HOTEL_IDENTITY` → `HOTEL_IDENTITY_AND_PROPERTY_UNDERSTANDING`
- `ROOMS` → `ROOMS_AND_ROOM_INFORMATION`
- `AMENITIES` → `AMENITIES_AND_GUEST_FACING_INFORMATION`
- `CONTACT` → `CONTACT_AND_LOCATION`
- `BOOKING` → `BOOKING_DISCOVERABILITY_AND_JOURNEY_SIGNALS`
- `DINING` → `AMENITIES_AND_GUEST_FACING_INFORMATION`
- typed material same-evaluation cross-source identity conflict → `TRUST_AND_CLARITY`

No synthetic mappings were added for discoverability/navigation, mobile/technical experience, or SEO/structured-data supporting signals.

### Assessable semantics

Assessable journey scope comes only from explicit journey stages on `QUALIFIED` signals. Assessable dimension scope comes only from the governed originating observation category mapping. `TRUST_AND_CLARITY` is added only when the existing typed `HospitalityFinding.isIdentityConflictWith(...)` contract identifies a material same-evaluation cross-source identity conflict.

### Limited semantics

Limited journey scope comes only from explicit limitation journey stages. Limited dimension scope comes only from explicit limitation categories using the same governed category-to-dimension mapping. Unscoped limitations remain unscoped.

### Evaluation isolation

The service validates the requested evaluation identity against every signal, typed identity-conflict fact, and limitation and rejects mismatches rather than filtering or reassigning them.

### Determinism and immutability

The derivation contains no network, persistence, current-time, AI, randomness, or mutable global state. It uses deterministic enum-set accumulation and constructs the existing immutable assessment, whose defensive-copy and structural-invariant behavior remains owned by REQ-035.

### Tests

Focused tests cover:

- all governed journey mappings;
- all supported dimension mappings;
- unsupported dimensions remaining unsupported;
- Dining mapping without a Dining dimension;
- missing Booking non-inference;
- explicit limited journey/dimension scope;
- unscoped limitations;
- typed material identity conflict trust coverage;
- non-conflicting identity observations;
- cross-evaluation rejection;
- evaluation identity and structural invariants;
- deterministic repeated derivation;
- returned assessment collection immutability.

### Validation

- focused REQ-036 tests: implemented in `HospitalityCoverageAssessmentDerivationServiceTest`;
- complete backend Maven test suite: required in CI;
- Backend Validation workflow: required against the exact final PR head;
- diff scope inspection: completed before PR creation;
- no network, persistence, API/UI, classifier invocation, orchestration, new taxonomy, scoring, or recommendation logic introduced.

### Known limitations

The current acquisition-created limitations still carry empty category/journey scope, so REQ-036 intentionally cannot attribute those failures to arbitrary hospitality stages or dimensions. The three unsupported dimensions remain unsupported by the current upstream observation/signal model.

### Final status

**PR_READY** — runtime implementation is complete for the governed REQ-036 contract, pending orchestrator review of the final PR head and CI validation.

## Governance

REQ-036 is now a runtime implementation boundary. The derivation implementation does not replace REQ-035 representation, REQ-034 classification, or the blocked REQ-032 orchestration boundary.

REQ-032 remains `BLOCKED` until the complete derivation → assessment → classifier → analysis sequence is available for orchestration.

**STOPPING FOR ORCHESTRATOR REVIEW.**
