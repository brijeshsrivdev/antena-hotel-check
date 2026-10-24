# REQ-040 — Ranking-Signal Site Check: owner-facing page (slice S3)

STATUS: PR_READY
REQUIREMENT_ID: REQ-040
TYPE: Implementation
BRANCH: `feature/site-check-ranking-signals` (same PR as REQ-038/039, #40 — Brijesh accepted one large PR on 24 Oct 2026)

> Process note: drafted by the implementation session inside the feature branch on Brijesh's instruction ("option a"); the orchestrator reviews it with the PR.

## Objective

Give Brijesh a screen on his machine to run the Hotel Site Check on real hotel sites and read the result the way an owner will: score, five stage rows, findings fix-first with evidence behind a toggle, limitations verbatim, and the list of pages read ([docs/plans/hotel-site-check-plan.md](../docs/plans/hotel-site-check-plan.md) §4 "Frontend").

## Scope

1. The repository's first owner-facing web application under `frontend/`, per ADR-001: Next.js 15 + TypeScript, no UI library, no state library. It carries the home page (hub) and `/tools/hotel-site-check`.
2. The page: URL form, progress line while polling `GET /api/site-checks/{runId}` every two seconds, result with a score dial, stage rows (weight and pass/partly/fix/unknown counts), findings ordered FAIL → PARTIAL → UNKNOWN → PASS each with the fix, why it matters and a "What we saw" evidence toggle, the "Antena does this for you" badge only where the backend set it, limitations shown verbatim, and the fetch log. Error states for a rejected URL (400), the per-host limit (429), a failed run and an unreachable backend.
3. `/api/site-checks` is proxied to the backend by a Next.js rewrite (`HOTEL_CHECK_API_URL`, default `http://localhost:8080`), so the Spring app needs no CORS rule.
4. Local-only: the route calls `notFound()` and the hub shows no card unless `NEXT_PUBLIC_SITECHECK_ENABLED=true`, which may be set only in the gitignored `frontend/.env.local` (`.env.example` documents the name). Because Next.js inlines `NEXT_PUBLIC_*` at build time, any build made without the flag serves a permanent 404 for the page. Both pages carry `robots: noindex`.
5. `SiteCheckFlagLeakTest` now also guards `frontend/package.json`, `frontend/next.config.*` and every tracked `frontend/.env*` except `.env.local` and `.env.example`.
6. `.github/workflows/frontend-validation.yml`: `npm ci`, `npm run typecheck`, `npm run build` on pull requests to `main` and pushes to `feature/**` touching `frontend/`. The workflow does not and must not set the flag.

## Non-scope

"Email me the PDF" and consent (S4); the interactive hotel preview (SPEC-008); any owner authentication; styling beyond a clean readable page; Playwright acceptance tests (no end-to-end guest journey is validated here).

## Dependencies

REQ-038, REQ-039 (the API and its response shape, mirrored as `frontend/lib/siteCheck.ts`), ADR-001, docs/engineering/FRONTEND-PATTERNS.md.

## Acceptance criteria

1. With `NEXT_PUBLIC_SITECHECK_ENABLED=true` at build time, `/` shows the Hotel Site Check card and `/tools/hotel-site-check` renders the form; without it, the card is absent and the route answers 404.
2. Submitting a URL posts to `/api/site-checks`, polls every two seconds, and renders score, stages, findings and limitations from the response; a 400 or 429 shows the backend's sentence; a backend that is not running shows a plain hint.
3. Findings are ordered FAIL, PARTIAL, UNKNOWN, PASS; the evidence is behind a toggle; the Antena badge appears only when `antenaHandles` is true and the check did not pass.
4. `npm run typecheck` and `npm run build` pass locally and in the Frontend Validation workflow.
5. `SiteCheckFlagLeakTest` passes and would fail if the flag were added to `frontend/package.json`, `next.config.ts`, a tracked `.env` file or a workflow.

## Completion record

### Completion status

PR_READY — committed and pushed on `feature/site-check-ranking-signals` (PR #40). Nothing is merged.

### Files changed

- `frontend/` (new): `package.json`, `package-lock.json`, `tsconfig.json`, `next.config.ts`, `next-env.d.ts`, `.gitignore`, `.env.example`, `README.md`, `app/layout.tsx`, `app/globals.css`, `app/page.tsx`, `app/tools/hotel-site-check/page.tsx`, `app/tools/hotel-site-check/SiteCheckTool.tsx`, `lib/siteCheck.ts`, `components/ScoreDial.tsx`, `components/StageRows.tsx`, `components/FindingItem.tsx`
- `.github/workflows/frontend-validation.yml` (new)
- `backend/src/test/java/com/antenapro/hotelcheck/sitecheck/SiteCheckFlagLeakTest.java` (frontend files guarded)
- `README.md`, `docs/plans/hotel-site-check-plan.md`, this file

### Validation performed

- Local, 24 Oct 2026, Node 22.22 / npm 10.9: `npm install`, `npm run typecheck`, `npm run build` → clean; built with the flag and served with `next start`: `/` 200 with the card, `/tools/hotel-site-check` 200 with `noindex`; built without the flag: `/` 200 without the card, `/tools/hotel-site-check` 404.
- Backend `SiteCheckFlagLeakTest` green with the frontend guards.
- Not exercised here: a live run against a real hotel site from the page (the development container has no outbound route for the backend's HTTP client). The API it calls is covered by the backend tests.

**STOPPING FOR ORCHESTRATOR REVIEW.**
