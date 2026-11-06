# Design

## Context

`Ticket` is the common abstraction (`IndividualTicket` and `GroupTicket` with its composition). `Attraction` keeps occupant emails and capacity. The source requires attraction access to reuse the common ticket abstraction instead of having separate mechanisms per ticket type.

## Goals / Non-Goals

**Goals:** one type-independent, atomic entry operation.
**Non-Goals:** specifying group-entry rules (F2-C07) or an exit operation.

## Decisions

- **`Ticket.admittedUsers()`** (abstract, returns a list of emails). `IndividualTicket` returns its owner, and `GroupTicket` returns the owners of its composed tickets. Polymorphism keeps the controller free of `instanceof` checks. Group admission is implemented by the same mechanism, but its rules are formally specified and verified in F2-C07.
- **Validity = `ticket.getDate().equals(currentDate)`**: "valid only on the date specified at creation", checked only at entry (`Ticket.isValidOn`).
- **`ParkController.enterAttraction(attractionId, ticketId)`** validates first and mutates second. It checks that the attraction and ticket exist and the date is valid, that none of the admitted users is already inside, and that `occupants + admitted <= capacity`. Only then does it call `Attraction.enter` for each user. All-or-nothing is therefore built in.
- **Message**: `ENTERED <attraction>: <email>[, <email>...]`.

## Risks / Trade-offs

- [The source defines no exit operation, so attractions only fill up] → Accepted. Capacity tests use small attractions.
