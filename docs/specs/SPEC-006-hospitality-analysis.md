# SPEC-006 — Hospitality Analysis Contract

**Status:** SPECIFIED  
**Version:** 1.0  
**Requirement:** REQ-006  
**Implementation:** Not started

## Purpose

Define the implementation-neutral product/domain contract for turning an evaluation's available evidence into a hospitality-specific analysis.

The governing relationship is:

**Hotel input → Evaluation → Evidence → Hospitality analysis → Analysis result + interactive hotel preview**

The analysis answers the product question:

> Can a guest find, understand, trust, explore, and book this hotel online?

This specification defines what the analysis means, what findings may claim, how evidence limitations are represented, and how the eventual preview can consume analysis responsibly. It does not define algorithms, technology, scoring formulas, UI, or preview runtime.

## Relationship to prior specifications

- `SPEC-001` establishes the hospitality-first product boundary, guest journey, provenance states, public-access boundary, and eventual report + interactive preview outcome.
- `SPEC-002` establishes the canonical evaluation input boundary and unresolved-target semantics.
- `SPEC-003` establishes evaluation lifecycle, partial-failure, retry, and completion semantics.
- `SPEC-004` establishes controlled public evidence acquisition and acquisition limitations.
- `SPEC-005` establishes evidence identity, provenance, source relationships, conflicts, freshness, availability, and finding traceability.

This specification refines only the analysis capability and does not authorize changes to those contracts.

## Hospitality-first analysis principle

Antena Hotel Check is not primarily a generic SEO checker, generic website audit, OTA/competitor scraping product, or generic AI website generator.

Technical website health can support the analysis when it materially affects a guest journey, but hospitality and guest experience remain the organizing purpose. A technical observation should be expressed in terms of its guest-facing relevance where applicable rather than allowing technical checks to replace the hospitality analysis.

## Analysis scope and dimensions

The analyzer should organize observations into the following product dimensions. These are semantic categories, not an implementation checklist or algorithm.

### 1. Hotel identity and property understanding

Purpose: determine whether a prospective guest can identify what the property is and understand the basic proposition.

Relevant observations may include:

- recognizable hotel/property name;
- location and address information;
- property description and positioning;
- contact/discovery information;
- important basic guest-facing facts where publicly observable.

The analysis must distinguish an observed absence from inability to access or verify the relevant information.

### 2. Discoverability and navigation

Purpose: determine whether a guest can find the important parts of the hotel experience through the public digital presence.

Relevant observations may include:

- clear entry points to important hotel information;
- navigation to rooms, amenities, location, contact, dining, and booking where applicable;
- relevant guest-facing pages being reachable;
- material navigation barriers that prevent progression.

### 3. Rooms and room information

Purpose: determine whether a guest can discover and understand available room offerings.

Relevant observations may include:

- room listing discoverability;
- room names and descriptions;
- occupancy or room attributes where stated;
- meaningful room-detail navigation;
- visible information needed to understand the room before booking.

A missing observation caused by an inaccessible room page must not be reported as proof that the hotel has no rooms or room information.

### 4. Amenities and guest-facing information

Purpose: determine whether important information needed to evaluate a stay is discoverable and understandable.

Relevant observations may include:

- amenities and facilities;
- guest services;
- check-in/check-out or relevant policies;
- dining information where applicable;
- other material guest-facing information within the evaluation scope.

### 5. Contact and location

Purpose: determine whether a guest can identify where the property is and how to contact or reach it.

Relevant observations may include:

- address/location;
- phone/email/contact paths;
- map or direction links;
- materially useful location context.

### 6. Booking discoverability and journey signals

Purpose: determine whether a guest can identify how to book and, where technically possible, reach the relevant booking journey.

Relevant observations may include:

- visible booking calls to action;
- reachable booking links or booking engine destinations;
- whether the public journey reaches a relevant booking step;
- material barriers or unclear handoffs;
- third-party booking limitations.

The analysis does not guarantee or require successful transaction completion. A third-party flow that cannot be reliably observed must be represented as a limitation rather than silently classified as a failed hotel booking capability.

