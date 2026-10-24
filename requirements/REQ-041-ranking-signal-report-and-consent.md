# REQ-041 — Ranking-Signal Site Check: persistence, PDF report and consent (slice S4)

STATUS: PR_READY
REQUIREMENT_ID: REQ-041
TYPE: Implementation
BRANCH: `feature/site-check-ranking-signals` (same PR as REQ-038 … REQ-040, #40 — Brijesh accepted one large PR on 24 Oct 2026)

> Process note: drafted by the implementation session inside the feature branch on Brijesh's instruction ("option a"); the orchestrator reviews it with the PR.

## Objective

Keep a durable record of every finished run (scores and findings, never the pages, never an IP address), and let the visitor receive the result as a PDF by email with explicit opt-in recorded as consent source `hotel_check_form` ([docs/plans/hotel-site-check-plan.md](../docs/plans/hotel-site-check-plan.md) §4 "DB", §4 "API" third endpoint, objective O4).

## Scope

1. `site_check_runs` as a JPA entity (`SiteCheckRunJpaEntity`, Spring Data repository, `JpaSiteCheckRunStore` behind the `SiteCheckRunStore` port): id, requested/final URL, host, status, score, stages and findings as JSON (the API DTOs, so the stored shape never drifts from the response), limitations, email + consent_at + consent_source (`hotel_check_form`), ip_hash, created_at; index on (host, created_at). Written when a run finishes (DONE, FAILED or timed out); a store failure is logged and never fails the run. The table is created by the repository's existing `ddl-auto=update` policy and stays empty wherever the flag is off.
2. `IpHasher`: `sha256(ip + UTC date + a per-process secret)`; the controller hashes `getRemoteAddr()` and the run stores only the hash.
3. Report: `ReportHtml` (XHTML with the same content as the page: score, stages, limitations, findings fix-first with evidence, pages read, the Antena line) rendered to PDF by `PdfReportRenderer` (openhtmltopdf, no network, no external fonts).
4. `ReportService.email(runId, email, consent)`: 400 unless `consent` is literally `true` and the address is usable; 404 for an unknown run; 409 while running or when failed; records consent in the store, then renders and sends. `ReportService.pdf(runId)` returns the bytes for a direct download.
5. Delivery behind `ReportSender`: `FileReportSender` (default, `hotelcheck.sitecheck.report-mode=log`) writes the PDF under `hotelcheck.sitecheck.report-output-dir` (default `${java.io.tmpdir}/hotelcheck-reports`) and logs the path, so a developer machine without SMTP still produces the report and nothing leaves the machine; `SmtpReportSender` (`report-mode=smtp`, needs `spring.mail.*`) emails it from `hotelcheck.sitecheck.report-from` (default `info@antenapro.com`) with the consent sentence in the body. `spring-boot-starter-mail` is added; a `JavaMailSender` exists only when `spring.mail.host` is set.
6. API: `POST /api/site-checks/{runId}/report {email, consent}` → 202 `{status, note}`; `GET /api/site-checks/{runId}/report.pdf` → `application/pdf` download.
7. Page: "Keep this report" card with the email field, the consent checkbox whose label says what is sent and that Antena may follow up by email, the button enabled only when ticked, and a direct download link.

## Non-scope

Reading the stored runs back through an API (the lead session exports by hand, plan §4); the `antena-sales-agent` `consent.py` entry (`hotel_check_form`) lives in the antena monorepo and is not changed from this repository; the tenant guard (S5); any dev or production exposure; a Flyway migration (this repository manages schema through JPA, not Flyway).

## Dependencies

REQ-038 … REQ-040, ADR-001 (PostgreSQL baseline; JPA persistence pattern of REQ-013), plan §4.

## Acceptance criteria

1. A finished run is stored with host, status, score, stages/findings JSON, limitations and ip_hash, without page HTML; consent can be recorded later by run id; a failed run is stored with its failure as the limitation.
2. The IP hash is 64 hex characters, stable within a day for one address, different across days, addresses and secrets, and never contains the address.
3. The PDF renders from a finished run and starts with `%PDF-`; evidence is HTML-escaped in the report; findings are ordered fix-first.
4. `consent` null or false → 400 and nothing is recorded or sent; a bad address → 400; unknown run → 404; running or failed run → 409.
5. In log mode the PDF is written under the report directory and the note says email delivery is off; the controller maps 202/400/404/409 and streams the PDF with a `Content-Disposition` attachment.
6. With the flag on and no JPA in the context the feature still wires (`SiteCheckRunStore.NONE`, `FileReportSender`); with the flag off none of it exists.
7. Frontend typecheck and build pass with the report form; backend suite green locally (the Docker-only test excluded) and in CI.

## Completion record

### Completion status

PR_READY — committed and pushed on `feature/site-check-ranking-signals` (PR #40). Nothing is merged.

### Files changed

- `backend/pom.xml` (openhtmltopdf-pdfbox 1.0.10, spring-boot-starter-mail)
- `backend/src/main/java/com/antenapro/hotelcheck/sitecheck/persistence/` (new: `SiteCheckRunJpaEntity`, `SpringDataSiteCheckRunRepository`, `JpaSiteCheckRunStore`), `SiteCheckRunStore`, `IpHasher`
- `.../sitecheck/report/` (new: `ReportHtml`, `PdfReportRenderer`, `ReportSender`, `FileReportSender`, `SmtpReportSender`, `ReportService`, `ReportRequestRejectedException`)
- `.../sitecheck/SiteCheckService` (store, ip hash), `SiteCheckConfiguration` (store, hasher, sender, report service), `SiteCheckProperties` (report-from, report-mode, report-output-dir), `api/SiteCheckController` (+ `ReportRequest`, two endpoints)
- `backend/src/main/resources/application-local.properties` (report-mode=log)
- tests: `IpHasherTest`, `report/PdfReportRendererTest`, `report/ReportServiceTest`, `persistence/JpaSiteCheckRunStoreTest` (`@DataJpaTest`, embedded H2), controller and flag-gate tests extended
- `frontend/components/ReportForm.tsx`, `frontend/lib/siteCheck.ts`, `frontend/app/tools/hotel-site-check/SiteCheckTool.tsx`
- `docs/plans/hotel-site-check-plan.md`, this file

### Validation performed

- Local, 24 Oct 2026, OpenJDK 21 / Maven 3.9: `cd backend && mvn --batch-mode test -Dsurefire.failIfNoSpecifiedTests=false -Dtest='!JpaEvaluationRepositoryTest'` → 357 tests, 0 failures, 0 errors (the Docker-only JpaEvaluationRepositoryTest excluded; `JpaSiteCheckRunStoreTest` runs on the embedded H2 through `@DataJpaTest`).
- Frontend: `npm run typecheck` and `npm run build` clean with the report form.
- CI: Backend Validation and Frontend Validation on the PR head (recorded by the orchestrator at review).

**STOPPING FOR ORCHESTRATOR REVIEW.**
