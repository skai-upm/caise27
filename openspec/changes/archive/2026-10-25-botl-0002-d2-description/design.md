# Design

## Context

Delivery 1 code (`es.upm.etsisi.park`): `App` + `CommandRunner` (input loop), `CommandProcessor` (parsing/dispatch), `UserService` (user registry + validation + uniqueness), `UserFileRepository` (`users.txt`), `UserValidator`. Users are persisted in `users.txt` in the working directory, overridable via the `park.users.file` system property. Constraints remain: Java 21 standard library only, no regex, no serialization, text files. Delivery 3 will require Singleton controllers, injectable non-singleton repositories and layered packages, so this design moves in that direction now.

## Goals / Non-Goals

**Goals:**
- One central `ParkController` instance per execution, coordinating users, groups, tickets, attractions, date and price.
- Ticket hierarchy that reuses price logic and lets entry/deletion/identifier checks work through the abstraction.
- Derived (never stored) lock state for users and groups.
- Same repositories for normal and `--fly` mode, differing only by directory.

**Non-Goals:**
- Persisting system date, price or attraction occupancy.
- Preventing a user from being in two different attractions simultaneously (not required by the assignment).
- Course milestone commits (see proposal Impact).

## Decisions

### Central controller (Singleton)
`controller.ParkController` with a private constructor and `getInstance()` (lazy, synchronized). `configure(ParkStorage)` loads all repositories and resets date (`LocalDate.now()`) and price (40.00); `App` calls it once at start-up, tests call it with a temporary directory. `CommandProcessor` receives the controller by constructor injection. `UserService` stays as the user registry collaborator, extended with `authenticate` and `remove` so `ParkController` can enforce ticket/group rules around user deletion. Alternative: a static utility class — rejected, cannot be configured/injected.

### Domain model (`model`)
- `Group`: id, name, ordered `List<User>` members (no ticket references).
- `Ticket` (abstract): id, date, base price. Shared behaviour: `isActiveOn(today)` (date ≥ today), `isValidOn(day)` (date = day), `isDeletableOn(today)` (date > today), `involves(user)`, `getIdentifiers()`, `findTicket(id)`, and protected `discounted(base, percent)` price helper. Abstract `getPrice()` and `getHolders()`.
- `IndividualTicket extends Ticket`: one `User`, discount percent (0 for standalone tickets); `getPrice()` = discounted base.
- `GroupTicket extends Ticket`: nullable `Group` reference, discount percent from `GroupDiscount`, and an unmodifiable list of `IndividualTicket` components it creates itself (`<id>:<dni>`) — composition. `getPrice()` = sum of components; `getHolders()` = component users; `getIdentifiers()` adds component ids; `findTicket(id)` also searches components. No parallel user list.
- `GroupDiscount` enum: `NONE(0)`, `SMALL(10)`, `MEDIUM(20)`, `LARGE(50)` with `forMembers(n)`.
- `Attraction`: id, name, capacity, ordered occupant `List<User>`; `admitAll(users)` is all-or-nothing; `release(email)`.

Users never reference tickets, groups or attractions; tickets reference users; group tickets reference the group (cleared on group deletion).

### Ticket registry
`ParkController` keeps top-level tickets in a `LinkedHashMap<String, Ticket>`. Identifier conflicts are checked by collecting `getIdentifiers()` of every top-level ticket against the new ticket's `getIdentifiers()` (one check for both types). Lookup by id (`ATTRACTION ENTER`, `TICKET DELETE`) uses `findTicket` polymorphically, so a component id is found too; `TICKET DELETE` rejects components (owner found by traversal) and group tickets.

### Locks and user deletion
`isGroupLocked(g)`: any `GroupTicket` whose group is `g` and is active. `hasActiveTicket(u)`: any active top-level ticket that `involves(u)`. `USER DELETE`: authenticate → reject if active ticket → reject if any containing group is locked → remove from groups and from attraction occupancy → remove user → persist. Historical tickets keep their `User` object in memory.

### Money
`BigDecimal`, scale 2, `HALF_UP`. `SYSTEM PRICE` accepts positive values with scale ≤ 2.

### Persistence (`persistence`)
`ParkStorage` bundles the repositories for one data directory: `users.txt` (unchanged format), `groups.txt` (`id;name;dni1,dni2,...`), `tickets.txt`, `attractions.txt` (`id;name;capacity`). Ticket lines:
- `INDIVIDUAL;id;date;basePrice;dni;email;firstName;surnames`
- `GROUP;id;date;basePrice;groupId-or-empty`
- `MEMBER;groupTicketId;dni;email;firstName;surnames` (one per component, in order)

On load, holders are resolved by DNI to active users; missing ones (deleted users) become detached `User` objects built from the snapshot (empty password/phone/card) so historical tickets remain intact. Group tickets are rebuilt with the same constructor, so component ids, discount and prices are re-derived deterministically from base price and member count. Every successful mutating command persists all repositories (small data; simplest consistency).

Data directory: system property `park.data.dir` (default: working directory). This replaces Delivery 1's `park.users.file` property; the default file location (`./users.txt`) is unchanged.

### `--fly`
`App` parses `[--fly] [commandFile]`. In fly mode it creates `tmp/<yyyyMMdd_HHmmss>` (suffix `_n` if it already exists), configures `ParkStorage` with that directory, and in a `finally` block deletes the directory recursively (and `tmp/` if it was created and is now empty). This covers `EXIT`, end of input and end of file.

### Output
Success messages: `SYSTEM DATE SET TO <d>`, `SYSTEM PRICE SET TO <p>`, `GROUP CREATED`, `USER ADDED TO GROUP`, `USER REMOVED FROM GROUP`, `GROUP DELETED`, `TICKET CREATED. PRICE: <p> EUR`, `TICKET DELETED`, `GROUP TICKET CREATED. MEMBERS: <n>, DISCOUNT: <d>%, TOTAL PRICE: <p> EUR`, `GROUP TICKET DELETED`, `ATTRACTION CREATED`, `<n> USER(S) ENTERED <id>`, `USER LEFT <id>`. `LIST` prints one email per line or `NO USERS IN <id>`; `STATUS` prints a multi-line block. Errors keep the `ERROR: ` prefix.

### UML
`uml/model.puml` documents model classes, enum, inheritance, composition (`GroupTicket *-- IndividualTicket`), directed associations with multiplicities, and the controller.

## Risks / Trade-offs

- [Assignment example wraps passwords across lines, e.g. `Marta4 %`] → treated as PDF artefacts; the intended password is `Marta4%`.
- [Re-registering a deleted user's DNI re-links their historical tickets on reload] → Acceptable: same DNI identifies the same person.
- [Rewriting all files on every change] → Fine for assignment-scale data.
- [Singleton state shared across unit tests] → `configure` fully resets state; tests call it in `@BeforeEach`.
