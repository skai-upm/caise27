# Design

## Context

Delivery 1 (see `openspec/specs/`) is a flat package `es.upm.etsisi.amusementpark` with `App`, `CommandProcessor` (shared parser for stdin/file), `UserService` (create/delete/update with save-then-swap atomicity), `UserRepository` (`users.txt`), `User`, `UserField`, `UserValidator` and `UserException`. See proposal.md for motivation. Delivery-1 restrictions still apply: Java 21, standard library only, no regex, no serialization/databases/frameworks, `split`-based parsing.

## Goals / Non-Goals

**Goals:**
- One central manager guaranteeing a single instance per execution.
- Domain relationships held as object references with the directions mandated by the source.
- Every rejected command leaves memory and files unchanged; group purchase and group entry are all-or-nothing.
- Normal and `--fly` modes differ only in the repository base directory.

**Non-Goals:**
- Delivery-3 features; design patterns beyond the required singleton.
- Persisting attraction occupants or the `SYSTEM PRICE`/`SYSTEM DATE` settings.

## Decisions

1. **`ParkController` singleton.** Eager `private static final` instance, private constructor, `getInstance()`. It owns the users, groups, tickets and attractions lists, the current date and base price, and implements every domain operation. `initialize(ParkRepositories, LocalDate today)` resets state (date = given day, price = 40.00) and loads data; `App` calls it with `LocalDate.now()`, tests with a temporary directory. `UserService` is folded into it because `USER DELETE` now needs tickets and groups; its tests move to `ParkControllerTest`. Alternative (keeping one service per aggregate) rejected: the source asks for one object centralising access to all elements.
2. **Ticket hierarchy.** `abstract class Ticket` (identifier, date; `getPrice()`, `getHolders()`, `isActiveOn(day)` = date ≥ day, `isValidOn(day)` = date = day, `isDeletableOn(day)` = date > day). `IndividualTicket extends Ticket` holds `User`, `basePrice`, `discount` and computes `basePrice × (1 − discount)` rounded to cents. `GroupTicket extends Ticket` holds a nullable `Group` and an unmodifiable `List<IndividualTicket>` composition; its price is the sum of its composition, holders are the composition's users (no parallel user list). Entry, deletion checks and listing work on `Ticket`. Discount brackets live in `GroupTicket.discountFor(members)`: <3 → 0 %, 3–5 → 10 %, 6–10 → 20 %, >10 → 50 %.
3. **Relationship directions.** `Attraction → User` (occupants, in memory only); `IndividualTicket → User`; `GroupTicket → Group` (nullable) and `GroupTicket ◆→ IndividualTicket`; `Group → User` (members). `User` and `Group` hold no ticket or attraction references; ticket lookups traverse the controller's ticket list. Lock states (`isGroupLocked`, user has active ticket) are computed, never stored.
4. **Identifier space.** The controller's ticket list holds standalone individual tickets and group tickets; `findTicket(id)` also searches compositions, so `TICKET CREATE`, `GROUPTICKET CREATE` (including every derived `<id>:<dni>`) check one space.
5. **Atomicity.** Each mutating operation validates everything first, then applies its change and calls `commit(undo)`, which saves all repositories; on a save failure it runs `undo`, re-saves the previous state best-effort and reports an error, preserving the Delivery-1 "no partial state" requirement.
6. **Persistence files** (base directory = working directory normally, `tmp/<yyyyMMdd_HHmmss>/` in `--fly`):
   - `users.txt` — unchanged Delivery-1 format.
   - `groups.txt` — `id;name[;memberDni...]`.
   - `attractions.txt` — `id;name;capacity`.
   - `tickets.txt` — `GT;id;date;groupId-or-empty` and `IT;id;date;basePrice;discount;ownerGroupTicketId-or-empty;dni;email;firstName;surnames`.
   Individual tickets store holder DNI, email and names, so historical tickets of deleted users reload as detached `User` objects (no password/card copied). Active holders are re-linked by DNI (immutable). A deleted group's tickets store an empty group id and reload with a null group. Repositories are separate classes configured by path; no mode-specific implementation.
7. **`--fly`.** `App` treats `--fly` only as the first argument, optionally followed by a command file; all other argument forms behave as in Delivery 1. The directory gets a numeric suffix if the timestamp already exists. A `finally` block deletes it recursively (plus `tmp/` when left empty) after `EXIT`, end of file or end of standard input.
8. **Money and dates.** `BigDecimal` with scale 2. `SYSTEM PRICE` accepts digits with an optional `.` and at most two decimals, strictly positive (checked character by character, no regex). Dates use `LocalDate.parse` (ISO `yyyy-MM-dd`).
9. **Output.** `TICKET CREATED. PRICE: 40.00 EUR`, `GROUP TICKET CREATED. TOTAL PRICE: 192.00 EUR`, plain confirmations for other commands, `ERROR: <reason>` for rejections. `ATTRACTION LIST` prints one email per line or `NO USERS IN ATTRACTION <id>`.
10. **UML.** `uml/model.puml` lists domain classes, the enum, inheritance, compositions, associations with directions and multiplicities, matching the fields. Its syntax is checked with the PlantUML jar already in the local Maven cache (`-checkonly`); this is a verification tool, not a project dependency.

## Risks / Trade-offs

- [Moving the system date backwards can reactivate historical tickets of deleted users] → Entry rejects tickets whose holder is no longer a registered user.
- [A user entering the same attraction twice would double-count] → Entry rejects a holder already inside that attraction (an invariant, not a new business rule).
- [Re-saving after a failed save may also fail] → Reported as an error; memory stays consistent and the next successful save rewrites all files.
- [Composition ticket identifiers like `TG01:12345678Z` can also be used with `ATTRACTION ENTER`] → Accepted: they are individual tickets valid for their date.

## Migration Plan

Existing `users.txt` files load unchanged; missing new files mean empty collections. Rollback is reverting the commit; the new files are simply ignored by Delivery 1.

## Open Questions (flagged, not resolved by invention)

- **Mandatory milestone commits** `[F2-C01] …` to `[F2-C10] …` cannot be reproduced individually: the batch orchestrator records one commit per proposal (same limitation as Delivery 1).
- **Blank group identifiers/names and attraction names** are rejected as malformed; the source only requires them to exist.
