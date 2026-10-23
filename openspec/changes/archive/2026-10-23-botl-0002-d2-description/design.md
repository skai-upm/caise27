# Design

## Context

Delivery 1 code (package `es.upm.etsisi.park`): `App` (argument handling, input loop), `cli.CommandProcessor` (`;` parsing and dispatch; errors as `ERROR: ...`), `user.User` / `UserField` / `UserValidator` / `UserService` / `UserException`, and `persistence.UserRepository` (`users.txt`, path from system property `park.users.file`). `UserService` loads users in its constructor and saves after each change with in-memory rollback on save failure. Constraints from the assignment: object relationships held as references and collections (not ids) in memory, prescribed relationship directions, ticket inheritance/abstraction with shared price behaviour, composition between group ticket and member tickets, exactly one central manager, text-file persistence without serialization, and a `--fly` mode that only changes repository paths.

## Goals / Non-Goals

**Goals:**
- A singleton `Park` that owns all state and implements the park operations; `CommandProcessor` only parses and formats.
- A ticket hierarchy where price and holder behaviour are shared, used polymorphically by attraction entry.
- One set of repositories, parameterised by a data directory, for both normal and `--fly` execution.
- Keep Delivery 1 behaviour and tests passing (adapted to the new wiring).

**Non-Goals:**
- Rolling back multi-file writes atomically across files.
- Attraction deletion/modification commands (not requested).
- Reproducing the course's milestone commit history.

## Decisions

### Central manager: `Park` singleton
`Park` uses an eager `private static final Park INSTANCE` with a private constructor and `getInstance()`, so there is one instance per JVM wherever it is requested. Because state must be (re)loaded from a configurable data directory, `Park.open(ParkStorage)` replaces its contents: loads users (through a new `UserService`), groups, tickets and attractions, resets the system date to `LocalDate.now()` and the price to 40.00. Tests call `open` with a temp directory before each case. Alternative: enum singleton — rejected because `open` with mutable state reads more naturally in a class and both give the same guarantee.

### Domain model (package `es.upm.etsisi.park.model` split by concept)
- `group.Group`: `id`, `name`, `List<User> members` (ordered, no duplicates). No ticket references.
- `ticket.Ticket` (abstract): `id`, `date`, `isActiveOn(LocalDate)`, `isValidOn(LocalDate)`, abstract `BigDecimal getPrice()` and `List<User> getHolders()`.
- `ticket.IndividualTicket extends Ticket`: `User user`, `BigDecimal basePrice`, `int discountPercent`; `getPrice()` = base × (100 − discount) / 100, scale 2 HALF_UP; holders = `[user]`.
- `ticket.GroupTicket extends Ticket`: `Group group` (nullable once the group is deleted), `List<IndividualTicket> members` (composition, unmodifiable after construction); `getPrice()` = sum of member prices; holders = members' users. No parallel user list.
- `ticket.GroupDiscount` enum: `NONE(0)`, `SMALL(10)`, `MEDIUM(20)`, `LARGE(50)` with `forMembers(int)`.
- `attraction.Attraction`: `id`, `name`, `capacity`, `List<User> occupants`; `available()`, `enter(List<User>)` (all-or-nothing), `exit(User)`.
- Directions: Attraction→User, IndividualTicket→User, GroupTicket→Group, GroupTicket◆→IndividualTicket. `User` and `Group` hold no ticket/attraction references.

### Ticket registry and identifier space
`Park` keeps `List<Ticket> tickets` (top-level: standalone individual tickets and group tickets) and a `Map<String, Ticket> ticketIndex` keyed by upper-cased id containing every ticket including member tickets. Uniqueness is checked through one method, `isTicketIdTaken`, used for both types; group ticket creation checks its own id and every derived `<id>:<dni>` before mutating. Whether an individual ticket is a member is found by traversing group tickets (no back-reference), as is any "tickets of user/group" query.

