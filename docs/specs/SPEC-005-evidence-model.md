# SPEC-005 — Evidence Model Contract

**Status:** SPECIFIED  
**Version:** 1.0  
**Requirement:** REQ-005  
**Implementation:** Not started

## Purpose

Define the durable product/domain contract for representing evidence collected during a hotel evaluation.

The evidence model connects the controlled public-evidence acquisition boundary in `SPEC-004` to later hospitality analysis while preserving the truthfulness and provenance requirements established by `SPEC-001` and the evaluation lifecycle established by `SPEC-003`.

The governing relationship is:

**evaluation → observations/evidence → derived representations → findings → owner-facing outcomes**

The model is implementation-neutral. It defines semantics, relationships, provenance, states, conflict handling, and traceability without prescribing database tables, Java classes, API payloads, serialization formats, storage technology, or analysis algorithms.

## Relationship to prior specifications

`SPEC-001` establishes the hospitality-first product boundary and the provenance states:

- `DISCOVERED`
- `NORMALIZED`
- `INFERRED`
- `DEMONSTRATION`

`SPEC-002` establishes the canonical evaluation request and the distinction between accepted input and a verified hotel target.

`SPEC-003` establishes the evaluation, attempt/run, capability, check, and terminal-outcome semantics. Evidence belongs to an evaluation attempt/run and does not itself determine the evaluation lifecycle state.

`SPEC-004` establishes how public evidence may be acquired, including acquisition outcomes, source relationships, timestamps, access limitations, bounded discovery, and non-bypass behavior.

This specification refines the evidence representation boundary only. It does not authorize new acquisition behavior, analysis rules, scoring, AI generation, or preview implementation.

## Core principles

1. **Evidence is traceable.** Every retained evidence item is associated with an evaluation and can be traced to its source or to an explicit derivation relationship.
2. **Observation is not the same as interpretation.** Directly observed content, normalized representations, inferences, and demonstration/generated content remain distinguishable.
3. **Provenance is retained, not overwritten.** A later transformation may create a related representation, but it must not erase the provenance of the source observation.
4. **Unavailable is not absent.** Failed, unavailable, stale, or not-attempted evidence must never be interpreted as proof that a hotel fact or capability does not exist.
5. **Conflicts remain visible.** Multiple sources and conflicting observations may coexist. The model must not silently overwrite one source with another.
6. **Source relationship matters.** First-party hotel sources, third-party public sources, user-provided information, derived information, and demonstration/generated information must remain distinguishable where relevant.
7. **Findings are traceable.** A downstream analysis finding must be able to identify the evidence supporting it and the material limitations affecting that evidence.
8. **No silent invention.** Inferred or generated information must not become verified discovered hotel information merely because it is stored in a common model.

## Evidence as a domain concept

An **evidence item** is a retained, traceable unit of information or an explicitly retained evidence limitation that can contribute to an evaluation.

An evidence item conceptually contains:

- a stable identity within the evaluation context;
- association with the evaluation and, where applicable, its attempt/run;
- a provenance state;
- source/provenance metadata;
- observation/acquisition context where applicable;
- an availability/outcome state;
- the observed or represented content/reference, where retained;
- relationships to parent/source evidence when it is derived;
- material limitations such as staleness, conflict, or access restriction.

This is a conceptual domain model, not a persistence schema.

### Evidence item versus source observation

A **source observation** is the product's record of what it observed or attempted to observe at a source at a particular time.

An **evidence item** is the durable traceable representation used by the evaluation and downstream analysis. It may represent a direct source observation or a later, explicitly related representation derived from one or more observations.

The model must preserve the relationship between an evidence item and the source observation(s) that support it. A normalized or inferred item must not appear to have been directly observed when it was not.

## Association with an evaluation

Every evidence item must belong to one evaluation context.

Where `SPEC-003` distinguishes multiple evaluation attempts/runs, an evidence item must also be attributable to the relevant attempt/run when the observation belongs to a specific execution. A later attempt must not silently reuse an earlier observation as though it occurred during the later attempt.

