# Design

## Context

`CommandProcessor` owns the system date and dispatches commands to `UserManager` and `GroupManager`; persistence follows the "separate storage class, `;` text file, save after success" style. `UserManager.delete` authenticates and removes in one step. See proposal.md.

## Goals / Non-Goals

**Goals:**
- Tickets reference the managed `User` objects; every date check uses the controller's date.

**Non-Goals:**
- Group tickets, discounts, attraction access, price persistence.

## Decisions

- **`Ticket`:** immutable `id`, `date` (`LocalDate`), `price` (`BigDecimal`, scale 2), `ownerDni` and a `User owner` reference. `owner` is the loaded user object; it is `null` only for a historical ticket restored after its owner was deleted, in which case `ownerDni` still identifies the former owner. Alternative of dropping such tickets rejected: the source requires historical retention.
- **`TicketManager`:** ordered map by id; `create(id, user, date, price, today)`, `delete(id, today)`, `hasTicketOnOrAfter(user, today)`; copy-save-swap for atomic changes. It receives `today` from the controller on every call, so it never reads the clock.
- **`TicketFileStorage`:** `id;ownerDni;date;price`, loading with a `Function<String, User>` resolver (same pattern as groups).
- **Price in the controller:** `BigDecimal currentPrice = 40.00` next to `currentDate` in `CommandProcessor`. The format check uses `Character` tests (no regex): digits with an optional `.`, then 1–2 decimals, and the value must be > 0. Zero and negative prices are rejected; the source gives no rule, so this is the smallest sensible one, recorded as an assumption. The price is printed with `setScale(2).toPlainString()`.
- **Deletion rule:** `CommandProcessor` calls the now-public `UserManager.authenticate(email, password)`, then `tickets.hasTicketOnOrAfter(user, currentDate)`. Only if no such ticket exists does it call `users.delete(...)` and `groups.removeUserFromAllGroups(...)`. This keeps `UserManager` unaware of tickets.
- **Comparisons:** creation needs `!date.isBefore(today)`; ticket deletion needs `date.isAfter(today)`; user deletion is blocked by `!date.isBefore(today)`.
- **Reply literal:** `OK TICKET CREATED -> 40.00`, exactly as in the source; other commands keep the `OK: ...` style.

## Risks / Trade-offs

- [A new user created later with a deleted user's DNI would be linked to that user's historical tickets on restart] → Acceptable for historical records. Noted as an open question.
- [Ticket identifiers are free text] → Only nonblank and unique are required; the source defines no format.
