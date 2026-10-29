# Design

## Context

`ParkController` (singleton) holds date, price, users and groups; `hasActiveGroupTicket` returns false because no group tickets exist. See proposal.md for flagged prerequisites.

## Goals / Non-Goals

**Goals:** reusable ticket abstraction, shared identifier space, TICKET CREATE/DELETE, derived `hasActiveTicket(User)`.

**Non-Goals:** group tickets, USER DELETE, persistence.

## Decisions

- **`abstract class Ticket`**: private final `identifier`, `date`, `price` (`BigDecimal`), getters; `isActiveOn(LocalDate today)` = `!date.isBefore(today)`; `isDeletableOn(today)` = `date.isAfter(today)`; `Collection<String> identifiers()` returns the identifiers this ticket occupies (default: its own) so composite tickets can add their nested ones; `boolean involves(User)` abstract for ticket-derived user state. An abstract class (not only an interface) is chosen because identifier/date/price state and price behavior must be reused, not re-implemented. Alternative: interface + default methods — cannot hold state.
- **`IndividualTicket extends Ticket`**: private final `User user`; `involves(u)` = `user.equals(u)`. Users get no ticket fields.
- **Registry**: `LinkedHashMap<String, Ticket>` of top-level tickets in `ParkController`. `isTicketIdentifierUsed(id)` traverses every ticket's `identifiers()` — the one common check for all ticket types; `addTicket(Ticket)` checks all identifiers of the new ticket before inserting anything (no partial creation).
- **Operations**: `createIndividualTicket(id, email, dateText)` → validates user, date ≥ current date, identifier free; price = current base price at creation (copied, so later SYSTEM PRICE does not affect it). `deleteTicket(id)` → must exist, must be an `IndividualTicket` registered at top level, `isDeletableOn(currentDate)`.
- **`ticketsOf(User)`** and **`hasActiveTicket(User)`**: traversal over registered tickets with `involves`, for the future USER DELETE.
- **Date parsing**: extract the strict `yyyy-MM-dd` parser into a static `parseDate` reused by SYSTEM DATE and TICKET CREATE.
- **Output**: `TICKET CREATED. PRICE: 40.00 EUR`, `TICKET DELETED`.

## Risks / Trade-offs

- [Group-ticket rules cannot be exercised yet] → `identifiers()`/`involves()` are the extension points the next milestone uses.
