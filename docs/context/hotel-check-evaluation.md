# antena-hotel-check — is it a marketing tool for Antena?

**Evaluated:** 24 Oct 2026 by the campaign-lead session, on request. Repo:
`brijeshsrivdev/antena-hotel-check` (public, last merge 5 Oct 2026, PR #39
"evaluation-execution-api"). Built and run locally against three Diveagar
hotel sites, one of them the Antena storefront lexisinnbeachresort.com.

> Copied into this repository on 24 Oct 2026 from the antena monorepo (`docs/business/marketing/hotel-check-evaluation.md`) when Brijesh decided the tool is built here. The code it evaluates is this repository at PR #39; the plan it points to supersedes the "what to build instead" section.

## Verdict in one line

**The idea is a strong lead magnet; the code is not reusable as a product.**
Keep the repository as the specification, build the check as a thin page in
the antena monorepo, and plug its output into the outreach sequences.

## What the repo is today

| Aspect | Observed |
|---|---|
| Stack | Spring Boot 3.5 / Java 21, H2 in-memory, one endpoint `POST /api/evaluations` taking `{hotelName, city}` or `{websiteUrl}` |
| Size | 77 main classes, 25 tests, 38 requirement files; no frontend, no PDF, no scoring |
| Acquisition | Fetches **one page** (the URL given) over HTTP; `hotelName + city` is validated but never resolved to a site |
| Analysis | Regex over `<title> <h1-h3> <a> <address>` for six categories: identity, rooms, amenities, contact, booking, dining |
| Output | JSON: findings, coverage state, guest-journey stages (DISCOVER / UNDERSTAND / EXPLORE / TRUST / BOOK), recommendations in six fixed categories, an executive summary with counts |
| Dimensions declared but not measured | mobile/technical, SEO & structured data, discoverability, trust — enum values only |

## What the run showed

| Site | Result | Why it matters |
|---|---|---|
| lexisinnbeachresort.com (Antena storefront) | 9 observations, 0 deficiencies, 0 recommendations. **BOOK stage "UNSUPPORTED", TRUST "UNSUPPORTED".** | The page has a "Check Availability" booking button and Hotel JSON-LD with `aggregateRating`; the scanner only reads anchors/headings, so it missed both. A lead magnet that tells our own client their site cannot be booked is worse than no tool. |
| lotusholidayresort.com | 228 MB response | Every finding embeds the full page HTML again (the Lexis Inn response repeated the 145 KB page 75 times, 12 MB). |
| betelnutresort.com | **2.5 GB** response, 27 s | Same defect; unusable behind a public form. |

## Where the larger impact is

A free "Check your hotel website" on antenapro.com is the one asset that
makes every channel in the agenda personal:

1. **Partner sequence** — "we ran the check on three of your members' sites"
   is a stronger touch 2 than any offer.
2. **NIDHI email channel** — each row has a website or not; a one-line result
   ("your site has no booking button, no WhatsApp link, blocked from Google")
   qualifies the lead before the first reply.
3. **Brochure / magnet QR** — "Scan to check your hotel's website" is a reason
   to scan; the current QR only opens the home page.
4. **Consent at scale** — the result is emailed, so the form captures email and
   WhatsApp number with an explicit opt-in (`consent.py` source
   `hotel_check_form`), which the WhatsApp channel lacks today.
5. **Proof** — the same checks run on every live tenant produce the before/after
   numbers the case study needs.

## What to build instead (spec for a development session)

Approved by Brijesh on 24 Oct 2026 (PG-06). The build plan for the development session is [docs/plans/hotel-site-check-plan.md](../plans/hotel-site-check-plan.md): **local machine only (not dev, not production), off by default, until Brijesh is satisfied with results on real sites.** The core objective was sharpened to in-depth analysis of what stops a hotel website ranking better in Google; lead capture is the side effect.

- **Page** `antenapro.com/check` (renderer or console marketing routes): URL or
  name + town; result on screen in under ten seconds; PDF by email.
- **Signals that are cheap and honest** (each one is a sales line):
  reachable over HTTPS; mobile viewport tag; a booking CTA found in *any*
  element text (buttons included) or a known booking-engine host; `tel:` and
  `wa.me` links; Hotel/LodgingBusiness JSON-LD and `aggregateRating`;
  `robots.txt` not disallowing `/`; sitemap present; page weight; last
  modified / copyright year; OTA-only presence (no own domain found).
- **Scoring**: five journey stages, pass/partial/fail each, no decimals; the
  repo's stage model is the right vocabulary.
- **Output**: one screen, one PDF, one row in the lead files with the result
  summary, consent source and timestamp.
- **Never**: resolve hotel name to site by scraping Google; fetch more than
  three pages; store page HTML.

## Decision requested from Brijesh

Approve PG-06 in the master sheet (Product-led Growth) so a development
session can take the spec, or park it until the first channel is measured.
