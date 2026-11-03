# Design

## Context

Delivery 1 is implemented in the flat package `es.upm.etsisi.park`: `App` (entry point), `CommandRunner` (line reader), `CommandProcessor` (single parser/executor with a `switch`), `User`, `UserField`, `UserValidator`, `UserService` (owns a `LinkedHashMap<String, User>` keyed by email, validate → mutate → save → roll back on `IOException`), `UserRepository` (one `;`-separated line per user, temp-file + move writes), `ValidationException`. Data lives in `users.txt`, located by the system property `park.users.file` (default: working directory). See proposal.md for motivation.

Restrictions carried over: Java 21 (built with `maven.compiler.release=21`), standard library only, no regex, no serialization, no databases/frameworks.

## Goals / Non-Goals

**Goals:**
- One central park manager owning all domain collections plus date and price; services implement the commands against it.
- Object relationships in memory with exactly the source's directions; derived (never stored) locks.
- Every multi-object command is atomic: validate everything, then mutate, then persist; undo in memory if persisting fails.
- Same repository classes for normal and `--fly` runs, differing only in the configured directory.

**Non-Goals:**
- Delivery 3 concerns (layered packages, controllers per entity, passes, BCrypt, invoices).
- Exact output wording beyond "understandable and complete" (source §12).

## Decisions

### D1. Central park manager as a singleton
`Park` has a private constructor and a `private static final Park INSTANCE`; `Park.getInstance()` is the only accessor (eager initialization is thread-safe and guarantees one instance wherever it is requested). Because the instance is shared, `Park.start(ParkStorage storage, LocalDate today)` (re)initializes it for an execution: clears collections, sets the date to `today`, resets the price to `40.00`, and loads data. Tests call `start` with a fresh temporary directory, giving isolation without a second instance.

Alternatives: passing a plain `Park` object around (does not *guarantee* a single instance, which the source requires); an enum singleton (cannot be re-initialized cleanly for tests without the same `start` method, and reads oddly for a stateful manager).

`Park` holds: `LinkedHashMap<String, User> users` (by email), `LinkedHashMap<String, Group> groups`, `LinkedHashMap<String, Ticket> tickets` (top-level tickets only: standalone individual tickets and group tickets), `LinkedHashMap<String, Attraction> attractions`, `LocalDate currentDate`, `BigDecimal basePrice`. It offers queries shared by the services: `findTicket(id)` (top-level or inside a composition), `isTicketIdInUse(id)`, `allIndividualTickets()` (standalone + composed), `isGroupLocked(group)`, `hasActiveTicket(user)`, `groupsOf(user)`, and `commit(Runnable undo)` which saves everything and runs `undo` + raises `ValidationException` if saving fails.

### D2. Services per area
`UserService` (existing, now backed by `Park`), `GroupService`, `TicketService`, `AttractionService`, `SystemService` (date/price). Each method validates, mutates, then calls `park.commit(undo)`. `CommandProcessor` only parses and delegates (keeps parsing separate from business validation, as in Delivery 1).

### D3. Command registry
`CommandProcessor`'s `switch` is replaced by a `LinkedHashMap<String, Command>` where `Command` is a record of (expected parameter count, syntax text, handler). This keeps one parsing path for all modes, derives `HELP` from the registered syntax list (order = source §15) and keeps each handler small. Parameter-count checking stays generic.

### D4. Ticket hierarchy
- `abstract class Ticket`: `id`, `date`; `abstract BigDecimal getPrice()`; `abstract List<User> getHolders()` (who enters an attraction); `isActiveOn(LocalDate)` (`!date.isBefore(today)`); `isValidOn(LocalDate)` (`date.equals(today)`).
- `class IndividualTicket extends Ticket`: `User user`, `BigDecimal basePrice`, `int discountPercent`; `getPrice()` = `basePrice × (100 − discount) / 100`, scale 2, `HALF_UP`. Standalone tickets use discount 0.
- `class GroupTicket extends Ticket`: `Group group` (nullable after group deletion), `final List<IndividualTicket> composition` (unmodifiable, created at purchase); `getPrice()` = sum of composition prices (reuses the individual price behavior); `getHolders()` = users of the composition; `static int discountFor(int members)` implements the brackets.
- Prices are stored as base price + discount per individual ticket, so `SYSTEM PRICE` never alters existing tickets.

Alternative: an interface plus duplicated price fields in both classes — rejected; the source forbids duplicated common logic.

### D5. Shared identifier space
`Park.isTicketIdInUse(id)` checks top-level ids **and** every composed id. `GROUPTICKET CREATE` builds the full candidate id set (group ticket id + all `<id>:<dni>`) and checks it before creating anything. Identifiers may contain `:` (so a manual `TG01:12345678Z` ticket produces exactly the collision the source describes).

### D6. Locks and USER DELETE
- Group locked ⇔ ∃ `GroupTicket` with `group == g` and `isActiveOn(currentDate)`. Computed on each call.
- User has active ticket ⇔ ∃ individual ticket (standalone or composed) with `user == u` and active.
- `USER DELETE`: credentials → no active ticket → no locked group among `groupsOf(u)` → remove from those groups and from all attraction occupant lists → remove user → commit (undo restores memberships, occupancy and the user).

**Flagged:** removing a deleted user from attractions is not stated by the source; it is needed so an occupant list never references a deleted user (possible when the date advances after entry).

