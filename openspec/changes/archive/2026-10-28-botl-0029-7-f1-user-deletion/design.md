# Design

## Context

`ParkController.deleteUser(email)` enforces active-ticket and locked-group rules and removes the user from groups; `authenticate` verifies with the injected `PasswordHasher`.

## Decisions

- **`deleteUser(email, password)`**: `findUser` → `NotFoundException`; `passwordHasher.matches` (no hash or mismatch) → `AuthenticationException`; then `deleteUser(email)`. Checks precede any change.
- **`UserDeleteCommand`** (2 fields, `USER DELETED`) registered as `USER DELETE`.
