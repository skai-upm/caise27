# Design

## Context

`ParkController` singleton holds date, price, groups and the `UserDirectory` port; commands use `ParkException` for rejections. No user model or USER DELETE yet. See proposal.md - Why.

## Goals / Non-Goals

**Goals:** a ticket hierarchy where common data and price behavior live once; a single identifier-space check; derived (never stored) active/locked states.

**Non-Goals:** group tickets and discounts (next milestone), USER DELETE command.

## Decisions

- **`abstract class Ticket`** (id, date, `getPrice()`, `isActiveOn(date)`, `isDeletableOn(date)` = date strictly after) with **`IndividualTicket extends Ticket`** holding a base price and its user's e-mail. Abstract class over interface because common state (id, date) and price behavior must be shared, not duplicated. Group tickets will extend `Ticket` and compute price from their components.
- **Single ticket registry** in `ParkController`: `LinkedHashMap<String, Ticket>`; `registerTicket` performs the shared identifier-space check for all types.
- **User reference by e-mail** (unidirectional; users never hold tickets) because no `User` class exists yet; `UserDirectory` validates existence.
- **`ensureUserDeletable(email)`** guard throws `ParkException` if any ticket associated with the user is active; it traverses the ticket registry (no stored state).
- **Commands** `TicketCommands`: messages `TICKET CREATED. PRICE: <price>` and `TICKET DELETED`; dates parsed with `SystemCommands.parseDate`.

## Risks / Trade-offs

- [E-mail reference becomes stale if a user's e-mail changes later (USER UPDATE)] → deferred to the user-model milestone, which must keep ticket references consistent.
