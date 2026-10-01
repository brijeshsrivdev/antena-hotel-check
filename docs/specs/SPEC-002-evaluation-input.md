# SPEC-002 — Evaluation Input Contract

**Status:** SPECIFIED  
**Version:** 1.1  
**Requirement:** REQ-002  
**Implementation:** Not started

## Purpose

Define the product boundary for initiating a hotel evaluation before discovery, evidence acquisition, analysis, scoring, or preview generation begins.

The contract supports two user entry modes:

1. **Hotel identity:** hotel name + city
2. **Hotel website:** hotel website URL

The governed boundary is:

**User input → validation → canonical evaluation request → future discovery/analysis pipeline**

This specification defines what may enter that pipeline. It does not define how a hotel is discovered, crawled, analyzed, scored, or rendered.

## Product principles

This contract preserves the principles established by `SPEC-001`:

- Antena Hotel Check is hospitality-first.
- The product analyzes publicly accessible digital presence; it does not require bypassing access controls or third-party protections.
- Evidence provenance matters.
- Inferred or generated information must not silently become verified hotel facts.
- A completed evaluation eventually produces both an analysis result/report and an interactive Antena-hosted hotel preview.
- Input resolution must not silently select an unrelated property.
- The product must not become a generic SEO or website-audit tool.

## Supported input modes

### Mode A — Hotel name + city

A valid identity input contains both:

- `hotelName`: non-blank hotel/property name.
- `city`: non-blank city/locality identifying the property's location.

Both fields are required for this mode. A name without a city, or a city without a name, is not a valid identity-mode request.

### Mode B — Hotel website URL

A valid website input contains:

- `websiteUrl`: an absolute URL using the `http` or `https` scheme and containing a host.

The URL is treated as a user-supplied candidate website, not as proof that the site belongs to a particular hotel. Ownership/identity relationship is resolved later and must not be assumed at input-validation time.

### Mode C — Website URL plus complete hotel identity

A user may supply all three fields: hotel name, city, and website URL.

This is accepted as a combined request. The website URL is the **canonical evaluation target** and therefore takes precedence for downstream evaluation targeting. The hotel name and city are retained as **supplemental user-provided identity context** and must not be discarded.

Retaining that context is required so a later resolution stage can determine whether the supplied website appears to represent the named hotel. If the relationship cannot be established or appears inconsistent, the evaluation must not silently proceed as though the identity were verified.

## Input normalization

Normalization is deterministic and must not change the user's intended property.

### Text fields

For `hotelName` and `city`:

- trim leading and trailing whitespace;
- collapse repeated internal whitespace to a single space for normalized comparison/use;
- apply ordinary Unicode normalization suitable for stable comparison;
- preserve the user's original values separately for display/audit purposes;
- do not treat casing differences as different values for validation purposes;
- do not invent, abbreviate, translate, or otherwise rewrite the hotel name or city.

An input containing only whitespace is treated as blank.

### Website URL

For `websiteUrl`:

- trim surrounding whitespace;
- accept only an absolute `http` or `https` URL;
- normalize scheme and hostname casing;
- preserve the meaningful path/query components rather than inventing a new URL;
- remove a URL fragment because it is client-side navigation state and is not part of the website target;
- preserve the normalized URL as the user-provided website target.

The contract does not require the URL to be reachable at validation time. Reachability and access behavior belong to later public-evidence acquisition specifications.

## Website URL validation

The following are invalid or unsupported at the input boundary:

- blank URL;
- relative URLs such as `/hotel`;
- protocol-relative URLs such as `//example.com/hotel`;
- non-web schemes such as `javascript:`, `data:`, `file:`, `mailto:`, or other schemes;
- missing or syntactically invalid host;
- URLs containing embedded username/password credentials;
- values that cannot be parsed as a single absolute HTTP(S) URL.

A URL that passes these checks is **syntactically valid**, not verified as a hotel website.

Network security controls such as SSRF protection, private-network blocking, redirect policy, timeouts, rate limits, robots/access handling, and fetched-content isolation are outside this specification and must be addressed before network acquisition is implemented.

## Input precedence and combinations

The following rules are explicit:

