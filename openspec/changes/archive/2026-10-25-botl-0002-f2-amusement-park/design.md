# Design

## Context

Delivery 1 code lives in package `es.upm.parque`: `App` (entry/modes), `CommandProcessor` (parsing, `OK`/`ERROR` output), `User`, `UserField`, `UserValidator`, `UserManager` (rules + in-memory list), `UserRepository` (file). Rules throw `IllegalArgumentException`, which `CommandProcessor` turns into `ERROR <reason>`. The users file is `data/users.txt`. Standard library only; layered packages, singletons and DI are explicitly postponed to Delivery 3, so this change stays in one package and keeps plain constructor wiring.

## Goals / Non-Goals

**Goals:**
- One main controller owning the system date and ticket price and enforcing rules that cross entities (user deletion vs tickets, group lock vs group tickets).
- A ticket hierarchy that lets attractions and lookups treat individual and group tickets uniformly.
- Persistence per entity, all rooted in one configurable data directory so `--fly` only changes the directory.

**Non-Goals:**
- Package layering, Singleton, dependency injection, invoices, passes, attraction types (Delivery 3).
- Persisting the system date, the ticket price, or attraction occupancy.

## Decisions

### Main controller: `ParkController`
Holds `systemDate` (initialised with `LocalDate.now()` once at construction — the only place the real clock is read), `ticketPrice` (`BigDecimal` 40.00) and the entity managers. `CommandProcessor` now depends on `ParkController` instead of `UserManager`. Time-dependent manager methods receive the date as a parameter from the controller, so managers never read the clock and tests can pass any date.
*Alternative:* a global static clock — rejected: hidden state, harder to test, and F3 will introduce Singleton deliberately.

### Ticket hierarchy
- `Ticket` (abstract): `id`, `date`, `price`; `getUsers()` (users admitted), `isValidOn(d)` = date equals d, `isCurrentOrFuture(d)` = `!date.isBefore(d)`, `isRefundable(d)` = `date.isAfter(d)`.
- `IndividualTicket extends Ticket`: `user` reference, `ownerDni` (kept for history when the user is deleted), optional `groupTicket` back-reference for internal tickets.
- `GroupTicket extends Ticket`: `group` (aggregation, `groupId` kept for history), `tickets` list of `IndividualTicket` (composition); price = total after discount; `static BigDecimal discountFor(int members)`.

`Attraction.enter(Ticket, LocalDate)` uses only `Ticket` methods, so the two kinds share one access path (polymorphism).

The internal tickets need a back-reference to their group ticket so that `TICKET DELETE` can refuse them and persistence can write the link. That is a real bidirectional navigation in the code and will appear as such in the UML.

### Internal ticket identifiers
`<groupTicketId>-<n>`, `n` = 1..members, skipping any number whose id is already used. They live in the same ticket registry as all other tickets, so they are valid for `ATTRACTION ENTER/EXIT` individually.

### Managers and repositories
| Manager | Holds | Repository / file |
|---|---|---|
| `UserManager` (existing) | users | `UserRepository` → `users.txt` |
| `GroupManager` | groups | `GroupRepository` → `groups.txt`: `id;name;dni1,dni2,...` |
| `TicketManager` | all tickets (individual incl. internal, and group) | `TicketRepository` → `tickets.txt`: `id;date;dni;price;groupTicketId` (empty last field for standalone) and `grouptickets.txt`: `id;groupId;date;price` |
| `AttractionManager` | attractions | `AttractionRepository` → `attractions.txt`: `id;name;capacity` |

Repositories resolve references during load by receiving lookup functions (`Function<String, User>` by DNI, `Function<String, Group>` by id), so they return linked domain objects without depending on managers. Members/owners are stored by DNI because it is immutable (emails can change). Load order: users → groups → tickets → attractions. A ticket whose DNI no longer resolves keeps `user == null` and its `ownerDni` (history of deleted users); a group ticket whose group was deleted keeps `groupId`.

`UserManager.delete(email, password)` is split into `authenticate(email, password)` (now public) and `remove(User)`, so `ParkController.deleteUser` can authenticate, check tickets, remove the user from groups, then remove it.

### Identifier validation
New `Identifiers.isValid(String)`: non-empty, every char a letter, digit, `-` or `_` (character checks, no regex). Ensures identifiers can never contain the `;`, `,` separators used in the files.

### Money and dates
`BigDecimal` with scale 2 (`RoundingMode.HALF_UP`), printed with `toPlainString()`, locale-independent. Dates parsed with `LocalDate.parse` (ISO, strict resolver → rejects `2026-02-30`, `2026-9-15`). Ticket purchases for dates earlier than the system date are rejected.

### Data directory and `--fly`
`App` parses arguments: `--fly` (optional, anywhere), at most one file path; anything else starting with `--` is an error. Normal mode uses `data/`; `--fly` creates `tmp/<yyyyMMdd_HHmmss>` (adding `_1`, `_2`… if it already exists). `ParkController` is built from a data directory: each repository gets `dir.resolve("<file>")`. Cleanup: `try/finally` in `App.run` deletes the temp directory recursively after EXIT, end of input or file errors; `main` also registers a shutdown hook so Ctrl+C removes it. `tmp/` is gitignored.

### Output messages
`OK SYSTEM DATE -> d`, `OK SYSTEM PRICE -> p`, `OK GROUP CREATED|MEMBER ADDED|MEMBER REMOVED|DELETED`, `OK TICKET CREATED -> p`, `OK TICKET DELETED`, `OK GROUP TICKET CREATED -> p`, `OK GROUP TICKET DELETED`, `OK ATTRACTION CREATED|DELETED`, `OK ATTRACTION ENTERED -> n`, `OK ATTRACTION EXITED -> n`; `ATTRACTION STATUS` prints a multi-line block (id, name, capacity, present, available, one line per user: name, surname, DNI).

### UML
`uml/model.puml` written by hand from the final code, following the design-model spec: `Ticket` abstract with `IndividualTicket`/`GroupTicket` subclasses, composition `GroupTicket *--> IndividualTicket : tickets`, aggregation `GroupTicket o--> Group : group`, navigable associations with `x` on the holder end, no methods, attributes only primitives/strings/dates/decimals/enums, usage arrows only from `App` (no incoming relationships). Validated with the PlantUML jar (`-checkonly`) fetched to a temp location, not added to the build.

## Risks / Trade-offs

- [Full file rewrite on every change] → Fine at lab scale; keeps persistence simple and consistent.
- [History tickets of deleted users have no user object] → Kept with `ownerDni`; documented; F3 invoices only target existing users.
- [Second-resolution `--fly` timestamps could collide] → Suffix added when the directory exists.
- [Shutdown hook not run on `kill -9`] → Unavoidable; leftover dirs are confined to the ignored `tmp/`.

## Migration Plan

Existing `data/users.txt` stays compatible (format unchanged). New files are created on first save. Rollback: revert the commit; extra data files are ignored by older code.
