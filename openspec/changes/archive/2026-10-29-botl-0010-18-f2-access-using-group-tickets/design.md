# Design

## Context

`ParkController.enterAttraction` already checks everything through `Ticket.admittedUsers()` before mutating, but rejects `GroupTicket` with a temporary guard.

## Goals / Non-Goals

**Goals:** atomic group entry via the same path; EXIT; STATUS.

**Non-Goals:** persistence, ticket consumption, USER commands.

## Decisions

- **Remove the `GroupTicket` guard**: the existing pre-checks (date, none already inside, `occupants + admitted ≤ capacity`) run before any `addOccupant`, which makes group entry all-or-nothing without a separate mechanism.
- **`exitAttraction(attractionId, email)`**: find the occupant by email in the attraction's own collection (not in the user directory), then `removeOccupant`; otherwise "user ... is not inside attraction ...".
- **`Attraction.getAvailablePlaces()`** = capacity − occupants; STATUS output lines: `ATTRACTION: <id>`, `NAME: <name>`, `CAPACITY: <n>`, `USERS INSIDE: <n>`, `AVAILABLE PLACES: <n>`, `OCCUPANTS: <email, ...|none>`.

## Risks / Trade-offs

- [Email lookup in occupants] → independent of the user directory, so it keeps working whatever later USER DELETE rules do.
