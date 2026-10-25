# Design

## Context

Managers (`UserManager`, `GroupManager`) hold their objects in insertion-ordered maps, return response strings and persist through a repository after every successful change. `CommandProcessor` dispatches, coordinates cross-manager rules (user deletion → groups) and owns the shared `SystemDate`. Users are persisted by immutable DNI.

## Goals / Non-Goals

**Goals:**
- Tickets referencing `User` objects; all date checks via `SystemDate`.

**Non-Goals:**
- Group tickets, discounts and attraction access (later proposals); a ticket hierarchy is not introduced until group tickets need it.

## Decisions

- **`Ticket`**: `id`, `date` (`LocalDate`), `holder` (`User`), `price` (`BigDecimal`, scale 2) — all final; `isCurrentOrFuture(LocalDate today)` = `!date.isBefore(today)`, `isRefundable(LocalDate today)` = `date.isAfter(today)`.
- **Money as `BigDecimal`** with scale 2 (`HALF_UP`), so `40.00`, `60.00` and later discount arithmetic are exact and print with two decimals via `toPlainString()`.
- **`TicketManager(TicketRepository, UserManager, SystemDate)`** keeps the tickets and the current price (`40.00` on construction, not persisted). Methods `create`, `delete`, `setPrice`, `hasActiveTickets(User)`.
- **Past-date purchase rejected** (`ERROR PAST DATE`): the proposal does not define buying for past dates; refusing it keeps history meaningful. Buying for today is allowed.
- **Deletion guard** in `CommandProcessor.deleteUser`: after the user is found and before calling `UserManager.delete`, `tickets.hasActiveTickets(user)` → `ERROR USER HAS ACTIVE TICKETS`. The credential check still comes first (`UserManager.authenticate` is exposed as `checkCredentials`) so the active-ticket message is not revealed to someone without the password.
- **History of deleted users**: in memory, past tickets keep referencing the removed `User` object. On restart, a stored DNI that no longer matches a registered user is restored as a detached historical holder (`User` carrying only the DNI, not registered in `UserManager`), so the ticket record survives as required.
- **`SYSTEM PRICE`** parsing: `new BigDecimal(text)` must be > 0; scale normalised to 2.
- **UML**: add `Ticket` with `Ticket "0..*" --> "1" User : holder` and `TicketManager` associations.

## Risks / Trade-offs

- [Detached historical holders have empty names after restart] → Acceptable: only the DNI is needed to identify the former holder; the user no longer exists.
- [Prices with more than two decimals are rounded HALF_UP] → Documented by the two-decimal response.
