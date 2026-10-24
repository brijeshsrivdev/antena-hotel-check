# Hotel Site Check — in-depth website analysis for hotels (local-only marketing tool)

## Status
- [x] Draft
- [x] Approved — Brijesh, 24 Oct 2026 (master sheet PG-06)
- [x] In Implementation — S1 ported into this repository (see Implementation record)
- [ ] Verified & Completed

**Author:** campaign-lead session, 24 Oct 2026 · **Owner:** a development session; cite this plan in commit messages.
**Build location (Brijesh, 24 Oct 2026):** this repository, `brijeshsrivdev/antena-hotel-check` — the monorepo copy of this plan (`antena` repo, `docs/plans/active/hotel-site-check-plan.md`) is the spec only; the module paths it names there (`antena-domain-growth`, `antena-console` `/tools`, `antena-sales-agent`) are patterns to copy, not places to change. Execution contract here: [requirements/REQ-038-ranking-signal-site-check-foundation.md](../../requirements/REQ-038-ranking-signal-site-check-foundation.md).
**Source evaluation:** [docs/context/hotel-check-evaluation.md](../context/hotel-check-evaluation.md) — why the earlier regex-based code in this repository is the spec, not reusable code.

> **LOCAL ONLY — NOT DEV, NOT PRODUCTION** (Brijesh, 24 Oct 2026). This tool
> runs on a developer machine (`mvn spring-boot:run` + `npm run dev`) and
> nowhere else. It is **not** deployed to `dev.antenapro.com` and **not** part
> of any `v*` release. The feature is off by default
> (`antena.sitecheck.enabled=false`); only the `local` Spring profile and a
> local `.env.local` may turn it on. Do not add it to the sitemap, to the
> `/tools` hub navigation in any built image, or to any email. Brijesh uses it
> locally on real hotel sites; when he is satisfied he records a separate
> decision in the master sheet (PG-06) before anyone talks about dev or
> production.

---

## 1. Problem & Objectives