### 7. Trust and clarity

Purpose: determine whether the public experience gives a guest enough clear, consistent information to proceed confidently toward exploration or booking.

Relevant observations may include:

- consistency of important hotel information across observed sources;
- clarity of guest-facing descriptions;
- material information gaps;
- contradictory public information;
- transparency of booking/contact paths.

Trust findings must remain grounded in observable evidence and must not speculate about hotel intent, quality, or reputation beyond what the evidence supports.

### 8. Mobile and technical guest experience

Purpose: identify technical conditions that materially affect a guest's ability to discover, understand, explore, trust, or book.

Relevant observations may include:

- mobile usability barriers observable during the evaluation;
- materially broken or inaccessible guest-facing paths;
- meaningful performance problems where observable;
- accessibility barriers affecting important guest journeys;
- technical failures that prevent access to relevant content.

Technical observations are supporting evidence for the hospitality journey, not a separate generic audit objective.

### 9. SEO and structured-data supporting signals

Purpose: capture technical discoverability or structured information that materially supports the hotel guest journey.

Relevant observations may include:

- useful search-facing metadata;
- hotel/property structured information where observable;
- relevant indexability/discoverability signals.

These observations must remain subordinate to the hospitality analysis. This specification does not define SEO scoring or search-engine ranking predictions.

## Guest journey lens

The analysis uses the established journey:

**Discover → Understand → Explore → Trust → Book**

### Discover

Can a prospective guest locate the hotel and reach important guest-facing information?

Evidence may include identity, navigation, public entry points, location, contact, and relevant technical accessibility.

### Understand

Can the guest understand what the property is, where it is, what it offers, and what information matters before considering a stay?

Evidence may include property descriptions, location, amenities, policies, dining, and clear basic information.

### Explore

Can the guest meaningfully explore rooms and other relevant aspects of the stay?

Evidence may include room listings, room details, amenities, dining, location, media or relevant guest-facing pages, and navigation between them.

### Trust

Is the information sufficiently clear, consistent, attributable, and transparent for the guest to continue toward booking?

Evidence may include consistency across observed sources, clear contact/booking paths, material information gaps, and conflicts.

### Book

Can the guest discover and reach a relevant booking journey where technically possible?

Evidence may include booking calls to action, booking links, booking-engine handoffs, and observed reachability. Third-party restrictions and transaction limitations must remain explicit.

A finding may affect more than one journey stage. The analysis should preserve the relationship rather than forcing every observation into exactly one stage.

## Findings

A **finding** is an analysis-level interpretation or observation about the hotel's guest-facing digital experience derived from available evidence and/or an explicit evidence limitation.

A finding is distinct from its supporting evidence and from the acquisition outcome that produced that evidence.

Conceptually a finding should be able to express:

- journey stage(s) and analysis dimension(s);
- the guest-facing observation or interpretation;
- factual/observational basis;
- importance/severity semantics;
- supporting evidence references;
- conflicting evidence where material;
- evidence limitations or inability to verify;
- whether the finding is observed, inferred, or otherwise qualified;
- a recommendation or explanation where appropriate.

This is a domain contract, not a required database or API shape.

## Finding status and evidence limitations

The analysis must distinguish at least these product-level states:

### Verified / observed finding

A relevant condition is directly supported by available evidence observed within the evaluation scope. The finding may be stated as an observed fact subject to the source and freshness limitations of that evidence.

### Limitation / unable to verify

The relevant source, page, or flow could not be observed reliably because of an access restriction, failure, unsupported behavior, incomplete acquisition, or other material limitation.

This state means the analyzer cannot establish the condition. It must not be phrased as proof that the hotel lacks the feature.

### Insufficient evidence

Some evidence exists, but it is not sufficient to support the intended conclusion. This may occur because evidence is incomplete, indirect, stale, conflicting, or otherwise too weak for an unqualified finding.

### Not applicable

The analysis area does not meaningfully apply to the evaluated property or journey based on sufficient contextual evidence. The reason should be retained where necessary to make the decision understandable.

