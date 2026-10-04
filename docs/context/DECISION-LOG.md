# Decision Log

This is a compact record of durable decisions that a future orchestrator should not accidentally reverse. It is not a complete project history.

## Decision 001 — Hospitality-first product boundary

**Status:** Active

**Decision:** Antena Hotel Check is a hospitality-specific digital experience analyzer. Generic SEO, performance, accessibility, and technical health are supporting concerns, not the product center.

**Rationale:** The product question is whether a guest can find, understand, trust, explore, and book a hotel online.

**Source:** Project product direction and repository specifications/context.

## Decision 002 — Bounded public-web acquisition

**Status:** Active

**Decision:** Public-web acquisition is bounded and service-owned. The product must not depend on aggressive scraping or bypassing third-party protections.

**Rationale:** Acquisition must be predictable, secure, and respectful of public-web boundaries while providing evidence for evaluation.

**Source:** Public-web acquisition requirements/specifications and ADR/architecture records in the repository.

## Decision 003 — Acquisition and evidence are separate boundaries

**Status:** Active

**Decision:** Acquisition produces typed acquisition results; downstream normalization produces structured evidence. Acquisition failures must not silently become hospitality-feature absence.

**Rationale:** Separating acquisition from interpretation preserves lifecycle semantics, provenance, and truthful failure handling.

**Source:** REQ-017 and REQ-018 plus the repository evidence model.

## Decision 004 — Evidence is not automatically a hotel fact

**Status:** Active

**Decision:** Downstream representations must remain traceable to source evidence and must distinguish observed signals from inference, demonstration, and unavailable information.

**Rationale:** A public page signal such as a booking CTA is not proof that booking succeeds; absence of an observation is not proof of absence.

**Source:** Evidence-model requirements and REQ-018/REQ-019 contracts.

## Decision 005 — Hospitality observations are deterministic first

**Status:** Active

**Decision:** The initial hospitality observation layer is deterministic and bounded. AI/LLM interpretation is not a foundational dependency at this stage.

**Rationale:** Deterministic behavior is easier to test, trace, constrain, and review while the domain boundaries are being established.

**Source:** REQ-019 and current architecture.

## Decision 006 — Requirements govern implementation sessions

**Status:** Active

**Decision:** A requirement is created on `main` before implementation. Sessions work only within that requirement, update the same requirement with completion evidence, create a PR, and stop for orchestrator review. Only the orchestrator approves a requirement.

**Rationale:** This keeps scope explicit and makes the project recoverable across sessions.

**Source:** `requirements/README.md` and development lifecycle documentation.

## Decision 007 — Repository is durable truth

**Status:** Active

**Decision:** Chat/session memory is temporary working context. Repository state, requirements, specifications, architecture/decision records, tests, and merged code are authoritative.

**Rationale:** A long-running project must remain recoverable if an orchestrator or implementation session ends or changes.

**Source:** Orchestrator durable-context foundation.

## Decision 008 — Antena is the downstream product integration

**Status:** Active

**Decision:** Antena (`https://antenapro.com`) is our own hospitality product and the eventual downstream destination for Hotel Check conversion opportunities. Once digital-presence analysis is mature enough, Hotel Check may provide an Antena-hosted hotel experience at `<hotel-name>.antenapro.com` to address problems identified by the analysis.

**Rationale:** The analysis must first become trustworthy and useful. Premature integration would couple the early analysis foundation to downstream site generation and could distort scope.

**Source:** Product direction maintained by the orchestrator.

## Decision 009 — Analysis maturity precedes Antena integration

**Status:** Active

**Decision:** Current development focuses on digital-presence analysis only. Antena-hosted hotel experience integration is intentionally downstream and must not be treated as implemented, required in early analysis slices, or used to justify premature architecture.

**Rationale:** Establish a reliable analysis product before building the downstream solution that addresses the identified problems.

**Source:** Product sequencing decision maintained by the orchestrator.
