# Design

## Context

`Ticket.getIndividualTickets()` exposes the users of any ticket; `Attraction` enforces capacity/no-duplicate per occupant; `ParkController.findTicket` returns top-level tickets.

## Goals / Non-Goals

**Goals:** ATTRACTION ENTER on the common abstraction with atomic capacity check.

**Non-Goals:** group-ticket-specific rules and verification (F2-C07), ticket/pass consumption (F3-C08), leaving attractions (not in the source).

## Decisions

- **`ParkController.enterAttraction(attractionId, ticketId)`**: looks up attraction and top-level ticket, checks `ticket.getDate().equals(currentDate)`, collects `ticket.getIndividualTickets()` users, rejects if any is already inside or if `occupants + users > capacity`, then adds all. Validation completes before any mutation → indivisible.
- **`Attraction.admit(List<User>)`**: model-level all-or-nothing add (checks first, then adds) reused by the controller, keeping capacity invariants in the model.
- **Command** `AttractionEnterCommand` → `ATTRACTION ENTERED: <n> user(s) in <id>`.

## Risks / Trade-offs

- [Group tickets admitted before F2-C07] → Required by the "common mechanism" rule; F2-C07 adds the explicit spec and tests.
