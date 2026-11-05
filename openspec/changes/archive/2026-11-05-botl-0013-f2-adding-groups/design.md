# Design

## Context

`ParkController` (singleton) holds users, date and price; `CommandProcessor` dispatches on the first `;`-element with a `switch`. Users are DNI-keyed and their email can change via `USER UPDATE`.

## Goals / Non-Goals

**Goals:**
- Group operations whose lock rule is already wired to a single derived query, so group tickets can plug in later without changing the commands.

**Non-Goals:**
- Group tickets and group persistence.

## Decisions

- **`model.Group`** has an id, a name and an ordered list of member `User` references. Holding references, not emails, means that an email change through `USER UPDATE` keeps memberships valid. Membership checks compare DNIs. `Group` exposes only query and mutate methods (`contains`, `add`, `remove`, `getMembers` as an unmodifiable view).
- **`repository.GroupRepository`** is an in-memory, id-keyed `LinkedHashMap`. `ParkController` owns it and resets it in `startExecution`.
- **Derived lock: `ParkController.hasActiveGroupTicket(Group)`** returns `false` for now and is documented as "any associated group ticket dated ≥ current date". No lock flag exists on `Group`. The group-tickets milestone implements the query over the real tickets, and the commands keep calling it.
- **`command.GroupCommands`** handles the four commands so `CommandProcessor` stays small. `CommandProcessor` delegates when the command name starts with `GROUP `.
- **User deletion cleanup**: the `USER DELETE` handler calls `groups.removeUserEverywhere(user)` before removing the user.
- **Identifier comparison** is exact and case-sensitive, consistent with the other identifiers.

## Risks / Trade-offs

- [The lock is always `false` until tickets exist] → Explicitly scoped. The single query point keeps the later change local.
