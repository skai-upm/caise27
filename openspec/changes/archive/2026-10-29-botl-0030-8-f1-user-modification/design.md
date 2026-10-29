# Design

## Context

`ParkController` has `verifyPassword`, `setPassword` (validates and re-hashes), an email-keyed users map, and `UserDataValidator`. `User` has setters for all fields but DNI.

## Goals / Non-Goals

**Goals:** USER UPDATE with enum fields and creation rules. **Non-Goals:** other user operations.

## Decisions

- **`business.model.UserField`** enum with `parse(text)` → `InvalidValueException("unknown or non-modifiable field ...")`.
- **`ParkController.updateUser(email, password, fieldText, newValue)`**: `verifyPassword` first; parse field; `switch`: `EMAIL` → `isValidEmail` and not used by another user, then `users.remove(old)`, `user.setEmail`, `users.put(new)`; `PASSWORD` → `setPassword(email, value)`; `CARD` → `isValidCard`; `NAME`/`SURNAME`/`PHONE` → non-empty, set. All validation before mutation.
- **`CommandProcessor`**: case `"USER UPDATE"` with 4 arguments → `USER UPDATED`.
- Tickets reference the `User` object and groups compare by DNI, so an email change needs no further updates; persistence writes the new email everywhere on the next save.

## Risks / Trade-offs

- [Re-keying the directory] → done in one place under the controller's control.
