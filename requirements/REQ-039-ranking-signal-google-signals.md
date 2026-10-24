# REQ-039 — Ranking-Signal Site Check: Google signals (slice S2)

STATUS: PR_READY
REQUIREMENT_ID: REQ-039
TYPE: Implementation
BRANCH: `feature/site-check-ranking-signals` (same PR as REQ-038, #40 — Brijesh accepted one large PR on 24 Oct 2026)

> Process note: drafted by the implementation session inside the feature branch on Brijesh's instruction ("option a"); the orchestrator reviews it with the PR.

## Objective

Complete the catalogue of [docs/plans/hotel-site-check-plan.md](../docs/plans/hotel-site-check-plan.md) §3 with the six checks that need more than the fetched pages: robots.txt, the XML sitemap, mobile-friendliness, Core Web Vitals, page weight (the last three from Google's own PageSpeed Insights API) and broken-link sampling; and let the last-updated check read the sitemap's `lastmod` when a page has no copyright year.

## Scope

1. `PsiClient` → `HttpPsiClient` (`https://www.googleapis.com/pagespeedonline/v5/runPagespeed`, strategy mobile, key from `hotelcheck.sitecheck.psi-key`) wrapped in `CachingPsiClient` (one result per host per 24 h, failures not cached). Without a key nothing is called and the three PSI checks report UNKNOWN with the limitation line. `PsiResponseParser` reads field data (CrUX LCP/CLS/INP) first and Lighthouse lab values second, plus the viewport, content-width, total-byte-weight and network-requests audits; every read is null-safe.
2. `AcquisitionPageFetcher` gains `fetchText` (robots.txt, sitemaps) and `probe` (status only) modes, still through the REQ-015 acquisition boundary.
3. `RobotsTxt` (is the whole site disallowed for `*` or Googlebot; `Sitemap:` lines) and `SitemapXml` (urlset / sitemapindex, URL count, newest lastmod, first child of an index) parsers.
4. `SiteSnapshotLoader` starts the PSI call and the robots/sitemap reads on a side executor at the start of the run, fetches the pages meanwhile, then probes up to six other internal links from the home page; side tasks still running at the fetch deadline become limitations (a PSI call keeps running so its result is cached for the host's next run).
5. Checks `robots`, `sitemap`, `mobile-friendly`, `core-web-vitals`, `page-weight`, `broken-links`; `last-updated` falls back to sitemap `lastmod` within 12 months. The catalogue is now the plan's full 27.
6. Properties: `psi-timeout-ms` (40 s) and `max-link-probes` (6). The local profile reads the key from `HOTELCHECK_SITECHECK_PSI_KEY`.

## Truth boundary

Same as REQ-038: a finding describes what was observed. "No robots.txt" is PARTIAL (Google treats it as allow-all), a 5xx robots.txt is FAIL (Google stops crawling), `Disallow: /` for `*` or Googlebot is FAIL. Mobile-friendliness without PSI uses only the viewport meta tag and says overflow was not measured. Core Web Vitals and page weight are UNKNOWN whenever PSI did not answer, with the reason verbatim. Broken links count only links that were actually probed or fetched.

## Non-scope

Any frontend (S3), persistence/PDF/email/consent (S4), the tenant guard (S5), browser rendering, more than six probes, re-running PSI inside a run.

## Dependencies

REQ-038 (S1 of the same catalogue), REQ-015 (acquisition boundary), ADR-001, plan §3 and §5.

## Acceptance criteria

1. The Antena storefront fixture with an allow-all robots.txt, a urlset sitemap and a good PSI response scores 100 with all 27 checks PASS and no limitation.
2. Without a PSI key: `core-web-vitals` and `page-weight` are UNKNOWN with the no-key limitation, `mobile-friendly` is PARTIAL with a viewport tag and FAIL without one, and the score excludes the UNKNOWN checks.
3. A quota-exceeded PSI response (real fixture, anonymised) degrades to UNKNOWN with "quota exceeded" as the limitation; a lab-only response grades LCP/CLS from Lighthouse and says INP was not reported.
4. `Disallow: /` for `*` or Googlebot fails `robots`; a Googlebot `Allow: /` record overrides a `*` block; a missing robots.txt is PARTIAL; a 5xx is FAIL.
5. A sitemap index is followed to its first child for the URL count and lastmod; an HTML page or empty urlset at /sitemap.xml fails `sitemap`.
6. Up to six other internal links are probed, pages already fetched are not probed twice, and a 4xx/5xx on any sampled link fails `broken-links` with the URL and status in the evidence.
7. A PSI call slower than the fetch deadline becomes a limitation and the run still completes; PSI results are served from the cache for 24 h per host.
8. Every new check has PASS, PARTIAL, FAIL and UNKNOWN cases; the full backend suite is green locally (minus the Docker-only test) and in CI.

## Completion record

### Completion status

PR_READY — committed and pushed on `feature/site-check-ranking-signals` (PR #40). Nothing is merged.

### Files changed

- `backend/src/main/java/com/antenapro/hotelcheck/sitecheck/psi/` (new: `PsiClient`, `HttpPsiClient`, `CachingPsiClient`, `PsiResponseParser`)
- `.../sitecheck/fetch/` (`RobotsTxt`, `SitemapXml` new; `PageFetcher`, `AcquisitionPageFetcher`, `SiteSnapshotLoader` extended)
- `.../sitecheck/model/` (`PsiResult`, `RobotsResult`, `SitemapResult` new; `FetchedPage`, `PageRole`, `SiteSnapshot` extended)
- `.../sitecheck/check/` (`RobotsCheck`, `SitemapCheck`, `MobileFriendlyCheck`, `CoreWebVitalsCheck`, `PageWeightCheck`, `BrokenLinksCheck` new; `LastUpdatedCheck`, `SiteChecks` extended)
- `.../sitecheck/SiteCheckProperties.java`, `SiteCheckConfiguration.java` (PSI client, side executor)
- tests: `psi/PsiResponseParserTest`, `psi/CachingPsiClientTest`, `fetch/RobotsTxtTest`, `fetch/SitemapXmlTest` new; fixture, state, loader, service and adapter tests extended; fixtures under `backend/src/test/resources/sitecheck/` (`psi/*.json`, `robots-*.txt`, `sitemap-*.xml`)
- `docs/plans/hotel-site-check-plan.md` (implementation record), this file

### Validation performed

- Local, 24 Oct 2026, OpenJDK 21 / Maven 3.9: `cd backend && mvn --batch-mode test -Dsurefire.failIfNoSpecifiedTests=false -Dtest='!JpaEvaluationRepositoryTest'` → 345 tests, 0 failures, 1 errors.
- CI: Backend Validation on the PR head (recorded by the orchestrator at review).

### Open questions for the orchestrator

- The PSI metric key names (`LARGEST_CONTENTFUL_PAINT_MS`, `CUMULATIVE_LAYOUT_SHIFT_SCORE`, `INTERACTION_TO_NEXT_PAINT`) follow the current v5 schema; a live keyed run is the confirmation. If Google renames one, the parser degrades that metric to "not reported" rather than failing.

**STOPPING FOR ORCHESTRATOR REVIEW.**