| User input | Result |
|---|---|
| hotel name + city only | Accept as hotel-identity target |
| website URL only | Accept as website target |
| hotel name + city + website URL | Accept; website URL is canonical target; name/city retained as identity context |
| hotel name only, no URL | Reject as incomplete identity input; `MISSING_CITY` / `INCOMPLETE_HOTEL_IDENTITY` |
| city only, no URL | Reject as incomplete identity input; `MISSING_HOTEL_NAME` / `INCOMPLETE_HOTEL_IDENTITY` |
| hotel name + incomplete/missing city + URL | Reject as an invalid combined request; `MISSING_CITY` / `INVALID_INPUT_COMBINATION` |
| city + incomplete/missing hotel name + URL | Reject as an invalid combined request; `MISSING_HOTEL_NAME` / `INVALID_INPUT_COMBINATION` |
| blank values with no usable mode | Reject; `BLANK_VALUE` or `NO_USABLE_INPUT` as applicable |

A complete identity pair plus a valid URL is therefore the only accepted multi-mode combination. This prevents accidental partial identity data from being interpreted as authoritative hotel identity while still making the precedence rule deterministic.

Importantly, **absence of `websiteUrl` is not itself an error**: hotel name + city is a complete accepted mode. Likewise, absence of `hotelName` and `city` is not an error when a valid website URL is supplied. `MISSING_WEBSITE_URL` is therefore not a product-level invalid-input category in this contract.

## Validation outcome

Validation produces one of two product-level outcomes:

### ACCEPTED

The request satisfies one of the supported input modes and can be represented as a canonical evaluation request.

Acceptance does **not** mean that the hotel has been resolved, the website belongs to the hotel, or the website is reachable.

### REJECTED — INVALID INPUT

The request does not satisfy the input contract. The response should identify the relevant category without attempting discovery or analysis.

The product-level invalid-input categories are:

- `MISSING_HOTEL_NAME` — the user attempted an identity-based request, but no usable hotel name was supplied; this does not apply to a valid URL-only request.
- `MISSING_CITY` — the user attempted an identity-based request, but no usable city was supplied; this does not apply to a valid URL-only request.
- `INCOMPLETE_HOTEL_IDENTITY` — hotel name and city were intended as the identity input but the pair is incomplete; this does not apply to URL-only input.
- `INVALID_INPUT_COMBINATION` — supplied fields form a combination that is not one of the supported modes, such as partial hotel identity together with a URL.
- `BLANK_VALUE` — a supplied required value is blank/whitespace-only and prevents a supported mode from being formed.
- `NO_USABLE_INPUT` — no supported input mode can be formed because there is no usable hotel identity pair and no usable website URL.
- `MALFORMED_WEBSITE_URL` — a supplied website value cannot be parsed as a valid URL.
- `UNSUPPORTED_WEBSITE_URL_FORM` — a supplied URL uses an unsupported form, such as a relative, protocol-relative, non-HTTP(S), or credential-bearing URL.

These categories describe **rejected conditions**, not missing fields in the abstract. In particular, `MISSING_WEBSITE_URL` is intentionally absent because a website URL is optional when hotel name + city is supplied.

The exact transport/API representation of these categories is implementation-specific and is not defined here.

## Canonical evaluation request

An accepted request is conceptually represented as:

```text
CanonicalEvaluationRequest
├── requestInput
│   ├── hotelNameOriginal?
│   ├── cityOriginal?
│   └── websiteUrlOriginal?
│
├── normalizedInput
│   ├── hotelName?
│   ├── city?
│   └── websiteUrl?
│
└── evaluationTarget
    ├── targetType: HOTEL_IDENTITY | WEBSITE
    ├── hotelName?
    ├── city?
    ├── websiteUrl?
    └── identityContext?
```

The conceptual rules are:

- Identity-only input produces `targetType = HOTEL_IDENTITY` with normalized hotel name and city.
- URL-only input produces `targetType = WEBSITE` with normalized website URL.
- Combined input produces `targetType = WEBSITE`, with normalized hotel name/city retained as `identityContext`.
- Original user values remain distinguishable from normalized values.
- No field in this contract asserts that a hotel identity has been verified.

This representation is conceptual. It does not prescribe application classes, API payloads, database tables, serialization formats, or technology choices.

## Resolution and ambiguity behavior

Input validation and hotel resolution are separate boundaries. Validation can accept a syntactically valid request even when the eventual property cannot yet be resolved.

### Hotel name + city identifies multiple properties

If later discovery/resolution finds multiple plausible properties for the supplied hotel name + city and cannot confidently select one, the evaluation must enter an **ambiguous/unresolved target** outcome and request clarification.

The system must not silently choose one property merely because it is the first or most prominent result.

