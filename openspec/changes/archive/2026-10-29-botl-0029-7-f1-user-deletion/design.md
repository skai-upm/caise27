# Design

## Context

`ParkController` offers `verifyPassword(email, plain)` (throws on mismatch), `canDeleteUser(User)` (active tickets or locked groups), groups with `removeMember`, and an email-keyed `users` map; persistence runs after each command; tickets embed email/DNI so they reload without the user.

## Goals / Non-Goals

**Goals:** USER DELETE following the source steps and existing rules. **Non-Goals:** USER UPDATE.

## Decisions

- **`ParkController.deleteUser(email, password)`**: `existingUser(email)` → `ElementNotFoundException`; `verifyPassword` → `OperationNotAllowedException("invalid password ...")`; `!canDeleteUser(user)` → `OperationNotAllowedException("user ... has an active ticket or belongs to a group with an active group ticket")`; then `group.removeMember(user)` for every group and `users.remove(email)`.
- **`CommandProcessor`**: case `"USER DELETE"` with 2 arguments → `USER DELETED`.
- Persistence unchanged: the next save writes the users and groups without the user.

## Risks / Trade-offs

- [Ticket objects still reference the deleted user object] → intended: historical records keep the purchaser's data.
