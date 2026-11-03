# Design

## Context

`CommandProcessor` is the main controller and holds `currentDate`. Users and groups are managed by `UserService`/`GroupService`, each with a file storage that references users by DNI. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:** individual tickets with a user reference and captured price; date checks always against the controller's date; a deletion rule for users with active tickets; history that survives user deletion.

**Non-Goals:** group tickets, discounts, attraction access.

## Decisions

- **`Ticket`** (state only): final `id`, `date` (`LocalDate`), `price` (`BigDecimal`, scale 2), `user` (`User` reference) and `ownerDni`. `ownerDni` is always set (taken from the user when one exists) so a historical ticket whose user was deleted can still be stored and reloaded; `getUser()` returns `Optional<User>`.
- **Price in the controller:** `CommandProcessor` gets `BigDecimal currentPrice = 40.00` next to `currentDate` (same lifecycle: per execution, not persisted). `SYSTEM PRICE;x` parses `new BigDecimal(x)`; rejected if not a number, negative or with more than 2 decimals (assumption: the source only shows `60.00`). Stored with `setScale(2)`.
- **`TicketService`** holds the tickets and `UserService`. `create(id, email, date, today, price)`, `delete(id, today)`, `hasActiveTickets(user, today)` (any ticket of that user with `date >= today`). The controller passes its own date/price, so no service reads the clock. Rules: non-blank unique id (exact match), registered user, `date >= today` for purchase (the original text is silent about past purchases; the change brief's scenario 2 requires rejecting them), `date > today` for deletion. Save after every change with revert on failure.
- **User deletion:** `UserService.authenticate(email, password)` becomes public. `USER DELETE` authenticates first (so ticket information is not revealed to wrong credentials), then rejects if `tickets.hasActiveTickets`, then deletes and removes the user from groups. The user's past tickets stay in memory with the user reference and in the file with the DNI.
- **`TicketFileStorage`**: `tickets.txt`, `Id;DNI;yyyy-MM-dd;Precio`. `load(users)` links by DNI; unresolved DNIs give tickets without user (history). Malformed lines skipped with a warning.
- **Responses:** `OK TICKET CREATED -> 40.00` exactly as the source; others follow the existing `OK: ...` / `ERROR: ...` style.
- Date parsing for TICKET CREATE reuses the strict `uuuu-MM-dd` formatter of SYSTEM DATE.

## Risks / Trade-offs

- [A new user created later with a deleted user's DNI would be linked to the old historical tickets after a restart] → accepted: the DNI is the national identity, so it is the same person.
- [Ticket ids case-sensitive] → consistent with group ids.
