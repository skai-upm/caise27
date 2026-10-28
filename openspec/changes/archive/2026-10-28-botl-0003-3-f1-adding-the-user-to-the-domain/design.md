# Design

## Context

The application currently consists of `App` (entry point and input loop). See proposal.md for motivation.

## Goals / Non-Goals

**Goals:** a plain, encapsulated `User` class ready for later validation, persistence and modification.

**Non-Goals:** validation of values (F1-C02), user storage/registration (F1-C03+), persistence (F1-C04).

## Decisions

- **Package `es.upm.amusementpark.model`** for domain classes, keeping `App` in the root package; later storage/persistence classes go in their own packages.
- **`dni` is a `final` field with a getter only**; all other attributes have getters and setters. Constructor takes all seven attributes. Alternative: fully immutable user with copy-on-modify — rejected because the delivery later requires modifying user data in place.
- **No `equals`/`hashCode` override**: identity rules (e.g. uniqueness of DNI) are defined by later milestones at the storage level, not invented here.

## Risks / Trade-offs

- [Setters accept any value until validation exists] → F1-C02 adds validation without changing this public shape.
