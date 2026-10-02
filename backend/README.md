# Antena Hotel Check Backend

This module is the Java 21 / Spring Boot foundation for the evaluation input, lifecycle, and persistence boundaries.

## Scope

REQ-011 implements only:

- the two supported input forms from SPEC-002;
- deterministic input normalization and validation;
- the typed accepted/rejected result model;
- unit coverage for normal and boundary cases.

REQ-012 implements only:

- evaluation and evaluation-attempt domain representations;
- deterministic lifecycle states and transition enforcement;
- terminal outcome and lifecycle metadata retention;
- retry/attempt semantics that preserve the accepted canonical request;
- minimal owner-facing completion gating for report + interactive preview outcomes;
- capability outcomes that can be partial/unavailable/failed without automatically failing the evaluation.

REQ-013 implements only:

- PostgreSQL/JPA persistence for the existing evaluation aggregate;
- repository/domain separation through `EvaluationRepository`;
- persistence and reconstruction of canonical request, attempts, lifecycle state, terminal outcomes, capability outcomes, and owner-facing outcomes;
- Testcontainers PostgreSQL integration coverage for reload and retry semantics.

The backend does not perform hotel resolution, crawling, network acquisition, analysis, scoring, AI generation, preview generation, authentication, or production deployment.

## Local validation

From `backend/`:

```bash
mvn test
```

REQ-013 persistence tests require Docker/Testcontainers to be available locally or in CI.

Run the Spring Boot foundation locally with:

```bash
mvn spring-boot:run
```

There is intentionally no public HTTP endpoint in these implementation slices. The input, lifecycle, and persistence boundaries remain separated so later application/API layers can invoke them without coupling the domain contract to transport or JPA concerns.
