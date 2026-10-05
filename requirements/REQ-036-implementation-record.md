# REQ-036 — Implementation Record Addendum

STATUS: PR_READY_PENDING_REVIEW

This addendum supersedes the earlier journey-derivation wording in the runtime implementation section of `requirements/REQ-036-hospitality-coverage-assessment-derivation-contract.md`.

## Journey derivation correction

`HospitalityCoverageAssessmentDerivationService` does **not** copy `HospitalityAnalysisSignal.journeyStages()` into the assessment.

For each `QUALIFIED` signal, the service derives assessable journey stages from the signal's originating `HospitalityObservationCategory` using the authoritative REQ-036 mapping:

- `HOTEL_IDENTITY` → `DISCOVER`, `UNDERSTAND`
- `ROOMS` → `EXPLORE`
- `AMENITIES` → `UNDERSTAND`, `EXPLORE`
- `CONTACT` → `DISCOVER`
- `BOOKING` → `BOOK`
- `DINING` → `UNDERSTAND`, `EXPLORE`

The existing signal qualification boundary remains upstream. REQ-036 does not qualify or create signals and does not introduce a second taxonomy.

A deliberately inconsistent qualified signal is covered by `HospitalityCoverageAssessmentJourneyDerivationContractTest`: a `ROOMS` signal carrying supplied `BOOK` stages derives only `EXPLORE`, and therefore does not make `BOOK` assessable.

The test suite also supplies deliberately inconsistent journey stages for all six observation categories and verifies that the resulting scope follows the governed category mapping rather than caller-supplied stages.

## Dimension semantics

Unchanged. The existing REQ-036 category-to-dimension mappings remain authoritative, including `DINING` → `AMENITIES_AND_GUEST_FACING_INFORMATION` and typed material identity conflict → `TRUST_AND_CLARITY`.

## Scope boundaries

No changes were made to REQ-034, REQ-035, acquisition, persistence, API/UI, orchestration, scoring, recommendation logic, or REQ-032. REQ-032 remains blocked.

## Validation

The branch head is `f691ee7b54a9191ce79e31d1f8b7462d919618e8` after the correction. Backend Validation run #370 is executing against that exact head.

Do not merge PR #36 until the orchestrator re-reviews the final head and CI is green.
