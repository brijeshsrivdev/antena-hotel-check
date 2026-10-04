# Orchestrator Context

## Purpose

This document is the compact, durable orientation for the Antena Hotel Check orchestrator. It is not a replacement for product specifications, architecture documents, requirements, or code.

## Product Objective

Antena Hotel Check evaluates a hotel's publicly accessible digital presence and guest journey with hospitality-specific analysis. The long-term product question is:

> Can a guest find, understand, trust, explore, and book this hotel online?

The product ultimately produces a useful hospitality analysis/report and an interactive Antena-hosted hotel preview. The preview is a core product capability, not merely a marketing add-on.

## Product Boundary

Hospitality is the center of the product. SEO, performance, accessibility, structured data, and technical health may contribute evidence, but the product must not drift into a generic SEO checker, generic website audit, OTA/competitor scraping product, or generic AI website generator.

Avoid aggressive scraping or bypassing third-party protections. Public-web acquisition must remain bounded and truthful.

## Engineering Method

The project uses Specification-Driven Development (SDD) plus Test-Driven Development (TDD):

`Requirement → Specification/Architecture → Implementation Session → Tests/Validation → PR → Orchestrator Review → Merge`

Implementation sessions are bounded. They inspect the repository first, follow the assigned requirement, implement only assigned scope, test and validate, update durable records, and stop for orchestrator review.

## Orchestrator Responsibilities

The orchestrator:

- protects the product objective and boundaries;
- inspects current repository state before deciding the next slice;
- creates/refines requirements when needed;
- provides bounded implementation prompts;
- reviews complete PRs against requirements, architecture, tests, security, UX/product behavior, documentation, and CI;
- requests changes when justified;
- approves requirements only when evidence supports approval;
- keeps durable project context current.

The orchestrator does not treat a green CI run as automatic approval.

## Truthfulness and Evidence

The system must distinguish publicly discovered information, normalized/derived representations, inferred information, and demonstration/generated information. A missing observation is not proof of absence. Source evidence must remain traceable through downstream representations.

An observation such as `Book Now` is evidence of a booking entry point, not proof that booking succeeds. Agents must not silently turn weak signals into verified hotel facts.

## Agent and GSD Principles

Agents should use scoped context rather than dumping the entire repository schema into prompts. Each implementation task should identify what to read, what to inspect, dependencies, what not to touch, acceptance criteria, and expected output.

Agents must stop on material ambiguity rather than inventing architecture, requirements, product behavior, or repository state. Engineering guidance should favor simple, established patterns appropriate to the existing codebase and production concerns such as security, reliability, testability, maintainability, and observability.

GSD is an execution aid, not the source of product truth. Requirements, specifications, architecture, code, tests, and repository state remain authoritative.

## Authority Order

When information conflicts, use this order of authority:

1. Current repository state and merged code/tests.
2. Approved requirements and their completion/review records.
3. Product specifications and architecture/decision records.
4. Current durable orchestrator context.
5. Temporary session/chat memory.

Chat memory is working context, not durable project truth.
