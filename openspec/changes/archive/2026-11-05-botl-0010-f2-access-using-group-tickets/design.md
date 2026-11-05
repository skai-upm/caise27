# Design

## Context

`ParkController.enterAttraction` already uses `Ticket.holders()` and an all-or-nothing capacity check, so group tickets need no new access path. See proposal.md - Why.

## Decisions

- **No type-specific code for group entry.** Group access is covered by tests through the existing operation.
- **`ParkController.leaveAttraction(attractionId, email)`** delegates to `Attraction.leave` and raises `ParkException` when the attraction is unknown or the user isn't inside.
- **STATUS output** is a fixed multi-line block computed from `Attraction` getters: `ATTRACTION <id>`, `NAME: <name>`, `CAPACITY: <n>`, `INSIDE: <n>`, `AVAILABLE: <n>`, then `USERS: (none)` or one e-mail per line. `Attraction.getAvailablePlaces()` is added.
- EXIT confirms with `USER <email> LEFT ATTRACTION <id>`.
