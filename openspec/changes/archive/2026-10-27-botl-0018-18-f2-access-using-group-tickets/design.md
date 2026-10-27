# Design

## Context

`AttractionCommands.enter` already works on `ticket.holders()` with an all-or-nothing check sequence, but rejects `GroupTicket` explicitly (F2-C06 scope).

## Goals / Non-Goals

**Goals:** group entry, EXIT, STATUS.
**Non-Goals:** persistence of occupants (F2-C08 decides).

## Decisions

- **Remove the `GroupTicket` guard**: the existing checks run over all holders before any `enter`, which makes group entry atomic without a separate code path.
- **EXIT** finds the occupant by email in `attraction.getOccupants()` (the attraction's own state), then `attraction.exit(user)`.
- **STATUS** builds a multi-line text from `getId/getName/getCapacity/occupancy/freePlaces/getOccupants`.
