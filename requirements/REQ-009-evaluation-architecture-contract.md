# REQ-009 — Evaluation Architecture Contract

**Status:** READY  
**Owner:** Next implementation/specification session  
**Type:** Architecture / Specification  
**Branch:** `spec/evaluation-architecture-contract`

## Objective

Define the first implementation-neutral architecture contract for Antena Hotel Check, connecting the approved product specifications into a coherent system boundary without prematurely locking frameworks, cloud services, persistence technology, rendering technology, or AI providers.

The governing product flow remains:

**Hotel input → Evaluation → Evidence acquisition → Evidence model → Hospitality analysis → Hotel Experience Model → analysis result + interactive hotel preview**

The architecture must protect the central product objective: a hotel owner submits a hotel name + city or website URL, receives a hospitality-specific evaluation, and can explore the resulting Antena-hosted hotel preview.

## Read before execution

The session MUST read the complete contents of:

- `docs/context/project-context.md`
- `docs/specs/SPEC-001-hotel-check.md`
- `docs/specs/SPEC-002-evaluation-input.md`
- `docs/specs/SPEC-003-evaluation-lifecycle.md`
- `docs/specs/SPEC-004-public-evidence-acquisition.md`
- `docs/specs/SPEC-005-evidence-model.md`
- `docs/specs/SPEC-006-hospitality-analysis.md`
- `docs/specs/SPEC-007-hotel-experience-model.md`
- `docs/specs/SPEC-008-interactive-preview.md`
- `docs/architecture/` relevant documents
- `docs/decisions/` relevant documents
- `requirements/` current workflow guidance and this requirement

If a referenced document does not exist or materially contradicts this requirement, STOP and report the ambiguity instead of inventing a resolution.

## Scope

Define the architecture-level boundaries and responsibilities for:

1. Evaluation request intake and target resolution.
2. Evaluation lifecycle/orchestration and attempt/run ownership.
3. Public evidence acquisition as a controlled boundary.
4. Evidence normalization, provenance, and storage/transport responsibilities at a conceptual level.
5. Hospitality analysis as a separate domain capability.
6. Hotel Experience Model construction as a governed semantic boundary.
7. Report/analysis-result production.
8. Interactive preview production and serving as a separate owner-facing outcome.
9. Failure, partial completion, retry, and idempotency boundaries.
10. Security/trust boundaries between user input, external hotel content, internal processing, and owner-facing output.
11. Observability/audit requirements needed to explain an evaluation result.
12. Data ownership and lifecycle boundaries at a conceptual level.
13. Clear seams where later technology-specific architecture decisions can be made.
14. Architecture acceptance criteria and implementation/test guidance.

## Explicit non-scope

Do NOT implement application code.

Do NOT lock:

- Next.js or another frontend framework;
- Java/Spring or another backend framework;
- PostgreSQL or another database;
- Redis, Kafka, Pub/Sub, or another queue/event technology;
- Playwright or another browser automation technology;
- a specific HTTP client;
- cloud provider/service topology;
- object storage provider;
- AI provider/model;
- rendering engine;
- CDN/DNS/domain implementation;
- authentication provider;
- deployment topology;
- exact API schemas or database schemas unless needed only as conceptual examples.

Do NOT redefine the approved product behavior in SPEC-001 through SPEC-008.
Do NOT introduce new hospitality capabilities.
Do NOT turn architecture work into implementation planning that requires technology selection.

## Required architecture principles

The resulting architecture contract MUST preserve these invariants:

- The evaluation is the system-level unit connecting input, evidence, analysis, Hotel Experience Model, report, and preview outcomes.
- A specific attempt/run remains traceable through downstream artifacts.
- External hotel content is untrusted input and never becomes executable internal authority.
- Evidence acquisition is bounded and cannot silently become unrestricted crawling.
- Evidence provenance survives normalization and downstream transformation.
- Analysis findings are distinct from hotel facts.
- The Hotel Experience Model remains the governed semantic source for the preview.
- The preview does not silently invent verified hotel facts.
- Report and preview are separate owner-facing outcomes that may share governed data but must not corrupt one another.
- Partial or failed upstream work must remain distinguishable from verified absence or negative hotel facts.
- Retries must not silently mutate historical evaluation outcomes.
- Security boundaries must be explicit at external-input, processing, persistence, and owner-facing-output boundaries.
- Architecture decisions must be independently evolvable where the specifications intentionally leave implementation choices open.

## Required output

Create/update the following documentation only:

1. `docs/architecture/EVALUATION-ARCHITECTURE.md` — implementation-neutral architecture contract.
2. `docs/architecture/README.md` — add the architecture document to the index, creating the index only if the repository convention requires it.
3. This requirement file — update the Session Completion Record when work is complete.
4. If a genuinely material architectural decision is made that cannot remain in the architecture contract, create an ADR under `docs/decisions/`; otherwise do not create an ADR merely for completeness.

The architecture document must clearly distinguish architectural requirements from future technology decisions.

## Acceptance criteria for this requirement

The resulting architecture contract MUST:

- map the approved specifications into clear conceptual components/responsibilities;
- define boundaries and data/control flow between evaluation, acquisition, evidence, analysis, Hotel Experience Model, report, and preview;
- preserve evaluation attempt/run traceability;
- define conceptual failure, partial, retry, and idempotency boundaries;
- define external/untrusted-content security boundaries;
- define provenance preservation across transformations;
- define report and preview as distinct outcomes of the same governed evaluation;
- define observability/audit expectations sufficient to explain what happened during an evaluation;
- identify which concerns are synchronous/request-path versus potentially asynchronous without selecting infrastructure technology;
- identify explicit extension seams for later architecture decisions;
- remain consistent with SPEC-001 through SPEC-008;
- avoid premature framework/cloud/database/vendor decisions;
- include implementation-oriented acceptance criteria and important edge cases;
- update the architecture index appropriately.

## Git / PR requirements

- Create the exact branch specified above: `spec/evaluation-architecture-contract`.
- Create the branch from the current `main`.
- Do not use another branch name.
- Documentation/specification/architecture changes only.
- Update this SAME requirement file with the completion record.
- Include exact branch, PR number, PR URL, changed files, validation performed, and open questions.
- Create the PR against `main`.
- Do not merge the PR.

## Session Completion Record

### Completion status

Not started.

## Orchestrator Review History

No orchestrator review has occurred yet.
