# Antena Hotel Check — owner-facing web application

Next.js + TypeScript (ADR-001). Today it carries one page: the **Hotel Site Check** at `/tools/hotel-site-check`,
the ranking-signal analysis described in `docs/plans/hotel-site-check-plan.md` (requirements REQ-038 … REQ-040).

## Local only

The page renders only when `NEXT_PUBLIC_SITECHECK_ENABLED=true` is set in `frontend/.env.local` (gitignored).
Without it the route is a 404 and the home page shows no card. The flag must never appear in a workflow,
Dockerfile or compose file; `SiteCheckFlagLeakTest` in the backend fails the build if it does.

## Run it

```
# terminal 1 — backend with the local profile (hotelcheck.sitecheck.enabled=true)
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local

# terminal 2 — frontend
cd frontend && cp .env.example .env.local   # then uncomment NEXT_PUBLIC_SITECHECK_ENABLED=true
npm install && npm run dev                  # http://localhost:3000/tools/hotel-site-check
```

`/api/site-checks` is proxied to the backend (`HOTEL_CHECK_API_URL`, default `http://localhost:8080`), so no CORS
configuration is needed.

## Checks

```
npm run typecheck
npm run build
```
