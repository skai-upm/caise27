# Design

## Context

`UserService` holds user operations, `UserRepository` persists after each change, `CommandProcessor` parses `;` commands (see specs for behaviour).

## Goals / Non-Goals

**Goals:**
- Deletion following the source's five steps in order.

**Non-Goals:**
- USER UPDATE (F1-C06); password hashing (Delivery 3).

## Decisions

- **`UserService.deleteUser(email, password)`**: `findByEmail` → if absent, `UserOperationException("No user with email ...")` → if `!password.equals(user.getPassword())`, exception "Incorrect password." → `repository.delete(email)` (which rewrites the users file). Keeping the checks in the service lets USER UPDATE reuse the same identification logic.
- **`CommandProcessor`**: `USER DELETE` requires exactly 3 elements; prints `USER DELETED` (as in the source example) or `ERROR: ...`.
- **Unknown email vs wrong password messages:** reported distinctly, following the source's separate steps "verify that the user exists" and "check the password".
- **Mandatory commit:** `[F1-C05] Implement user deletion` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Distinct messages reveal whether an email is registered] → Acceptable for this course application; the source lists the checks as separate steps.
