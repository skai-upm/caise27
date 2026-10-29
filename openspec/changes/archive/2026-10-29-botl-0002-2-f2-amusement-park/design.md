# Design

## Context

Delivery 1 code (package `es.upm.etsisi.amusementpark`): `App` → `CommandProcessor` → `UserService` → `UserRepository` (`users.txt` in the working directory), `UserValidator`, `User`, `UserField`, `UserOperationException`. See proposal.md - Why. Constraints: Java 21 standard library only, no regex, no serialization, objects and collections for in-memory relationships with the directions mandated in §16.

## Goals / Non-Goals

**Goals:**
- One singleton `ParkController` coordinating all state and cross-entity rules.
- A ticket hierarchy that reuses identifier/date/price behaviour and lets ATTRACTION ENTER treat all tickets uniformly.
- Same repository classes for normal and `--fly` runs, differing only in the directory.

**Non-Goals:**
- Delivery 3 features (layers, extra pass types, encryption).
- Deleting or modifying attractions (no commands specified).

## Decisions

### Central controller (singleton)
`ParkController` has a private constructor and `getInstance()` returning a static eager instance, so no second instance can exist anywhere. `load(Storage, LocalDate today)` (re)initialises its state from a storage location; `App` calls it once per run, tests call it with a temp dir. It holds `currentDate`, `currentPrice` (`BigDecimal` 40.00), the `UserService`, `List<Group>`, `List<Ticket>` (top-level tickets only), `List<Attraction>`. Alternative (static utility class) rejected: the source requires an object.

### Exceptions
`UserOperationException` is renamed `OperationException` since it now covers all park operations.

### Ticket hierarchy
- `abstract Ticket`: `id`, `date`, `basePrice`, `discountPercent`; shared `unitPrice()` = base × (100 − discount) / 100 rounded half-up to cents; `isActive(today)`; `isValidOn(day)`; abstract `getPrice()`, `getUsers()` (users admitted by this ticket) and `findTicket(id)` (itself or a composition ticket).
- `IndividualTicket extends Ticket`: `user` (one-directional); price = `unitPrice()`.
- `GroupTicket extends Ticket`: `group` (nullable, cleared on GROUP DELETE), `List<IndividualTicket> composition` (composition — created inside the group ticket, never shared); price = sum of composition prices; users = composition users (no parallel user list).
- `GroupDiscount.percentFor(memberCount)` holds the bracket table.
ATTRACTION ENTER looks up `Ticket` by id across top-level tickets and compositions and uses `getUsers()`; no type-specific branch.

### Identifier space
`ParkController.ticketIdInUse(id)` checks every top-level ticket and every composition ticket. GROUPTICKET CREATE builds all derived ids first and checks them all before adding anything (no partial creation).

### Derived locks
`Group.isLocked` is not stored; `ParkController.isGroupLocked(group)` scans group tickets whose `group == g` and `isActive(currentDate)`. A user's blocking tickets are found by scanning tickets' `getUsers()`. Users and groups hold no ticket references.

### USER DELETE
Moved to `ParkController.deleteUser`: `UserService.authenticate` (made public) → reject if any active ticket includes the user → reject if any containing group is locked → remove user from all groups → `UserService.remove(user)` → persist all. Historical tickets keep their `User` object, now inactive.

### Persistence
`Storage` record (`Path dir`) yields `users.txt`, `groups.txt`, `tickets.txt`, `attractions.txt`. Normal mode: working directory (Delivery 1 compatible). Repositories (`UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository`) take a `Path`; they write whole files atomically (temp + move). Formats (`;`-separated):
- `attractions.txt`: `id;name;capacity`
- `groups.txt`: `id;name;dni1,dni2,...` (members by immutable DNI)
- `tickets.txt`: `INDIVIDUAL;id;date;basePrice;discount;dni;email;firstName;surnames`, `GROUP;id;date;basePrice;discount;groupIdOrEmpty`, followed by `MEMBER;groupTicketId;id;dni;email;firstName;surnames` lines.
Users referenced by tickets are resolved by DNI among active users; if absent (deleted user) a detached `User` is rebuilt from the stored snapshot (empty password/phone/card) so historical records survive. Load order: users → attractions → groups → tickets. After every successful mutating command the controller saves all repositories (simple and keeps snapshots consistent after email changes).

### --fly
`App` parses `args[0] == "--fly"`: creates `tmp/<yyyyMMdd_HHmmss>` (suffix `_n` on collision) under the working directory, runs with `Storage(tmpDir)`, and in `finally` deletes the directory tree and `tmp/` if empty. The remaining argument (if any) is the command file. Without `--fly`, behaviour is identical to Delivery 1.

### Commands
`CommandProcessor` delegates to `ParkController`. Outputs: `SYSTEM DATE SET`, `SYSTEM PRICE SET`, `GROUP CREATED|USER ADDED TO GROUP|USER REMOVED FROM GROUP|GROUP DELETED`, `TICKET CREATED. PRICE: 40.00 EUR`, `TICKET DELETED`, `GROUPTICKET CREATED. TOTAL PRICE: 192.00 EUR`, `GROUPTICKET DELETED`, `ATTRACTION CREATED`, `ATTRACTION ENTERED: n user(s)`, `ATTRACTION EXITED`, list/status as multi-line text, errors `ERROR: <reason>`.

### Money and dates
`BigDecimal` with scale 2; SYSTEM PRICE parsed by a character loop (digits, optional single `.`, ≤ 2 decimals) then `> 0`. Dates via `LocalDate.parse` (ISO `yyyy-MM-dd`), `DateTimeParseException` → error.

### UML
`uml/model.puml` lists every domain class with attributes, the `Ticket` inheritance, `GroupTicket *-- IndividualTicket`, directed associations (Ticket → User, GroupTicket → Group 0..1, Group → User *, Attraction → User *, ParkController → collections), and the `UserField` enum.

## Risks / Trade-offs

- [Singleton complicates test isolation] → `load()` fully resets state; tests call it in `@BeforeEach`.
- [Saving all files after each mutation] → Negligible at this scale; guarantees consistency.
- [Detached users for deleted historical holders] → They are never added to the active user list, so they cannot log in or be found by email.
