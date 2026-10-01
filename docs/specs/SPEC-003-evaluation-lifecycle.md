# SPEC-003 — Evaluation Lifecycle

**Status:** SPECIFIED  
**Version:** 1.0  
**Requirement:** REQ-003  
**Implementation:** Not started

## Purpose

Define the product/domain lifecycle of a hotel evaluation after a valid `CanonicalEvaluationRequest` exists and before detailed public-evidence acquisition, analysis, report assembly, and interactive preview implementation are built.

An evaluation is the governed unit of work that carries one accepted hotel target through the product's analysis and preview outcomes while preserving evidence limitations, provenance, and repeatability.

The product outcome remains:

**Hotel input → analysis → analysis result + interactive hotel preview → owner explores preview**

A completed evaluation is therefore more than an analysis report: it represents availability of both owner-facing outcomes.

## Relationship to prior specifications

`SPEC-001` establishes the hospitality-first product boundary, evidence/provenance states, public-access boundary, security baseline, reliability baseline, and the requirement that a completed evaluation provides both an analysis result/report and an interactive Antena-hosted hotel preview.

`SPEC-002` establishes the accepted input boundary and the conceptual `CanonicalEvaluationRequest`. This specification begins after that request has been accepted.

This specification does not change either prior contract.

## Evaluation as a domain unit

An **evaluation** represents one governed attempt to produce a trustworthy assessment and explorable preview for one accepted canonical evaluation target.

Conceptually, an evaluation retains:

- the accepted canonical evaluation request;
- the target identity/context available at the time evaluation starts;
- the lifecycle state;
- one or more evaluation attempts/runs;
- evidence and its provenance/availability limitations;
- capability and check outcomes;
- analysis/findings derived from available evidence;
- the owner-facing report outcome;
- the interactive Antena-hosted preview outcome;
- terminal outcome and material limitations;
- retry/re-run relationship where another attempt is performed.

These are domain concepts only. This specification does not prescribe classes, tables, APIs, queues, storage technology, or other implementation mechanisms.

## Lifecycle states

The evaluation lifecycle uses the following product-level states:

### `ACCEPTED`

The input boundary has accepted a `CanonicalEvaluationRequest`. No claim has yet been made that the real-world hotel target is resolved or that any public evidence has been acquired.

### `RUNNING`

The evaluation is actively progressing through target resolution, public-evidence acquisition, analysis, report preparation, and/or preview preparation as applicable to later capabilities.

An evaluation may have successful, unavailable, or failed individual capabilities while remaining `RUNNING`.

### `COMPLETED`

The evaluation satisfies all completion criteria in this specification. Both owner-facing outcomes are available:

1. an analysis result/report; and
2. an interactive Antena-hosted hotel preview that the owner can explore.

Known evidence gaps and public-web limitations may exist in a completed evaluation, but they must be retained and surfaced rather than hidden.

### `INCOMPLETE`

The evaluation produced meaningful partial work but cannot truthfully satisfy the `COMPLETED` criteria. At least one required owner-facing outcome is unavailable, unusable, or cannot be produced with sufficient trustworthy evidence, while the evaluation itself was not invalidated by an evaluation-level failure.

`INCOMPLETE` is a terminal outcome for that evaluation attempt. It may be retryable or may require corrected input depending on the cause.

### `UNRESOLVED`

The accepted request could not be confidently associated with one specific hotel/property, or a material identity conflict prevents trustworthy continuation. The evaluation must not proceed as though an unrelated or guessed property were the target.

`UNRESOLVED` is a terminal outcome for that evaluation attempt and normally requires clarification or corrected identity context before a new attempt.

### `FAILED`

An evaluation-level failure prevented the product from producing a trustworthy evaluation outcome. Examples include a systemic failure that prevents evaluation execution or a failure that makes the resulting evaluation state internally untrustworthy.

A failed evaluation must retain the failure reason and any useful partial evidence/results. It may be retried when the underlying cause is transient or correctable.

## Allowed lifecycle transitions

The product-level transition model is:

