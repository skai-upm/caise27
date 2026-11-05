# Design

## Context

The application has an entry point (`App`) and an input session (`InteractiveSession`). The assignment requires separate classes for the user, for starting the application and (later) for storing users; the user class must not manage persistence.

## Goals / Non-Goals

**Goals:**
- A plain `User` domain class in `es.upm.amusementpark.model`, independent of I/O and persistence.

**Non-Goals:**
- No validation (F1-C02), no storage (F1-C04), no commands.

## Decisions

- **Private fields, `final` DNI with getter only**: immutability is enforced by the type itself (no setter exists). Other attributes have getters and setters so USER UPDATE (F1-C06) can modify them without recreating the user. Alternative — a fully immutable value object with "with" copies — rejected: update semantics in the assignment modify an existing user.
- **All attributes stored as `String`**: DNI, telephone and card number have formats with leading zeros/letters; numeric types would lose information.
- **No validation in the constructor yet**: validation rules belong to F1-C02 and will be reusable by later operations.

## Risks / Trade-offs

- [Constructor accepts invalid data until F1-C02] → acceptable for this milestone; F1-C02 adds the rules.
