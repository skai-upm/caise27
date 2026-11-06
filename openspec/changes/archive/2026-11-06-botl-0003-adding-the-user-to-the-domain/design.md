# Design

## Context

The code base has only the entry point `es.upm.park.Main` (see `executable-application`). Sections 3.1 and 3.2 set the user's data and the separation constraints. Design patterns are not required in this delivery.

## Goals / Non-Goals

**Goals:** a plain, encapsulated user model that later milestones (validation, registration, persistence, modification) can extend without breaking it.

**Non-Goals:** validation, storage, commands and patterns.

## Decisions

- **`es.upm.park.model.User`.** A class with private fields, a constructor taking all seven attributes, and getters for all of them. Setters exist for every attribute except DNI. The DNI field is `final` with no setter, so immutability is enforced by the compiler rather than at runtime. A Java `record` was considered and rejected: a record makes every field immutable, but the modification milestone needs the non-DNI fields to change.
- **Package-by-layer start (`model`).** Keeps the domain separate from the entry point and from the future store class required by section 3.2.
- **No validation in the constructor.** Validation rules belong to milestone F1-C02. Adding checks now would preempt that milestone's defined rules.

## Risks / Trade-offs

- [Without validation, a User can hold invalid data] → This is accepted and is temporary until F1-C02.
