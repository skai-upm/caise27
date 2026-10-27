# Design

## Context

The application has a command loop (`App`, `CommandRunner`, `CommandProcessor`) but no domain model. Constraints from the assignment for Delivery 1: Java standard library only, encapsulated attributes, separate user / main / persistence classes, no design patterns required.

## Goals / Non-Goals

**Goals:**
- `User` with the seven attributes, encapsulated, DNI immutable.
- A storage class separate from `User` and `App`.

**Non-Goals:**
- Field validation (F1-C02), USER commands (F1-C03, F1-C05, F1-C06), file persistence (F1-C04).

## Decisions

- **`User`**: private fields; `dni` is `final` with only a getter; the other six attributes have getters and setters so later update operations (F1-C06) can modify them. No validation inside the constructor yet: validation rules are introduced by F1-C02 and must not be invented here.
- **`UserRepository`**: in-memory `LinkedHashMap<String, User>` keyed by email (the identifier used by USER DELETE/UPDATE), with `save`, `findByEmail` (returns `Optional<User>`), `delete`, `findAll`. Later F1-C04 adds file loading/saving behind this class. Alternative: keying by DNI — rejected because commands identify users by email.
- **Mandatory commit:** `[F1-C01] Add user model` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Email used as key may change via a later EMAIL update] → The repository will re-key the user when that update is introduced (F1-C06).
