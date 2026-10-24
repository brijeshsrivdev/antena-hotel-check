# REQ-042 — Ranking-Signal Site Check: tenant guard (slice S5)

STATUS: PR_READY
REQUIREMENT_ID: REQ-042
TYPE: Implementation
BRANCH: `feature/site-check-ranking-signals` (same PR as REQ-038 … REQ-041, #40)

> Process note: drafted by the implementation session inside the feature branch on Brijesh's instruction ("option a"); the orchestrator reviews it with the PR.

## Objective

Objective O3 of [docs/plans/hotel-site-check-plan.md](../docs/plans/hotel-site-check-plan.md): never report a false negative on an Antena storefront. A live tenant site must not FAIL robots, sitemap, structured data or the booking CTA.

## Scope

`LiveTenantGuardTest`: an opt-in JUnit test, enabled only when `HOTELCHECK_TENANT_URL` is set, that runs the real acquisition service, loader and the full 27-check catalogue against that URL and asserts that none of `robots`, `sitemap`, `structured-data`, `booking-cta`, `https`, `indexable`, `canonical`, `own-booking-engine` is FAIL and that the booking control is found (PASS or PARTIAL). For a locally running antena renderer (`http://lexisinn.localhost:3000`) the test opts into loopback targets through `allowLocalhost` and a permissive policy, in the test only; for a public tenant (`https://lexisinnbeachresort.com`) it uses the normal policy. `HOTELCHECK_SITECHECK_PSI_KEY` is optional (UNKNOWN is not FAIL). The offline fixture equivalent already exists: `SiteAnalyzerFixtureTest.antenaStorefront_ShouldPassEveryCheck_AndScore100`.

## Non-scope

Seeding or starting the antena renderer from this repository (it lives in the antena monorepo); running the guard in CI (no live site there); any change to main code.

## Acceptance criteria

1. Without `HOTELCHECK_TENANT_URL` the test is skipped and the suite stays offline.
2. With it set to an Antena storefront, the guarded checks are never FAIL and the booking CTA is found; the assertion message lists every finding with its evidence and the limitations, so a failure says exactly which check disagreed.
3. With it set to a non-Antena site that lacks these items, the test fails (the guard is not vacuous).

## Completion record

### Completion status

PR_READY — committed and pushed on `feature/site-check-ranking-signals` (PR #40).

### Files changed

- `backend/src/test/java/com/antenapro/hotelcheck/sitecheck/LiveTenantGuardTest.java` (new)
- `docs/plans/hotel-site-check-plan.md`, this file

### Validation performed

- Local: the test compiles and is reported as skipped without the variable (full suite green). Not run against a live storefront from the development container (no outbound route for the backend's HTTP client); Brijesh runs it on his machine: `HOTELCHECK_TENANT_URL=https://lexisinnbeachresort.com mvn -f backend/pom.xml test -Dtest=LiveTenantGuardTest -Dsurefire.failIfNoSpecifiedTests=false`.

**STOPPING FOR ORCHESTRATOR REVIEW.**
