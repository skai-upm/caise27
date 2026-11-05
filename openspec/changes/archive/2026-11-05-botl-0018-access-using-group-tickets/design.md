# Design

## Context

`AttractionService.enter` already uses `Ticket.admittedUsers()` and `Attraction.admitAll` (all-or-nothing); `GroupTicket.admittedUsers()` returns its composition's users.

## Goals / Non-Goals

**Goals:** verify group entry atomicity on the shared path; add exit and status. **Non-Goals:** persistence.

## Decisions

- **No type-specific entry code**: group entry reuses `enter` unchanged; tests confirm atomicity.
- **`exit(attractionId, email)`** finds the occupant by email among the attraction's own occupants (not through the user repository), so it relies only on the attraction state.
- **Status text** (multi-line): `ATTRACTION A01 - Dragon Rojo`, `Capacity: 20`, `Inside: 2`, `Available: 18`, `Users: pedro@correo.com, luis@correo.com` (or `Users: none`).

## Risks / Trade-offs

- None significant.
