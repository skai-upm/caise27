# Design

## Context

Delivery 1 code (package `es.upm.park`): `model.User`/`UserField`, `validation.UserValidator`, `persistence.UserFileRepository`, `service.UserService` (+ `UserOperationException`), `cli.CommandProcessor`/`CommandRunner`/`CommandResult`, `App`. Users live in `users.txt`, whose location `App` reads from the `park.users.file` system property. Delivery 2 adds park entities whose rules cross entity boundaries (user deletion depends on tickets and groups; group locking depends on tickets and the date). See proposal.md for motivation and the specs for behaviour. Constraints carried over: Java standard library only, no regex, no serialization, no databases.

## Goals / Non-Goals

**Goals:**
- One central `Park` object (singleton) owning the date, price and all entity collections, so every cross-entity rule has one place to look.
- Ticket hierarchy that shares identity, date and price behaviour, uses composition for group tickets, and lets entry and id-space checks work on the abstraction.
- Persistence per entity type with paths coming from configuration, so `--fly` changes only paths.

**Non-Goals:**
- Reproducing the ten mandatory `[F2-C01]..[F2-C10]` milestone commits: the BOTL batch produces one commit per proposal (same deviation as Delivery 1).
- Persisting attraction occupants (explicitly excluded).

## Decisions

### Central manager: `service.Park` singleton
`Park.getInstance()` returns the single instance (static holder idiom, private constructor). `Park.open(DataPaths)` (re)loads all state from files and resets date = `LocalDate.now()`, price = 40.00. Tests call `open` with a temp directory; there is still only one instance per JVM. Alternative: enum singleton — rejected because `open` must reload state and an enum with mutable state reads awkwardly; dependency-injected non-singleton — rejected because the assignment requires a guaranteed single instance.

`Park` keeps: `UserService users`, `Map<String, Group> groups`, `Map<String, Ticket> tickets` (top-level tickets only: standalone individual tickets and group tickets), `Map<String, Attraction> attractions` (all `LinkedHashMap` for stable output), and derived queries: `findTicket(id)` (searches top-level tickets and group-ticket compositions — the single shared id-space check), `isLocked(group)`, `hasActiveTicket(user)`, `findContainingGroupTicket(ticket)`. Operations are implemented by `GroupService`, `TicketService`, `AttractionService` (each given the `Park`), and `Park.deleteUser` (cross-cutting). Locked states are never stored.

### Ticket hierarchy (`model`)
- `abstract class Ticket` — `id`, `date`; `abstract BigDecimal getPrice()`, `abstract List<User> getHolders()`; concrete `isActiveOn(today)` (date ≥ today), `isDeletableOn(today)` (date > today), `isValidOn(today)` (date == today), `involves(user)`.
- `IndividualTicket extends Ticket` — `User user`, `BigDecimal basePrice`, `GroupDiscount discount`; `getPrice()` = base × (100 − %)/100, scale 2 HALF_UP.
- `GroupTicket extends Ticket` — `Group group` (nullable), `List<IndividualTicket> composition` (unmodifiable, fixed at construction); `getPrice()` = sum of composition prices; `getHolders()` = composition users. No user list duplicated.
- `GroupDiscount` enum (`NONE 0`, `SMALL 10`, `MEDIUM 20`, `LARGE 50`) with `forMembers(n)`.
Entry (`ATTRACTION ENTER`) uses only `Ticket.isValidOn` and `getHolders()` — one access path for both types.

### Other model
- `Group` — `id`, `name`, ordered `List<User> members`; no ticket references.
- `Attraction` — `id`, `name`, `capacity`, `List<User> occupants`; `available()`, `enter(List<User>)` all-or-nothing, `exit(user)`. Users never reference attractions. A user already inside any attraction cannot enter again (checked by `AttractionService` across attractions).

### Money and parsing
`BigDecimal` everywhere; printed with two decimals (`40.00`). `validation.ValueParser` parses `yyyy-MM-dd` (`LocalDate.parse` with `ResolverStyle.STRICT`), prices (digits with optional `.` and ≤2 decimals, > 0, char scanning — no regex, no exponent forms) and capacities (digits only, > 0).

### User deletion
`UserService.delete` is replaced by `UserService.authenticate` + `UserService.remove(user)`. `Park.deleteUser` authenticates, rejects when `hasActiveTicket(user)` (standalone or composition) or the user belongs to a locked group, removes the user from all groups, removes the user, then saves. Historical tickets keep the in-memory `User` object.

### Persistence (`persistence`)
- `DataPaths(dir)` record → `users.txt`, `groups.txt`, `tickets.txt`, `attractions.txt`. Normal mode: `park.data.dir` system property, default working directory (Delivery 1 default `users.txt` location is preserved; the `park.users.file` property is replaced by `park.data.dir`). `--fly`: `tmp/yyyyMMdd_HHmmss[_n]/`.
- `groups.txt`: `id;name;dni1;dni2;...`.
- `attractions.txt`: `id;name;capacity`.
- `tickets.txt`: `GROUP;id;date;groupId-or-empty` and `INDIVIDUAL;id;date;basePrice;discount;parentGroupTicketId-or-empty;dni;email;firstName;surnames`. Load resolves users by DNI among active users; if absent (deleted user) a historical `User` is rebuilt from the snapshot (password/phone/card empty, not registered as active). Group resolved by id, else `null`.
- `Park.save()` rewrites all four files after every successful mutating command (small data; always consistent; snapshots stay fresh).

### `--fly`
`App` parses `[--fly] [file]`. In fly mode it creates the timestamped directory, opens the park on it, runs, and in a `finally` deletes the directory recursively (and `tmp/` if it created it and it is empty). Same repositories, different `DataPaths`.

### Command processing
`CommandProcessor` becomes a registry `LinkedHashMap<String, CommandSpec(syntax, fieldCount, handler)>`; HELP is generated from the registry so it always lists every command. Errors keep the `ERROR: ` prefix. `UserOperationException` is generalised to `OperationException` (same package) and used by all services.

### UML
`uml/model.puml` documents model classes, enum, inheritance, composition GroupTicket ◆→ IndividualTicket, directed associations with multiplicities, and the `Park` singleton.

### Sample-data artifacts
The proposal's example passwords split across lines (`Laura3$` / `;`, `Marta4` / `%`, `Marta4 %`) are document line-wrap artifacts; tests use `Laura3$` and `Marta4%`.

## Risks / Trade-offs

- [Singleton complicates test isolation] → `open(DataPaths)` fully resets state; tests always call it with a fresh temp directory.
- [A new user reusing a deleted user's DNI would be linked to that user's historical tickets after a restart] → Accepted: historical tickets never block operations; documented.
- [Whole-file rewrite on every change] → Negligible at assignment data sizes; gives atomic, consistent files.
