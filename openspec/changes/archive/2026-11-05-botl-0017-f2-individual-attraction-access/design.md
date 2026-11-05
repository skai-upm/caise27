# Design

## Context

`Ticket` (abstract) has `IndividualTicket` (one user) and `GroupTicket` (composition of individual tickets). `Attraction` has `capacity`, `addOccupant`, `isInside`. `AttractionCommands` implements CREATE and LIST. Source §10 requires one access mechanism built on the common ticket abstraction, and group access must be indivisible.

## Goals / Non-Goals

**Goals:**
- An `ATTRACTION ENTER` implementation that is already correct for any number of holders, so group access only needs its guard removed.

**Non-Goals:**
- Leaving attractions and group access semantics (next milestone).

## Decisions

- **`Ticket.getHolders()`** (abstract) returns the users who enter with the ticket. `IndividualTicket` returns a list with just its user, and `GroupTicket` returns the users of its composition. Access code depends only on `Ticket`.
- **Validity**: `ticket.getDate().equals(currentDate)`. A ticket is "valid only on the date specified at creation" (§7.2).
- **Entrants** = holders not already inside. If there are none, the response is `ERROR: … already inside`. Capacity check: `occupants + entrants ≤ capacity`. Users are added only after every check passes, which makes the operation all-or-nothing for any number of holders.
- **Temporary guard**: `if (ticket instanceof GroupTicket)` → `ERROR: access with group tickets is not available yet`. It is placed before the generic path and removed in the next milestone.
- **Response**: `OK: 1 user(s) entered attraction A01 (Dragon Rojo): ana@correo.com`.

## Risks / Trade-offs

- [No exit command, so attractions fill up during an execution] → The source defines no exit operation. The capacity rule is still enforced.