### Problem statement
Antena sells hotels a website they own plus a booking engine. The sale starts
from a fact the owner does not have: *how their current site performs in
Google and whether a guest can book from it*. Today that fact is produced by
hand (the SEO agent's audits, `gsc_query.py`), one site at a time, and cannot
be attached to the partner sequence, the NIDHI emails or the brochure QR.

### Single core objective
**An in-depth, honest analysis of one hotel website that tells the owner what
stops it ranking better in Google — and what to do about each item.** Lead
capture is the side effect, not the product: a report nobody trusts captures
nothing.

### Objectives
- [ ] O1 Given a URL, produce a ranked list of findings in under 20 seconds, each with evidence (the exact tag, header or URL observed), impact on ranking, and a plain-language fix.
- [ ] O2 Cover the signals Google documents as ranking or eligibility factors for a local lodging business (section 3), not a generic SEO checklist.
- [ ] O3 Never report a false negative on an Antena storefront: every live tenant site must score "pass" on booking, structured data, robots and sitemap once PR #278 is released.
- [ ] O4 Email the report as PDF only to an address the visitor typed, with explicit opt-in recorded as consent source `hotel_check_form`.
- [ ] O5 Local-only, flag-gated, rate-limited, no stored HTML.

---

## 2. Existing architecture & gaps

| Piece | Where today | Gap |
|---|---|---|
| Public free tools | `antena-console/src/app/tools/{revpar,food-cost,housekeeping}-calculator` and `tools/page.tsx` hub | All client-side; no tool calls the backend |
| Lead ingestion | `antena-domain-growth/.../web/LeadIngestController.java` (`POST /api/v1/ingest/leads`, `LeadDto(phoneNumber, businessName, googleMapsId, city, category)`) | No email, no consent, no free-text result field |
| Rate limiting | `antena-platform-core/.../security/RateLimitingFilter.java`, `RateLimitProperties` | Reuse for the new public endpoint |
| Feature flags | `@ConditionalOnProperty(name = "antena.<feature>.enabled")` pattern (health monitor, alerts) | Add `antena.sitecheck.enabled` |
| Consent ledger | `antena-sales-agent/consent.py` `OPT_IN_SOURCES` | Add `hotel_check_form` |
| Manual audits | `docs/business/marketing/seo/site-audit-2026-10.md`, `client-site-seo-audit.md` (SEO agent) | The per-page table there (status, ms, title chars, description chars, canonical, H1, JSON-LD types, words, flags) is the checklist to automate |
| Reference repo | `brijeshsrivdev/antena-hotel-check` | One-page regex over headings/anchors; keep its journey-stage vocabulary (Discover / Understand / Explore / Trust / Book), drop the code |

---

## 3. Proposed solution

### Overview
One backend service in `antena-domain-growth` (`sitecheck` package) fetches up
to **five pages** of the target site (home, plus the first matching
rooms/accommodation, contact, booking and about links found on the home page),
plus `robots.txt`, `sitemap.xml` and a Google PageSpeed Insights call, and runs
a fixed set of **checks**. Each check returns `PASS | PARTIAL | FAIL | UNKNOWN`,
evidence, and a fix text. Checks roll up into five journey stages and one
0–100 score. A Next.js page under `/tools/hotel-site-check` renders the result
and offers the PDF by email.

### Key technical decisions
1. **Parse with jsoup, not regex.** Add `org.jsoup:jsoup` to the growth module. The reference repo's regex missed a `<button>Check Availability</button>` and a JSON-LD rating on an Antena storefront; the tool must read every element's text, `<script type="application/ld+json">` and `<meta>` tags.
2. **Google's own signals first.** Core Web Vitals and mobile-friendliness come from the PageSpeed Insights API (`https://www.googleapis.com/pagespeedonline/v5/runPagespeed`, key in `antena.sitecheck.psi-key`, free quota 25k/day). Everything else is derived from the fetched HTML and headers. No third-party SEO vendors.
3. **URL only in v1.** `hotelName + city` resolution needs a Places lookup and a guess; it is out of scope until the URL path is trusted.
4. **No HTML stored.** Persist the check result (scores, findings, evidence snippets ≤ 300 chars each), never the page. Response size must stay under 200 KB.
5. **Local-only by configuration, not by branch.** The code may merge to `develop`; the flag keeps it dark in dev and production. Verification (section 6) includes proving the dev and production profiles return 404 for the endpoint and that no built console image shows the page.

### Check catalogue (v1 — each line is one finding the owner can act on)

| Stage | Check | PASS when | Why it matters for ranking |
|---|---|---|---|
| Discover | HTTPS | final URL is https, no mixed-content on home | HTTPS is a ranking signal; Chrome warns otherwise |
| Discover | `robots.txt` | present, does not `Disallow: /` for `*` or Googlebot | A blocked site cannot rank at all (our own tenants were, A1) |
| Discover | XML sitemap | referenced in robots or at `/sitemap.xml`, ≥1 URL, valid XML | Discovery of room pages |
| Discover | Title tag | 30–60 chars, contains the hotel name and the town, unique per fetched page | Primary relevance signal |
| Discover | Meta description | 70–160 chars, not truncated mid-word, unique | CTR in results |
| Discover | Canonical | self-referencing on each fetched page | Duplicate-content protection |
| Discover | Indexable | no `noindex` meta or header | |
| Discover | Mobile-friendly | PSI mobile `viewport` audit passes; no horizontal overflow | Mobile-first indexing |
| Discover | Core Web Vitals | PSI field or lab LCP ≤ 2.5 s, CLS ≤ 0.1, INP ≤ 200 ms | Page-experience signal |
| Discover | Page weight | home ≤ 3 MB transferred, ≤ 60 requests | |
| Understand | One H1 | exactly one, names the property or its offer | |
| Understand | Hotel name consistency | title, H1, JSON-LD `name` and footer agree (fuzzy ≥ 0.8) | NAP consistency for local search |
| Understand | Rooms page | a rooms/accommodation page exists, ≥ 2 room types with a price or a "from ₹" | Lodging intent pages |
| Understand | Content depth | home ≥ 300 words of visible text, not a single image | Thin-content filter |
| Understand | Language | `<html lang>` set; Devanagari/English both fine | |
| Explore | Internal links | home links to rooms, contact, booking with descriptive anchor text | Crawl paths |
| Explore | Broken links | 0 of the sampled internal links return ≥ 400 | |
| Explore | Image alt text | ≥ 80 % of `<img>` have non-empty alt | Image search, accessibility |
| Trust | Structured data | JSON-LD `Hotel`/`LodgingBusiness`/`Resort` with `name`, `address` (full `PostalAddress`), `telephone`, `image`, `url` = request host | Rich result eligibility (F1, F4) |
| Trust | Rating markup is honest | if `aggregateRating` present, `reviewCount` ≥ 1 and a visible review block exists on the page | Fabricated ratings get manual actions (F2) |
| Trust | Address and phone visible | postal address and `tel:` link in page text | Local pack |
| Trust | Google Business Profile link | a `maps.google.com`/`g.page` link, or the business name appears in Maps (manual field in v1) | Local pack |
| Trust | Last-updated signal | copyright year current or `lastmod` in sitemap ≤ 12 months | Freshness |
| Book | Booking CTA | an element whose text or `href` matches book / reserve / check availability / wa.me, above the fold on home | The outcome every other signal serves |
| Book | Own booking engine | CTA leads to the same host or a known engine, not only to an OTA | Commission leakage — the Antena pitch |
| Book | WhatsApp / phone tap targets | `wa.me` or `tel:` link present | India-specific conversion path |
| Book | Booking page reachable | the CTA target returns 200 in ≤ 3 s and has a date picker or form | |

Scoring: each stage is the mean of its checks (PASS 1, PARTIAL 0.5, FAIL 0,
UNKNOWN excluded); overall = weighted mean (Discover 30, Trust 25, Book 25,
Understand 10, Explore 10). The report lists findings FAIL-first, each with
`evidence` (what we saw) and `fix` (one sentence; "Antena does this for you"
is allowed only on checks where it is true).

---

## 4. Data & API contracts

### DB
```sql
-- V<next>__site_check.sql (harmless when the feature is off; the table stays empty outside local runs)
CREATE TABLE site_check_runs (
  id            UUID PRIMARY KEY,
  requested_url TEXT NOT NULL,
  final_url     TEXT,
  host          TEXT NOT NULL,
  score         SMALLINT,
  stages_json   JSONB NOT NULL,      -- {discover:..,understand:..,explore:..,trust:..,book:..}
  findings_json JSONB NOT NULL,      -- [{check,stage,status,evidence,fix}]
  email         TEXT,                 -- only when the visitor asked for the PDF
  consent_at    TIMESTAMPTZ,          -- set with email; source = 'hotel_check_form'
  ip_hash       TEXT NOT NULL,        -- sha256(ip + daily salt) for rate limiting, never the IP
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ON site_check_runs (host, created_at DESC);
```

### API (public, unauthenticated, flag-gated)
`POST /api/v1/public/site-check` — `{ "url": "https://example.com" }` →
`202 { "runId": "<uuid>" }`. Validation: http(s) only, no private/loopback/link-local
hosts, no `*.antenapro.com` other than tenant storefront hosts, max 3 runs per
host per day and 10 per ip_hash per day (reuse `RateLimitingFilter`).

`GET /api/v1/public/site-check/{runId}` → `{ status: RUNNING|DONE|FAILED, score, stages, findings[], fetched: [{url,status,ms}], limitations[] }`.
Polling every 2 s from the page; server-side timeout 25 s, then DONE with
`UNKNOWN` for unfinished checks and a limitation line.

`POST /api/v1/public/site-check/{runId}/report` — `{ email, consent: true }` →
`202`. Renders the PDF (reuse the billing PDF generator's stack), sends it via
the platform mailer from `info@antenapro.com`, stores email + consent_at.
Rejected with 400 when `consent` is not literally `true`.

### Frontend
`antena-console/src/app/tools/hotel-site-check/page.tsx` — form, progress,
result (score dial, five stage rows, findings list with evidence toggles),
"Email me the PDF" with a checkbox whose label states what will be sent and
that we may follow up by email about Antena. Metadata: `noindex`. Card on
`tools/page.tsx` and the route itself rendered only when
`NEXT_PUBLIC_SITECHECK_ENABLED=true`, which is set in `.env.local` only and in
no Dockerfile, workflow or build arg.

### Sales-agent hook
`consent.py`: add `"hotel_check_form"` to `OPT_IN_SOURCES` with the comment
`# typed their email into the Hotel Site Check and ticked the follow-up box`.
No automation reads the table in v1; the lead session exports runs by hand.

---

## 5. Implementation slices (one PR each)

1. **S1 Backend core** — jsoup fetcher (5-page budget, 3 s connect / 8 s read, 2 MB per page, follow ≤ 5 redirects, UA `AntenaSiteCheck/1.0 (+https://antenapro.com/tools/hotel-site-check)`), check interfaces, 12 HTML-only checks, scoring, in-memory run registry, flag, rate limit, unit tests against saved fixtures (an Antena storefront HTML, a Wix/OTA-only site, a WordPress site with booking plugin).
2. **S2 Google signals** — PSI client with caching per host for 24 h; robots/sitemap checks; broken-link sampling; remaining checks.
3. **S3 Frontend page** — local-only card and route, result UI, polling, limitations shown verbatim.
4. **S4 PDF + consent** — report PDF, mailer, `site_check_runs` persistence, `hotel_check_form` consent source.
5. **S5 Tenant guard** — a test that runs the full check against a locally running renderer for one seeded tenant (`lexisinn` fixture) and asserts no FAIL on robots, sitemap, structured data, booking CTA (O3).

---

## 6. Verification

- Unit: every check has a PASS, PARTIAL, FAIL and UNKNOWN fixture.
- Contract: endpoints return 404 when `antena.sitecheck.enabled` is unset; neither the dev nor the production Spring profile sets it, and no workflow (`deploy_dev.yml`, `deploy_prod.yml`) or Dockerfile passes `NEXT_PUBLIC_SITECHECK_ENABLED`. Add a test that greps those files for the two flag names and fails if found.
- Live, locally: Brijesh runs it on lexisinnbeachresort.com, lotusholidayresort.com, betelnutresort.com (the three evaluated on 24 Oct) and three partner-referred sites; results reviewed against the SEO agent's manual audit of the same sites — any disagreement is a bug in the tool, not the audit.
- Safety: a run against `http://127.0.0.1:8080` and `http://169.254.169.254` is rejected at validation; response bodies for all six live sites stay under 200 KB.
- Done (local) = the master sheet row PG-06 updated by the campaign lead after Brijesh's sign-off. Dev and production are separate decisions recorded there.

## 7. Out of scope (v1)
Name + city resolution; crawling more than five pages; competitor comparison;
keyword rankings or Search Console data for sites we do not own; automatic
sending of the result into outreach sequences; any dev or production exposure.

---

## Implementation record

Kept by the development session; one entry per slice, updated as PRs land. The first attempt (antena monorepo PR #282, 24 Oct 2026) was denied by Brijesh — the tool is built here — and closed unmerged; S1 below is that work ported onto this repository's acquisition boundary.

| Slice | Status | PR | Notes |
|---|---|---|---|
| S1 Backend core | Done, awaiting orchestrator review | [PR #40](https://github.com/brijeshsrivdev/antena-hotel-check/pull/40) (`feature/site-check-ranking-signals` → `main`, REQ-038) | 21 page-derived checks (not 12 — see deviations), scoring, in-memory registry, flag, host limit, unit tests on three fixtures |
| S2 Google signals | Done, awaiting orchestrator review | [PR #40](https://github.com/brijeshsrivdev/antena-hotel-check/pull/40) (same branch, REQ-039) | PSI client + 24 h cache, robots, sitemap, broken links; 27 checks total; without a PSI key the three PSI checks report UNKNOWN |
| S3 Frontend page | Not started | | Owner-facing app per ADR-001 (Next.js) |
| S4 PDF + consent | Not started | | Persistence needs its own requirement (PostgreSQL per ADR-001) |
| S5 Tenant guard | Not started | | |

### S1 — what landed (24 Oct 2026)
- Package `com.antenapro.hotelcheck.sitecheck` (`fetch`, `html`, `check`, `model`, `api`); jsoup 1.17.2 added to `backend/pom.xml`.
- Flag: `hotelcheck.sitecheck.enabled=false` in `application.properties`; `=true` only in `application-local.properties`. `SiteCheckConfiguration` declares every bean under `@ConditionalOnProperty`, so `/api/site-checks` is a 404 everywhere else (`SiteCheckFlagGateTest`). `SiteCheckFlagLeakTest` fails the build if a flag name appears in `.github/workflows/*.yml`, a Dockerfile or a compose file, or if any properties file other than `application-local.properties` turns it on.
- API: `POST /api/site-checks {url}` → 202 `{runId}` (400 with a visitor-safe reason, 429 after 3 runs per host per day); `GET /api/site-checks/{runId}` → status, score, stages, findings FAIL-first (each with evidence ≤ 300 chars, fix, why, `antenaHandles`), fetch log, limitations. 25 s timeout → DONE with every check UNKNOWN and a limitation line.
- Fetching reuses the REQ-015 acquisition boundary (`AcquisitionPageFetcher` over `HttpPublicWebAcquisitionService`: SSRF-safe DNS on every hop, ≤ 5 redirects, 2 MB cap, 3 s / 8 s timeouts, UA `AntenaSiteCheck/1.0`). `UrlPolicy` adds the visitor-facing rules: bare host → https, no credentials, default ports, Antena's own hosts rejected, tenant storefront subdomains allowed. The acquisition service now also records `X-Robots-Tag` in provenance metadata (one line) so the indexability check can read it.
- Checks: the 21 that need only the fetched pages — Discover `https`, `title`, `meta-description`, `canonical`, `indexable`; Understand `single-h1`, `hotel-name-consistency`, `rooms-page`, `content-depth`, `language`; Explore `internal-links`, `image-alt`; Trust `structured-data`, `rating-honesty`, `address-phone`, `gbp-link`, `last-updated`; Book `booking-cta`, `own-booking-engine`, `tap-targets`, `booking-page-reachable`. Findings are worded as observations of the pages read ("no element on the 5 pages read says book…"), never as claims about booking success, in line with REQ-027's truth boundary. "Antena does this for you" is set only on `https`, `canonical`, `structured-data`, `rating-honesty`, `booking-cta`, `own-booking-engine`, `booking-page-reachable`.
- Fixtures (`backend/src/test/resources/sitecheck/`): an Antena storefront → 21 PASS, score 100 (the `<button>Check Availability</button>` and JSON-LD rating the regex scanner missed are now evidence); a Wix site linking only to Booking.com; a WordPress site with a booking plugin.

### S2 — what landed (24 Oct 2026, same PR, requirement REQ-039)
- `PsiClient` → `HttpPsiClient` (PageSpeed Insights v5, mobile strategy, key `hotelcheck.sitecheck.psi-key` from `HOTELCHECK_SITECHECK_PSI_KEY` in the local profile) wrapped in `CachingPsiClient` (24 h per host, failures not cached). Keyless calls are not made: Google's keyless quota is zero (observed 24 Oct 2026, the error response is the `psi/quota-exceeded.json` fixture).
- `RobotsTxt` and `SitemapXml` parsers; `fetchText` and `probe` modes on the acquisition-backed fetcher; the loader runs PSI and robots/sitemap in parallel with the page fetches and probes up to six other internal links, all bounded by the fetch deadline.
- Checks `robots`, `sitemap`, `mobile-friendly`, `core-web-vitals`, `page-weight`, `broken-links`; `last-updated` now reads the sitemap `lastmod` when there is no copyright year. The catalogue is the plan's full 27.
- Tests: 344 tests, 0 failures, 0 errors (the Docker-only JpaEvaluationRepositoryTest excluded) locally; the storefront fixture scores 100 with 27 PASS when robots, sitemap and a good PSI response are served.

### Deviations from the plan (S1)
1. **21 checks in S1, not 12** — everything page-derived; S2 keeps robots, sitemap, mobile-friendly, Core Web Vitals, page weight, broken-link sampling and the sitemap-`lastmod` half of `last-updated`.
2. **API path is `/api/site-checks`**, this repository's convention, instead of the monorepo's `/api/v1/public/site-check`.
3. **"Above the fold" is estimated** from document order (header/nav/hero block or first third of the body); the evidence says so.
4. **`last-updated` is UNKNOWN without a copyright year** until S2; **`gbp-link` has no PARTIAL state**.
5. **No persistence in S1**: runs live in memory for two hours; the `site_check_runs` table (plan §4) needs its own requirement because ADR-001 makes PostgreSQL the datastore and this repository's persistence is JPA, not Flyway.
6. **Per-IP limit is not implemented here** (the monorepo's `RateLimitingFilter` does not exist in this repository); the per-host limit is. A per-IP limit belongs with the deploy decision.

### Deviations from the plan (S2)
1. **Broken-link sampling is six probes plus the fetched pages**, sequential and deadline-bound, not a crawl.
2. **Page weight is PSI-only**: without PSI the check is UNKNOWN (the HTML size alone would understate it and is only mentioned in the evidence).
3. **Mobile-friendliness degrades to the viewport tag** without PSI (PARTIAL/FAIL), so a site with no viewport tag is still flagged.

### Decisions needed from Brijesh (S1)
- None blocking. For S2 the PageSpeed Insights key is read from `HOTELCHECK_SITECHECK_PSI_KEY` in the local profile; without it S2 degrades to UNKNOWN with a limitation line.
