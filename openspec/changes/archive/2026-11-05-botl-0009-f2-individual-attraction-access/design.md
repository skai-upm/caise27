# Design

## Context

`Ticket` (abstract) has `IndividualTicket` and `GroupTicket` subtypes. `Attraction` has capacity-bounded package-private `enter`. See proposal.md - Why.

## Decisions

- **`Ticket.holders()`** is an abstract method returning the e-mails of the users the ticket admits. `IndividualTicket` returns its user. `GroupTicket` returns the users of its components. The access operation never checks the concrete type, which avoids separate access mechanisms.
- **`ParkController.enterAttraction(attractionId, ticketId)`** checks existence, the date (`ticket.getDate().equals(currentDate)`), that no holder is already inside, and that `capacity − occupants ≥ holders`. Only then does it admit everyone, so the operation is all-or-nothing by construction.
- **Command output**: `ENTERED ATTRACTION <id>: <email>[, <email>...]`, with errors prefixed `ERROR:`.
- Nothing is stored about when someone entered, so later date changes can't affect presence.
