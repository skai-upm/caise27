# Design

## Context

`ParkController` holds date, price, users (by e-mail) and groups, with a derived `isGroupModifiable` hook. Commands extend `ControllerCommand` (field-count/empty checks, `ERROR:` reporting). Users only have an e-mail so far.

## Goals / Non-Goals

**Goals:** ticket abstraction, individual tickets, TICKET CREATE/DELETE, shared id space, derived user lock and user-deletion operation.

**Non-Goals:** group tickets and discounts (F2-C04), attraction access (F2-C06), USER DELETE command and password check (F1-C05), persistence.

## Decisions

- **`abstract class Ticket`** (`model`): `identifier`, `date`, abstract `getPrice()`, shared `isActive(LocalDate today)` (`!date.isBefore(today)`) and `isDeletable(LocalDate today)` (`date.isAfter(today)`), plus `abstract List<IndividualTicket> getIndividualTickets()` so operations can traverse users of any ticket type through the abstraction. An abstract class (over an interface) lets the common fields and date logic live in one place.
- **`IndividualTicket extends Ticket`**: holds `User user` and `BigDecimal price`; `getIndividualTickets()` returns itself. The `User` class gets no ticket field.
- **Ticket registry in `ParkController`**: `Map<String, Ticket> tickets` keyed by identifier (top-level tickets). `registerTickets(List<String> identifiers, Ticket ticket)` checks every identifier (the ticket's own plus derived ones) against all known identifiers — including individual tickets inside compositions — before inserting, so creation is all-or-nothing. `identifierInUse(id)` traverses all tickets and their compositions.
- **TICKET DELETE** only removes top-level `IndividualTicket` entries; an identifier found only inside a composition is reported as "belongs to a group ticket".
- **User lock**: `hasActiveTickets(User)` traverses all tickets' individual tickets comparing user identity and `isActive(currentDate)`. `deleteUser(email)` checks lock and group modifiability for every group containing the user, then removes the user from those groups and from the registry. Historical tickets keep their `User` reference (the object survives deletion).
- **Commands**: `TicketCreateCommand` (`TICKET CREATED. PRICE: <price>`), `TicketDeleteCommand` (`TICKET DELETED`).

## Risks / Trade-offs

- [User deletion has no command yet] → Covered by controller tests; F1-C05 wires `USER DELETE` to `deleteUser`.
