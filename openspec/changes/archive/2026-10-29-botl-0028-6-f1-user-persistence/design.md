# Design

## Context

`TextFileUserRepository` writes `email;dni;hash;firstName;surnames;phone;card` and reads 7-, 3- and 2-field lines.

## Goals / Non-Goals

**Goals:** assignment field order. **Non-Goals:** plain passwords.

## Decisions

- **Write** `join(firstName, surnames, dni, email, hash-or-empty, phone, card)`.
- **Read** 7-field lines in the assignment order; a 7-field line whose first field contains `@` is the earlier email-first layout of this batch and is mapped accordingly; 2/3-field lines as before.
- No change to `User` or `App` (separation already holds and is enforced by the architecture test).

## Risks / Trade-offs

- [Heuristic for the earlier layout] → names cannot contain `@` in practice; only affects data written by earlier commits of this batch.
