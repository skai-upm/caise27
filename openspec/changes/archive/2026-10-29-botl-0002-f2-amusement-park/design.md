# Design

## Context

Delivery 1 code (package `es.upm.amusementpark`) has `App`, `CommandRunner`, `CommandProcessor`, `UserService`, `UserRepository`, `User`, `UserField` and `UserValidator`, and stores `users.txt` in the working directory. See proposal.md for the Delivery 2 scope. Constraints carry over: Java 21, standard library only, no regex for parsing/validation, text-file persistence (no serialization), and object references (not ids) for in-memory relationships with the mandated directions.

## Goals / Non-Goals

**Goals:**
- One central `ParkController` singleton that owns all state and enforces the cross-entity rules (locks, active tickets).
- A ticket hierarchy that reuses price behaviour and enables uniform attraction entry.
- The same repository classes for normal and `--fly` mode, configured only by a base directory.

**Non-Goals:**
- Delivery 3 features (BCrypt, passes, statistics, etc.).
- Persisting attraction occupants.

## Decisions

**Singleton**: `ParkController` has a private constructor and a `static final INSTANCE` returned by `getInstance()`. `initialize(Path dataDir)` (re)loads every repository from `dataDir`, sets `currentDate = LocalDate.now()` and resets `price = 40.00`. `App` calls it once at start-up, and tests call it with a temp directory. This keeps a single instance while letting tests start from clean state. Alternative considered: an `enum` singleton. It was rejected because it cannot be re-pointed at a different directory as cleanly; both guarantee one instance.

**Ticket hierarchy**:
- `abstract class Ticket`: `id`, `date`; `abstract BigDecimal getPrice()`; `abstract List<User> getHolders()`; plus shared `isActive(today)` (date ≥ today), `isDeletable(today)` (date > today) and `isValidOn(today)`.
- `IndividualTicket extends Ticket`: `User user`, `BigDecimal basePrice`, `int discountPercent`; `getPrice()` = base × (100 − pct) / 100, rounded HALF_UP to 2 decimals. Standalone tickets use pct = 0.
- `GroupTicket extends Ticket`: `Group group` (nullable), `List<IndividualTicket> composition` (built in the constructor and exposed read-only); `getPrice()` = sum of composition prices; `getHolders()` = composition users; static `discountFor(int members)` holds the brackets.
- `TicketRegistry` (inside the controller) holds top-level tickets in a `LinkedHashMap<String, Ticket>`. `exists(id)` checks top-level ids and every composition id, which gives the shared id space. Lookup `find(id)` also resolves composition tickets, so `ATTRACTION ENTER` works with any existing ticket and `TICKET DELETE` can say "belongs to group ticket X".

**Groups**: `Group` holds `id`, `name` and `List<User> members`. `isLocked` is computed by the controller as "any `GroupTicket` with `group == g` and `isActive(today)`".

**Attractions**: `Attraction` holds `id`, `name`, `capacity` and `List<User> occupants`. `enter(List<User>)` checks capacity and duplicates and then adds all of them, so entry is atomic. `exit(email)` removes the occupant with that email.

**User deletion**: `UserService` gains `authenticate(email, pw)` and `remove(User)`. `ParkController.deleteUser` authenticates, rejects if any ticket's `getHolders()` contains the user while active, rejects if any group containing the user is locked, then removes the user from groups, calls `userService.remove`, and saves.

**Money**: `BigDecimal`. `SYSTEM PRICE` is parsed by a character scan: digits, at most one `.`, at most 2 decimals, and the value must be > 0. Output uses `toPlainString()` at scale 2 (e.g. `40.00`).

**Dates**: `LocalDate.parse` with `DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(STRICT)`.

**Persistence** (all `;`-separated, in `dataDir`):
- `users.txt`: unchanged.
- `groups.txt`: `id;name;dni1,dni2,...` (empty member list allowed).
- `attractions.txt`: `id;name;capacity`.
- `tickets.txt`:
  - `INDIVIDUAL;id;date;basePrice;dni;firstName;surnames;email`
  - `GROUP;id;date;groupId(or empty);basePrice;discountPercent`
  - `MEMBER;groupTicketId;dni;firstName;surnames;email` (one per composition ticket, in order; the id is derived as `groupTicketId:dni`).

On load, the ticket's user is resolved by DNI among active users. If none is found (the user was deleted), a detached `User` is rebuilt from the snapshot fields (password, phone and card left empty) so historical records survive. Groups are resolved by id, or null. Each repository (`GroupRepository`, `TicketRepository`, `AttractionRepository`, existing `UserRepository`) is constructed with a `Path`, and `ParkController.saveAll()` writes every file after each successful mutation.

**`--fly`**: `App` computes `dataDir = Path.of("tmp", yyyyMMdd_HHmmss)`, appending `_1`, `_2`, … if it already exists. It creates the directory, runs, and deletes the directory recursively in a `finally` block, then removes `tmp/` if it is empty and was created by this run. Normal mode uses `dataDir = Path.of("")` (the working directory), as in Delivery 1.

**Output messages**:
- `SYSTEM DATE SET` / `SYSTEM PRICE SET`
- `GROUP CREATED|ADDED|REMOVED|DELETED`
- `TICKET CREATED;PRICE=40.00` / `TICKET DELETED`
- `GROUPTICKET CREATED;TOTAL=192.00` / `GROUPTICKET DELETED`
- `ATTRACTION CREATED|ENTERED|EXITED`
- LIST: one email per line, or `NO USERS`
- STATUS: a multi-line block

The source leaves the exact wording to the student.

**UML**: `uml/model.puml` is written by hand to match the classes above. PlantUML is not installed locally, so syntax is checked by inspection (`@startuml`/`@enduml`, standard class syntax).

## Risks / Trade-offs

- [Ambiguity: `ATTRACTION ENTER` with a user already inside] → Rejected for the whole entry, to keep occupancy a set and group entry atomic. Not stated in the source.
- [Ambiguity: entering with a composition ticket id (`TG01:dni`)] → Allowed: it is an existing individual ticket, and only that user enters.
- [Ambiguity: `SYSTEM PRICE` format] → A positive amount with at most 2 decimals, interpreted as euros.
- [Deleted user's DNI reused by a new user] → On reload, historical tickets link to the new active user with that DNI. This is acceptable because the DNI identifies the person.
- [Per-milestone commit messages] → Recorded in the single BOTL commit body, as in Delivery 1.
- [Singleton + tests] → Tests must call `initialize` in `@BeforeEach` and must not run in parallel (Surefire default is sequential).