```text
ACCEPTED
   |
   v
RUNNING
   |\
   | +--> UNRESOLVED
   | +--> FAILED
   | +--> INCOMPLETE
   | +--> COMPLETED
   |
   +----> RUNNING (continued progress/retry within the same attempt)
```

Terminal states are `COMPLETED`, `INCOMPLETE`, `UNRESOLVED`, and `FAILED`.

A terminal evaluation is not mutated back into an active state. A repeat/retry creates a new conceptual evaluation attempt/run associated with the same logical evaluation request/target. The product may present the latest eligible attempt as the current evaluation outcome, but it must preserve the relationship between attempts so prior outcomes are not silently overwritten.

`ACCEPTED → RUNNING` is the only normal start transition. An accepted request that cannot be resolved to a specific property may transition from `RUNNING` to `UNRESOLVED`; it must not be treated as a generic invalid-input rejection because input validation has already succeeded.

## Evaluation attempt/run

An **evaluation attempt/run** is one execution of the evaluation contract against an accepted canonical request.

Conceptually, each attempt has:

- a stable association to the parent evaluation request/target;
- its own start and terminal outcome;
- its own evidence observations and limitations;
- its own capability/check outcomes;
- its own report and preview production outcome;
- a reason for retry/re-run when applicable.

Attempts are conceptually distinct so a retry cannot silently erase the provenance or limitations of an earlier attempt.

A re-run should start from the accepted canonical request or an explicitly corrected request. It must not silently change the hotel target. If the user changes the target materially, that is a new evaluation request rather than an invisible retry of the old target.

## Capability, check, and evidence outcomes

The evaluation lifecycle must distinguish evaluation-level state from lower-level outcomes.

### Evidence outcome

For an individual evidence item or source observation, later specifications may use outcomes such as:

- **AVAILABLE** — evidence was successfully observed and retained with provenance;
- **UNAVAILABLE** — evidence could not be obtained or reliably observed, with the limitation recorded;
- **FAILED** — an acquisition/observation operation encountered an error, with the error recorded;
- **NOT_ATTEMPTED** — the evidence was intentionally not acquired because a prerequisite, scope rule, or capability boundary prevented it.

An unavailable or failed evidence item is not automatically an evaluation failure.

### Capability outcome

A bounded capability such as evidence acquisition, analysis, report assembly, or preview preparation may have an outcome such as:

- **SUCCEEDED** — the capability produced its expected product artifact;
- **PARTIAL** — some expected work completed but material limitations remain;
- **UNAVAILABLE** — the capability could not be performed for a known access or product-boundary reason;
- **FAILED** — the capability encountered an error preventing its expected result.

A capability failure is not automatically an evaluation-level failure. The evaluation state is determined by the overall completion criteria and whether the remaining results can be represented truthfully.

### Check outcome

An individual analysis check may be successful, unsuccessful, unavailable, or errored according to later check specifications. A check result must never be treated as equivalent to the lifecycle state of the entire evaluation.

## Partial failure principle

The failure or unavailability of one sub-check, evidence source, or capability does not automatically invalidate the entire evaluation.

For example, if a third-party booking engine prevents reliable observation of a booking flow, other public hotel information may still be analyzed and represented. The resulting limitation must be retained and surfaced.

A partially observed evaluation may still become `COMPLETED` when both required owner-facing outcomes are available and the known limitations do not make the overall result misleading.

If one required owner-facing outcome cannot be produced, the evaluation cannot be `COMPLETED`; it should become `INCOMPLETE` unless the reason is an evaluation-level failure or unresolved target, in which case the corresponding terminal state applies.

## Evidence and provenance relationship

Evidence is the traceable basis for findings, report content, and verified preview content.

The provenance states inherited from `SPEC-001` remain authoritative:

- **DISCOVERED** — directly observed in a public source;
- **NORMALIZED** — transformed into a consistent internal representation;
- **INFERRED** — derived interpretation based on evidence;
- **DEMONSTRATION** — generated content used to make a preview explorable and clearly not verified.

The lifecycle must retain enough provenance and limitation information to distinguish what was observed from what was inferred or generated.

If evidence is unavailable, the product must prefer an explicit unknown/missing state or clearly marked demonstration content over silent invention.

