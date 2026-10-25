# Design

## Context

F1 left a single package `es.upm.parque` with `App` (I/O loop), `CommandProcessor` (parsing/dispatch), `UserManager` + `UserRepository` + `User`/`UserValidator`/`UserField`, and `UserException`. Persistence is one `;`-separated text file per entity rewritten on every change. F2 must not introduce layering, Singleton or other patterns reserved for later deliveries (F3), so new code follows the same flat style.

## Goals / Non-Goals

**Goals:**
- One main controller owning the system date and ticket price, orchestrating rules that span several entities (user deletion vs tickets, group lock vs group tickets).
- Polymorphic ticket handling for attraction access through an abstract `Ticket`.
- Every persistence file located under one configurable data directory so `--fly` only changes a path.

**Non-Goals:**
- Packages/layers, Singleton, dependency injection frameworks, passes, attraction types (F3).
- Persisting attraction occupancy, the system date or the price.

## Decisions

### Main controller
`ParkController` holds `currentDate` (`LocalDate`, initialised with `LocalDate.now()`) and `ticketPrice` (`BigDecimal` 40.00) plus references to `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`. Managers never read the clock: the controller passes the current date as a parameter to every date-dependent call, so the date exists in exactly one place. `ParkController.load(Path dataDir)` builds repositories for that directory and loads in dependency order: users → groups → tickets → attractions. `CommandProcessor` keeps parsing with `String.split` and delegates to controller methods that return the message to print.

Alternative: a static/global clock. Rejected: a static is effectively a Singleton (F3) and hides the dependency.

### Domain model
- `Group`: `id`, `name`, `List<User> members` (references to registered users).
- `Ticket` (abstract): `id`, `date`, `price`; abstract `List<User> getUsers()`.
- `IndividualTicket extends Ticket`: `User user`; `getUsers()` = `[user]`.
- `GroupTicket extends Ticket`: `Group group` (aggregation), `List<IndividualTicket> tickets` (composition, created in the purchase); `price` is the discounted total; `getUsers()` = users of its tickets.
- `Attraction`: `id`, `name`, `capacity`, `List<User> present`; `enter(Ticket)` checks capacity and duplicates for all `ticket.getUsers()` before adding any (atomic), `exit(Ticket)` removes those present. The attraction does not distinguish ticket kinds — polymorphism via `getUsers()`.
- No back-reference from `IndividualTicket` to `GroupTicket`; "is this ticket inside a group ticket" is answered by `TicketManager` scanning group tickets. Keeps navigability one-way, as the UML must reflect.

### Rules placement
- `TicketManager` keeps all individual tickets (standalone and generated) and group tickets; offers `hasActiveTicket(User, date)` (any individual ticket with `date >= today`) and `hasActiveGroupTicket(Group, date)`.
- `ParkController` enforces: user deletion blocked by active tickets, then removes the user from groups; group add/remove/delete blocked by active group tickets; ticket purchase date `>= today`; deletion only if `date > today`; attraction entry only if `ticket.date == today` and all its users are still registered.
- Group discount: `<3 → 0`, `3..5 → 10%`, `6..10 → 20%`, `>10 → 50%`; computed with `BigDecimal`, scale 2, `HALF_UP`.
- Generated ticket ids: `<groupTicketId>-<n>` (1-based in member order); the purchase is rejected if any id collides. All ticket ids (individual, generated, group) share one namespace.

### Persistence formats (data directory)
- `users.txt`: unchanged.
- `groups.txt`: `id;name;dni1;dni2;...` (members by DNI, which is immutable).
- `tickets.txt`: `id;dni;yyyy-MM-dd;price` for every individual ticket.
- `group_tickets.txt`: `id;groupId;yyyy-MM-dd;price;ticketId1,ticketId2,...`.
- `attractions.txt`: `id;name;capacity`.
References that no longer resolve (ticket of a deleted user, group ticket of a deleted group) are restored with a detached placeholder object carrying only the identifier, so history is not lost; such tickets cannot be used to enter attractions because their holder is not registered.

### Exceptions
New `ParkException` (unchecked, user-facing message); `UserException` now extends it so existing code is untouched. `CommandProcessor` catches `ParkException`.

### --fly
`App` scans arguments: `--fly` enables fly mode; the first other argument is the command file. `TemporaryStorage.create(Path.of("tmp"))` makes `tmp/yyyyMMdd_HHmmss` (suffix `_1`, `_2` on collision). The controller is loaded with that directory. Deletion runs in a `finally` block and in a JVM shutdown hook (for Ctrl+C); deletion is idempotent and also removes `tmp/` if it ends up empty. `tmp/` is added to `.gitignore`.

### UML conventions
`uml/model.puml` lists attributes without visibility markers, uses `x-->` (cross at holder, arrow at target), `o-->` for the group-ticket→group aggregation and `*-->` for group-ticket→tickets composition, labels each relation with the holding attribute's name, and shows `..>` usage only for classes without any incoming relation (e.g. `App ..> CommandProcessor`). Validity is checked with the PlantUML jar fetched into a scratch directory (not a project dependency).

## Risks / Trade-offs

- [Moving SYSTEM DATE backwards can make past tickets "active" again after their holder was deleted] → Entry requires the holder to be registered; locks still apply consistently by date.
- [Reusing a deleted group's id lets an old group ticket resolve to the new group after restart] → Accepted; only past group tickets can belong to deleted groups, so no lock results unless the date is moved back.
- [Whole-file rewrites] → Data sets are small; same approach as F1.
