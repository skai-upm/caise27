# Design

## Context

`AttractionService.enter` already works on any `Ticket` through `individualTickets()` with all-or-nothing checks; `ParkController.enterAttraction` rejects `GroupTicket`. See proposal.md.

## Goals / Non-Goals

**Goals:** enable group entry with no new access path; exit and status from attraction state.
**Non-Goals:** ticket consumption rules, persistence.

## Decisions

- **Remove the group-ticket guard**; nothing else changes in the entry algorithm.
- **`AttractionService.exit(attractionId, email)`** finds the occupant by email among `attraction.getOccupants()` and removes it.
- **`AttractionService.status(attractionId)`** returns a small immutable view (`record AttractionStatus(id, name, capacity, occupants, available, emails)`) built from the attraction; the processor formats it.

## Risks / Trade-offs

- None significant.
