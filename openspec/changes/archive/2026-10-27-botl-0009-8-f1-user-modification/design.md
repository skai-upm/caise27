# Design

## Context

`UserService` (create, delete), `UserValidator` (per-field rules), `UserRepository` (keyed by email, saves after each change), `CommandProcessor` (dispatch).

## Goals / Non-Goals

**Goals:**
- Enum-driven field update with validation-before-mutation and persistence.

**Non-Goals:**
- Telephone, name and surname validation (no rules defined by the source).

## Decisions

- **`UserField` enum** `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`; parsing with a loop over `values()` comparing `name()` exactly (no `valueOf` exception flow). Field names are matched exactly as written in the source (uppercase); `phone` is rejected (ambiguity, flagged).
- **`UserService.updateUser(email, password, field, value)`**: identify (same checks as delete, extracted into a private `authenticate`), validate the new value for EMAIL/PASSWORD/CARD with `UserValidator`, check email uniqueness against *other* users (keeping one's own email is allowed), then mutate and call `repository.update(oldEmail, user)`.
- **`UserRepository.update(oldEmail, user)`** removes the old key and re-inserts under the current email, then persists. Insertion order changes for an email change; acceptable since order is not specified.
- **Output:** `USER UPDATED` (source example) / `ERROR: ...`.
- **Mandatory commit:** `[F1-C06] Implement user update` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Mutating the `User` object before persisting] → Validation happens before mutation, so a failed check never leaves a partially updated user; a write failure surfaces as an error message.
