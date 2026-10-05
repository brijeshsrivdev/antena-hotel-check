# REQ-037 — Evaluation Execution API Boundary

STATUS: IN PROGRESS
REQUIREMENT_ID: REQ-037
TYPE: Implementation
BRANCH: `feature/evaluation-execution-api`

## Objective

Expose the existing synchronous evaluation execution pipeline through a thin HTTP boundary without introducing new hospitality semantics.

## Endpoint

```text
POST /api/evaluations
Content-Type: application/json
```

The HTTP request uses the existing `HotelEvaluationInput` transport shape and is canonicalized by the existing `HotelEvaluationInputValidator` into the governed `CanonicalEvaluationRequest`.

Example website-only request:

```json
{
  "websiteUrl": "https://examplehotel.com"
}
```

A complete hotel identity may also be supplied according to the existing input contract:

```json
{
  "hotelName": "Example Hotel",
  "city": "Pune",
  "websiteUrl": "https://examplehotel.com"
}
```

## Execution Boundary

```text
HTTP POST
   ↓
EvaluationExecutionController
   ↓
HotelEvaluationInputValidator
   ↓
CanonicalEvaluationRequest
   ↓
EvaluationExecutionOrchestrator
   ↓
Existing governed evaluation pipeline
   ↓
EvaluationExecutionResult
   ↓
Thin HTTP response representation
```

The controller performs no acquisition, parsing, evidence normalization, observation, signal, finding, coverage, hospitality analysis, journey, recommendation, or report logic.

## Response

`EvaluationExecutionResponse` is a thin transport representation of the existing `EvaluationExecutionResult`. It preserves the existing evaluation, attempt, acquisition, evidence, analysis, guest journey, recommendations, and report artifacts without introducing parallel domain semantics.

## Validation

The existing canonical input validation rules are authoritative. In particular:

- missing usable input is rejected;
- blank website URL is rejected when it is the only input;
- malformed or unsupported website URL forms are rejected;
- no additional URL restrictions are introduced by the API boundary.

No new SSRF subsystem is introduced. Existing acquisition URL normalization and validation remain authoritative.

## Error Behavior

- Invalid evaluation input: HTTP 400 with machine-readable validation errors.
- Malformed JSON/request body: HTTP 400 with a machine-readable request-body error.
- Execution failure: HTTP 500 with a generic machine-readable execution error.
- Stack traces, credentials, secrets, and internal infrastructure details are not returned.

An execution failure is never converted into a successful/fake evaluation response.

## Security

The API exposes the existing public-web acquisition pipeline and does not weaken its target validation. No authentication or authorization is introduced because the current repository has no established API security boundary for this local/product-development endpoint.

## Persistence / Integrations / Async

REQ-037 adds no new persistence, database tables, external integrations, queues, background jobs, polling, SSE, WebSockets, schedulers, or workflow engines.

Execution remains synchronous.

## Tests

The API test boundary covers:

- successful HTTP request;
- canonical request delegation to the orchestrator;
- missing input;
- blank website URL;
- invalid website URL;
- malformed JSON;
- execution failure without fake success;
- preservation of important execution result fields.

The integration-style test exercises HTTP → controller → orchestrator using the repository's Spring MVC testing facilities without performing a real external hotel crawl.

## Explicit Non-Scope

- new hospitality dimensions;
- new journey stages;
- new coverage heuristics;
- new finding/deficiency/recommendation rules;
- scoring;
- AI/LLM analysis;
- UI/frontend;
- OpenAPI tooling when none exists;
- authentication/authorization;
- new SSRF subsystem;
- persistence changes;
- asynchronous execution;
- external integrations.

## Implementation Record

Added:

- Spring MVC web dependency;
- `EvaluationExecutionController`;
- `EvaluationExecutionResponse`;
- minimal API exception/error handling;
- API configuration wiring the existing execution pipeline;
- HTTP boundary integration tests.

The existing `EvaluationExecutionOrchestrator` remains unchanged.

## Requirement Status

`STATUS: IN PROGRESS`

PR: pending

Do not merge until orchestrator review is complete.
