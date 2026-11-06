# Design

## Context

`UserController` (singleton) creates users with injected `UserRegistry`, `PasswordService` and `UserStorage`. `ParkController.deleteUserReferences(email)` checks active tickets (including composed ones) and locked groups first, then removes the user from groups and saves the park state. If a check fails it throws and changes nothing.

## Goals / Non-Goals

**Goals:** USER DELETE following the source's steps, reusing the park rules through an injected collaboration.
**Non-Goals:** cascading ticket deletion (historical tickets must remain as fiscal records).

## Decisions

- **`UserController.setParkController(ParkController)`** is injected by `AppConfiguration`. Like `InvoiceController`, it fails fast if the collaborator is missing.
- **`deleteUser(email, rawPassword)`**:
  1. `registry.findByEmail`, else `NotFoundException`.
  2. `passwords.verify(raw, stored)`, else `BusinessRuleException("Incorrect password")`.
  3. `park.deleteUserReferences(email)`, which can throw and otherwise removes the user from groups and saves.
  4. `registry.remove(email)` and `save()`.
  Steps 1-2 never mutate. Step 3 mutates only after its own checks pass, so a rejected deletion changes nothing.
- **Command**: `withArgs(args, 2, "USER DELETE;<Email>;<Password>", …)` → `USER DELETED`.
- **Test support** wires `UserController` to `ParkController`.

## Risks / Trade-offs

- [If saving users fails after the groups were updated, the park and user files diverge] → Both stores use atomic writes, and the failure is reported. It is acceptable for this course scope.
