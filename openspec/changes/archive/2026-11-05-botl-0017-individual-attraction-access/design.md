# Design

## Context

`Ticket` (abstract) with `IndividualTicket` and `GroupTicket` (composition of individual tickets); `Attraction.admitAll` is all-or-nothing and respects capacity.

## Goals / Non-Goals

**Goals:** one entry path for all ticket types. **Non-Goals:** EXIT/STATUS and the group-ticket entry specification (F2-C07).

## Decisions

- **`abstract List<User> admittedUsers()` on `Ticket`**: `IndividualTicket` returns its user; `GroupTicket` returns its composition's users (implemented now because it is required to satisfy the abstract contract; its behavior is specified and verified in F2-C07).
- **`AttractionService.enter(attractionId, ticketId)`** (needs the ticket repository): find attraction and ticket → `ticket.isValidOn(today)` → `attraction.admitAll(ticket.admittedUsers())`; a false result becomes "not enough capacity or user already inside".
- Response: `ENTRY GRANTED: 1 user(s) entered A01`.

## Risks / Trade-offs

- [A user could be inside two different attractions] → the source does not forbid it; not checked.
