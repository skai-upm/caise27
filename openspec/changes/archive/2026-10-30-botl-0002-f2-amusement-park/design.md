# Design

## Context

F1 code (package `es.upm.amusementpark`): `App` (entry, input modes), `CommandProcessor` (shared parsing/dispatch, `OK:`/`ERROR:` messages), `User`, `UserField`, `UserValidator`, `UserService` (in-memory list + persist on success), `UserRepository` (`users.txt`, `;`-separated). Requirements are in this change's `specs/`; motivation in proposal.md.

## Goals / Non-Goals

**Goals:**
- One park controller holding the single system date and base price, orchestrating cross-entity rules.
- A ticket class hierarchy so attractions handle individual and group tickets uniformly.
- Repositories that receive file paths; normal vs `--fly` only differ in the base directory.
- All-or-nothing behavior: validate everything before mutating; restore in-memory state if saving fails.

**Non-Goals:**
- Singleton, F3 features, generic repository frameworks, persisting occupancy, date or price.

## Decisions

1. **Park controller (`Park`).** Holds `systemDate` (initialized with `LocalDate.now()` once, at construction) and `basePrice` (`BigDecimal` 40.00), plus `UserService`, `GroupService`, `TicketService`, `AttractionService`. Services never read the clock: Park passes `systemDate` as a parameter. `CommandProcessor` parses commands and calls Park. Alternative (each service reading a shared static date) rejected: hidden global state and harder tests.
2. **Ticket hierarchy.** Abstract `Ticket` (id, date; `getUsers()`, `getPrice()`, `isActiveOn(d)` = date ≥ d, `isValidOn(d)` = date = d, `isRefundableOn(d)` = date > d). `IndividualTicket extends Ticket` (user, price). `GroupTicket extends Ticket` (group reference = aggregation, `List<IndividualTicket> tickets` = composition, total price). `Attraction.enter(Ticket)` uses `getUsers()` polymorphically; no type checks in access logic.
3. **Pricing.** `BigDecimal`, scale 2, `HALF_UP`. Discount from member count `n`: `<3` 0 %, `3..5` 10 %, `6..10` 20 %, `>10` 50 %. Total = base × n × (1 − d); constituent price = base × (1 − d). Output via `toPlainString()` of scale-2 value (e.g. `108.00`).
4. **Identifiers (open question 3).** A single ticket-id namespace covers standalone tickets, group tickets and constituents, because `ATTRACTION ENTER/EXIT` take any ticket id. Constituents are `<groupTicketId>-<n>` (1-based, membership order); creation fails if any generated id already exists. Constituent ids can be used to enter/exit individually but cannot be deleted with `TICKET DELETE`.
5. **Attraction exit (open question 1).** The authoritative F2 source example `ATTRACTION EXIT;A01;T001` is used: the parameter is a ticket id. All users of that ticket currently present leave; error if none is present. No date check.
6. **Ticket date on purchase.** "Allowable date" interpreted as on or after the system date (past purchases rejected). `ATTRACTION ENTER` requires date = system date.
7. **Historical references (open question 4).** Ticket files store the user's name, surname, DNI and email. On load, a ticket resolves its user by DNI among registered users; if absent (user deleted), a detached `User` is rebuilt from the snapshot (empty password/phone/card) and is not registered. Group tickets store group id and name and resolve the group the same way. Deleting a user removes them from all groups (cannot affect locked groups, because a locked group's members hold active tickets and cannot be deleted).
8. **Group lock.** Computed: `TicketService.isGroupLocked(group, date)` checks for a group ticket referencing that group with date ≥ system date. No stored flag.
9. **Persistence files** in the base directory: `users.txt` (unchanged), `groups.txt` (`id;name;dni1,dni2`), `tickets.txt` (`id;date;price;dni;email;name;surname`), `grouptickets.txt` (`G;id;groupId;groupName;date;price` followed by `T;id;date;price;dni;email;name;surname` lines for its constituents), `attractions.txt` (`id;name;capacity`). Load order: users → groups → tickets → attractions. Each service saves its whole list after every successful change.
10. **`--fly` (`StorageLocation`).** `App` parses args: `--fly` anywhere sets fly mode; first other arg is the command file. Fly base dir = `tmp/<yyyyMMdd_HHmmss>` (suffix `_n` if it exists). Cleanup in a `finally` block after the command loop (covers EXIT, end of input/file and file errors) plus a JVM shutdown hook for interrupted interactive runs; deletion is idempotent.
11. **UML (open question 2).** The later, more restrictive rule is applied: no methods and no usage relationships, except usage arrows for classes with no incoming relationship (e.g. `App` → `Park`). Attributes only for primitives/enums/`String`/`LocalDate`/`BigDecimal`; object references drawn as associations labelled with the field name, with `x--> ` style marking at the owner. Diagram covers domain, services, repositories and entry classes.
12. **Messages.** New commands follow F1's `OK: ...` / `ERROR: ...` style except the two mandated outputs `OK TICKET CREATED -> <Precio>` and `OK GROUP TICKET CREATED -> <Precio>`.

## Risks / Trade-offs

- [Detached historical users could collide if a new user registers with the deleted DNI] → On load the ticket resolves to the newly registered user; accepted, documented.
- [Whole-file rewrite per change] → Small data volumes; simplicity preferred.
- [Constituent price rounding may not sum exactly to total] → Total stored separately and authoritative.
- [Shutdown hook not run on `kill -9`] → Out of our control; documented.

## Migration Plan

Existing `users.txt` is read unchanged; new files are created on first save. Rollback = revert the change; new files are ignored by F1.

## Open Questions

None blocking; interpretation decisions 4, 5, 6, 7 and 11 above should be confirmed by the course staff.
