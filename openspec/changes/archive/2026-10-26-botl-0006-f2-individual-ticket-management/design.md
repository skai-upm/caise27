# Design

## Context

`CommandProcessor` is the controller owning the system date; `UserManager`/`GroupManager` follow a manager + storage pattern with whole-file rewrites. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- Tickets as first-class objects referencing users, with date rules driven only by the controller's system date.

**Non-Goals:**
- Group tickets and discounts (next delivery), listing tickets, refunds.

## Decisions

- **`Ticket`**: `id`, `date` (`LocalDate`), `price` (`BigDecimal`, scale 2), `owner` (`User`) and `ownerDni`. `ownerDni` is the immutable DNI captured at sale time so the ticket stays identifiable as history; `owner` becomes `null` when the user is deleted (`detachOwner`) or cannot be resolved on load, keeping in-memory and reloaded states consistent.
- **`TicketManager`**: `LinkedHashMap<String, Ticket>`, depends on `UserManager` (email/DNI lookup) and `TicketStorage`. Methods receive the system date as a parameter (`create(id, email, date, today, price)`, `delete(id, today)`, `hasTicketOnOrAfter(user, today)`, `detachOwner(user)`), so no class reads the clock.
- **`TicketStorage`**: `data/tickets.txt`, `Id;yyyy-MM-dd;Precio;DNI` lines; malformed lines skipped.
- **Price** (`BigDecimal currentPrice = 40.00`) lives in `CommandProcessor` beside the system date: both are per-execution system variables changed with `SYSTEM` commands. Validation: `new BigDecimal`, `> 0`, `scale() <= 2`; output `setScale(2).toPlainString()`.
- **USER DELETE flow** in the controller: `users.authenticate` → `tickets.hasTicketOnOrAfter(user, date)` → reject, else `users.delete`, `groups.removeUserFromAll`, `tickets.detachOwner`. `UserManager` stays unaware of tickets.
- **Past purchase dates rejected**: buying for a date before the system date is treated as invalid (assumption: the proposal sells tickets for a date of use; history arises as time passes).
- **Response** for creation exactly `OK TICKET CREATED -> 40.00`, other ticket responses follow the existing `OK: ...` / `ERROR: ...` style.

## Risks / Trade-offs

- [Re-registering a deleted DNI reattaches its historical tickets on reload] → acceptable: the DNI identifies the same person.
- [BigDecimal input like `1E2`] → accepted as 100 (scale -2 ≤ 2), still a positive amount; harmless.
