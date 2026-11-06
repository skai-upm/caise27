# Design

## Context

See proposal.md – Why. `Ticket` (abstract) has `IndividualTicket` and `GroupTicket` subclasses; `Attraction` has `enter/leave/isFull/contains`; `ParkController` coordinates cross-element operations and knows the current date.

## Goals / Non-Goals

**Goals:**
- One access algorithm over the common `Ticket` type; group tickets (F2-C07) plug in by their own `holders()`.

**Non-Goals:**
- Exiting attractions; restricting a user to one attraction at a time (the source does not define it).

## Decisions

- **`Ticket.holders()`** (abstract, returns `List<User>`) and **`Ticket.isValidOn(date)`** (`date.equals(getDate())`). `IndividualTicket.holders()` = its user; `GroupTicket.holders()` = users of its composition (implemented now for completeness of the abstraction, not yet reachable from the command).
- **`AttractionService.enter(attractionId, Ticket, today)`**: validity, then "already inside" check for every holder, then capacity check `occupants + holders ≤ capacity`, then all holders enter. All checks happen before any change, so entry is all-or-nothing by construction.
- **`ParkController.enterAttraction(attractionId, ticketId)`** resolves the attraction first, then the ticket with `TicketService.findIndividualForAccess(id)` (top-level individual tickets only in this milestone; F2-C07 widens it to any ticket).
- **Messages**: `Entered A01: ana@correo.com`; errors `no attraction A01`, `no ticket T999`, `ticket T002 is not valid on 2027-04-15`, `attraction A03 does not have capacity for 1 users`, `user ana@correo.com is already inside attraction A01`.
