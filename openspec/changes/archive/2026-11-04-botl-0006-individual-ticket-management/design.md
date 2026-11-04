# Design

## Context

`CommandProcessor` is the controller and holds the run-wide `currentDate`. Users (`UserManager`) and groups (`GroupManager`) follow an entity / manager / file-repository split with rollback on save failure, and `UserException` is the general "command rejected" exception. Groups reference the `User` objects owned by `UserManager` and are persisted by DNI. `USER DELETE` currently authenticates, deletes, then removes the user from groups.

## Goals / Non-Goals

**Goals:**
- Same structure as groups for tickets.
- All date logic receives the controller's date as a parameter; nothing calls `LocalDate.now()`.
- Exact success literal `OK TICKET CREATED -> <Precio>`.

**Non-Goals:**
- Group tickets and discounts.
- Persisting the price setting.

## Decisions

- **`Ticket`**: `id` (final), `date` (final `LocalDate`), `price` (final `BigDecimal`, scale 2), `ownerDni` (final) and `user` (`User`, nullable). `getUser()` returns `Optional<User>`. `detachUser()` sets the reference to null when the owner is deleted and keeps `ownerDni`, so past tickets stay as history without creating placeholder users.
- **`TicketManager`**: `LinkedHashMap<String, Ticket>`.
  - `create(id, email, date, today, price)`: checks a non-blank unused id, an existing user via `UserManager.findByEmail`, and `!date.isBefore(today)`.
  - `delete(id, today)`: requires the ticket to exist and `date.isAfter(today)`.
  - `hasTicketOnOrAfter(user, today)`: identity match on `user` and `!date.isBefore(today)`.
  - `detachUser(user)`.
  - Saves after each change, with rollback on `IOException`, like `GroupManager`.
- **`TicketFileRepository`**: line `Id;yyyy-MM-dd;DNI;Precio`, loaded with a DNI → user lookup. An unknown DNI keeps the ticket with no user. Malformed lines (wrong field count, unparsable date or price) are skipped with a warning on stderr.
- **Price lives in the controller.** `CommandProcessor` has `BigDecimal ticketPrice = 40.00`, like `currentDate`, because the source calls it an internal per-run variable. `SYSTEM PRICE` validation is done without regular expressions: one optional `.`, digits only, at most two decimals, value > 0. Accepted values are normalised to scale 2 (`60` → `60.00`). The output is `OK: Ticket price set to 60.00`.
- **Strict dates.** `TICKET CREATE` reuses the same `uuuu-MM-dd` STRICT formatter as `SYSTEM DATE`, moved into a shared private helper `parseDate`.
- **USER DELETE order.** `UserManager.authenticate` becomes public. The controller authenticates first, so a wrong password reports the credential error and reveals no ticket information. It then rejects deletion if `tickets.hasTicketOnOrAfter(user, currentDate)` (`ticketDate >= SYSTEM DATE`). Otherwise it deletes the user, removes them from groups and detaches their tickets (in memory only; the file keeps the DNI).
- **Success literal.** `TICKET CREATE` prints exactly `OK TICKET CREATED -> 40.00`, with no `:` after `OK`, as the source states. It still starts with `OK`, consistent with the other commands.
- **Wiring.** `App.run` derives `tickets.txt` as a sibling of the users file, and it is added to `.gitignore`. `CommandProcessor` constructors take the `TicketManager` as well.
- **UML.** Add `Ticket`, `TicketManager`, `TicketFileRepository`, `Ticket "0..*" --> "0..1" User : owner`, `TicketManager "1" *-- "0..*" Ticket`, and the controller's `ticketPrice`.

## Risks / Trade-offs

- [A user recreated with a deleted user's DNI is relinked to their historical tickets after a restart] → acceptable, since a DNI identifies the same person. Within a run, detached tickets stay detached.
- [Ticket file and users file are saved separately] → each manager rolls back its own state. No operation in this change writes both files.

## Open Questions

- The source does not say whether the same user may hold several tickets for the same date. Nothing forbids it, so it is allowed.
