# REQ-038 — Ranking-Signal Site Check Foundation (slice S1)

STATUS: PR_READY
REQUIREMENT_ID: REQ-038
TYPE: Implementation
BRANCH: `feature/site-check-ranking-signals`

> Process note: this requirement was drafted by the implementation session inside the feature branch, not created on `main` first, on Brijesh's instruction of 24 Oct 2026 ("option a"). The orchestrator reviews it together with the PR.

## Objective

Give a hotel owner an in-depth, honest analysis of **what stops their website ranking better in Google**, one URL at a time, as approved by Brijesh on 24 Oct 2026 (master sheet PG-06). The product plan is [docs/plans/hotel-site-check-plan.md](../docs/plans/hotel-site-check-plan.md); the evaluation that motivated it is [docs/context/hotel-check-evaluation.md](../docs/context/hotel-check-evaluation.md). This slice (S1 of five) is the backend core: fetch up to five pages of one site through the existing public-web acquisition boundary, run the page-derived checks of the plan's catalogue, score them, and expose the result through a flag-gated API that runs on a developer machine only.

## Product question

The catalogue is organised by the guest journey this repository already uses — **Discover → Understand → Explore → Trust → Book** — and each finding tells the owner what was observed on their site, why it matters for ranking, and the one change that fixes it. Lead capture is a side effect for later slices; a report nobody trusts captures nothing.

## Scope

1. A new bounded capability under `com.antenapro.hotelcheck.sitecheck`, parallel to the hospitality analysis pipeline (REQ-019 … REQ-027), consuming the same acquisition boundary (REQ-015) and nothing else of the existing pipeline.
2. Page fetching through `PublicWebAcquisitionService` (SSRF-safe DNS on every hop, bounded redirects and size, timeouts) with jsoup parsing — never regex over raw HTML; the earlier regex scan missed `<button>Check Availability</button>` and JSON-LD on an Antena storefront.
3. A `UrlPolicy` for the visitor's input: bare host → https, http(s) only, no credentials, default ports, no private/loopback/link-local/CGNAT targets, none of Antena's own hosts (tenant storefront subdomains allowed).
4. Up to five pages per run: home plus the first rooms, contact, booking-CTA target and about links found on the home page, within a fetch deadline.
5. The 21 catalogue checks that read only the fetched pages (plan §3): Discover `https`, `title`, `meta-description`, `canonical`, `indexable`; Understand `single-h1`, `hotel-name-consistency`, `rooms-page`, `content-depth`, `language`; Explore `internal-links`, `image-alt`; Trust `structured-data`, `rating-honesty`, `address-phone`, `gbp-link`, `last-updated`; Book `booking-cta`, `own-booking-engine`, `tap-targets`, `booking-page-reachable`. Each returns `PASS | PARTIAL | FAIL | UNKNOWN`, evidence (≤ 300 characters, what was observed), a one-sentence fix, why it matters for ranking, and whether an Antena storefront provides it (only where verified against the renderer).
6. Scoring per plan §3: stage mean (PASS 1, PARTIAL 0.5, FAIL 0, UNKNOWN excluded), overall weighted Discover 30 / Trust 25 / Book 25 / Understand 10 / Explore 10, integers only.
7. API: `POST /api/site-checks {url}` → 202 `{runId}`; `GET /api/site-checks/{runId}` → status, score, stages, findings FAIL-first, fetch log, limitations. In-memory run registry (2 h retention), 3 runs per host per day, 25 s server-side timeout after which the run is DONE with every check UNKNOWN and a limitation line.
8. Local-only: `hotelcheck.sitecheck.enabled=false` in `application.properties`, `true` only in `application-local.properties`; every bean of the feature declared in one `@ConditionalOnProperty` configuration so the path is a 404 otherwise; a test that fails the build if a flag name reaches a workflow, Dockerfile or compose file.
9. The plan and the evaluation note copied into `docs/` so this repository is self-contained, with the implementation record kept in the plan.

## Truth boundary (REQ-027 alignment)

A finding describes the pages that were read. "FAIL" on `booking-cta` means *no element on the pages read says book / reserve / check availability or opens WhatsApp*; it does not claim the hotel cannot be booked. `UNKNOWN` is used whenever the signal could not be observed (home page unreadable, page not fetched within budget, no copyright year yet no sitemap data), and UNKNOWN is excluded from the score. No finding asserts booking success, hotel quality or anything beyond what the evidence line shows. The hospitality analysis pipeline and its contracts are not changed.

## Explicit non-scope

Robots/sitemap, PageSpeed Insights (mobile-friendliness, Core Web Vitals, page weight) and broken-link sampling (slice S2); any frontend (S3); PDF, email, consent and persistence (S4); the tenant guard (S5); hotel name + city resolution; crawling beyond five pages; competitor comparison; per-IP rate limiting (no shared deployment exists for this tool); any change to the hospitality observation/signal/finding pipeline; any dev or production exposure.

## Dependencies

- REQ-015 — Public Web Acquisition Foundation (`PublicWebAcquisitionService`, `AcquisitionUrlNormalizer`, `AcquisitionConfig`)
- ADR-001 — Technology Baseline (Java 21 / Spring Boot; parsing library left to implementation → jsoup)
- SPEC-004 — public-access boundary, bounded requests, non-public destination rule
- docs/plans/hotel-site-check-plan.md — catalogue, PASS conditions, scoring, API contract, slices

## Acceptance criteria

