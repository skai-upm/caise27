# Design

## Context

Delivery 1 is implemented in `es.upm.etsisi.amusementpark`: `App` (entry point), `user` (`User`, `UserField`, `UserValidator`, `UserManager`, `UserOperationException`), `persistence.UserStorage` (`users.txt`, `;`-separated), and `command` (`CommandProcessor`, `CommandRunner`, `CommandResult`), with 82 JUnit tests. `UserManager.update` currently replaces the `User` object with an updated copy, which is safe only while nothing else references users. Delivery 2 adds objects that do (groups, tickets, attractions). See proposal.md for scope and specs/ for behavior.

## Goals / Non-Goals

**Goals:**
- One singleton `Park` that owns all domain collections and enforces cross-entity rules (locks, active tickets, capacity).
- Object references for in-memory relationships, in the directions mandated by the source; derived (never stored) lock state.
- Every mutation either fully succeeds (memory and files) or leaves state unchanged.
- Same storage classes for normal and `--fly` mode; only paths differ.

**Non-Goals:**
- Delivery 3 features.
- Persisting the system date, the system price or attraction occupancy.
- Concurrency or multi-process access to data files.

## Decisions

### Central park singleton
`park.Park` has a private constructor and a `private static final Park INSTANCE`, exposed via `Park.getInstance()`; that guarantees one instance per JVM wherever it is requested. `Park.load(DataPaths)` (re)initializes its state from a data directory and resets date (`LocalDate.now()`) and price (40.00); `App` calls it once, and tests call it with a temporary directory before each test. Alternative (passing a `Park` instance around without a singleton) rejected: the source requires a design that guarantees a single instance.

### Package layout
- `park`: `Park`, `DataPaths` (record of the four file paths, built from a base directory).
- `group`: `Group` (identifier, name, `List<User>` members).
- `ticket`: abstract `Ticket` (identifier, date, `getPrice()`, `getUsers()`, `isActiveOn(date)`, shared `discounted(base, percent)` price helper), `IndividualTicket extends Ticket` (user, base price, discount percent), `GroupTicket extends Ticket` (nullable group, `List<IndividualTicket>` composition; price = sum of composition prices).
- `attraction`: `Attraction` (identifier, name, capacity, `List<User>` occupants).
- `persistence`: `UserStorage` (unchanged format), `GroupStorage`, `TicketStorage`, `AttractionStorage`, `TemporaryDirectory` (creates and recursively deletes the `--fly` directory).
- `OperationException` (top-level package) replaces `user.UserOperationException` so every domain rejection uses one checked exception that `CommandProcessor` turns into `ERROR: <reason>`.

### Ticket abstraction and pricing
`Ticket` is abstract; both concrete types reuse its identifier/date/activity logic and the common discount helper. Entry uses `Ticket.getUsers()` polymorphically (an individual ticket returns its user, a group ticket the users of its composition), so `ATTRACTION ENTER` has one code path. Money uses `BigDecimal` with scale 2 (`HALF_UP`). Discount brackets: `<3` 0%, `3–5` 10%, `6–10` 20%, `>10` 50%.

### Shared identifier space
`Park` keeps a list of top-level tickets (standalone individual and group tickets). `findTicket(id)` searches top-level tickets and every group composition, so one lookup serves `TICKET CREATE`, `GROUPTICKET CREATE` (main and all `<id>:<dni>` derived ids, checked before creating anything), `TICKET DELETE` and `ATTRACTION ENTER`. Whether an individual ticket is part of a composition is determined by traversing group tickets (no back-reference, keeping composition one-directional).

### Derived locks and activity
- Ticket active ⇔ `!date.isBefore(currentDate)`.
- Group locked ⇔ some `GroupTicket` with `group == g` is active.
- User blocked ⇔ some active ticket's `getUsers()` contains the user, or the user is a member of a locked group.
No lock flags are stored.

### USER DELETE and in-place updates
`Park.deleteUser` authenticates through `UserManager.authenticate`, checks the derived locks, removes the user from all groups and from attractions they are inside (occupancy can only remain if the date moved after entry), then removes the user. `UserManager.update` is changed to validate and save a copy first and, only after a successful save, apply the change to the existing `User` object, so tickets, groups and attractions keep referencing a valid object.