`Not applicable` must not be used merely because evidence is missing. Missing evidence belongs to limitation or insufficient-evidence semantics.

## Observed deficiency versus inability to verify

This distinction is mandatory.

An **observed deficiency** means the analyzer observed relevant content or behavior and found a meaningful gap or problem. For example, an observed guest-facing page may contain no visible booking action where a booking action is expected within the evaluated journey.

An **inability to verify** means the analyzer could not reliably observe the relevant source or behavior. For example, a booking engine may be inaccessible because it requires an unavailable authentication path or is blocked by a permitted public-web restriction.

The second case must not be converted into the first.

Similarly, `NOT_ATTEMPTED` acquisition does not establish absence, and a failed request does not establish a missing hotel capability.

## Severity / importance semantics

The product may classify findings by qualitative importance where this is necessary to communicate guest-journey impact. The contract intentionally avoids arbitrary numerical scores.

A future implementation may use a small set of descriptive levels such as:

- **critical journey impact** — materially prevents or severely obstructs a core guest journey stage;
- **material impact** — meaningfully reduces clarity, exploration, trust, or booking progression;
- **notable improvement** — a meaningful but non-blocking opportunity;
- **informational** — useful context without a material deficiency.

These labels describe product impact, not a universal mathematical score or ranking. Their exact implementation and calibration require later validation if introduced.

A severity label must be supported by the finding's guest-journey relevance. It must not be assigned merely because a technical check has a conventional severity in another audit product.

## Evidence and provenance rules

Findings must follow `SPEC-005`.

1. Every evidence-dependent factual finding must reference its supporting evidence where available.
2. Evidence references must preserve provenance and observation context.
3. Conflicting evidence must remain visible when it materially affects the conclusion.
4. An inferred conclusion must remain distinguishable from directly discovered information.
5. Demonstration/generated information cannot be used as proof that a hotel fact was observed.
6. User-provided information, if later supported by the product, remains distinguishable from public discovery unless a separate explicit product decision establishes otherwise.
7. Stale evidence may support a finding only with an appropriate qualification when age materially affects the claim.
8. An unavailable or failed evidence item may support a limitation finding, but not a negative assertion about the hotel's underlying capability.
9. The analysis must preserve enough traceability for a later report or preview layer to understand why a finding exists.

## Conflicting evidence

When sources disagree, analysis must not silently overwrite one value.

A finding may explicitly describe an inconsistency when the disagreement is itself relevant to the guest journey. If analysis selects a representative interpretation for presentation, it must retain the conflicting source evidence and explain or qualify the selection according to evidence quality/relevance rules established by the analysis implementation.

The evidence model does not define a universal first-party-over-third-party hierarchy, and this specification does not create one. Source relationship, recency, directness, and relevance may be considered by later analysis rules, but the underlying conflict must remain traceable.

## Analysis completeness

Analysis completeness describes how much of the intended hospitality evaluation could be responsibly assessed. It must not be represented as a claim that every page or fact about the hotel was verified.

At product level, analysis should be able to distinguish at least:

- **substantially assessed** — the intended core journey areas were sufficiently observable for the analysis scope, with limitations surfaced;
- **partially assessed** — meaningful analysis exists but important journey areas or evidence sources remain unavailable or insufficient;
- **insufficient coverage** — too little relevant evidence was available to support a useful analysis across the intended scope.

These are coverage semantics, not numerical scores. Exact thresholds are implementation/product calibration concerns and must not be invented by a future implementation without a governed decision.

A partial or insufficiently covered analysis may still contain useful findings. Its coverage limitation must remain visible to downstream report/preview consumers.

## Recommendations

A recommendation is a proposed improvement or next action derived from an observed finding or a meaningful evidence gap.

Recommendations must:

- identify the relevant guest-journey stage or hospitality concern;
- be grounded in an observed finding or meaningful evidence limitation;
- avoid claiming unsupported facts about the hotel;
- distinguish what was observed from what is being suggested;
- remain hospitality-specific when the improvement concerns the guest journey;
- avoid implying that a recommendation itself proves a deficiency.