A `COMPLETED` evaluation does not imply that every hotel fact was verified. It means the required outcomes are available and their evidence limitations are represented truthfully.

## Relationship between evaluation outcomes

The evaluation has four related but distinct product artifacts/outcomes:

### Evidence

Observable public information and its provenance/availability limitations. Evidence is the basis for downstream interpretation but is not itself the owner-facing evaluation outcome.

### Findings / analysis

Interpretations and hospitality-focused observations derived from available evidence. Analysis must distinguish observed facts from inference and must preserve relevant limitations.

### Owner-facing report

A completed report communicates what was observed, what it means for the guest journey, material gaps, evidence/provenance, areas requiring owner verification, and public-web limitations, consistent with `SPEC-001`.

### Interactive hotel preview

A completed evaluation also provides an interactive Antena-hosted hotel preview that the owner can explore as a guest. The preview is an owner-facing product outcome, not merely an internal rendering artifact.

The detailed preview content model, renderer, runtime, hosting, and UI remain future specifications.

The report and preview are related outcomes of the same evaluation, but neither substitutes for the other when determining `COMPLETED`.

## Completion criteria

An evaluation may be presented to the owner as `COMPLETED` only when all of the following are true:

1. The evaluation originated from an accepted `CanonicalEvaluationRequest`.
2. A specific hotel/property target has been resolved sufficiently for the evaluation result being presented; the evaluation is not merely guessed or silently substituted.
3. The evaluation retains relevant evidence provenance and material acquisition limitations.
4. Analysis/findings have been produced to the extent required by later analysis specifications, without representing unavailable evidence as observed fact.
5. An owner-facing analysis result/report is available.
6. An interactive Antena-hosted hotel preview is available and explorable by the owner.
7. The report and preview do not silently present inferred, generated, or demonstration information as verified hotel facts.
8. Material unavailable/unknown evidence and public-web limitations are surfaced where they affect interpretation.
9. No evaluation-level failure remains that makes the resulting outcomes untrustworthy.

Completion does **not** require every possible check, page, booking flow, or evidence source to succeed. It requires the required owner-facing outcomes to exist and to be truthful about their limitations.

## When an evaluation is incomplete, unresolved, or failed

### `INCOMPLETE`

Use when meaningful evaluation work exists but one or more required completion outcomes cannot be provided, without evidence of an evaluation-level invalidation or unresolved target.

Examples include:

- analysis/report exists but the required interactive preview cannot be produced;
- preview exists but a required analysis/report outcome cannot be assembled;
- material capability limitations prevent a complete outcome, while the partial results remain trustworthy and useful.

### `UNRESOLVED`

Use when the target itself cannot be confidently established, including the unresolved and mismatch outcomes defined by `SPEC-002`.

Do not continue analysis against a guessed property merely to avoid an unresolved state.

### `FAILED`

Use when an evaluation-level failure prevents trustworthy evaluation completion or prevents meaningful continuation. The product should retain any partial work that remains trustworthy and identify the failure cause.

An individual check, evidence item, or capability failure alone is insufficient to mark the entire evaluation `FAILED`.

## Retry and re-run semantics

Retries and re-runs are product concepts, not infrastructure requirements.

A new attempt may be appropriate when:

- a transient public-access or execution problem prevented completion;
- a capability was unavailable and can reasonably be attempted again;
- the user corrects an unresolved or mismatched target;
- the product definition later permits a more complete evaluation without changing the intended hotel target.

A retry must preserve the original request/target relationship and must not silently substitute another hotel.

When an evaluation is re-run against the same intended target, the new attempt should be distinguishable from the previous attempt so differences in evidence, limitations, findings, report, and preview can be understood rather than overwritten without trace.

If the user materially changes the hotel identity or canonical website target, the product should treat it as a new evaluation request rather than a retry of the previous target.

## Repeatability and idempotency expectations

At the conceptual product level:

- Repeating the same accepted request should not silently create a different hotel target.
- A retry against the same target should be identifiable as a separate attempt.
- Re-running should not destroy the provenance or outcome history of earlier attempts.
- Repeated evaluations may observe different public evidence because the public web changes; the product should preserve the observation context rather than falsely implying that all runs are identical.
- If a later implementation exposes an operation that requests a repeat of an existing evaluation, repeated invocation should not accidentally merge unrelated targets or erase an existing completed outcome.