An evaluation may contain:

- multiple evidence items for the same conceptual hotel fact;
- evidence from different sources;
- evidence with different acquisition times;
- successful and unsuccessful observation outcomes;
- historical evidence from earlier attempts.

The model must therefore support many-to-one and many-to-many relationships between conceptual facts, source observations, evidence items, and findings without requiring a single canonical value to erase alternatives.

## Provenance states

The following four provenance states are authoritative for the product.

### `DISCOVERED`

Information directly observed from an allowed source, or an explicit record of an allowed source observation/limitation.

A `DISCOVERED` item must retain enough source and observation context to understand where and when it was observed. Acquisition failure does not create a discovered hotel fact.

### `NORMALIZED`

A representation transformed from one or more known evidence items into a consistent internal form without adding unsupported factual meaning.

A normalized item must retain references to its supporting source evidence. Normalization may standardize formatting, units, naming, or structure, but it must not silently upgrade uncertain or conflicting information into verified fact.

### `INFERRED`

An interpretation derived from evidence rather than directly observed as stated.

An inferred item must retain the evidence it depends on and remain explicitly identifiable as inferred. It cannot be represented downstream as `DISCOVERED` merely because its value appears plausible or is useful.

### `DEMONSTRATION`

Generated or demonstration-only content used to make the eventual interactive preview or other product experience explorable when verified hotel evidence is insufficient.

Demonstration content must retain its demonstration status and must not be represented as discovered or verified hotel fact. It may optionally identify the evidence or product context that motivated its generation, but such motivation does not change its provenance state.

## Provenance relationships and allowed derivation

The provenance states describe the status of a representation, not a mutable lifecycle that can silently change from one state into another.

The preferred relationship is a directed chain or graph of explicit derivation:

```text
DISCOVERED source observation
        |
        +--> NORMALIZED representation
        |
        +--> INFERRED interpretation

DISCOVERED / NORMALIZED / INFERRED evidence
        |
        +--> INFERRED interpretation
        |
        +--> DEMONSTRATION content
```

Important rules:

- A representation may reference one or many supporting evidence items.
- Creating a `NORMALIZED`, `INFERRED`, or `DEMONSTRATION` item does not mutate the source item into the new state.
- `INFERRED` does not become `DISCOVERED` because a later source happens to agree with it; instead, the later source is retained as separate evidence and the relationship may be recorded.
- `DEMONSTRATION` content never becomes verified merely because it is rendered in a guest-facing preview.
- A later observation can support or contradict an earlier representation without rewriting the earlier provenance.
- If a transformation cannot preserve a meaningful relationship to its supporting evidence, it must not be presented as a verified evidence-backed representation.

## Source semantics

Evidence must preserve the relationship between the information and its source where applicable.

At minimum, the product distinguishes:

### First-party hotel source

A public source understood to belong to the evaluated hotel/property or its directly controlled web presence, subject to the identity-resolution and acquisition rules of prior specifications.

First-party source evidence may support discovered hotel facts, but source relationship alone does not make every statement independently verified beyond what was actually observed.

### Third-party public source

A public source outside the hotel's first-party web presence, such as an externally hosted booking, dining, map, or reservation destination.

Third-party evidence can establish facts about the existence or behavior of that third-party destination, but it must not silently become a first-party hotel fact. The source relationship must remain visible to downstream consumers where it affects interpretation.

### User-provided information

Information explicitly supplied by the hotel owner/user or by an authorized product interaction.

User-provided information is not equivalent to public discovery. It may be retained as evidence/context, but its provenance must remain distinguishable from `DISCOVERED` public evidence.

If a future product maps user-provided information into an existing provenance vocabulary, it must do so through an explicit product decision rather than silently labeling user input as public discovery.

### Derived information

Information produced from one or more existing evidence items, including normalized or inferred representations.

Derived information must retain references to its supporting evidence and its derivation/provenance state.

### Demonstration/generated information

Content created by the product for demonstration, preview exploration, or other generated experiences.

It must remain distinguishable from verified hotel evidence regardless of where it is rendered or stored.

