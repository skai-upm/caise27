# Design

## Context

F1 code (package `es.upm.amusementpark`): `App` (mode selection), `CommandProcessor` (single parse/dispatch path, holds a `UserManager`), `UserManager` (validation, uniqueness, create/delete/update, saves through `UserStorage`), `UserStorage` (`users.txt`, `;`-separated), `User`, `UserField`, `UserValidator`, `UserOperationException`. Errors are reported as `Error: <reason>` and processing continues. See proposal.md for scope.

## Goals / Non-Goals

**Goals:**
- Keep the F1 structure (manager + storage per entity, one command path) and extend it with groups, tickets and attractions.
- Put cross-entity rules (date, price, user-deletion blocking, group locking) in one controller so no manager needs to know about the others.
- Every rejected operation leaves memory and files unchanged; multi-object operations (group ticket create/delete, group entry) are all-or-nothing.

**Non-Goals:**
- Singleton, repository interfaces, DI frameworks, or any F3 feature.
- Persisting the system date, base price or attraction occupancy.

## Decisions

### D1. `ParkController` as main controller
A new `ParkController` owns the system date (`LocalDate`, initialized once with `LocalDate.now()` — the only OS-date access), the base price (`BigDecimal`, 40.00) and the four managers (`UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`). It implements every command-level operation and the cross-entity checks. `CommandProcessor` now holds a `ParkController` instead of a `UserManager` and stays a pure parser/dispatcher.
*Alternative:* letting managers reference each other — rejected; it creates cycles (tickets ↔ users ↔ groups) and spreads date logic.

### D2. Ticket hierarchy (common abstraction)
`abstract class Ticket { id, date, price; abstract List<User> getUsers(); boolean isActiveOn(LocalDate d) → !date.isBefore(d); boolean isValidOn(LocalDate d) → date.equals(d) }`.
- `IndividualTicket extends Ticket` — `User user`.
- `GroupTicket extends Ticket` — `Group group` (aggregation, not owned) and `List<IndividualTicket> tickets` (composition, created and deleted with the group ticket). `getUsers()` returns the constituents' users.
`Attraction.enter(Ticket, LocalDate)` and the active-ticket checks only use the `Ticket` API, so both kinds are handled polymorphically.
*Alternative:* an interface + two unrelated classes — rejected because id/date/price and the date predicates would be duplicated.

### D3. Managers and responsibilities
- `GroupManager(GroupStorage)` — create, add/remove member, delete, find, `removeUserEverywhere(User)`; saves after each change, rolls back memory on `IOException`.
- `TicketManager(TicketStorage)` — holds top-level tickets (individual + group); `find(id)` also finds constituents; `createIndividual`, `createGroup`, `deleteIndividual`, `deleteGroup`, `hasActiveTicket(User, date)`, `isGroupLocked(Group, date)`, `save()`.
- `AttractionManager(AttractionStorage)` — create, find. Occupancy changes happen on `Attraction` objects in memory only.
- `UserManager` gains public `authenticate(email, password)` and `delete(User)`; the F1 `delete(email, password)` is replaced so deletion always goes through the controller's rules.

### D4. Persistence format (all `;`-separated, UTF-8, one record per line)
- `groups.txt`: `id;name;dni1,dni2,...` (members by DNI — immutable key, survives email updates). Unknown DNIs are skipped on load.
- `tickets.txt`, one file for the whole hierarchy with a type tag:
  - `I;id;date;price;dni;email;name;surname` — individual ticket.
  - `G;id;date;price;groupId;groupName` — group ticket.
  - `C;parentId;id;date;price;dni;email;name;surname` — constituent of group ticket `parentId`.
- `attractions.txt`: `id;name;capacity` (no occupants).
- Load order: users → groups (resolve DNIs) → tickets (resolve DNIs and group ids) → attractions.