### Persistence formats (text, `;`-separated, one record per line)
- `groups.txt`: `id;name;dni1;dni2;...` (members resolved to `User` objects by DNI on load).
- `attractions.txt`: `id;name;capacity`.
- `tickets.txt`:
  - `I;id;date;basePrice;discount;dni;firstName;surnames;email` — standalone individual ticket.
  - `G;id;date;groupId` — group ticket (`groupId` empty when the group was deleted).
  - `C;groupTicketId;id;date;basePrice;discount;dni;firstName;surnames;email` — composition ticket, written right after its `G` line.
  The user snapshot (DNI, names, email) lets historical tickets of deleted users be rebuilt; on load a ticket links to the active user with that DNI if one exists, otherwise to a reconstructed former-user `User` that is not registered.
Values never contain `;` because commands are split on it. Load order: users → groups → tickets → attractions.

### Atomic multi-file mutations
Each operation validates everything first, then mutates memory, then saves the affected files. If a save fails, the operation reverts its in-memory changes and re-saves the previous state of the files it touched (best effort), then reports an error. Single-file operations (e.g. `GROUP ADD`) undo one change; `USER DELETE` touches users and groups (and rewrites tickets so snapshots stay current); `GROUP DELETE` touches groups and tickets.

### Data locations and `--fly`
Normal mode keeps Delivery 1's location: files in the working directory (`users.txt`, `groups.txt`, `tickets.txt`, `attractions.txt`). With `--fly`, `TemporaryDirectory` creates `tmp/<yyyyMMdd_HHmmss>/` (appending `_1`, `_2`, … if it already exists), `Park` is loaded with paths inside it, and `App` deletes it in a `finally` block after the runner returns (EXIT, end of file or end of stdin, or a missing command file). The parent `tmp/` is removed too if it is left empty. Argument forms: `[]`, `[file]`, `[--fly]`, `[--fly, file]`.

### Commands
`CommandProcessor` keeps the Delivery 1 parsing (`split(";", -1)`, trimmed elements, exact element counts, case-sensitive keywords) and delegates to `Park`. Dates are parsed with `DateTimeFormatter.ofPattern("uuuu-MM-dd")` in `STRICT` resolver style. `SYSTEM PRICE` accepts digits with an optional `.` and at most two decimals, and the value must be > 0. Capacity must be a positive integer. Success messages: `SYSTEM DATE SET`, `SYSTEM PRICE SET`, `GROUP CREATED/ADDED/REMOVED/DELETED`, `TICKET CREATED. PRICE: 40.00 EUR`, `TICKET DELETED`, `GROUPTICKET CREATED. TOTAL PRICE: 192.00 EUR`, `GROUPTICKET DELETED`, `ATTRACTION CREATED`, `ATTRACTION ENTERED: <n> user(s)`, `ATTRACTION EXITED`, plus list/status text.

### HELP
The HELP text switches to the exact Delivery 2 syntax list (section 15 of the source), whose placeholders are capitalized differently from Delivery 1 (`<FirstName>` vs `<firstName>`). The Delivery 1 test asserting the old casing is updated; the help-and-exit delta records this as a MODIFIED requirement.

### UML
`uml/model.puml` is hand-written from the final code and checked by rendering it with the PlantUML jar already present in the local Maven cache (`net.sourceforge.plantuml:plantuml:1.2024.7`). That is a verification tool only, not a project dependency.

### Git milestone history
As in Delivery 1, the 10 prescribed milestone commits (`[F2-C01] Add park controller and system date` … `[F2-C10] Complete delivery 2 integration`) are not produced by this change: the batch harness that runs it owns Git history and creates one commit per proposal. Tasks follow milestone order for traceability. This is a known, flagged deviation from the source's process requirement.

## Risks / Trade-offs

- [Source ambiguity: may a composition ticket identifier (`TG01:12345678Z`) be used directly with `ATTRACTION ENTER`?] → Allowed: it is an existing ticket valid for the date, and its single user enters.
- [Source silent on a user entering an attraction they are already inside] → Rejected (whole group entry is rejected if any member is already inside), since an occupant collection of "users currently inside" cannot contain a person twice.
- [Source silent on repeated use of the same ticket, or a user being inside several attractions] → Not restricted; no rule is invented.
- [Ticket of a deleted user becomes valid again if `SYSTEM DATE` is moved back] → Entry is rejected for users who are not active registered users.
- [A new user may reuse the DNI of a deleted user who has historical tickets] → On reload those historical tickets link to the new user with that DNI; acceptable since DNI identifies the person.
- [Plaintext persistence of passwords] → Unchanged from Delivery 1; encryption is out of scope until a delivery requires it.
- [Best-effort rollback of multi-file saves can itself fail] → Reported as an error; files are small and written by one process.
- [Singleton makes tests share state] → `Park.load` fully resets state; tests load a fresh temporary directory in `@BeforeEach`.
