# Design

## Context

Delivery 1 code lives in the flat package `es.upm.parque`: `App` (modes), `CommandProcessor` (split + dispatch), `UserManager` (rules + in-memory list), `UserRepository` (file), `User`, `UserField`, `UserValidator`. `App.run` receives the users file path. See proposal.md for motivation and the delta specs for behavior. Layered packages and patterns are explicitly deferred to Delivery 3, so this design stays in the flat package.

## Goals / Non-Goals

**Goals:**
- One main controller that owns the simulated date and the ticket price, used by every time-dependent rule.
- Object references between users, groups, tickets and attractions (no copies), restored from files at startup.
- A ticket abstraction so attractions handle individual and group tickets uniformly.
- `--fly` implemented purely by choosing the data directory passed to the repositories.

**Non-Goals:**
- Singleton, dependency-injection containers, layered packages (Delivery 3).
- Passes, invoices, password hashing.
- Persisting attraction occupancy, the date or the price.

## Decisions

1. **Main controller `ParkController`.** Holds `currentDate` (initialised with `LocalDate.now()` once at construction) and `ticketPrice` (`BigDecimal` 40.00), plus `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`. It implements operations that span several managers (user deletion, ticket creation with price, group-lock checks, entering attractions) and passes `currentDate` to manager methods as a parameter, so no other class ever calls `LocalDate.now()`. `CommandProcessor` parses and validates parameter counts, then calls the controller.
   *Alternative:* a static/global date holder — rejected; the spec asks for the date in the main controller and Singleton is not wanted yet.

2. **Ticket hierarchy.** `abstract class Ticket { id, date; abstract List<User> getUsers(); abstract BigDecimal getPrice(); }`, `IndividualTicket extends Ticket { user, price, groupTicket (nullable back-reference to owner) }`, `GroupTicket extends Ticket { group (aggregation), tickets: List<IndividualTicket> (composition), price }`. `Attraction.enter(Ticket, LocalDate)` uses only `Ticket` methods (polymorphism).
   Internal ticket ids are `<groupTicketId>-<n>` (1-based); creation fails if any id collides.

3. **Discounts.** `GroupTicket.discountFor(int members)` returns 0, 10, 20, 50 % for `<3`, `3..5`, `6..10`, `>10`; total = `price × members × (1 − d)` rounded to 2 decimals (HALF_UP).

4. **Locking rules.** `TicketManager.hasActiveTicket(User, date)` (any individual ticket incl. internal with `date >= current`), `TicketManager.isGroupLocked(Group, date)` (any group ticket of the group with `date >= current`). Deletion of tickets requires `date > current`.

5. **Historical references.** Past tickets keep their `User`/`Group` references after those are deleted. To restore them, ticket files store a snapshot: individual ticket lines `id;date;price;groupTicketId;dni;name;surname;email`; group ticket lines `id;date;price;groupId;groupName`. On load, the DNI/group id is resolved against live objects; if missing, a detached `User`/`Group` is built from the snapshot (not registered in managers).

6. **Files** (in the data directory): `users.txt` (unchanged format), `groups.txt` (`id;name;dni1,dni2,...`), `grouptickets.txt`, `tickets.txt`, `attractions.txt` (`id;name;capacity`). Loading order: users → groups → group tickets → individual tickets (attached to their group ticket) → attractions. Each repository is constructed with its own `Path`. Group membership is resolved by DNI (immutable), so email changes don't break links.

7. **User deletion** moves to `ParkController.deleteUser`: authenticate → reject if `hasActiveTicket` → `GroupManager.removeUserFromAll` → `UserManager.delete`. Groups are saved afterwards.

8. **Attractions.** `Attraction { id, name, capacity, present: List<User> }`. Enter: date check, all-or-nothing check (capacity and not already present). Exit: remove present users of the ticket, error if none. Status renders all required fields. Only definitions are saved.

9. **`--fly`.** `App.main` parses args: optional leading `--fly`, then optional file path. With `--fly`, it creates `tmp/<yyyyMMdd_HHmmss>` (appending `_n` on collision), runs, and deletes the directory recursively in a `finally` block; a JVM shutdown hook also deletes it on abnormal termination (Ctrl+C). Without it, the directory is `data/`. `tmp/` is gitignored.

10. **PlantUML.** `uml/model.puml` written after the code is complete, using `x-->` (cross at the holder) for associations, `*-->` composition (GroupTicket→IndividualTicket), `o-->` aggregation (GroupTicket→Group), `<|--` inheritance, `..>` usage only for classes with no incoming relationship. Attributes without visibility marks. Syntax checked with the PlantUML jar available in the local Maven repository (`-syntax`).

11. **Output formats.** Prices printed with `setScale(2).toPlainString()` (e.g. `40.00`), date replies `OK SYSTEM DATE -> 2026-09-15`, status as a short multi-line block.

## Risks / Trade-offs

- [Detached snapshot users have no password/phone/card] → they are only used for history display; they are never registered or authenticated.
- [A new user/group created with an id/DNI of a deleted one will be linked to old past tickets on restart] → acceptable edge case; past tickets never block anything.
- [Ticket creation for past dates is rejected] → assumption: purchases only make sense for today or later; history arises as the date advances.
- [Users can be in several attractions at once] → not restricted by the spec; left as is.
