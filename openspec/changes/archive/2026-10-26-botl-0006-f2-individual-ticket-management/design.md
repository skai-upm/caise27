# Design

## Context

`UserManager`, `GroupManager` and `SystemDate` exist; `CommandProcessor` dispatches; `App.run` wires managers with files under a data directory. User deletion currently only removes the user from groups.

## Goals / Non-Goals

**Goals:** ticket purchase/deletion with date rules from `SystemDate`; ticket history; deletion guard on users.

**Non-Goals:** group tickets and discounts (next change), refunds/payments.

## Decisions

- **`Ticket`**: `id`, `date` (`LocalDate`), `user` (`User` reference), `price` (`BigDecimal`, scale 2) — all final. `isCurrentOrFuture(LocalDate today)` = `!date.isBefore(today)`; `isFuture(today)` = `date.isAfter(today)`.
- **`TicketManager`** (receives `TicketRepository`, `UserManager`, `SystemDate`): holds tickets and the current price (`40.00`, in memory only). Methods `create`, `delete`, `setPrice`, `hasCurrentOrFutureTickets(User)`. Creating tickets dated before the system date is rejected (a purchase must be usable); this is an assumption recorded here.
- **Deletion guard**: `UserManager.addDeletionCheck(Function<User, String>)`; `delete` runs each check after authentication and throws with the returned reason if non-null. `TicketManager` registers its ticket check on the `UserManager` it receives, so every wiring (application and tests) applies it. Alternative: checking in `CommandProcessor` — rejected to keep business rules out of command parsing; alternative: `UserManager` depending on `TicketManager` — rejected (circular: tickets need users).
- **Persistence**: `data/tickets.txt`, line `Id;yyyy-MM-dd;price;DNI`. Holders are relinked by DNI. When a holder was deleted (allowed when only past tickets remain), the ticket keeps its in-memory `User` reference; after a restart, an unresolved DNI is restored as a detached holder `User` carrying only that DNI (name `(deleted user)`), so history is never lost and the user list is unaffected.
- **Price parsing**: `new BigDecimal(text)`; must be `> 0` with scale ≤ 2; stored with `setScale(2)`. Output uses `toPlainString()`.
- **Date parsing** reuses the strict `yyyy-MM-dd` parser in `CommandProcessor`.

## Risks / Trade-offs

- [Detached holders after restart lose name/email] → acceptable for history; the DNI still identifies the person.
