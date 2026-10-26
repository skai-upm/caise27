# Design

## Context

`UserManager` owns users (deletion = authenticate + remove), `GroupManager` owns groups, `CommandProcessor` dispatches and coordinates cross-cutting effects (removing a deleted user from groups). `SystemDate` is the shared date. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- Tickets reference users by object; all date rules read `SystemDate`.
- Money handled exactly (`BigDecimal`, 2 decimals).

**Non-Goals:**
- Group tickets and discounts (next change), ticket listing commands.

## Decisions

- **`Ticket`**: `id`, `date` (`LocalDate`), `user` (`User`), `price` (`BigDecimal`), all final. `isCurrentOrFuture(LocalDate today)` = `!date.isBefore(today)`; `isAfter(today)` used for deletion.
- **`TicketManager(TicketRepository, UserManager, SystemDate)`**: `LinkedHashMap<String, Ticket>`; `currentPrice` initialized to `40.00` in the constructor (not persisted); `create`, `delete`, `setPrice`, `hasCurrentOrFutureTicket(User)`. Rejects tickets dated before the system date (assumption: a ticket cannot be bought for a day that has passed).
- **User deletion flow**: `UserManager` gains public `authenticate(email, password)` and `delete(User)`; `CommandProcessor.deleteUser` does authenticate → `ticketManager.hasCurrentOrFutureTicket` check → delete → remove from groups. Alternative: `UserManager` querying tickets — rejected, it would make the user module depend on tickets.
- **Persistence**: `data/tickets.txt`, line `id;date;price;dni;email;name;surname`. On load the DNI is resolved against registered users; if the user no longer exists (deleted after the ticket became past), a detached `User` is rebuilt from the stored identity so the historical record is kept. Alternative: keep deleted users in the users file — rejected, it changes user semantics.
- **Price parsing**: `new BigDecimal(text)`, must be `> 0` and `scale() <= 2`; output via `setScale(2).toPlainString()`.
- **UML**: add `Ticket`, `TicketManager`, `TicketRepository` and `Ticket --> "1" User : user`.

## Risks / Trade-offs

- [Stored holder identity can be stale for a registered user] → only used when the DNI no longer resolves; registered users always win.
- [Deleted users' history only keeps DNI, email, name and surname] → sufficient for a historical record.
