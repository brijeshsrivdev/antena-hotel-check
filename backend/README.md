# Antena Hotel Check Backend

This module is the initial Java 21 / Spring Boot foundation for the evaluation input boundary.

## Scope

REQ-011 implements only:

- the two supported input forms from SPEC-002;
- deterministic input normalization and validation;
- the typed accepted/rejected result model;
- unit coverage for normal and boundary cases.

It does not perform hotel resolution, crawling, network acquisition, analysis, scoring, AI generation, preview generation, persistence, authentication, or production deployment.

## Local validation

From `backend/`:

```bash
mvn test
```

Run the Spring Boot foundation locally with:

```bash
mvn spring-boot:run
```

There is intentionally no public HTTP endpoint in REQ-011. The input boundary is implemented as a deterministic domain service so a later application/API layer can invoke it without coupling the domain contract to transport concerns.
