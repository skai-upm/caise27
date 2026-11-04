# Design

## Context

`CommandProcessor` (main controller) owns the system date and dispatches to `UserService` and `GroupService`; services follow "save the would-be state, then apply" for atomicity; repositories write `;` text files in the data directory passed to `App.run`. Groups reference `User` objects and persist DNIs.

## Goals / Non-Goals

**Goals:** tickets referencing users, price captured per ticket, deletion rules evaluated with the controller's date, history retention.

**Non-Goals:** group tickets, discounts, attractions, authentication for ticket commands (the source defines none).

## Decisions

- **`Ticket`**: `final String id`, `final User user` (may be `null` only for history of a user deleted in an earlier execution), `final String holderDni`, `final LocalDate date`, `final BigDecimal price`. Immutable — no ticket field ever changes.
- **Money**: `BigDecimal` with scale 2 (`setScale(2)`), printed with `toPlainString()` → `40.00`. Avoids floating-point rounding; independent of locale.
- **Price ownership**: `CommandProcessor` holds `BigDecimal ticketPrice = 40.00`, alongside the date; `SYSTEM PRICE` parses with a character check (digits, optional `.` + 1–2 digits) then `new BigDecimal(..)`, rejecting zero.
- **`TicketService(TicketRepository, UserService)`**: `create(id, email, date, today, price)`, `delete(id, today)`, `hasCurrentOrFutureTicket(User, today)`, `find`, `getTickets`. The date and price are passed in by the controller on each call, so the service never reads a clock and there is still one date/price owner.
- **Deletion rule** lives in the controller's `USER DELETE`: `users.authenticate(email, password)` (made public), then `tickets.hasCurrentOrFutureTicket(user, currentDate)` → reject, else `users.delete` + `groups.removeUserFromAllGroups`. Comparison: `!ticket.getDate().isBefore(today)` (i.e. `ticketDate >= SYSTEM DATE`). `TICKET DELETE` requires `ticket.getDate().isAfter(today)`.
- **Persistence**: `Identificador;DNI;yyyy-MM-dd;Precio`. On load, DNI → `users.findByDni`; if absent the ticket is kept with `user == null` and its `holderDni`. Malformed lines are skipped with a message on standard error, consistent with the other repositories.
- **Response text**: `TICKET CREATE` returns the full line `OK TICKET CREATED -> 40.00`; `process()` prefixes `OK: ` for all other commands. Implemented by letting command handlers return a value already marked as a complete response.

## Risks / Trade-offs

- [Reused DNI after deletion] → a later user registered with the DNI of a deleted user would, after restart, appear as owner of the old history tickets. Rare; accepted and documented.
- [History tickets without a user after restart] → only reachable for users deleted in earlier runs; all rules that need a user (deletion check) only concern registered users.