### Website does not appear to represent the named hotel

For a combined request, if later evidence indicates that the supplied website does not appear to represent the named hotel, the evaluation must enter an **identity mismatch** outcome and must not silently replace either the named hotel or the supplied website with another property.

The user must be given a path to correct the input or continue with a deliberately corrected target. This specification does not define the UI or the evidence-resolution algorithm used to establish the mismatch.

### Hotel cannot be confidently resolved

If a valid identity input cannot be resolved to a sufficiently identified property, the evaluation must be marked **unresolved** and must not proceed as though the target were known.

### Website target cannot be confidently associated with a single property

If a valid website points to a multi-property/brand-level destination and a single hotel cannot be confidently identified, the evaluation must be marked **unresolved** unless the user supplies sufficient additional identity context to identify the intended property.

These outcomes are not invalid-input errors because the original input can be valid while the real-world target remains unresolved.

## Resolution outcome categories

The downstream resolution boundary should distinguish at least:

- `RESOLVED` — a specific hotel/property can be identified with sufficient confidence for the next stage;
- `AMBIGUOUS_TARGET` — multiple plausible properties remain and clarification is required;
- `IDENTITY_MISMATCH` — supplied hotel identity and website evidence conflict materially;
- `UNRESOLVED_TARGET` — a valid request does not provide enough reliable evidence to identify a specific property.

A `RESOLVED` outcome does not make unobserved hotel facts verified; evidence provenance rules from `SPEC-001` continue to apply.

## Boundary with later capabilities

This specification ends once an accepted request has been validated and represented canonically, or an invalid request has been rejected.

It does **not** define:

- hotel discovery/search algorithms;
- search-engine or maps APIs;
- web crawling, scraping, browser automation, or public-evidence acquisition;
- robots/access-control behavior beyond the input URL syntax boundary;
- hotel identity matching algorithms or confidence calculations;
- SEO or technical website analysis;
- hospitality/guest-journey analysis;
- scoring or ranking;
- AI/model usage;
- report generation;
- preview content generation;
- interactive preview rendering or hosting;
- booking or OTA integration;
- persistence/database schema;
- authentication or tenancy architecture;
- application framework, language, datastore, queue, browser, or hosting technology.

Those capabilities require later specifications and/or decision records.

## Testable acceptance criteria for future implementation

A future implementation of this contract is conformant only if automated tests demonstrate at least:

1. hotel name + city is accepted when both are non-blank;
2. website URL is accepted when it is an absolute HTTP(S) URL with a valid host;
3. hotel name without city and without URL is rejected with an identity-incomplete category such as `MISSING_CITY` / `INCOMPLETE_HOTEL_IDENTITY`;
4. city without hotel name and without URL is rejected with an identity-incomplete category such as `MISSING_HOTEL_NAME` / `INCOMPLETE_HOTEL_IDENTITY`;
5. whitespace-only values are rejected as blank when they prevent a supported input mode from being formed;
6. leading/trailing whitespace is normalized;
7. repeated text whitespace is normalized for canonical comparison/use while original values remain available;
8. text casing differences do not cause otherwise equivalent hotel/city values to be rejected;
9. malformed, relative, non-HTTP(S), credential-bearing, or otherwise unsupported website URLs are rejected;
10. a complete hotel name + city + valid website URL is accepted with the website URL as the canonical evaluation target and the identity retained as supplemental context;
11. partial hotel identity combined with a website URL is rejected rather than silently interpreting the partial identity;
12. the canonical representation distinguishes original user input, normalized input, and evaluation target;
13. absence of `websiteUrl` does not reject a valid hotel name + city request;
14. absence of `hotelName` and `city` does not reject a valid website-only request;
15. ambiguous hotel-name/city resolution does not silently select a property;
16. a material hotel/website mismatch does not silently proceed as a verified match;
17. an unresolved valid target is represented as unresolved rather than converted into an invalid-input error;
18. no supported input mode is available when there is no usable identity pair and no usable website URL, and the request is rejected with `NO_USABLE_INPUT` (or a more specific applicable invalid-input category);
19. validation does not perform crawling, analysis, scoring, AI generation, or preview generation.

## Relationship to SPEC-001

`SPEC-002` refines the `Inputs` and identity-resolution boundary in `SPEC-001`. It does not change the product outcome, evidence states, public-access boundary, security baseline, reliability baseline, or explicit non-goals established there.
