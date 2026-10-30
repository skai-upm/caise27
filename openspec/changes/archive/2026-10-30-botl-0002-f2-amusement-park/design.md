# Design

## Context

Delivery 1 code (see `openspec/specs/*`) has `model.User`, `validation.UserValidator`, `repository.UserFileStore`/`UserRepository` (self-persisting), `controller.UserController`, `cli.CommandProcessor` (switch) / `CommandRunner`, and `App` (stdin or file). The users file is `users.txt` in the working directory. Delivery 2 adds several aggregates whose relationships must be real object references in memory (section 16) and whose rules depend on a shared system date.

## Goals / Non-Goals

**Goals:**
- One central `ParkController` singleton owning date, price and the four repositories.
- A ticket hierarchy that shares identity, date, price and access behaviour.
- Same repositories for normal and `--fly` mode; only the data directory changes.
- Keep `CommandProcessor` simple as commands grow from 5 to 20.

**Non-Goals:**
- Delivery 3 patterns (Builder, DI framework-free wiring beyond what is needed, SpeedPass, BCrypt).
- Restricting users to one attraction at a time or limiting ticket re-entry (not specified).

## Decisions

1. **Central management (`controller.ParkController`).** Eager singleton (`getInstance()`), private constructor. `initialize(Path dataDir)` builds repositories for that directory and resets date to `LocalDate.now()` and price to 40.00; App calls it once, tests call it per test for isolation. It also exposes derived queries: `isUserLocked`-style checks are delegated to `TicketRepository` traversals using `today()`. Alternative (static class) rejected: the source asks for a single object instance.
2. **Money.** `BigDecimal` with scale 2, `RoundingMode.HALF_UP`. `SYSTEM PRICE` parsed with `new BigDecimal(text)`; rejected if not a number, ≤ 0 or more than two decimals (assumption: "monetary amount" = cents precision). Dates parsed with `LocalDate.parse` (ISO `yyyy-MM-dd`).
3. **Ticket hierarchy (`model`).** `abstract Ticket` (id, date; `getPrice()`, `getHolders()`, `isActiveOn(d)` = date ≥ d, `isValidOn(d)` = date = d, `isDeletableOn(d)` = date > d, protected `discounted(base, pct)`). `IndividualTicket extends Ticket` (user, basePrice, discountPercent). `GroupTicket extends Ticket` (nullable `group`, final unmodifiable `List<IndividualTicket>` composition; price = sum; static `discountFor(members)`). Users/groups hold no ticket references; queries traverse `TicketRepository`.
4. **Identifier space.** `TicketRepository` stores top-level tickets (stand-alone individual + group); `findById` also searches compositions. Group ticket creation computes all derived ids first, checks them all, then adds — atomic. A composition ticket id (e.g. `TG01:12345678Z`) is a valid individual ticket for `ATTRACTION ENTER` (assumption flagged; the source only forbids deleting it directly).
5. **Groups (`model.Group`).** id, name, `List<User>` members (no duplicates). Lock = `tickets.hasActiveGroupTicket(group, today)`.
6. **Attractions (`model.Attraction`).** id, name, capacity, `List<User>` occupants (not persisted). `enter(List<User>)` checks capacity and duplicates for the whole list before adding any. Re-entering while already inside is rejected (assumption: an occupant collection cannot hold the same user twice).
7. **USER DELETE.** `UserController.delete` → authenticate → reject if `tickets.hasActiveTicketFor(user, today)` or any containing group is locked → remove from groups → remove user → re-save groups and tickets (tickets carry a snapshot of the user for history).
8. **Persistence files** (all `;`-separated, in the data directory):
   - `users.txt` — unchanged.
   - `groups.txt` — `id;name;dni1,dni2,...` (DNI is the immutable user key).
   - `attractions.txt` — `id;name;capacity`.
   - `tickets.txt` — `INDIVIDUAL;id;date;basePrice;discount;userState;dni;firstName;surnames;email`, `GROUP;id;date;groupId-or-empty`, followed by its `MEMBER;groupTicketId;` + individual fields. `userState` is `ACTIVE` when the user object is in the user repository at save time, else `HISTORIC`; on load `ACTIVE` links to the live user by DNI, `HISTORIC` builds a detached `User` (no password/phone/card) so a later re-registration of the same DNI does not hijack history.
   A shared `repository.LineFile` helper reads/writes lines (missing file = empty) to avoid duplicated I/O code. Repositories self-save on add/remove and expose `save()` for in-place changes.
9. **`--fly`.** `App` parses args: `--fly` first → data dir `tmp/<yyyyMMdd_HHmmss>` (suffix `_n` if it already exists), created up front, deleted recursively in `finally` (also removes `tmp/` if then empty). Otherwise data dir is the working directory and args behave as in Delivery 1.
10. **Command dispatch.** `CommandProcessor` keeps a `LinkedHashMap<String, Command>` of `(fieldCount, usage, action)` entries. HELP text is generated from the registered usages plus `HELP`/`EXIT`, so help and parsing cannot drift. Usage strings are the exact section 15 syntax.
11. **Output.** `TICKET CREATED - PRICE: 40.00 EUR`, `GROUPTICKET CREATED - TOTAL PRICE: 192.00 EUR`, `GROUP CREATED/USER ADDED/USER REMOVED/DELETED`, `ATTRACTION CREATED`, `ATTRACTION ENTERED: <n> user(s)`, `ATTRACTION EXITED`, LIST prints one email per line or `NO USERS INSIDE`; errors remain `ERROR: <reason>`.
12. **UML.** `uml/model.puml` covers `model` classes and the `ParkController` aggregation of repositories; written last from the final code.

## Risks / Trade-offs

- [Singleton complicates test isolation] → `initialize(dataDir)` fully resets state; tests run sequentially (Surefire default).
- [Ticket snapshot of user data may go stale] → tickets file is re-saved after every user update/delete.
- [Timestamp collisions for `--fly` within one second] → numeric suffix keeps directories independent.
- [Ambiguities flagged, not new rules]: ticket entry with composition identifiers allowed; duplicate occupancy rejected; price precision two decimals; identifiers must be non-blank.
- [Mandatory per-milestone commits] → one BOTL commit, messages listed in body.
