# Design

## Context

`Attraction` keeps its occupants and exposes package-level `enter(List<User>)`; tickets share the abstract `Ticket`. The source requires access to use the common ticket abstraction rather than separate mechanisms per ticket type.

## Goals / Non-Goals

**Goals:**
- One access path driven by `Ticket`, used now by individual tickets.

**Non-Goals:**
- Group ticket access (atomic, F2-C07), ATTRACTION EXIT and STATUS (F2-C07), ticket consumption rules (Delivery 3).

## Decisions

- **`Ticket.getHolders()`** (abstract, `List<User>`): the users admitted by the ticket. `IndividualTicket` returns its user. `AttractionService.enter(attractionId, ticketId)` uses only `Ticket`: validity (`date.equals(today)`), capacity (`holders.size() <= available`), then `attraction.enter(holders)`.
- **Group tickets this milestone:** `enter` rejects `GroupTicket`s with "group ticket access is not available yet"; F2-C07 implements `GroupTicket.getHolders()` and removes the guard, reusing the same path.
- **Validity:** the source says a ticket is "valid only on the date specified at creation" and must be "valid for the current system date" → `ticket.getDate().equals(currentDate)`.
- **Duplicates (ambiguity, flagged):** a user already inside the attraction cannot enter it again (would duplicate the occupant). Reusing a ticket after leaving is allowed in Delivery 2 (the source defines no consumption until Delivery 3).
- **Output:** `ENTERED ATTRACTION A01: ana@correo.com`.
- **Mandatory commit:** `[F2-C06] Add individual attraction access` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [A user may be inside several attractions at once] → Not forbidden by the source; not checked.
