# Testing Standards

**Status:** SPECIFIED

Tests should give confidence in behavior and contracts, not merely reproduce implementation details.

## Test Priorities

For a change, consider the cases that matter to the contract:

- Happy path and important supported variants.
- Validation failures and invalid input.
- Boundary conditions.
- Dependency failures and partial failures.
- Security-sensitive behavior.
- Concurrency or idempotency where relevant.
- Deterministic behavior and repeatability.

## Test Selection

Use the smallest test level that provides meaningful confidence. Unit tests are appropriate for isolated behavior; integration tests are appropriate when a real boundary or collaboration must be verified. Do not add integration complexity when a deterministic unit test proves the contract.

Do not write tests that depend on live third-party systems unless the requirement explicitly defines a safe integration test for them.

Avoid tests that overfit private implementation details when the same behavior can be verified through a public contract.

## Execution Truth

An agent must not claim that a test passed unless it actually executed the test command and observed the result.

Report the command or validation performed and distinguish local results from CI results.

CI is authoritative for repository-level validation when local infrastructure differs from CI.

## Before PR_READY

Confirm that important behavior and failure paths are covered, tests are deterministic, and the executed validation matches what is reported in the requirement and PR.