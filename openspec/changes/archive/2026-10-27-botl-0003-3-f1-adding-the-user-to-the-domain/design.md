# Design

## Context

The code base has a single `Main` entry point (package `es.upm.amusementpark`). See proposal.md for motivation.

## Goals / Non-Goals

**Goals:** a plain, encapsulated `User` entity ready for later validation, persistence and modification.

**Non-Goals:** validation, uniqueness, persistence, commands.

## Decisions

- **Package `es.upm.amusementpark.model`** for domain entities, keeping `Main` (start-up) separate as §3.2 demands; a future `repository`/storage package will hold persistence.
- **`final String dni`** with no setter; other fields private with getters/setters. Alternative (fully immutable record with `with…` copies) rejected: F1-C06 modifies users in place and records cannot hide the DNI component from reflection-free updates.
- **Constructor taking all seven attributes** in source order. No defensive validation now to avoid inventing rules before F1-C02.
- **`equals`/`hashCode` on DNI**, since DNI is the stable identity used by delete/modify operations later.

## Risks / Trade-offs

- [Setters allow invalid data before F1-C02 exists] → validation milestone will route all mutations through validation.
