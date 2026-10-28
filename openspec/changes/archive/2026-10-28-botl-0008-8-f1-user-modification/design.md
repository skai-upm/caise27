# Design

## Context

`UserService` already authenticates by email/password (deletion) and persists before touching the repository. See proposal.md.

## Goals / Non-Goals

**Goals:** enum-driven updates reusing validation, uniqueness, authentication and persistence.
**Non-Goals:** changing several fields in one command.

## Decisions

- **`model.UserField` enum** (`NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`) with `static Optional<UserField> fromName(String)`; parsing happens in the processor, which reports `Field cannot be modified: <name>` for anything else (covers `DNI`).
- **Copy-then-commit**: `UserService.updateUser(email, password, field, value)` authenticates, validates the value for the field, builds a modified copy of the user, persists the list with the copy, then stores the copy in the repository (same key = DNI, order preserved). A failed save leaves the original untouched.
- **Validation per field** in a `switch` on the enum, reusing `UserValidator` and the email-uniqueness check from creation (excluding the user itself).

## Risks / Trade-offs

- [Copying the user duplicates the attribute list] → kept local to the service in one helper.
