# Design

## Context

Delivery 1 left a layered application (`es.upm.etsisi.park`): `model` (User, UserField), `validation`, `persistence.UserFileStore`, `repository.UserRepository`, `service.UserService`, `cli.CommandProcessor`/`CommandRunner`, and `App` with hard-coded `users.txt` in the working directory. See proposal.md for motivation and specs/ for behavior. Constraints carried over: Java 21 standard library only, no regex for parsing/validation, no serialization/DB/frameworks; new constraints: one central park object, inheritance-based ticket abstraction, composition for group tickets, prescribed relationship directions.

## Goals / Non-Goals

**Goals:**
- Add groups, tickets and attractions with the exact relationship directions required (attraction → users, individual ticket → user, group ticket ◆→ individual tickets, group ticket → group (nullable), group → users).
- Keep all lock/ownership questions (group locked, user has active tickets, tickets of a group) derived by traversing the ticket repository.
- One persistence mechanism parameterized by a data directory, so `--fly` only changes the path.
- Keep the command processor small enough to extend in Delivery 3.

**Non-Goals:**
- Exception hierarchy, dependency injection, generics-based repositories, BCrypt (Delivery 3).
- Course Git milestone commits `[F2-C01]`…`[F2-C10]`.

## Decisions

**Central management: `Park` singleton.** `Park.getInstance()` returns the only instance (private constructor, eager static field). `Park.load(Path dataDir)` (re)builds repositories and services from a data directory and resets date (today) and price (40.00). Tests call `load(tempDir)` to get a clean state without a second instance. Alternative: passing a `Park` around without enforcing uniqueness — rejected, the assignment requires that a second instance be impossible.

**System state.** `SystemState` holds `LocalDate currentDate` and `BigDecimal basePrice`; it is owned by `Park` and shared with services that need it. Prices use `BigDecimal` with scale 2 (`HALF_UP`) to avoid floating-point money errors.

**Ticket hierarchy.**
- `abstract class Ticket` — `id`, `date`; `isActiveOn(date)` (date ≥ today), `isDeletableOn(date)` (date > today), `isValidOn(date)`, abstract `BigDecimal getPrice()` and abstract `List<User> getUsers()`.
- `IndividualTicket extends Ticket` — `User user`, `BigDecimal basePrice`, `int discountPercent`; price = base × (100 − discount) / 100. A stand-alone ticket has discount 0.
- `GroupTicket extends Ticket` — `Group group` (nullable), `List<IndividualTicket> tickets` (composition, unmodifiable after construction); price = sum of component prices; users = component users.
- `GroupDiscount` — static bracket lookup (0/10/20/50 %).
Alternative: interface `Ticket` + duplicated fields in both classes — rejected (requirement to reuse common data/behavior).

**Identifier space.** `TicketRepository` stores top-level tickets (stand-alone individual and group tickets). `findById` also searches group components; `isIdInUse` is the single check used for both ticket types (and for every derived `<id>:<dni>` before a group ticket is built, so nothing is created partially).

**Derived state.** `TicketRepository` answers `hasActiveTicket(User, today)` (stand-alone and components), `hasActiveGroupTicket(Group, today)` and `groupTicketsOf(Group)`. No lock flags are stored on `User` or `Group`.

**Attractions.** `Attraction` holds `List<User> occupants`; `enter(List<User>)` checks capacity for the whole list before adding any (atomic). `AttractionService.enter` also rejects users already inside any attraction (keeps the occupant collections consistent; a person can only be in one place). `exit` resolves the user by email among active users.

**Services.** `UserService` (now also given group/ticket/attraction repositories and `SystemState` for the extended `USER DELETE`), `GroupService`, `TicketService`, `AttractionService`, `SystemService` (date/price). All signal rejections with `UserOperationException` (kept for compatibility; renamed in Delivery 3 if needed).

**Persistence.** `DataPaths(Path dir)` resolves `users.txt`, `groups.txt`, `tickets.txt`, `attractions.txt`. Formats (`;`-separated, members `,`-separated):
- `groups.txt`: `id;name;dni1,dni2,…` (DNI is immutable, so it is the stable member key).
- `attractions.txt`: `id;name;capacity`.
- `tickets.txt`: `INDIVIDUAL;id;date;basePrice;discount;dni;email;firstName;surnames`, `GROUP;id;date;groupId|-`, followed by its components `MEMBER;groupTicketId;id;basePrice;discount;dni;email;firstName;surnames`.
Holder identity is stored with each individual ticket so tickets of deleted users can be rebuilt with a detached `User` (not registered in the user repository). Load order: users → groups → attractions → tickets. Each store writes via temp file + atomic move (same as `UserFileStore`). Every successful mutating command saves the affected files.

**Command processor.** A registry `Map<String, Command>` (name, syntax, argument count, handler) replaces the `switch`; `HELP` prints the registry in registration order, guaranteeing help and dispatch stay consistent. Output: success messages (`USER CREATED`, `GROUP CREATED`, `TICKET CREATED. Price: 40.00 EUR`, …) and `ERROR: <reason>`.

**`--fly`.** `App.run(args, workDir, in, out)` parses: no args → interactive; first arg `--fly` → temporary mode, optional second arg = command file; otherwise first arg = command file (unchanged). Temporary dir `workDir/tmp/<yyyyMMdd_HHmmss>` (suffix `_1`, `_2`… if it already exists); deleted recursively in a `finally` block (also removes `tmp/` when left empty). Normal mode data dir = working directory, so existing `users.txt` files keep working.

**UML.** `uml/model.puml` documents model classes, the ticket hierarchy, composition and directed associations with multiplicities.

## Risks / Trade-offs

- [The assignment's sample lines are wrapped (`Clave1` / `!`, `Marta4 %`)] → Treated as formatting artefacts; intended passwords are `Clave1!`, `Otra2#`, `Laura3$`, `Marta4%`.
- [Detached historical users could collide with a later user registered with the same DNI] → On load, the ticket resolves to the registered user with that DNI if one exists; acceptable since the DNI identifies the same person.
- [Rejecting entry for users already inside another attraction is an added rule] → Documented in the attractions spec; prevents inconsistent occupancy.
- [Singleton complicates test isolation] → `load(dataDir)` fully resets state; tests always load a fresh temp dir.
- [Several files are not written atomically together] → Each file is atomic; operations save all affected files immediately, so a crash can at worst lose the last command.