For example, if room-detail information could not be verified because the relevant public page was unavailable, a recommendation may be to provide or verify accessible room-detail information; it must not state that the hotel has inadequate room information as an established fact.

The specification does not define recommendation UI, templates, automated copy generation, or AI prompts.

## Technical observations as supporting evidence

Technical, SEO, structured-data, performance, mobile, and accessibility observations may be included when they materially affect the guest journey.

They should be translated into hospitality relevance where possible. For example, a technical failure that prevents a guest from reaching the booking path is materially relevant; a generic technical observation with no meaningful guest-facing consequence should not displace the hospitality analysis.

The analysis must not expand into unrestricted technical auditing merely because additional checks are technically possible.

## Relationship to report

The eventual owner-facing report may assemble findings, evidence/provenance, limitations, recommendations, and coverage information from this contract.

The report must preserve the distinction between:

- what was observed;
- what was inferred or interpreted;
- what could not be verified;
- what is recommended.

Report UI and presentation are outside this specification.

## Relationship to interactive preview

The eventual product flow remains:

**Hotel input → Evaluation → Evidence → Hospitality analysis → Analysis result + interactive hotel preview**

The preview may consume analysis findings and evidence-backed content to determine what verified, qualified, or demonstration information can responsibly be presented.

The preview relationship has these product constraints:

- verified/publicly observed information may be used as supported by its evidence/provenance;
- inferred information must retain its inferred status and follow later preview disclosure rules;
- demonstration/generated information must remain explicitly non-verified and must not appear to be discovered hotel fact;
- analysis limitations must not be hidden by filling gaps with invented content;
- a recommendation is not itself hotel fact and must not be rendered as though it were observed hotel information.

This specification does not define preview pages, rendering, hosting, domains, visual design, or interaction mechanics.

## Non-scope

This specification does not implement or define in detail:

- crawler/browser implementation;
- scraping infrastructure;
- search APIs;
- AI providers, prompts, or model architecture;
- numerical scoring formulas, score calibration, or rankings;
- report UI/frontend;
- preview UI/runtime/hosting;
- database schema or application classes;
- booking integrations;
- OTA integrations;
- pricing/monetization;
- production infrastructure;
- a universal source hierarchy;
- hotel quality/reputation judgments unsupported by evidence.

## Testable acceptance criteria for future implementation

A future implementation is conformant only if automated tests demonstrate at least:

1. analysis is organized around Discover, Understand, Explore, Trust, and Book rather than generic website-audit categories;
2. the major hospitality dimensions are represented and have a defined guest-facing purpose;
3. a finding is distinguishable from its supporting evidence and from evidence limitations;
4. evidence-dependent findings retain traceability to supporting evidence where applicable;
5. unavailable or failed evidence does not automatically produce a negative hotel finding;
6. a source observed to lack relevant content can be distinguished from a source that could not be observed;
7. conflicting evidence can be represented without silently overwriting one source;
8. inferred and demonstration information cannot silently become verified discovered hotel information;
9. stale/qualified evidence can be represented without falsely implying current verification;
10. analysis can represent limitation, insufficient evidence, and not-applicable states distinctly;
11. missing evidence does not become a deficiency solely because the analyzer could not access the source;
12. recommendations reference an observed finding or meaningful evidence gap and remain distinguishable from factual observations;
13. technical/SEO/accessibility observations remain supporting dimensions rather than replacing the hospitality focus;
14. analysis coverage can be represented as substantially assessed, partially assessed, or insufficient coverage without implying exhaustive verification;
15. analysis remains compatible with the lifecycle requirement that a completed evaluation ultimately has both a report and an interactive hotel preview;
16. preview consumers can identify the provenance/limitations needed to prevent inferred, generated, or demonstration content from silently appearing as verified hotel fact;
17. no implementation requires a particular framework, database, AI provider, browser, queue, cloud provider, or scoring formula.

## Future implementation handoff

Later implementation requirements may define concrete analysis checks, finding storage, report assembly, and preview generation. Those implementations must map their results to this contract without weakening hospitality-first focus, evidence traceability, limitation semantics, provenance truthfulness, or evaluation lifecycle constraints.
