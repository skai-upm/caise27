# Design

## Context

`ParkController` holds users, groups, date and price. Delivery 2 rules: tickets know their user (users do not know tickets); ticket types share an abstraction with reused price behaviour; tickets for today or earlier are historical records and cannot be deleted; locked states are derived, never stored.

## Goals / Non-Goals

**Goals:**
- Ticket abstraction ready for group tickets; individual ticket create/delete; ticket-aware USER DELETE.

**Non-Goals:**
- Group tickets and group-locked users (F2-C04), attraction access (F2-C06), ticket persistence (F2-C08).

## Decisions

- **`abstract class Ticket`**: `final String identifier`, `final LocalDate date`, abstract `BigDecimal getPrice()`, `isActiveOn(LocalDate today)` (`!date.isBefore(today)`), `isDeletableOn(today)` (`date.isAfter(today)`). Shared date/identity/price contract; group tickets will compute their price from their composition.
- **`IndividualTicket extends Ticket`**: `final User user`, `final BigDecimal price`, and a `GroupTicket`-owner marker reserved for F2-C04 (`isPartOfGroupTicket()`, false until then).
- **`TicketRepository`**: one `LinkedHashMap<String, Ticket>` = the common identifier space (`exists(id)`, `save`, `findById`, `delete`, `findAll`, `findIndividualTicketsOf(User)` by traversal). Registered in `ParkController` (`getTickets()`).
- **`TicketService`**: create (unique id via `exists`, user exists, date ≥ system date, price = `park.getCurrentPrice()`), delete (exists, individual, not in a group ticket, date > system date). Failures throw `TicketOperationException`.
- **USER DELETE:** `UserService.deleteUser` asks `park.hasActiveTicket(user)` (traverses individual tickets, including future group compositions later) before deleting. The deleted `User` object stays referenced by historical tickets, preserving their data.
- **Dates:** the same strict `uuuu-MM-dd` parsing as SYSTEM DATE (shared helper in `CommandProcessor`).
- **Output:** `TICKET CREATED. PRICE: 40.00 EUR`, `TICKET DELETED`; errors `ERROR: ...`.
- **Mandatory commit:** `[F2-C03] Add individual tickets` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Deleted users remain referenced in memory by historical tickets] → Intended: historical tickets are fiscal records; persistence (F2-C08) must store the needed user data.
