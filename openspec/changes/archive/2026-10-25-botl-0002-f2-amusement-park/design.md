# Design

## Context

Delivery 1 code lives in `com.amusementpark`: `App` (start-up, input source), `CommandProcessor` (split + dispatch), `UserManager` (user rules, owns the user list, saves after each change), `User`, `UserField`, `Validator`, `UserRepository` (`users.txt` in the working directory). Messages use `OK:` / `ERROR:` prefixes. This change adds several entities whose rules cross each other (user deletion depends on tickets; group edits depend on group tickets; attraction entry depends on the system date), so a single place must own the shared state. Requirements are in `specs/`.

## Goals / Non-Goals

**Goals:**
- One main controller (`Park`) owns the system date, the ticket price and all collections, so every rule sees the same date.
- Individual and group tickets share an abstract `Ticket` so lookup and attraction entry/exit are polymorphic.
- Persistence path chosen once (a data directory), so `--fly` only changes configuration.

**Non-Goals:**
- Singleton or other global access to the date (explicitly not required).
- Persisting attraction occupants, the system date or the price.
- Changing Delivery 1 validation rules or the users file format.

## Decisions

### Main controller `Park`
`Park(Path dataDir, LocalDate startDate)` builds the repositories for `users.txt`, `groups.txt`, `tickets.txt`, `group_tickets.txt` and `attractions.txt` inside `dataDir`, loads everything (users → groups → group tickets → individual tickets → attractions) and keeps `currentDate` and `ticketPrice` (`BigDecimal 40.00`). `App` passes `LocalDate.now()` once at start-up; nothing else reads the real clock. `CommandProcessor` now depends on `Park` instead of `UserManager`; `Park` delegates user create/update to the existing `UserManager` and wraps delete with the ticket lock and group clean-up.
*Alternative:* separate managers per entity with cross references — more classes and circular dependencies for rules that are inherently cross-entity.

### Ticket hierarchy
- `abstract class Ticket { String id; LocalDate date; BigDecimal price; abstract List<User> getUsers(); }` plus helpers `isActiveOn(d)` (date ≥ d), `isFutureOn(d)` (date > d), `isValidOn(d)` (date = d).
- `IndividualTicket extends Ticket` holds `User user`.
- `GroupTicket extends Ticket` holds `Group group` (aggregation) and `List<IndividualTicket> tickets` (composition, created by the group ticket at purchase); `getUsers()` returns the users of its tickets; `static BigDecimal discountFor(int members)`.
- Generated individual tickets get ids `<groupTicketId>-<n>` (1-based). Creation is rejected if any generated id collides with an existing ticket id.
- `Park.findTicket(id)` searches standalone tickets, group tickets and their generated tickets, returning `Ticket`.

### Attraction
`Attraction { String id; String name; int capacity; List<User> present; }` with `enter(Ticket)` and `exit(Ticket)` using only `ticket.getUsers()` — no type checks. Entry checks capacity and already-present users before adding anything, so group entry is atomic. Date validation (`isValidOn(currentDate)`) is done by `Park` before calling `enter`, keeping `Attraction` free of the system date.

### Identifier rule
Ids for groups, tickets and attractions must be non-empty and contain only letters, digits, `-`, `_` (checked with `Character` methods in `Validator.isValidId`). This keeps the `,` and `:` separators used in persistence unambiguous.

### Persistence formats (one line per entity, `;` separated, one repository class each)
- `groups.txt`: `id;name;dni1,dni2,...`
- `tickets.txt` (standalone individual tickets): `id;date;price;dni`
- `group_tickets.txt`: `id;date;totalPrice;groupId;groupName;tid1:dni1:price1,tid2:dni2:price2,...`
- `attractions.txt`: `id;name;capacity`

Users are referenced by DNI (immutable). History of deleted owners: when a ticket's DNI or group id no longer resolves, the repository creates a detached `User` (only DNI known) or detached `Group` (id and stored name) that is not registered in the park, so historical tickets survive restarts. Malformed lines are skipped, as for users.

### Commands
`CommandProcessor` keeps the `split(";", -1)` + arity check approach and adds the new operations, each delegating to one `Park` method returning a message. Ticket purchase replies use the exact `OK TICKET CREATED -> 40.00` / `OK GROUP TICKET CREATED -> 108.00` formats (`BigDecimal.setScale(2, HALF_UP).toPlainString()`).

### Date and price parsing
`DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)` rejects `2026-9-15` and `2026-02-30`. Price: manual character check (digits, at most one `.`, ≤ 2 decimals), then `new BigDecimal`, must be > 0.

### Purchase date rule
Tickets (individual and group) may not be bought for a date before the system date. The source text does not state this explicitly; buying for the past would make the "past ticket is history" rule meaningless, so it is rejected.

### `--fly`
`App.run(args, stdin, out, baseDir)`: removes `--fly` from the argument list; when present, creates `baseDir/tmp/yyyyMMdd_HHmmss` (suffix `_1`, `_2`… if it exists) and uses it as `Park`'s data directory. Cleanup runs in a `finally` block and in a shutdown hook (registered for the run, removed afterwards) so `EXIT`, end of input, errors and Ctrl+C all delete it. The repository classes are unchanged. `tmp/` is git-ignored.

### UML
`uml/model.puml` is written last from the final code. Convention: associations as `Holder "m" x--> "n" Target : field`, aggregation `o-->`, composition `*-->`, inheritance `<|--`; no methods; only simple-typed attributes; `..>` usage only for classes without incoming relationships. Validated by running the PlantUML jar (fetched to the local Maven cache, not added to the project).

## Risks / Trade-offs

- [`Park` grows large] → It is the explicitly required "main controller"; entity behaviour (membership, discount, capacity) stays in the entity classes.
- [Detached historical users/groups carry only partial data] → Only the history of deleted owners is affected; documented here.
- [Users in two attractions at once are not prevented] → Not required; only duplicate presence in the same attraction is rejected.
- [Breaking change to `USER DELETE`] → Intended by the proposal; existing tests still pass because they create no tickets.
