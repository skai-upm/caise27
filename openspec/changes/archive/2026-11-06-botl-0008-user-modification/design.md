# Design

## Context

See proposal.md – Why. `UserService` already authenticates for deletion and validates for creation; `UserRepository` persists before updating memory.

## Goals / Non-Goals

**Goals:**
- One enum as the single definition of modifiable fields; reuse authentication and validation code paths.
- A failed validation or failed file write leaves memory and file unchanged.

**Non-Goals:**
- Updating several fields in one command.

## Decisions

- **`UserField` enum (package `es.upm.park.model`)**: `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`. Parsing uses `UserField.valueOf(name)` guarded by a lookup that maps unknown names (including `DNI`) to `UserException("field <name> cannot be modified")`. The DNI is excluded by construction, matching the immutable-DNI requirement.
- **`UserService.update(email, password, fieldName, value)`**: shared private `authenticate(email, password)` (also used by `delete`); then per-field validation (`EMAIL`: `isValidEmail` + not used by another user; `PASSWORD`: `isValidPassword`; `CARD`: `isValidCardNumber`; `NAME`/`SURNAME`/`PHONE`: no extra rules, as at creation). It builds a modified copy of the user and calls `repository.replace(old, copy)`.
- **`UserRepository.replace(old, updated)`** saves the list with the copy, then swaps it in memory; the original object is never mutated before the write succeeds. Alternative: mutate then roll back — rejected as error-prone.
- **Messages**: `User updated: <DNI> <FIELD>`; field count error `Error: USER UPDATE expects 4 fields`; the field name is matched exactly (upper case), as in the source examples.

## Risks / Trade-offs

- [Updating EMAIL to the user's own current email] → allowed (not "used by another user").
