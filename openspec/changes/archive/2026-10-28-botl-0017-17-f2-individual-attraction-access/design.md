# Design

## Context

`Ticket.individualTickets()` yields the individual tickets of any ticket; `TicketRepository.findIndividualById` and `findById` locate tickets; `Attraction` has occupants and capacity. See proposal.md.

## Goals / Non-Goals

**Goals:** one entry algorithm for all ticket types, atomic.
**Non-Goals:** group-ticket entry being enabled (F2-C07), exit/status commands, ticket consumption.

## Decisions

- **`AttractionService.enter(Attraction, Ticket, LocalDate today)`**: date check (`ticket.getDate().equals(today)`), entering users = `ticket.individualTickets()` → users; reject if any is already inside; reject if `occupants + entering > capacity`; only then add all. Atomic by construction.
- **Ticket lookup** in `ParkController.enterAttraction(attractionId, ticketId)`: top-level ticket by id, else an individual composition ticket by id; in this milestone a `GroupTicket` is rejected before calling `enter`.
- Returns the list of users who entered; the processor prints one `USER ENTERED ATTRACTION: <email>` line per user.

## Risks / Trade-offs

- [Temporary restriction on group tickets] → a single guard in the controller, removed in F2-C07.