1. With the flag unset or false, no bean of the feature exists and `/api/site-checks` is not mapped.
2. With the flag true, `POST /api/site-checks` validates the URL (400 with a visitor-safe reason for loopback, link-local, private, non-http, credentialed, non-default-port and Antena-own targets), counts the host (429 after three runs in 24 h) and answers 202 with a run id.
3. `GET /api/site-checks/{runId}` returns RUNNING, then DONE with score, stage scores, findings FAIL-first, the fetch log and limitations; unknown ids are 404; a run overdue by the timeout plus grace is DONE with every check UNKNOWN and a limitation line.
4. Every check has a PASS, PARTIAL, FAIL and UNKNOWN case in tests; an unreadable home page makes every check UNKNOWN and the score null.
5. The Antena storefront fixture scores 100 with all 21 checks PASS, and the `<button>Check Availability</button>` is the booking-CTA evidence.
6. The Wix OTA-only fixture fails `own-booking-engine`, `structured-data`, `rating-honesty`, `tap-targets` and `gbp-link`; the WordPress fixture passes the booking checks and flags the mixed-content script and the 404 rooms page.
7. The response JSON for a full run stays under 200 KB and contains no page HTML; evidence never exceeds 300 characters.
8. Pages are fetched only through `PublicWebAcquisitionService`; the adapter maps status, timing, headers (including `X-Robots-Tag`) and failures, proven against a loopback server.
9. `SiteCheckFlagLeakTest` passes and would fail if the flag name were added to `.github/workflows/backend-validation.yml`.
10. The existing backend suite still passes; Backend Validation CI is green on the PR head.

## Engineering expectations

Small explicit collaborators, no rule engine: `SiteCheckService` (validate → count → executor) → `SiteSnapshotLoader` (fetch) → `SiteAnalyzer` (run `SiteChecks.all()`) → `ScoreCalculator`. Checks are plain classes extending one small base; the catalogue is a static list in order. Constructor injection, records for values, no Lombok, Spring types kept to the configuration and controller.

## Testing requirements

Deterministic unit tests only (no live site): fixture-driven analyzer tests on the three sites, per-check state tests, URL policy tests with a fake resolver, loader budget/deadline tests with a fake fetcher, service limit/timeout tests with a hand-moved clock, controller tests with standalone MockMvc, the flag-gate test with `ApplicationContextRunner`, the flag-leak test, and the acquisition adapter test against a JDK loopback `HttpServer` (the same pattern as REQ-015).

## Completion record

### Completion status

PR_READY — implementation committed and pushed on `feature/site-check-ranking-signals`; awaiting orchestrator review. Nothing is merged.

### PR

- **PR URL:** https://github.com/brijeshsrivdev/antena-hotel-check/pull/40
- **Base:** `main`
- **Merged:** No (awaiting orchestrator review)

### Files changed

- `backend/pom.xml` (jsoup 1.17.2)
- `backend/src/main/java/com/antenapro/hotelcheck/acquisition/HttpPublicWebAcquisitionService.java` (one line: `header.x-robots-tag` in provenance metadata)
- `backend/src/main/java/com/antenapro/hotelcheck/sitecheck/**` (new: `SiteCheckProperties`, `SiteCheckConfiguration`, `SiteCheckService`, `SiteAnalyzer`, `ScoreCalculator`, `SiteCheckRunRegistry`, `HostLimitExceededException`; `fetch/` `UrlPolicy`, `InvalidUrlException`, `PageFetcher`, `AcquisitionPageFetcher`, `SiteSnapshotLoader`; `html/` `HtmlSupport`, `JsonLd`, `PageLinks`, `BookingCta`; `check/` 21 checks + `SiteCheck`, `AbstractSiteCheck`, `PropertyName`, `BookingEngines`, `SiteChecks`; `model/` `Finding`, `Stage`, `CheckStatus`, `StageScore`, `PageRole`, `FetchedPage`, `SiteSnapshot`, `AnalysisResult`, `SiteCheckRun`, `RunStatus`; `api/` `SiteCheckController`, `SiteCheckRequest`, `SiteCheckResponse`)
- `backend/src/main/resources/application.properties`, `application-local.properties` (flag)
- `backend/src/test/java/com/antenapro/hotelcheck/sitecheck/**` and `backend/src/test/resources/sitecheck/*.html` (tests and fixtures)
- `docs/plans/hotel-site-check-plan.md`, `docs/context/hotel-check-evaluation.md`, `README.md`, this file

### Validation performed

- Local, 24 Oct 2026, OpenJDK 21 / Maven 3.9: `cd backend && mvn --batch-mode test -Dsurefire.failIfNoSpecifiedTests=false -Dtest='!JpaEvaluationRepositoryTest'` → 318 tests, 0 failures, 0 errors (the 236 pre-existing tests plus 82 new under `sitecheck/`). `JpaEvaluationRepositoryTest` is excluded locally because it needs Docker (Testcontainers), which the development container lacks; it runs in CI.
- CI: Backend Validation on the PR head — see the PR checks (recorded by the orchestrator at review).

### Open questions for the orchestrator

- The plan's `site_check_runs` table (slice S4) conflicts with nothing yet, but will need a persistence requirement in this repository's JPA/PostgreSQL style rather than the monorepo's Flyway.
- Whether later slices should fold the site-check findings into `HospitalityAnalysisResult` (SPEC-006 dimension `SEO_AND_STRUCTURED_DATA_SUPPORTING_SIGNALS`) or stay a parallel capability. S1 keeps them parallel to avoid touching governed contracts.

**STOPPING FOR ORCHESTRATOR REVIEW.**
