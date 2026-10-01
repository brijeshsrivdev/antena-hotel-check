# Durable Project Context

This directory contains durable context that implementation sessions must read before making changes.

## Status vocabulary

- **IMPLEMENTED** — verified in the repository and usable.
- **IN PROGRESS** — actively being implemented.
- **SPECIFIED** — behavior is agreed enough to implement, but is not implemented.
- **PLANNED** — intended future work without a complete implementation contract.
- **PROPOSED** — candidate direction awaiting an explicit decision.
- **DEFERRED** — intentionally postponed.
- **REJECTED** — explicitly not part of the product direction.

Never describe SPECIFIED, PLANNED, PROPOSED, DEFERRED, or REJECTED work as IMPLEMENTED.

## Session rule

Every implementation session must start by reading the relevant files in `docs/context/` and `docs/specs/` and checking repository state. If context is contradictory or insufficient for a material decision, stop and report the ambiguity to the orchestrator.
