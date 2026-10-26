# Design

## Context

`AttractionController.enter(attractionId, ticketId)` resolves the ticket and calls `Attraction.enterWith(access, today)`, which validates admission type, date, duplicates and capacity before adding anyone (already atomic). `PassController` owns passes and saves on change. See proposal.md.

## Goals / Non-Goals

**Goals:**
- Validate everything (including pass availability) before any state change; consume only after a successful entry.
- Pass rules per attraction expressed polymorphically.

**Non-Goals:**
- Choosing a specific SpeedPass (the first unused one, in purchase order, is used).

## Decisions

- **`Ticket.getIndividualTickets()`** (abstract): `IndividualTicket` → itself; `GroupTicket` → its composed tickets. Used to collect one SpeedPass per involved ticket without type switches.
- **Attraction hooks**: `acceptsSpeed()` (default `true`) and `requiresSpecialPass()` (default `false`); `SpecialAttraction` overrides both. Alternative: checks on `getType() == SPECIAL` in the controller — rejected (type switches).
- **Entry algorithm** (`AttractionController.enter(attractionId, ticketId, speed)`): resolve attraction and ticket; `admit` (type check); if `speed` and `!acceptsSpeed()` → reject; collect passes to consume: Special Pass (`requiresSpecialPass()`), or one unused SpeedPass per individual ticket (`speed`) — reject if any is missing; `enterWith` (date/duplicates/capacity, atomic); then `passController.consume(passes)` (marks and saves). Returns an `EntryResult(record: users, consumed passes)`.
- **Dependencies**: `AttractionController.configure(repository, ticketController, passController, systemDate)`; `App` configures `PassController` before `AttractionController`.
- **Response**: `OK ATTRACTION ENTERED -> <n> users` + `, SpeedPasses consumed: <ids>` or `, Special Pass consumed: <id>`.
- **UML**: `AttractionController x--> PassController`, `EntryResult` record.

## Risks / Trade-offs

- [Pass consumption is saved after the in-memory entry] → occupancy is not persisted anyway; a save failure surfaces as an error after the user entered, acceptable for a file store.
