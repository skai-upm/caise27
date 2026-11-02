# Design

## Context

Delivery 1 code (package `es.upm.etsisi.amusementpark`): `App` (entry point), `command.CommandProcessor` / `CommandRunner` (one shared pipeline for stdin and files), `user.User` / `UserValidator` / `UserField` / `UserService` (validation, uniqueness, copy-on-write commit), `persistence.UserRepository` / `UserFileRepository` (`users.txt`, temp file + move). `UserService` only swaps its in-memory list after its repository saved successfully. See proposal.md for motivation and `specs/` for behaviour.

## Goals / Non-Goals

**Goals:**
- One central controller (`park.Park`) through which every command touches park state.
- Ticket types share data and price logic through inheritance; entry uses one mechanism for both types.
- Relationships held as object references in the directions the source mandates.
- Every rejected command leaves memory and all data files unchanged; persistence is all-or-nothing across files.
- `--fly` reuses the very same repository classes, only with another directory.

**Non-Goals:**
- Delivery 3 features; deleting attractions; persisting occupants, system date or system price.

## Decisions

### D1. Central controller as a lazy singleton
`park.Park` has a private constructor and `static Park getInstance()` (holder idiom), so a second instance cannot be created anywhere. `initialize(ParkStorage storage, LocalDate startDate)` loads all data, sets the date to `startDate` and the price to 40.00. Tests and `App` call `initialize` to (re)configure the single instance; `initialize` does not create a new object. *Alternative:* enum singleton — rejected because the instance must be re-initialisable per test/run.

### D2. Users keep Delivery 1 logic
`Park` owns a `UserService` built with a `UserRepository` adapter whose `saveAll(users)` persists the **whole** park state with the given users list. So `USER CREATE/UPDATE` keep their Delivery 1 behaviour and rollback, and any user change also rewrites the ticket file (user data embedded there stays current). `Park.deleteUser` adds the Delivery 2 checks (active tickets, locked groups) before delegating, removes the user from groups and attractions, and restores those memberships if the delegated delete fails.

### D3. Ticket hierarchy
- `ticket.Ticket` (abstract): `id`, `date`; `getPrice()`; `getIndividualTickets()`; `getHolders()` (users of the individual tickets); `isActiveOn(d)` (date ≥ d), `isValidOn(d)` (date = d), `isDeletableOn(d)` (date > d).
- `ticket.IndividualTicket extends Ticket`: `User user`, `BigDecimal basePrice`, `int discountPercent`; `getPrice()` = base × (100 − discount) / 100, scale 2 HALF_UP. Standalone tickets use discount 0.
- `ticket.GroupTicket extends Ticket`: `Group group` (nullable after group deletion), unmodifiable `List<IndividualTicket> composition`; `getPrice()` = sum of composition prices; `static int discountFor(int members)` → 0/10/20/50.
- Lookup across the shared identifier space walks the top-level tickets and every composition. Entry calls `ticket.getHolders()` regardless of type.

### D4. Groups and attractions
- `group.Group`: `id`, `name`, `List<User> members` (no ticket references). Lock = `Park.isLocked(group)`: any `GroupTicket` with `group == g` and `isActiveOn(today)`.
- `attraction.Attraction`: `id`, `name`, `capacity`, `List<User> occupants`; `admit(List<User>)` is all-or-nothing (checks free places and duplicates before adding any); `release(User)`.

### D5. Persistence layout and atomic save
Directory-based configuration: `persistence.ParkStorage(Path dir)` builds `UserFileRepository(dir/users.txt)`, `GroupFileRepository(dir/groups.txt)`, `AttractionFileRepository(dir/attractions.txt)`, `TicketFileRepository(dir/tickets.txt)`. Normal mode uses the working directory (so `users.txt` stays where Delivery 1 put it).
- `groups.txt`: `id;name;dni1,dni2,...`
- `attractions.txt`: `id;name;capacity` (no occupants)
- `tickets.txt`: `INDIVIDUAL;id;date;basePrice;discount;dni;firstName;surnames;email` for standalone tickets; `GROUP;id;date;groupId` (empty groupId when the group was deleted) followed by one `MEMBER;groupTicketId;id;basePrice;discount;dni;firstName;surnames;email` per constituent.
- The user part embedded in ticket lines is the "minimum information" to rebuild historical tickets of deleted users: on load a DNI that matches an active user links to that object; otherwise a detached `User` with that DNI, name, surnames and email (empty password/phone/card) is created only for the ticket.
- `ParkStorage.save(...)` reads the current bytes of all four files, writes them in order (each via temp + move), and on any `IOException` restores the previous bytes of every file before rethrowing; `Park` then undoes its in-memory change. No Java serialization.

### D6. `--fly`
`App` treats `args[0] == "--fly"` as fly mode with optional `args[1]` command file; any other first argument keeps Delivery 1 behaviour. Fly creates `tmp/<yyyyMMdd_HHmmss>/` (adding `_1`, `_2`… if it already exists), passes it to `ParkStorage`, and deletes it recursively in a `finally` block after the runner returns (EXIT or end of input), removing `tmp/` too if left empty.

### D7. Commands and parsing
`CommandProcessor` now delegates to `Park` and dispatches the Delivery 2 commands by first token with the same exact-arity rule. Dates use `LocalDate.parse` (ISO `uuuu-MM-dd`, strict, so `2027-02-30` is rejected). Prices are checked character by character (digits, optional single `.`, at most 2 decimals, > 0) and then converted with `BigDecimal`. Capacity: positive `int` via `Integer.parseInt` with `NumberFormatException` handled. Prices are shown with two decimals, e.g. `TICKET CREATED; PRICE: 40.00 EUR`. Errors keep the `ERROR:` prefix.

### D8. UML
`uml/model.puml` lists `Park`, `User`, `UserField`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction` with relevant attributes, inheritance, composition `GroupTicket *-- "1..*" IndividualTicket`, and directed associations with multiplicities matching fields. It is syntax-checked with the PlantUML jar in the local Maven cache (`-syntax`), used only as a verification tool, not a project dependency.

## Risks / Trade-offs

- [Prescribed Git history: 10 separate commits `[F2-C01] …` to `[F2-C10] …`] → The BOTL batch harness owns Git history and records one commit per proposal, so these milestone commits are **not** produced. Implementation still follows checkpoint order (task groups 1–10) and is verified per group. Flagged, not silently resolved.
- [Ambiguity: may a constituent ticket id such as `TG01:12345678Z` be used alone with `ATTRACTION ENTER`?] → The source says an individual ticket admits its user; constituents are individual tickets, so it is accepted and admits that one user. Group-level indivisibility applies when the group ticket id is used.
- [Ambiguity: same user entering twice] → Rejected for the same attraction (it would double-count capacity). Being in two attractions at once is not restricted by the source and is not checked.
- [Ambiguity: deleted user still inside an attraction] → A user can only be deleted once their tickets are historical, so they may still be inside after a date change; deletion removes them from occupants to keep the model consistent.
- [Ambiguity: `SYSTEM PRICE` format] → "valid, positive monetary amount" is read as at most two decimals with `.`; `10.999`, `1e3` and `10,5` are rejected.
- [A new user reusing the DNI of a deleted user with historical tickets] → After restart those tickets link to the new active user with that DNI. Accepted: the DNI identifies the person.
- [Plain-text passwords] → Still required (no encryption permitted).
- [Singleton state across tests] → Each test re-initialises the instance with its own temporary `ParkStorage`.

## Migration Plan

Existing `users.txt` files keep their format and location; the new files are created on the first successful save. Without `--fly`, CLI arguments behave as in Delivery 1.