### D5. Historical references (open question 4)
A ticket stores a holder snapshot (DNI, email, name, surname). On load the holder is the registered user with that DNI; if none exists (user deleted after the ticket became historical) a detached `User` is rebuilt from the snapshot with empty password/phone/card. The detached user is never registered, so it cannot log in or block DNI/email reuse. The tickets file is re-saved after a user deletion so the snapshot holds the user's latest details. Group tickets likewise store `groupId;groupName`; a deleted group is rebuilt as a detached empty `Group`.

### D6. Constituent ticket identifiers (open question 3)
Constituents get `<groupTicketId>-<n>` with `n` = 1..members in group order (e.g. `TG01-1`, `TG01-2`). Creation is rejected if the group-ticket id or any generated id is already used by any ticket; ticket ids are unique across all kinds, so later `TICKET CREATE;TG01-1;...` is rejected too.

### D7. Attraction exit identifier (open question 1)
The F2 example `ATTRACTION EXIT;A01;T001` is authoritative: the third parameter is a ticket identifier (individual, constituent or group). The ticket's users who are present leave; the date is not checked; if none of them is inside, the command fails. This keeps ENTER/EXIT symmetric.

### D8. Attraction rules (open question 5)
Entry requires `ticket.date == systemDate`, no ticket user already inside this attraction, and `present + users ≤ capacity`; checks run before any mutation so group entry is atomic. Definitions persist in `attractions.txt`; occupants are never written, so attractions start empty. A user deleted while inside an attraction is removed from it.

### D9. Pricing
`BigDecimal`, scale 2, `HALF_UP`. Group total = `base × n × (1 − discount)`; each constituent stores `base × (1 − discount)`. Output uses `toPlainString()` (e.g. `40.00`) independent of locale. `SYSTEM PRICE` accepts any nonnegative decimal. Tickets may be bought for today or later; a ticket for a past date is rejected.

### D10. Rules on dates
- active (blocks user deletion / locks group): `ticketDate ≥ systemDate`.
- refundable (`TICKET DELETE`, `GROUPTICKET DELETE`): `ticketDate > systemDate`.
- `TICKET DELETE` rejects group and constituent ids (constituents are owned by their group ticket).
- Date parsing: exactly 10 characters, `LocalDate.parse` (ISO `yyyy-MM-dd`), invalid calendar dates rejected.

### D11. `--fly` and storage paths
`StoragePaths(Path dir)` exposes `users()`, `groups()`, `tickets()`, `attractions()`. Normal mode uses the working directory (so `users.txt` stays where F1 put it). With `--fly`, `App` creates `tmp/<yyyyMMdd_HHmmss>/` (adding `_1`, `_2`… if the name exists), builds the same storages on it, and deletes the directory recursively in a `finally` block; a shutdown hook covers abrupt termination (e.g. Ctrl+C) and is removed after normal cleanup. Arguments: `--fly` anywhere; the first other argument is the command file. `tmp/` is git-ignored.

### D12. UML (open question 2)
The later, more restrictive rules win: no methods, no object-typed attributes (shown as named relationships), cross on the non-navigable end (`x-->`), composition `*-->` for `GroupTicket.tickets`, aggregation `o-->` for `GroupTicket.group`, and dependency arrows only for classes without any incoming relationship (`UserValidator`, `UserOperationException`, `UserField`). The diagram is written after the code and changes no code.

### D13. Packaging
`<finalName>parque</finalName>` so the JAR is `target/parque.jar`, matching the documented commands.

## Risks / Trade-offs

- [Moving the system date backward can make historical tickets "active" again, e.g. for a detached user or a re-created group id] → Accepted; date travel exists for testing and the rules are evaluated literally.
- [A crash between saving users and saving groups during user deletion leaves a dangling DNI in `groups.txt`] → Unknown DNIs are skipped on load, so state self-heals.
- [Rounding: constituent shares may differ by a cent from total/n for odd prices] → Totals follow the specified formula; shares are informational.
- [Plain-text passwords remain] → Out of scope until F3 (BCrypt).