### Derived locks
- `isGroupLocked(g)`: some `GroupTicket` with `group == g` and `date >= systemDate`.
- `hasActiveTicket(u)`: some top-level ticket with `date >= systemDate` whose holders contain `u`.
- `USER DELETE`: authenticate (existing `UserService.authenticate`, made public), then reject on active ticket or any locked group containing the user; then remove from groups and delete via `UserService`.

### Dates and money
- Dates parsed with `LocalDate.parse` (ISO `yyyy-MM-dd`, strict calendar validation) after a length-10 check; no regex.
- Prices are `BigDecimal` scale 2. `SYSTEM PRICE` accepts only digits with at most one `.` and ≤2 decimals, > 0 (character scan, no regex). Output format `40.00 EUR`.

### Persistence
- `ParkStorage` (package `persistence`) holds the data directory and builds the four repositories: `UserRepository` (unchanged format), `GroupRepository`, `TicketRepository`, `AttractionRepository`. Each has `load(...)`/`save(...)` with the same temp-file + move strategy and skip-with-warning for malformed lines; the shared file-writing helper moves into a small `TextFiles` utility.
- Data directory: system property `park.data.dir` (default: working directory, so `users.txt` stays where Delivery 1 put it). `park.users.file` is removed.
- Formats (`;`-separated):
  - `groups.txt`: `id;name;dni1,dni2,...` (members resolved by DNI among active users).
  - `attractions.txt`: `id;name;capacity`.
  - `tickets.txt`: `INDIVIDUAL;id;date;basePrice;discount;state;dni;email;firstName;surnames`, `GROUP;id;date;groupId-or-empty`, followed by its `MEMBER;parentId;id;date;basePrice;discount;state;dni;email;firstName;surnames` lines. `state` is `A` if the user was active when saved, else `F` (former). On load, `A` resolves the DNI to the active `User`; `F` (or an unresolved `A`) builds a detached `User` from the snapshot (empty password/phone/card) shared by DNI, so historical tickets survive user deletion even if the DNI is later reused.
- `Park` saves all park files after every successful mutation (`saveAll`), and `UserService` keeps saving `users.txt` itself. On an `IOException` the operation reports `ERROR: Could not save ...`; the in-memory state is kept and the next successful save rewrites every file.

### `--fly`
`App` parses arguments: if `args[0]` is `--fly`, it creates `tmp/<yyyyMMdd_HHmmss>` (suffix `_2`, `_3`... if taken), sets the storage data directory to it, and treats `args[1]` (if any) as the command file. A `try/finally` around the run deletes the directory tree and, if `tmp/` did not exist before, `tmp/` when empty. Same repositories, different path.

### Commands and output
`CommandProcessor` gains a dispatch entry per new keyword, delegating to `Park` and printing confirmations (`GROUP CREATED`, `TICKET CREATED: T001 (2027-04-15) price 40.00 EUR`, `GROUP TICKET CREATED: TG01 (2027-04-15) 3 members, 10% discount, total 108.00 EUR`, `ATTRACTION ENTERED: 3 user(s) in A01`, ...). Domain failures throw `ParkException`; `UserException` now extends `ParkException` so one catch handles both.

### UML
`uml/model.puml` documents the model classes, enum, inheritance, composition, directed associations and multiplicities, plus `Park` with its collections. It is maintained by hand to match the code.

## Risks / Trade-offs

- [Save failure after partial multi-file write] → Error reported; next successful save rewrites all files from memory. Accepted for course scope.
- [Singleton state shared across tests] → Tests always call `Park.open` with a fresh temp directory first.
- [A user can only be inside one attraction at a time] → Interpretation of "present user"; documented in README.
- [Same-second `--fly` runs] → Directory name gets a numeric suffix.
- [Example command lines in the assignment are line-wrapped, e.g. `Marta4` / `%`] → Treated as formatting artefacts (`Marta4%`).