## Source location and observation context

Where a source exists, evidence should retain enough source location/reference information to identify the origin of the observation.

Conceptually this includes, where applicable:

- source URL or equivalent source reference;
- source/domain relationship;
- acquisition/observation method category from `SPEC-004`;
- acquisition/observation timestamp;
- relevant access or availability limitation;
- source-provided canonical/equivalence information when relevant;
- a reference to the retained observed content or a suitable content fingerprint/reference where raw content retention is not required.

The model must distinguish the time the source was observed from any later time when a normalized or inferred representation was created.

An evidence item must not claim a source observation timestamp if the information was never observed by the product.

## Evidence availability and observation outcome

Evidence provenance state and observation outcome are separate dimensions.

The product must preserve at least the acquisition/evidence outcomes established by `SPEC-003` and `SPEC-004`:

- `AVAILABLE` — relevant source evidence was successfully observed and retained;
- `UNAVAILABLE` — evidence could not be obtained or reliably observed because of an access, availability, unsupported-content, or product-boundary limitation;
- `FAILED` — an observation/acquisition operation encountered an operational error and did not produce the requested observation;
- `NOT_ATTEMPTED` — the source was intentionally not acquired because it was out of scope, a prerequisite was missing, or the acquisition boundary/budget prevented it.

These outcomes do not themselves determine whether a hotel feature exists.

For example:

- `FAILED` because a room page timed out does not establish that the hotel has no room information;
- `UNAVAILABLE` because a booking engine is restricted does not establish that booking is unavailable to guests;
- `NOT_ATTEMPTED` because a page was outside the evaluation scope does not establish that the page or feature is absent.

An evidence record representing an unavailable/failed/not-attempted observation may therefore be useful as a limitation/traversal record without containing a verified hotel fact.

## Observation versus represented value

A product value can be:

1. directly observed;
2. normalized from observed evidence;
3. inferred from observed/normalized evidence; or
4. generated for demonstration.

The evidence model must preserve this distinction even when the values happen to be identical.

For example, if a page states that a room accommodates two adults, that statement may be `DISCOVERED`. If the system standardizes it into a numeric occupancy field, the resulting representation may be `NORMALIZED` and must reference the discovered source. If the system concludes that another room likely has the same occupancy based on a pattern, that conclusion is `INFERRED` and must not be presented as though it was observed.

## Evidence strength and confidence semantics

The product does not require a universal numerical confidence score for evidence.

Where downstream analysis genuinely needs a qualitative assessment of support strength, the model may distinguish:

- **strong support** — directly observed, relevant evidence with clear provenance and no material contradiction known to the evaluation;
- **qualified support** — usable evidence with a material limitation, source qualification, age, or other reason that should affect interpretation;
- **weak/uncertain support** — evidence that is indirect, incomplete, conflicting, or otherwise insufficient for an unqualified assertion.

These are descriptive support semantics, not scores or rankings. They must not override provenance state, source relationship, availability outcome, or conflicts.

If no product capability requires such a support assessment, it should be omitted rather than introducing an artificial confidence field.

## Duplicate and equivalent evidence

The same conceptual hotel information may be observed through multiple URLs, pages, attempts, or sources.

The model must distinguish:

- duplicate/equivalent observations of substantially the same source content;
- separate observations of the same conceptual fact from independent sources;
- related but materially different observations.

Deduplication may reduce redundant storage or downstream processing, but it must not erase provenance that is needed to understand source, time, or attempt/run.

If two URLs are canonical/equivalent representations of the same relevant source, the model may link them as equivalent observations. It must retain enough information to reconstruct the source relationship and observation context.

Two sources that merely contain the same value must not be assumed to be duplicates; independent provenance should remain available.

## Conflicting evidence

Conflicting evidence is a first-class condition.

When sources disagree about a conceptual hotel fact, the model must retain the conflicting evidence and its provenance rather than silently overwriting one value with another.

A downstream analysis may determine that one source is more directly relevant or more recent, but that conclusion is an analysis decision and must preserve the underlying conflict. The evidence model itself does not declare a universal source hierarchy.

