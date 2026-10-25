# Design

## Context

Delivery 1 code lives in the flat package `es.upm.parque`: `User`, `UserField`, `UserValidator`, `UserRepository` (users file), `UserManager` (rules, returns `OK`/`ERROR` strings), `CommandProcessor` (shared `;`-split parser with HELP/EXIT) and `App` (mode selection, data path `data/users.txt`). Delivery 3 will reorganize packages and introduce Singleton/DI, so this change keeps the flat package and avoids patterns aimed at that delivery. Behavior is specified in the delta specs.

## Goals / Non-Goals

**Goals:**
- One main controller owning the system date and ticket price and coordinating cross-entity rules.
- Model classes that reference each other directly (no copies), a ticket hierarchy usable polymorphically, and one repository per persisted entity configured only by file paths.
- A `--fly` mode implemented purely by choosing a different storage directory.

**Non-Goals:**
- Layered packages, Singleton, dependency-injection frameworks, password hashing, passes (later deliveries).
- Persisting attraction occupancy, the system date or the ticket price.

## Decisions

### D1. Model
- `Group`: `id`, `name`, `members: List<User>` (association to the same `User` objects held by `UserManager`).
- `Ticket` (abstract): `id`, `date: LocalDate`; abstract `getUsers(): List<User>` and `getPrice(): BigDecimal`; helpers `isCurrentOrFuture(today)` (`date >= today`), `isFuture(today)` (`date > today`), `isValidOn(today)` (`date == today`).
- `IndividualTicket extends Ticket`: `user: User`, `price: BigDecimal`; `getUsers()` returns the single holder.
- `GroupTicket extends Ticket`: `group: Group` (aggregation), `tickets: List<IndividualTicket>` (composition), `price` (total after discount); `getUsers()` returns the holders of its internal tickets. Static `discountFor(members)` implements the brackets and `totalFor(unitPrice, members)` rounds HALF_UP to 2 decimals.
- `Attraction`: `id`, `name`, `capacity`, `present: List<User>`; `enter(Ticket)` and `exit(Ticket)` use only `Ticket.getUsers()`, so individual and group tickets share one code path. `enter` validates capacity and "already inside" before adding anyone (atomic) and throws `IllegalStateException` with a message on rejection.
*Alternative:* an `Accessible` interface for tickets — rejected for now; the abstract `Ticket` already provides the common abstraction and F3 introduces access interfaces explicitly.

### D2. Controllers
- `ParkController` (main controller): holds `currentDate` (initialized from `LocalDate.now()` — the only use of the real clock in business code) and `ticketPrice` (40.00), owns `UserManager`, `GroupManager`, `TicketManager` and `AttractionManager`, and implements every command as a method returning `OK ...`/`ERROR ...`. Cross-entity rules live here: user deletion (ticket check, cascade), group locking, ticket purchase (user/group lookup, price), attraction access (date check).
- `GroupManager`, `TicketManager`, `AttractionManager` keep their collections, enforce entity-local rules (unique ids, membership) and persist after each change, like `UserManager`.
- `UserManager.deleteUser` is replaced by `authenticate` + `remove(User)` so deletion always goes through `ParkController`.
- `CommandProcessor` now depends only on `ParkController` and keeps parsing, parameter counts, HELP and EXIT.
*Alternative:* managers calling each other directly — rejected because it creates cycles (users ↔ tickets).

### D3. Date and price parsing
Dates use `DateTimeFormatter.ofPattern("uuuu-MM-dd")` with `ResolverStyle.STRICT` plus a length-10 check (rejects `2026-9-15` and `2026-02-30`). Prices use `BigDecimal`: must be > 0 with at most 2 decimals, stored with scale 2 and printed with `toPlainString()`.

### D4. Ticket rules (assumptions recorded)
- Ticket identifiers are unique across individual tickets, internal tickets and group tickets.
- Tickets (individual or group) cannot be bought for a date before the system date.
- Internal tickets are identified `<groupTicketId>-<memberDni>` (DNI is immutable, so the id is stable); they cannot be deleted with `TICKET DELETE`.
- Deleting a user (allowed only when all their tickets are past) deletes their remaining tickets and removes internal ones from their group tickets; the group ticket keeps its stored total. Deleting a group (allowed only when unlocked) deletes its past group tickets with their internal tickets. This keeps every persisted reference resolvable.
- A group ticket for an empty group is rejected.

### D5. Persistence
A shared `TextFileStore` reads lines and writes them atomically (temp file + move); every repository uses it:
- `users.txt` — unchanged format.
- `groups.txt` — `id;name;dni1,dni2,...`, resolved against loaded users by DNI.
- `tickets.txt` — `id;date;price;userDni;groupTicketId` (last field empty for standalone tickets).
- `grouptickets.txt` — `id;date;groupId;total`. Loaded before `tickets.txt` so internal tickets attach to their group ticket in file order.
- `attractions.txt` — `id;name;capacity`; attractions always load empty.
Lines with unresolved references or bad values are skipped with a warning on standard error.
`StoragePaths(dir)` exposes the five file paths; `ParkController` receives it at construction.

### D6. `--fly`
`App.run(args, in, out, baseDir)` separates `--fly` from the other arguments; the first remaining argument is the command file. In fly mode it creates `baseDir/tmp/<yyyyMMdd_HHmmss>` (adding `_1`, `_2`… if the directory already exists), builds `StoragePaths` on it, and deletes it recursively in a `finally` block; a shutdown hook (removed after normal completion) covers Ctrl-C in interactive mode. Normal mode uses `baseDir/data`. `tmp/` is Git-ignored.

### D7. UML
`uml/model.puml` is written after the code: classes with primitive/enum attributes only (no visibility markers, no methods), relationships named after the holding attribute, `x` on the non-navigable end, `*--`/`o--` for composition/aggregation, `<|--` for inheritance, multiplicities from the collection/field types, and dependency arrows only from classes without incoming relationships (`App`, `CommandProcessor`). Validated with PlantUML's `-checkonly`.

## Risks / Trade-offs

- [Cascade deletion removes past history of deleted users/groups] → Only possible when no current/future ticket exists; documented in the specs.
- [Non-atomic multi-file updates on I/O errors] → Each file write is atomic; cross-file inconsistencies are skipped with warnings at load.
- [`ParkController` grows large] → Acceptable for this delivery; F3 reorganizes into layers.