### D7. Attractions and entry
- `Attraction`: `id`, `name`, `int capacity`, `List<User> occupants` (transient). `enter(List<User>)` is all-or-nothing: rejects if `occupants.size() + users.size() > capacity` or any user is already inside; otherwise adds all.
- `ATTRACTION ENTER` resolves the ticket via `park.findTicket(id)` and calls `ticket.getHolders()` — one mechanism for both types (polymorphism).
- **Flagged ambiguities:** (a) a composed ticket id (`TG01:12345678Z`) is accepted for entry as an individual ticket because it is one; (b) a ticket may be used more than once (no "consumed" state in Delivery 2); (c) a user already inside the same attraction is rejected rather than counted twice; a user may be inside several attractions at once (not restricted by the source).
- `ATTRACTION ENTER/EXIT` change only transient state, so they do not write files.

### D8. Validation of new inputs
- Dates: `LocalDate.parse` (ISO `yyyy-MM-dd`); parse failures → error.
- Price: digits with at most one `.` and at most two decimals, value > 0, checked character by character (no regex), then `new BigDecimal(...)` scaled to 2. **Flagged:** "valid monetary amount" interpreted as at most two decimals.
- Capacity: `Integer.parseInt`, must be > 0.
- Identifiers and names for groups, tickets and attractions must be non-blank. **Flagged:** the source does not define their format.

### D9. Persistence layout
Data directory `D`: normal runs use system property `park.data.dir` (default `.`, so `users.txt` stays where Delivery 1 put it; the Delivery 1 property `park.users.file` is replaced by `park.data.dir`). Files:
- `users.txt` — unchanged format.
- `groups.txt` — `id;name;dni1,dni2,…` (members by immutable DNI).
- `attractions.txt` — `id;name;capacity`.
- `tickets.txt` — one record per line, first field is the record type:
  - `I;id;date;basePrice;discount;dni;email;firstName;surnames` — standalone individual ticket;
  - `G;id;date;groupId` — group ticket (`groupId` empty when the group was deleted);
  - `C;groupTicketId;id;date;basePrice;discount;dni;email;firstName;surnames` — composed ticket, listed after its `G` line.
- Holder snapshot (`dni;email;firstName;surnames`) lets historical tickets be rebuilt after the user is deleted: on load the holder is the active user with that DNI if one exists, otherwise a detached `User` built from the snapshot (not registered as an active user). **Flagged:** if a new user is later registered with the same DNI, historical tickets link to them on the next load.
- Load order: users → attractions → groups → tickets. Malformed lines are skipped with a warning (as in Delivery 1).
- `ParkStorage` (directory → four repositories) writes all four files on every commit, each via temp file + move.

### D10. `--fly`
`App` argument handling: `[]` interactive; `[file, …]` file mode (unchanged); `["--fly"]` interactive; `["--fly", file, …]` file mode. With `--fly`, the data directory is `<park.data.dir>/tmp/<yyyyMMdd_HHmmss>/` (suffix `_1`, `_2`, … if it already exists). Cleanup runs in a `finally` block after processing ends — through `EXIT`, end of file or end of standard input — deleting the run directory recursively and then `tmp/` if this run created it and it is empty. Same repository classes; only the path differs.

### D11. Output
Success: `SYSTEM DATE SET TO 2027-04-15`, `SYSTEM PRICE SET TO 45.50`, `GROUP CREATED`, `USER ADDED TO GROUP`, `USER REMOVED FROM GROUP`, `GROUP DELETED`, `TICKET CREATED. PRICE: 40.00 EUR`, `TICKET DELETED`, `GROUPTICKET CREATED. TOTAL PRICE: 192.00 EUR`, `GROUPTICKET DELETED`, `ATTRACTION CREATED`, `ATTRACTION ENTERED: <n> USER(S)`, `ATTRACTION EXITED`. `ATTRACTION LIST` prints one email per line or `NO USERS INSIDE`. `ATTRACTION STATUS` prints labelled lines for each required item. Errors keep the `ERROR: ` prefix.

### D12. UML
`uml/model.puml` lists domain classes (`Park`, `User`, `UserField`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`) with relevant attributes, inheritance, composition `GroupTicket *-- "1..*" IndividualTicket`, directed associations with multiplicities, and the service/repository classes with their dependencies. Validity is checked with the PlantUML jar from the local Maven cache (`-syntax`); consistency is checked by a test that every class named in the diagram exists in `src/main/java`.

### D13. Mandatory milestone commits
As in Delivery 1, the batch harness owns Git history (one commit per proposal) and per-phase SDDX git measurements would be distorted by intermediate commits, so the ten `[F2-Cxx]` messages are not produced as separate commits. Tasks are still ordered and verified by checkpoint. **Flagged conflict.**

## Risks / Trade-offs

- [Rewriting all files per command] → Small data; simple and consistent. A crash between two file moves could leave files from different commits; the next successful command rewrites all of them.
- [Singleton re-initialization via `start`] → Needed for repeatable tests; documented as the only reset point.
- [Detached historical users] → Not registered as active users, so they never satisfy uniqueness or login checks.
- [Behavior change of USER DELETE] → Required by the source; covered by MODIFIED/ADDED `user-deletion` requirements and regression tests.

## Migration Plan

Existing `users.txt` files keep working unchanged; the other files are created on the first successful command. Rollback: revert the commit (Delivery 1 ignores the new files).
