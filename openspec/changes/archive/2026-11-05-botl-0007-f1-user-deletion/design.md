# Design

## Context

`CommandProcessor` dispatches on the first `;`-element and currently implements only `USER CREATE`. `UserRepository` is DNI-keyed, file-backed, and saves after `add`. The file format and load/save logic live in `UserFileStore`.

## Goals / Non-Goals

**Goals:**
- `USER DELETE` following source §8's five steps in order.

**Non-Goals:**
- Lockout after repeated wrong passwords, password hashing, and confirmations. None of these is requested.

## Decisions

- **`UserRepository.findByEmail(String)` and `remove(String dni)`**. `remove` saves through the store just like `add`, which keeps persistence in one place. Email lookup is a linear scan; an email index is unnecessary at this scale.
- **Steps in `CommandProcessor`**: element count → `findByEmail` (not found → `ERROR: no user with email ...`) → `password.equals` (mismatch → `ERROR: incorrect password`) → `remove(dni)` → `OK: user <dni> deleted`. The not-found and wrong-password cases get distinct messages, which mirrors §8's separate "verify exists" and "check password" steps.
- **Exact, case-sensitive comparison of email and password**, consistent with how they are stored. The source describes no normalisation.

## Risks / Trade-offs

- [Distinct "no user" / "incorrect password" messages reveal whether an email is registered] → Acceptable for this coursework CLI. The source lists the two checks as separate steps.
