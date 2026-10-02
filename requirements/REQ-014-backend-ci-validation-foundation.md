# REQ-014 — Backend CI Validation Foundation

STATUS: READY
REQUIREMENT_ID: REQ-014
BRANCH: ci/backend-validation

## Objective

Establish a repository-level GitHub Actions validation gate for the Java 21/Maven backend so backend tests can execute in a clean CI environment with Docker available for Testcontainers.

This requirement exists to provide trustworthy execution evidence for REQ-013 and to become the reusable backend validation foundation for subsequent implementation requirements.

## Context

REQ-013 introduces PostgreSQL/JPA persistence and Testcontainers PostgreSQL integration tests. The implementation session could not execute Maven or Docker locally, so the repository currently lacks evidence that those integration tests actually run.

The CI environment must therefore become the authoritative execution environment for backend tests when the local implementation environment cannot provide the required tooling.

Read completely before implementation:

- `docs/context/PROJECT-CONTEXT.md`
- `docs/specs/SPEC-001-hotel-check.md`
- `docs/specs/SPEC-002-hospitality-analysis.md`
- `docs/specs/SPEC-003-guest-journey.md`
- `docs/specs/SPEC-004-evidence-provenance.md`
- `docs/specs/SPEC-005-interactive-preview.md`
- `docs/architecture/ARCH-001-evaluation-architecture.md`
- `docs/decisions/ADR-001-technology-baseline.md`
- `requirements/REQ-013-evaluation-persistence-foundation-implementation.md`

## In Scope

1. Add a GitHub Actions workflow for backend validation.
2. Use Java 21.
3. Execute the Maven test lifecycle from the repository backend module/project.
4. Ensure the GitHub-hosted runner provides Docker support required by Testcontainers.
5. Ensure the workflow executes the REQ-013 PostgreSQL/Testcontainers integration tests rather than merely compiling the project.
6. Fail the workflow when tests fail.
7. Keep the workflow suitable for reuse by later backend implementation PRs.
8. Document how the CI validation gate is intended to be used.

## Out of Scope

Do NOT implement:

- application features
- crawler/acquisition logic
- hospitality analysis
- AI integration
- preview generation
- REST APIs
- authentication/authorization
- production deployment
- external database provisioning
- changes to the REQ-013 persistence implementation unless required solely to make the existing tests executable in CI

## Acceptance Criteria

### AC-1 — Workflow exists

A GitHub Actions workflow exists under `.github/workflows/` and is clearly named for backend validation.

### AC-2 — Java/Maven execution

The workflow installs/configures Java 21 and executes the repository's Maven test command from the correct backend project directory.

### AC-3 — Testcontainers execution

The workflow environment supports Docker/Testcontainers and the REQ-013 PostgreSQL integration tests actually execute in CI.

A workflow that only compiles, runs unit tests, or skips Testcontainers does not satisfy this criterion.

### AC-4 — Failure propagation

A failing Maven test causes the workflow/job to fail.

### AC-5 — Pull request validation

The workflow runs for pull requests targeting `main` so implementation PRs receive CI evidence before orchestrator review/merge.

### AC-6 — No production infrastructure dependency

The workflow uses ephemeral CI resources and does not require a persistent production PostgreSQL instance, production credentials, or secrets merely to execute the test suite.

### AC-7 — Documentation

Repository documentation explains the backend CI validation command/workflow and states that CI is the validation authority when local Maven/Docker tooling is unavailable.

### AC-8 — Scope discipline

No unrelated application functionality is introduced.

## Validation Required Before PR

The implementation session must:

1. Run the relevant Maven tests locally if Maven/Docker are available.
2. Push the workflow.
3. Verify that GitHub Actions starts for the implementation PR/commit.
4. Confirm from the actual workflow result that the PostgreSQL/Testcontainers tests execute and pass.
5. If CI cannot be made to execute the integration tests, do not claim success; document the exact blocker in this requirement.

## Completion Record

Leave this section unchanged until implementation is complete. The session must update this SAME requirement file before creating its PR with:

- STATUS: PR_READY
- implementation summary
- branch used
- PR number and URL
- files changed
- validation performed, including actual CI run/result
- open questions/blockers

## Governance

- Create the exact branch named in `BRANCH` from current `main`.
- Execute ONLY REQ-014.
- Do not merge the PR.
- Stop after creating/updating the PR and wait for orchestrator review.
