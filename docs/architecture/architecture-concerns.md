# Architecture Decision Areas

**Status:** PROPOSED / OPEN

No final technology stack is selected in Session 1.

## System shape
Determine minimum boundaries for input/API, public evidence acquisition, normalization, analysis, report generation, preview generation, and preview runtime/hosting.

## Execution model
Decide synchronous vs asynchronous/queued/hybrid execution, including retries, timeouts, cancellation and partial completion.

## Web acquisition
Decide static HTTP vs browser rendering where justified, JavaScript-heavy-site handling, access/robots constraints, rate limits, domain concurrency, SSRF protection and isolation.

## Evidence/provenance
Define a durable chain from source observation → normalized data → finding → preview content while preserving discovered, normalized, inferred and demonstration states.

## Analysis model
Decide rule-based checks, deterministic heuristics, optional model-assisted interpretation, scoring semantics, evidence thresholds and repeatability. Do not introduce AI merely because it is available.

## Preview architecture
Decide content representation, rendering model, assets, navigation, external booking handoff, hotel isolation, lifecycle and hostname routing.

## Storage
Determine what persists: evaluation request, evidence, analysis results, preview model, assets and execution logs; then define retention.

## Security/abuse
Design for SSRF, untrusted remote content, script execution, resource exhaustion, malicious URLs, rate abuse, tenant isolation and preview isolation.

## Observability
Define evaluation states, per-capability outcomes, errors/limitations, traceability, metrics and auditability.

## Technology selection
Only after requirements and constraints are sufficiently understood should language/runtime, framework, datastore, browser technology, queue/workflow, hosting, storage/CDN and optional model providers be selected.

## Decision rule
Material choices must become decision records under `docs/decisions/`.
