# Requirements

This directory contains the durable execution contracts used by the Antena Hotel Check orchestrator and implementation sessions.

## Purpose

A requirement is the bridge between product orchestration and an implementation/specification session. It contains the exact scope, non-scope, acceptance criteria, and completion/review record for one bounded piece of work.

## Lifecycle

`PROPOSED → READY → IN_PROGRESS → PR_READY → IN_REVIEW → CHANGES_REQUIRED → IN_REVIEW → APPROVED → COMPLETED`

The `CHANGES_REQUIRED` loop may repeat for multiple review rounds.

## Rules

- Requirements are created on `main` before a session starts work.
- The assigned session reads its requirement file first and works only within its scope.
- The session updates the same requirement file with completion details and PR information.
- The orchestrator reviews the PR against the requirement and records review findings in the same file.
- The session addresses review remarks in the same file and updates the implementation/response record.
- Only the orchestrator can mark a requirement `APPROVED`.
- A requirement must not be considered complete merely because a session reports completion or CI is green.

## Naming

Use:

`REQ-NNN-short-description.md`

Requirements are execution contracts. Product specifications belong under `docs/specs/`. Architecture decisions belong under `docs/architecture/` and `docs/decisions/` as appropriate.