Exact request identifiers, idempotency keys, persistence rules, and concurrency mechanisms are implementation/architecture concerns and are intentionally deferred.

## Security and access constraints

The lifecycle inherits the `SPEC-001` public-access boundary and security baseline.

An evaluation must not be represented as successfully observing private or unauthorized information. Access restrictions, anti-bot protections, authentication requirements, and other acquisition limitations must be represented as unavailable/limited evidence where applicable rather than bypassed.

Later implementations must address URL validation, SSRF, resource/time limits, fetched-content isolation, untrusted HTML/script handling, evaluation isolation, secrets, abuse/rate limiting, and preview lifecycle/access controls.

This specification does not select the mechanisms used to enforce those controls.

## Owner-facing semantics

The owner-facing product must distinguish the terminal outcome from the existence of partial work.

- `COMPLETED` means the two required outcomes are available and trustworthy with surfaced limitations.
- `INCOMPLETE` means useful partial work exists but the completed-evaluation contract has not been met.
- `UNRESOLVED` means the intended hotel target is not sufficiently established.
- `FAILED` means an evaluation-level failure prevented a trustworthy evaluation outcome.

The product must not label an `INCOMPLETE`, `UNRESOLVED`, or `FAILED` evaluation as a completed evaluation merely because some analysis or preview content exists.

## Scope boundaries

This specification does not define or implement:

- crawler or scraper implementation;
- browser automation;
- search-engine or maps APIs;
- detailed public-source selection algorithms;
- hotel identity matching algorithms or confidence calculations;
- analysis rules/check catalog;
- scoring or ranking;
- AI/model architecture;
- report UI;
- preview UI or content model;
- preview renderer/runtime;
- preview hosting infrastructure;
- booking or OTA integration;
- database schema;
- authentication/tenancy implementation;
- queue/orchestration technology;
- cloud provider;
- application framework or language/runtime;
- storage/retention implementation.

Those require later requirements, specifications, or architecture decisions.

## Testable acceptance criteria for future implementation

A future implementation of this lifecycle is conformant only if automated tests demonstrate at least:

1. an accepted canonical request can enter `ACCEPTED` and then `RUNNING`;
2. only `RUNNING` evaluations can reach a terminal evaluation outcome;
3. `COMPLETED` requires both an owner-facing analysis/report outcome and an interactive Antena-hosted hotel preview outcome;
4. a missing report or missing interactive preview prevents `COMPLETED` and results in `INCOMPLETE`, `UNRESOLVED`, or `FAILED` according to the applicable cause;
5. a failed or unavailable individual evidence item does not automatically mark the evaluation `FAILED`;
6. a failed or unavailable individual capability does not automatically mark the evaluation `FAILED` when trustworthy required outcomes can still be produced;
7. material evidence limitations remain attached to the evaluation outcome and are surfaced to the owner;
8. an unresolved or materially mismatched hotel target never silently proceeds as a different property;
9. an evaluation-level failure is distinguishable from a check-level or capability-level failure;
10. terminal evaluation outcomes are not silently mutated into another outcome; a retry creates a distinguishable new attempt/run;
11. retrying the same target does not silently change the canonical target;
12. materially changing the hotel identity or website target creates a new evaluation request rather than an invisible retry;
13. previous attempt provenance and terminal outcome are retained when a new attempt is performed;
14. a completed evaluation may contain unavailable checks when those limitations are truthfully represented and the completion criteria remain satisfied;
15. verified/publicly observed, inferred, and demonstration information remain distinguishable throughout the evaluation outcome;
16. unauthorized/private information is never represented as successfully observed evidence;
17. lifecycle semantics do not require a particular framework, database, queue, browser, cloud provider, or other technology choice.

## Future implementation handoff

Later implementation requirements may define concrete capabilities such as public-evidence acquisition, analysis, report assembly, preview generation, and preview hosting. Each must map its capability/check outcomes into this lifecycle without weakening the completion, partial-failure, provenance, truthfulness, or security constraints defined here.
