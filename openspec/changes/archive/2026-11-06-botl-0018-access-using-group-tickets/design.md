# Design

## Context

See proposal.md – Why. `AttractionService.enter(attractionId, Ticket, today)` already works over `Ticket.holders()` with all checks before any change; `ParkController.enterAttraction` currently resolves only individual tickets.

## Goals / Non-Goals

**Goals:**
- No new access code for group tickets: only the ticket lookup widens.

**Non-Goals:**
- Automatic exit on date changes.

## Decisions

- **`TicketService.findForAccess(id)`** replaces `findIndividualForAccess`: any top-level ticket (individual or group). Constituent ids are not top-level and are not accepted on their own.
- **Exit by occupant email**: `AttractionService.exit(attractionId, email)` searches the attraction's occupants by email (state kept by the attraction itself), so it works regardless of entry path.
- **Status text** (multi-line): `Attraction A01 - Dragon Rojo`, `Capacity: 20`, `Inside: 2`, `Available: 18`, `Users: ana@correo.com, pedro@correo.com` (or `Users: none`).
- **Messages**: `Exited A01: ana@correo.com`; error `user luis@correo.com is not inside attraction A01`.
