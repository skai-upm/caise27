# Design

## Context

`Attraction` already offers `hasRoomFor`, `isInside`, `enter`, `exit`. Tickets share the abstract `Ticket`.

## Goals / Non-Goals

**Goals:** one ENTER implementation driven by the ticket abstraction.
**Non-Goals:** group entry semantics (F2-C07: atomic), EXIT and STATUS (F2-C07).

## Decisions

- **`Ticket.holders()`** (abstract, returns `List<User>`): `IndividualTicket` → its user; `GroupTicket` → the users of its components. `Ticket.isValidOn(date)` (`date.equals(this.date)`).
- **`AttractionCommands.enter`**: resolve attraction and top-level ticket → validity on current date → `holders()` → none already inside → `hasRoomFor(holders.size())` → enter all. Written for a list of holders so F2-C07 only has to accept group tickets. In this milestone `GroupTicket` instances are rejected explicitly.
- **`ParkController.removeFromAllAttractions(User)`** called by USER DELETE next to `removeFromAllGroups`.
