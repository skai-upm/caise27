# Design

## Context

`MainController` holds the simulated date; `UserManager` and `GroupManager` follow the same shape (in-memory list, repository with one `;`-separated line per object, persist after each change with reload on failure). `CommandProcessor` validates parameter counts from `COMMAND_SYNTAX` and dispatches; on USER DELETE it already coordinates `UserManager` and `GroupManager`.

## Goals / Non-Goals

**Goals:**
- Every date rule reads `MainController.getCurrentDate()`.
- Money handled exactly (`BigDecimal`, two decimals, `.` separator regardless of locale).

**Non-Goals:**
- Group tickets and a ticket hierarchy (next change), refunds.

## Decisions

- **`Ticket`**: `id`, `date` (`LocalDate`), `user` (`User` reference), `price` (`BigDecimal`), all final.
- **Current price in `MainController`** (`ticketPrice`, initialised to `40.00` in the constructor, not persisted), next to the date, since both are execution-wide internal variables. `parsePrice(String)` accepts positive values with scale ≤ 2 and normalises to scale 2; `formatPrice` uses `toPlainString()` so output never depends on the default locale.
- **`TicketRepository`** (`data/tickets.txt`): `id;dni;yyyy-MM-dd;price`; `loadAll(Map<String, User>)` resolves users by DNI and skips unresolvable or malformed lines.
- **`TicketManager`** (repository, `UserManager`, `MainController`): `create`, `delete`, `hasCurrentOrFutureTicket(User)` (`!date.isBefore(today)`), `removeTicketsOf(User)`, `findById`, `getTickets`.
- **Purchase date rule**: tickets cannot be bought for a date before the system date (assumption — the proposal does not allow back-dated purchases; past tickets only arise as the system date advances).
- **User deletion**: `UserManager.authenticate` becomes public. `CommandProcessor.deleteUser` authenticates, asks `TicketManager.hasCurrentOrFutureTicket` (error `User has current or future tickets`), then deletes the user, removes them from groups and removes their past tickets. Alternative: inject `TicketManager` into `UserManager` — rejected because tickets already depend on users (cycle).
- **UML**: add `Ticket` (attributes `id`, `date`, `price`; association `user` to `User`, `0..*` → `1`), `TicketManager`, `TicketRepository`, and the new `MainController.ticketPrice`.

## Risks / Trade-offs

- [Deleting a user removes their historical tickets] → Needed so every ticket refers to an existing user; only possible once all their tickets are past.
