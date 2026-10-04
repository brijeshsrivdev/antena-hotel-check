# Implementation Session Workflow

**Status:** SPECIFIED

This is the reusable workflow for a bounded coding-agent session. It complements `docs/workflows/development-lifecycle.md` and does not replace the repository requirement lifecycle.

## 1. READY

The orchestrator has assigned one requirement with an explicit branch and scope.

## 2. Read the Requirement

Read the assigned requirement completely. Treat its scope, acceptance criteria, validation requirements, and governance instructions as the contract.

## 3. Build Scoped Context

Use the requirement's `READ`, `INSPECT`, `DEPENDENCIES`, `DO NOT TOUCH`, `ACCEPTANCE`, and `OUTPUT` sections. Follow references progressively rather than loading unrelated repository context.

## 4. Inspect

Inspect the current `main`, relevant implementation, tests, APIs, migrations/configuration, and existing patterns that directly participate in the change.

If evidence is insufficient for a required decision, stop and report the ambiguity. Do not guess.

## 5. Plan

Choose the smallest complete implementation that satisfies the requirement. Confirm scope and non-scope before editing.

## 6. Implement

Implement only the assigned slice. Reuse existing abstractions where appropriate. Add or update tests for meaningful behavior and failure paths. Avoid unrelated refactoring.

## 7. Validate

Run applicable tests and repository validation. Record only commands/results that were actually observed. Distinguish local validation from CI evidence.

## 8. Self-Review

Use the self-review gate in `docs/engineering/AGENT-GUIDELINES.md`, covering scope, context, design, correctness, testing, security/production concerns, and repository hygiene.

## 9. Update the Same Requirement

Before `PR_READY`, update the assigned requirement with the real implementation summary, files changed, validation, PR information, limitations/open questions, and status. Do not create a parallel completion record.

## 10. PR_READY

Set `STATUS: PR_READY` only when the acceptance criteria are genuinely satisfied and the self-review is complete.

## 11. Create or Update the PR

Use the assigned branch and existing PR when one exists. The PR should explain what changed, why it satisfies the requirement, validation performed, and what is explicitly out of scope.

## 12. STOP

The implementation session stops after creating/updating the PR. It does not merge its own PR unless the orchestrator explicitly assigns that responsibility.

## 13. Orchestrator Review

The orchestrator reviews the PR against the requirement, architecture, correctness, tests, security/privacy, documentation, and CI/validation. Review findings are recorded through the repository's established requirement lifecycle.

```text
READY
  ↓
Read requirement
  ↓
Scoped context
  ↓
Inspect dependencies/tests
  ↓
Plan
  ↓
Implement
  ↓
Test / validate
  ↓
Self-review
  ↓
Update SAME requirement
  ↓
PR_READY
  ↓
Create/update PR
  ↓
STOP
  ↓
Orchestrator review
  ↓
Fix / re-review
  ↓
Approval
  ↓
Merge
```

## Reusable Session Prompt

```text
You are an implementation session for `antena-hotel-check`.

Work ONLY on the assigned requirement:
<REQUIREMENT PATH>

Branch:
<BRANCH>

First:
1. Inspect current `main`.
2. Read the assigned requirement completely.
3. Read only its scoped READ/context references.
4. Inspect the existing implementation and direct dependencies/tests.
5. Confirm scope and non-scope.

Use the requirement's READ / INSPECT / DEPENDENCIES / DO NOT TOUCH / ACCEPTANCE / OUTPUT model. Follow references progressively; do not load the entire repository unless the task requires it.

Before creating anything new, search for an existing implementation and inspect callers/tests.

If repository evidence is insufficient for a required decision, STOP and report the ambiguity. Do not invent classes, APIs, database fields, behavior, or architecture.

Implement only the assigned requirement. Use the simplest reasonable solution and existing project conventions. Add meaningful deterministic tests and run the applicable validation.

Before PR_READY, perform the self-review covering scope, context, design, correctness, testing, security, production readiness, and repository hygiene.

Update the SAME requirement file with the actual implementation summary, files changed, validation, PR information, limitations, and final status. Create or update the assigned PR. Do not merge it.

STOP FOR ORCHESTRATOR REVIEW.
```