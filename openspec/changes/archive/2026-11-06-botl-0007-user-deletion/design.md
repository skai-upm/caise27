# Design

## Context

See proposal.md – Why. Commands go through `CommandProcessor` → `UserService` → `UserRepository`, which saves the whole user list through `UserFileStore` before updating memory.

## Goals / Non-Goals

**Goals:**
- Reuse the existing layering; deletion follows the same save-before-update rule as creation.

**Non-Goals:**
- Password hashing or encryption, case-insensitive email lookup.

## Decisions

- **`UserService.delete(email, password)`** implements the five source steps in order: `repository.findByEmail(email)` (absent → `UserException("no user with email <email>")`), exact `String.equals` password check (mismatch → `UserException("incorrect password")`), then `repository.remove(user)`.
- **`UserRepository.remove(user)`** builds the list without the user, saves it through the store, and only then removes it from memory, so a failed write leaves memory and file consistent. Alternative: remove then save and roll back on failure — more code for the same guarantee.
- **`CommandProcessor`** dispatches `USER DELETE` with exactly 2 fields; success message `User deleted: <email>`; field count error `Error: USER DELETE expects 2 fields`.

## Risks / Trade-offs

- [Error message reveals whether an email exists] → acceptable for a console application; the source asks to "verify that the user exists" as a distinct step.
