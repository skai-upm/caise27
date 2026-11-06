# Design

## Context

`ParkController.enterAttraction` already obtains the admitted users through `Ticket.admittedUsers()` and checks validity, presence and capacity before anyone enters. `GroupTicket.admittedUsers()` returns the owners of its composition. `Attraction` keeps occupants and capacity and has `leave`.

## Goals / Non-Goals

**Goals:** verified atomic group entry with no new access path, an exit operation, and a status view computed from the attraction.
**Non-Goals:** occupant persistence. Any rule tying exit to tickets (the source defines none).

## Decisions

- **No new entry code**: group entry reuses `enterAttraction`. This change adds tests that pin down atomicity (capacity, date, already-inside) for group tickets.
- **`ParkController.exitAttraction(attractionId, email)`** removes one occupant. It rejects an unknown attraction or a user who is not inside. It does not check tickets, because the source ties exit only to presence.
- **Status** is formatted by the command layer from `Attraction` (`getCapacity`, `getOccupantEmails`). Available places are computed as `capacity - occupants`, never stored. Output:
  `ATTRACTION A01 - Dragon Rojo | CAPACITY: 20 | INSIDE: 2 | AVAILABLE: 18` followed by `USERS: a, b` (or `USERS: none`).
- **Messages**: `EXITED <attraction>: <email>`.

## Risks / Trade-offs

- [Status output format is a free choice] → It contains every required field on labelled lines, which tests assert.
