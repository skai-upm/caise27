# Design

## Context

F1 baseline (package `com.amusementpark`): `App` (mode selection, stream wiring), `CommandProcessor` (single `String.split(";", -1)` dispatcher shared by both modes), `UserManager` (validation, uniqueness, persistence with rollback), `User`, `UserField`, `UserValidator`, `UserFileStorage` (`users.txt` in the working directory). Messages are one line starting with `OK:` or `ERROR:`. See `proposal.md` for motivation and `specs/` for behavior.

## Goals / Non-Goals

**Goals:**
- One controller owning the system date and base price; managers receive the date as a parameter instead of reading the clock.
- One ticket abstraction used uniformly by attractions, deletion locks, and persistence.
- Every failed command leaves memory and files unchanged.
- Repositories identical in normal and `--fly` modes; only paths differ.

**Non-Goals:**
- Singleton, dependency-injection frameworks, F3 features, multi-day attraction sessions, concurrent access.

## Decisions

### D1. Controller and date/price ownership
A new `ParkController` holds `LocalDate systemDate` (initialized from `LocalDate.now()` once at construction), `BigDecimal basePrice` (`40.00`), and the four managers. All date checks call manager methods with `systemDate` as an argument, so there is exactly one date and no business class calls `LocalDate.now()`. `CommandProcessor` now depends on `ParkController` instead of `UserManager`. *Alternative:* a static/global clock — rejected (hidden global state; Singleton is not requested).

Dates are parsed with `DateTimeFormatter.ofPattern("uuuu-MM-dd")` and `ResolverStyle.STRICT` (rejects `2026-02-30`, `2026-9-15`). Prices are `BigDecimal`, positive, at most two decimals, stored with scale 2 and printed with `toPlainString()` (e.g. `40.00`).

### D2. Ticket hierarchy
`Ticket` (abstract: `id`, `date`, `price`; abstract `getHolders()`) with subclasses `IndividualTicket` (`user`) and `GroupTicket` (`group`, `tickets: List<IndividualTicket>` by composition). `Attraction.enter(Ticket, LocalDate)` works only through `Ticket`, so individual and group access share one code path. Locks use `Ticket#getDate()` plus `getHolders()`. *Alternative:* an interface plus duplicated fields — rejected; the shared state belongs in a base class.

There is no back-reference from a constituent ticket to its group ticket (no reverse navigation); `TicketManager` finds a constituent's owner by scanning group tickets.

### D3. Open question 1 — `ATTRACTION EXIT` identifier
The F2 example `ATTRACTION EXIT;A01;T001` is kept as the primary form. Because the earlier Delivery 2 text used an email, the parameter is interpreted as an email when it contains `@`, otherwise as a ticket identifier (individual → its holder leaves; group → all its present holders leave). Ticket identifiers never contain `@` in the examples, so both forms are unambiguous and neither source is broken.

### D4. Open question 2 — UML display conflict
The later, more restrictive rule wins: no methods and no usage relationships, except usage arrows toward classes that have no incoming relationship. Composition keeps its diamond at the owner end (a cross cannot share that end); plain associations carry a cross at the holder end (`A x--> B : field`). The F2 text calls GroupTicket→Group an aggregation, but the code holds a plain reference and the group does not own tickets; the diagram shows what the code has: a navigable association named `group`.

### D5. Open question 3 — constituent ticket identifiers
Constituents are named `<groupTicketId>-<n>` (`TG01-1`, `TG01-2`, …) in membership order. All ticket identifiers (individual, constituent, group) share one namespace; if any generated identifier exists, the purchase fails before anything is created. A constituent is an `IndividualTicket` and can be used for `ATTRACTION ENTER`; `TICKET DELETE` rejects it so composition is not broken.

### D6. Open question 4 — historical references
Tickets reference users by object in memory, and the tickets file stores the holder snapshot `dni;email;name;surname`. At startup a ticket's DNI is resolved to the live `User`; if that user was deleted, a detached `User` is built from the snapshot (empty password/phone/card) and is not registered. Group tickets store `groupId;groupName` and use the same approach for deleted groups. The DNI is the stable key because it is immutable; the email snapshot is refreshed on every save, so it follows email updates. User deletion rules are not weakened: a deleted user can't log in, and detached users are not in `UserManager`.

### D7. Open question 5 — attraction lifecycle
`attractions.txt` stores only `id;name;capacity`. Visitors live in memory, so every run starts with empty attractions. A user may not be present twice in the same attraction; entering another attraction is not restricted (not specified).

### D8. Persistence layout and shared data access
`StoragePaths` (record of five `Path`s) is built by `StoragePaths.in(dir)` → `users.txt`, `groups.txt`, `tickets.txt`, `group_tickets.txt`, `attractions.txt`. A shared `LineFile` helper reads non-blank lines split on `;` (skipping malformed ones with a warning) and writes lines; every storage class uses it. Formats:
- `groups.txt`: `id;name;dni1,dni2,…`
- `tickets.txt`: `id;date;price;dni;email;name;surname;groupTicketId` (empty for standalone)
- `group_tickets.txt`: `id;date;price;groupId;groupName`
- `attractions.txt`: `id;name;capacity`

Load order: users → groups → group tickets → tickets (constituents attached to their group ticket) → attractions. `users.txt` keeps the F1 format and location.

### D9. Atomic multi-object operations
Each manager mutates its in-memory lists, saves, and on `IOException` restores the previous lists and reports an error. Group-ticket purchase and deletion change the group-ticket and ticket files together; on failure both lists are restored and both files rewritten. User deletion removes the user from groups first; if saving users then fails, the group membership snapshot is restored.

### D10. `--fly`
`App` treats a first argument `--fly` as the flag and the next argument (if any) as the command file. It creates `tmp/<yyyyMMdd_HHmmss>` (suffix `_n` if taken), runs with `StoragePaths.in(thatDir)`, and deletes the directory recursively in a `finally` block, which covers `EXIT`, end of input, end of file, and command-file errors. A JVM shutdown hook also deletes it on interruption (e.g. Ctrl+C). `tmp/` is added to `.gitignore`.

### D11. JAR name
`<finalName>parque</finalName>` makes the build produce `target/parque.jar`, matching the documented `java -jar target/parque.jar --fly` examples.

## Risks / Trade-offs

- [Detached historical users have no password/phone/card after reload] → They are only used to identify a ticket holder; they are never registered or authenticated.
- [A deleted group's identifier can be reused; a reload would then link old group tickets to the new group] → Acceptable for this delivery; history keeps the group ticket's own data.
- [Several files are written per operation without a transaction] → In-memory rollback plus rewrite on failure; the window for inconsistency is a mid-operation I/O failure.
- [`LocalDate.now()` is still used for the startup date and the `--fly` directory timestamp] → Both are allowed: the startup value is specified, and the timestamp is not a business rule.
