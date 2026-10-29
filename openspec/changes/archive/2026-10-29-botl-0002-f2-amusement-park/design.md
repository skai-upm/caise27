# Design

## Context

Delivery 1 code (package `es.upm.amusementpark`): `model.User`/`UserField`, `validation.UserValidator`, `persistence.UserRepository` (path-configurable), `service.UserService` (in-memory users + persists through the repository), `command.CommandProcessor`/`CommandRunner`, `App`. Motivation and scope: proposal.md. Behaviour: specs/. Constraints: Java 21 standard library only, no regex, no serialization, object references for in-memory relationships with the mandated directions.

## Goals / Non-Goals

**Goals:**
- One central controller (`Park`) coordinating every cross-entity rule (locks, ticket checks on user deletion, attraction access) and persistence.
- Ticket hierarchy where price behaviour lives in the abstraction and group pricing is derived from composed individual tickets.
- Repositories parametrised only by file path so `--fly` is a path change.
- Keep Delivery 1 behaviour and tests green (adapting only internal APIs).

**Non-Goals:**
- Delivery 3 features.
- Concurrency; the application is single-threaded.

## Decisions

### Park singleton (`service.Park`)
Eager `private static final Park INSTANCE` with a private constructor and `getInstance()`. Because the instance is unique for the JVM, tests and `App` call `initialize(ParkStorage storage, LocalDate today)` which resets in-memory state (price 40.00, date `today`) and loads from the given repositories. *Alternative:* passing a `Park` instance around without a singleton — rejected: the source requires the design to guarantee uniqueness wherever access is requested. *Alternative:* lazy holder idiom — equivalent; eager is simpler.

`Park` owns: `currentDate`, `currentPrice` (`BigDecimal`, scale 2), `UserService users`, `Map<String, Group> groups`, `Map<String, Ticket> tickets` (all tickets incl. composition tickets — this *is* the shared identifier space), `Map<String, Attraction> attractions`, `ParkStorage storage`. Each mutating command calls `persist()` which rewrites all four files.

### UserService becomes an in-memory registry
`UserService` keeps creation/update validation and uniqueness but no longer persists; `Park` persists after each mutation. `UserService.authenticate` becomes public and `remove(User)` is added; deletion policy moves to `Park.deleteUser`. `UserOperationException` is renamed `OperationException` (used by every park operation). Delivery 1 persistence assertions move from `UserServiceTest` to Park-level tests.

### Ticket hierarchy (`model.ticket`)
```
abstract Ticket { id, date; abstract BigDecimal getPrice(); abstract List<User> getUsers();
                  boolean isActive(today) = !date.isBefore(today); boolean isDeletable(today) = date.isAfter(today);
                  boolean isValidOn(day) = date.equals(day);
                  protected static BigDecimal discounted(BigDecimal base, int discountPercent) }
IndividualTicket extends Ticket { User user; BigDecimal basePrice; int discountPercent }
GroupTicket extends Ticket { Group group (nullable); final List<IndividualTicket> composition (unmodifiable);
                             static int discountFor(int members); price = Σ composition prices }
```
`ATTRACTION ENTER` uses only `Ticket.isValidOn` and `Ticket.getUsers()`, so there is one access mechanism. Whether an individual ticket belongs to a composition is found by traversing group tickets (no back-reference needed). *Alternative:* `Ticket` interface + abstract class — rejected as unnecessary indirection; an abstract class already gives a common abstraction and shared price logic.

### Groups and attractions
`model.Group { id, name, List<User> members }` (no ticket references). `model.Attraction { id, name, capacity, List<User> occupants }` with `freePlaces()`, `contains(User)`, `enter(List<User>)` (checks all first, then adds), `exit(User)`. Lock state: `Park.isLocked(group)` = any `GroupTicket` with `group == g && isActive(currentDate)`.

### Deletion of users/groups and historical data
- `USER DELETE`: authenticate → reject if any `IndividualTicket` of the user is active → reject if any group containing the user is locked → remove from groups and attraction occupants → remove from registry. Ticket objects keep referencing the (now inactive) `User` object.
- `GROUP DELETE`: reject if locked → set `group = null` on its group tickets → remove.

### Persistence (`persistence`)
`ParkStorage(Path dataDir)` exposes the four repositories built on `dataDir.resolve("users.txt" | "groups.txt" | "tickets.txt" | "attractions.txt")`. Formats (`;` separated, `split(";", -1)`):
- `groups.txt`: `id;name;dni1,dni2,…` (members resolved against loaded users by DNI).
- `attractions.txt`: `id;name;capacity`.
- `tickets.txt`: `G;id;date;groupId` (groupId empty when detached) and `I;id;date;basePrice;discountPercent;parentGroupTicketId;state;dni;email;firstName;surnames` where `state` is `A` (user active at save time → relink to the active user with that DNI) or `H` (deleted user → rebuild a detached `User` with the stored identity and empty password/phone/card). Loader reads all lines, builds group tickets first, then individual tickets, attaching those with a parent id to their group ticket composition.

Repositories receive/return domain objects and resolve references through lookup maps passed in by `Park`, keeping file I/O out of the model and `App`.

### `--fly` (`App`)
Argument parsing: if `args[0].equals("--fly")` → fly mode, command file = `args[1]` if present; otherwise Delivery 1 behaviour. Fly mode creates `tmp/<yyyyMMdd_HHmmss>` (suffix `_1`, `_2`… if it exists), initialises `Park` with `ParkStorage(thatDir)`, runs, and deletes the directory tree in a `finally` block (also removing `tmp/` if this run created it and it is empty).

### Parsing helpers (`validation.ValueParser`)
`parseDate` uses `DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(STRICT)`; `parsePrice` checks characters manually (digits, at most one `.`, ≤ 2 decimals) then `new BigDecimal`; `parsePositiveInt` checks digits then `Integer.parseInt` with overflow handling. No regex.

### Command output
Prices printed with `BigDecimal.setScale(2).toPlainString()` + ` EUR`. `ATTRACTION LIST` prints one email per line or `(no users)`. `ATTRACTION STATUS` prints labelled lines.

### UML
`uml/model.puml` lists the model classes (`User`, `UserField`, `Group`, `Attraction`, `Ticket`, `IndividualTicket`, `GroupTicket`) and `Park` with attributes and relationships: `Ticket <|-- IndividualTicket`, `Ticket <|-- GroupTicket`, `GroupTicket *--> "1..*" IndividualTicket`, `IndividualTicket --> "1" User`, `GroupTicket --> "0..1" Group`, `Group --> "*" User`, `Attraction --> "*" User`, `Park` aggregations. A unit test checks that every class in `model` appears in the diagram.

## Risks / Trade-offs

- [Singleton complicates test isolation] → `initialize` fully resets state; tests always initialise with a temp directory.
- [Rewriting all files after every mutation is O(n)] → acceptable for coursework-sized data; keeps consistency simple.
- [A detached historical user is rebuilt with blank password/phone/card] → only identity is needed for fiscal records; documented.
- [`--fly` cleanup does not run if the JVM is killed] → cleanup covers EXIT, end of file and end of input as required.

## Migration Plan

Existing `users.txt` files remain readable (same format and location). New files are created on first save. Rollback: revert the commit; the extra files are ignored by Delivery 1.