Where a current evaluation needs a representative value despite conflict, the selected representation must:

- retain links to the supporting source evidence;
- preserve the existence of materially conflicting evidence;
- avoid claiming certainty that the evidence does not support;
- allow downstream consumers to understand why the value was selected or qualified.

A conflict may itself be useful evidence for a finding, such as inconsistent room information across public sources. It must not be hidden merely to simplify the final representation.

## Staleness and freshness

Evidence is time-bound.

Every observed source item must preserve its observation/acquisition timestamp. A later analysis must not imply that an old observation is current when freshness materially affects the claim.

The model must support a distinction between:

- evidence that is current enough for the applicable evaluation context;
- evidence whose age is material and should be qualified as stale/possibly stale;
- evidence whose freshness cannot be established.

This specification does not define universal numeric freshness thresholds. Later analysis or product specifications may define them for particular evidence types.

A stale observation remains historical evidence. It is not deleted or rewritten solely because a newer observation exists. A newer observation may supersede it for a current view while preserving the older provenance.

## Partial, unavailable, and failed evidence

An evaluation may contain a mixture of successful observations and limitations.

The model must support retaining:

- available evidence;
- unavailable/failed observations where their limitation is material;
- intentionally not-attempted relevant sources when their omission affects interpretation;
- evidence from partial acquisition runs.

Partial evidence coverage must not be represented as exhaustive coverage unless a later product specification explicitly establishes that the relevant scope was sufficiently covered.

Downstream analysis must be able to tell the difference between:

- “the source was observed and the feature was not found in the observed content”; and
- “the source could not be observed”;

because these have different evidentiary meaning.

## Evidence supporting downstream findings

A downstream **finding** is an analysis result derived from one or more evidence items.

Every finding that makes a factual or evidence-dependent assertion must be traceable to its supporting evidence items or explicitly marked as lacking sufficient evidence.

Conceptually, a finding contains:

- its analysis meaning;
- its provenance/derivation status as defined by the later analysis specification;
- references to supporting evidence;
- material conflicting evidence when relevant;
- material limitations affecting interpretation.

The evidence model does not define the finding schema or analysis rules, but it requires traceability in both directions where useful:

```text
source observation
      ↓
evidence item(s)
      ↓
finding / analysis result
      ↓
report and/or preview content
```

If a finding is based on an inference, the supporting evidence chain must make that inferential step inspectable. If a finding is generated without sufficient discovered evidence, it must not be represented as a verified hotel fact.

## Preview truthfulness

The evidence model directly supports the `SPEC-001` requirement that an interactive preview distinguish verified/publicly discovered information from inferred, generated, or demonstration information.

Preview content derived from evidence should retain a reference to its supporting evidence or derived representation where practical.

A preview element may use:

- `DISCOVERED` or a directly supported `NORMALIZED` representation for verified/publicly observed content, subject to later preview rules;
- `INFERRED` content only when the preview explicitly communicates its non-verified status according to later preview specifications;
- `DEMONSTRATION` content only when it is clearly identified as demonstration/generated content and cannot be mistaken for a verified hotel fact.

The evidence model does not define preview UI, labels, or inspection mechanics. It only requires that the provenance information needed to enforce those rules is not lost.

## Evidence lifecycle and immutability expectations

Evidence provenance is historical context and should be treated as append-oriented at the product level.

The product should prefer creating a new observation or derived representation when information changes rather than mutating historical evidence so that a previous evaluation attempt appears to have observed the newer value.

A later correction may explicitly mark an earlier representation as superseded, contradicted, or no longer preferred for a current view, but it must not erase the underlying provenance history.

Exact persistence and immutability mechanisms remain implementation concerns.

## Security and trust boundaries

Evidence content is untrusted external or user-provided data unless a later specification establishes a stronger source relationship.

The model must not treat storage of content as proof of authenticity. Source metadata must remain separate from the product's interpretation of that content.

