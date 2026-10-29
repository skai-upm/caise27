# Design

## Context

`Attraction.addOccupant` enforces capacity and no duplicates; tickets are top-level `IndividualTicket`/`GroupTicket` in `ParkController`.

## Goals / Non-Goals

**Goals:** ATTRACTION ENTER for individual tickets via a common mechanism.

**Non-Goals:** group-ticket entry (next milestone), ATTRACTION EXIT/STATUS, ticket consumption.

## Decisions

- **`Ticket.admittedUsers()`** (abstract, `List<User>`): `IndividualTicket` → `List.of(user)`; `GroupTicket` → users of its composition in order. Access logic depends only on this.
- **`ParkController.enterAttraction(attractionId, ticketId)`** → `List<User>` entered: resolve attraction, resolve top-level ticket (composed id → "belongs to group ticket ... use the group ticket"; unknown → not found); reject `GroupTicket` for now ("access with group tickets is not available yet"); require `ticket.getDate().equals(currentDate)`; compute admitted users; reject if any is already inside or if `occupants + admitted > capacity`; only then add all (all-or-nothing by checking before mutating).
- Messages: one `USER <email> ENTERED ATTRACTION <id>` line per entrant.

## Risks / Trade-offs

- [Temporary group-ticket guard] → a single `instanceof` check the next milestone removes.
