# Design

## Context

`CommandProcessor` is the main controller and owns the system date; `UserManager` and `GroupManager` follow a "save candidate state, then swap" pattern with semicolon text files; group members reference live `User` objects and are persisted by DNI. `Result.toString()` prefixes `OK: `/`ERROR: `. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- Tickets reference live users, persist by DNI, survive restarts, and keep history even after their user is deleted.
- Exact output `OK TICKET CREATED -> <Precio>`.

**Non-Goals:**
- Group tickets, discounts, ticket listing commands, ticket ownership authentication (not in the source).

## Decisions

- **`Ticket`** (immutable): `id`, `LocalDate date`, `BigDecimal price` (scale 2), `User user`.
- **`TicketManager`**: owns `List<Ticket>` plus `List<String> historicalLines` (stored lines whose DNI no longer resolves). Methods: `create(id, email, date, today, price)`, `delete(id, today)`, `hasTicketOnOrAfter(User, today)`. Uniqueness checks include ids in historical lines. Save-then-swap like the other managers; `save` writes linked tickets followed by the preserved historical lines.
- **Price lives in the controller** next to the date (`BigDecimal currentPrice = 40.00`), because both are execution-wide "SYSTEM" settings that are not persisted. `TicketManager.create` receives the price, so the ticket captures it.
- **Validation of `SYSTEM PRICE`** without regex: only ASCII digits and at most one `.`, at least one digit before the `.`, at most two digits after it; parsed into `BigDecimal` with scale 2. Zero is allowed (smallest rule consistent with "non-negative"); negative values cannot pass the character check.
- **Shared strict date parsing**: the existing `SYSTEM DATE` parsing becomes a private helper reused by `TICKET CREATE`.
- **Verbatim output**: `Result` gets a fourth component `verbatim`; `Result.verbatim(success, text)` makes `toString()` return the text unchanged. Used for `OK TICKET CREATED -> 40.00`; all other messages keep the existing prefixes.
- **User deletion order in the controller**: (1) `UserManager.authenticate(email, password)` returns the user or empty → error; (2) `TicketManager.hasTicketOnOrAfter(user, currentDate)` → error "has active tickets"; (3) `UserManager.delete`; (4) remove from groups. Authentication runs first so the ticket rule does not reveal ticket existence to callers without the password.
- **Data directory**: `App.run(args, stdin, out, err, Path dataDir)` resolves `users.txt`, `groups.txt`, `tickets.txt` inside `dataDir` (`Path.of("")` = working directory in production), replacing the per-file parameters.

## Risks / Trade-offs

- [Moving the system date backward can make an old ticket "future" again] → rules are evaluated against the current configured date by design ("all checks use the configured system date").
- [Historical lines of deleted users are opaque] → they are preserved verbatim; if a new user later registers with the same DNI, the line resolves to that user on next load. Accepted edge case, noted here.
- [Ambiguity: creation for past dates] → the source says "buy ... for a particular date"; the brief's acceptance scenario rejects past dates, so creation requires date ≥ system date.