Evidence retained from public-web acquisition inherits the security boundaries of `SPEC-004`, including SSRF prevention, bounded acquisition, redirect controls, resource limits, and safe handling of untrusted retrieved content.

User-provided and third-party information must not be silently elevated to first-party verified hotel facts.

## Relationship to evaluation lifecycle

Evidence belongs inside the `RUNNING` evaluation work defined by `SPEC-003` and contributes to later analysis/report/preview outcomes.

Conceptually:

```text
CanonicalEvaluationRequest
        |
        v
Evaluation / Attempt
        |
        v
Evidence observations + limitations
        |
        +--> normalized representations
        +--> inferred representations
        +--> demonstration content
        |
        v
Hospitality analysis/findings
        |
        +--> owner-facing report
        +--> interactive hotel preview
```

Evidence availability or conflict does not automatically determine the evaluation terminal state. `SPEC-003` remains authoritative for whether the evaluation is `COMPLETED`, `INCOMPLETE`, `UNRESOLVED`, or `FAILED`.

A later implementation must ensure that retries/re-runs produce evidence associated with the correct attempt and do not silently replace historical observations.

## Explicit non-goals

This specification does not define or implement:

- crawler or browser implementation;
- HTTP client libraries;
- search-engine or maps APIs;
- acquisition infrastructure or proxy services;
- hotel identity resolution algorithms;
- analysis rule catalogs;
- scoring formulas or ranking;
- AI/model providers or generation architecture;
- report UI;
- preview UI/runtime or hosting;
- booking or OTA integrations;
- database schema, Java classes, API payloads, or persistence technology;
- retention duration or legal/compliance policy;
- universal confidence scores or source rankings.

Those concerns require later bounded requirements, specifications, or decision records.

## Testable acceptance criteria for future implementation

A future implementation of this contract is conformant only if automated tests demonstrate at least:

1. every retained evidence item is associated with the correct evaluation and, where applicable, evaluation attempt/run;
2. source observations preserve source reference, source relationship, acquisition/observation timestamp, acquisition method/category, and relevant availability/limitation context where applicable;
3. `DISCOVERED`, `NORMALIZED`, `INFERRED`, and `DEMONSTRATION` remain distinguishable in persisted/runtime representations;
4. creating a normalized, inferred, or demonstration representation does not mutate the supporting discovered evidence into that later state;
5. normalized and inferred representations retain traceable links to the evidence from which they were derived;
6. demonstration/generated content cannot be represented as discovered hotel fact merely because it is used in a preview;
7. first-party, third-party, user-provided, derived, and demonstration source relationships remain distinguishable where relevant;
8. multiple observations of the same conceptual fact can coexist without silent provenance loss;
9. equivalent/duplicate observations can be associated without erasing source/time/attempt history;
10. conflicting evidence from different sources is retained and cannot be silently overwritten by a single selected value;
11. stale evidence remains distinguishable from current/adequately fresh evidence when freshness is material;
12. `AVAILABLE`, `UNAVAILABLE`, `FAILED`, and `NOT_ATTEMPTED` outcomes remain distinguishable and do not by themselves become hotel findings;
13. unavailable or failed evidence is not interpreted as proof that the underlying hotel feature is absent;
14. downstream findings can reference the evidence supporting them and preserve material limitations/conflicts;
15. a finding based on inference remains traceable to the underlying evidence and cannot be represented as directly discovered;
16. evidence from separate evaluation attempts is not silently mixed as though it were observed in one attempt;
17. a later observation can supersede or contradict an earlier observation without erasing the earlier provenance;
18. the evidence model does not require a numerical confidence score, source ranking, database schema, framework, or other implementation technology;
19. evidence semantics remain compatible with `SPEC-001`, `SPEC-003`, and `SPEC-004` and preserve the eventual requirement for truthful analysis and interactive preview outcomes.

## Future implementation handoff

Later requirements may define concrete evidence acquisition, normalization, analysis, report, and preview capabilities. Each must consume and produce evidence in a way that preserves the provenance, source relationship, availability, conflict, freshness, attempt/run, and traceability semantics established here.
