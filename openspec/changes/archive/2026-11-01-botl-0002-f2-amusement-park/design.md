# Design

## Context

F1 baseline (package `es.upm.amusementpark`): `App` (mode selection), `CommandProcessor` (parses `;`-separated commands, `OK:`/`ERROR:` output, shared read loop), `User`/`UserField`/`UserValidator`, `UserManager` (in-memory users, rules, persistence with revert on save failure), `UserRepository` (`users.txt`). See proposal.md for motivation and specs/ for behaviour.

## Goals / Non-Goals

**Goals:**
- One controller object owning the system date and base price, shared by all date/price-dependent rules.
- A ticket hierarchy that lets attraction entry/exit and deletion rules treat both ticket kinds uniformly.
- Repositories that differ only by configured path between normal and `--fly` mode.
- Every rejected command leaves memory and files unchanged.

**Non-Goals:**
- Singleton, generic repository frameworks, or other patterns not needed now.
- Persisting occupancy, system date, or base price.

## Decisions

### Main controller: `Park`
`Park` is the application's main controller: it holds the single `LocalDate currentDate` (initialised with `LocalDate.now()` once at construction) and `BigDecimal basePrice` (`40.00`), plus the users (`UserManager`), groups, tickets, and attractions with their repositories. All F2 business operations are `Park` methods; date-dependent checks read `currentDate` only. `CommandProcessor` parses commands and delegates to `Park` (user create/update still go through `UserManager`, reached via `Park`). Alternative — a global/static clock or Singleton — rejected: not required and harder to test.

### Domain model
| Class | Fields (relationships) |
|-------|------------------------|
| `User` | unchanged |
| `Group` | `id`, `name`, `members: List<User>` (association to existing `User` objects) |
| `Ticket` (abstract) | `id`, `date`, `price`; abstract `getUsers()` (users admitted) |
| `IndividualTicket extends Ticket` | `user: User` |
| `GroupTicket extends Ticket` | `group: Group` (aggregation), `tickets: List<IndividualTicket>` (composition, created by `GroupTicket`) |
| `Attraction` | `id`, `name`, `capacity`, `visitors: List<User>` (runtime only) |

`Attraction.enter(Ticket)` and `exit(Ticket)` use only `Ticket.getUsers()`, so there is no per-type access logic (polymorphism). Group-ticket pricing lives in `GroupTicket` (`discountFor(int members)`).

### Rules
- **Active ticket** = `!ticket.getDate().isBefore(currentDate)` (date ≥ system date). Used for user-deletion blocking (individual tickets and group-ticket constituents) and group locking (group tickets whose `group` is that group).
- **Refundable ticket** = `ticket.getDate().isAfter(currentDate)` (strictly later), for `TICKET DELETE` and `GROUPTICKET DELETE`.
- **Purchase date**: tickets cannot be bought for a date before the system date (dates equal to or after are allowed).
- **Entry**: ticket date must equal the system date; attraction checks `visitors.size() + n <= capacity` and that none of the admitted users is already present before adding any (atomic).
- **Price parsing**: `SYSTEM PRICE` accepts digits with at most one `.` (no sign/exponent), must be > 0; stored with scale 2 (`HALF_UP`). Output uses `Locale.ROOT`-style `%.2f` via `BigDecimal.setScale(2).toPlainString()`.
- **Date parsing**: `DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(STRICT)`.
- **Group pricing**: per-member price = `basePrice × (1 − discount)` rounded to 2 decimals; total = per-member × members. For the documented base prices this equals the spec's "sum minus discount" exactly; the per-member price is what each constituent ticket stores, keeping total = Σ constituents.

### Resolved open questions (recorded interpretations)
1. **Attraction exit identifier** → `ATTRACTION EXIT;<AttractionId>;<TicketId>`, as in the authoritative F2 source example (`ATTRACTION EXIT;A01;T001`). It removes the present users admitted by that ticket (one user for an individual ticket, all present constituents for a group ticket). The email-based form from an earlier draft is not implemented.
2. **UML presentation** → the later, more restrictive F2 acceptance rules govern: no methods, no usage relationships except for classes with no incoming relationship, attributes only for primitive/enum/value types (`String`, `int`, `LocalDate`, `BigDecimal`, `Path`), relationships named after the field, cross on the field-holder's end. "Relevant public methods" from the earlier paragraph is superseded.
3. **Constituent ticket identifiers** → `<groupTicketId>-<n>` with `n` = 1-based member position at purchase (e.g. `TG01-1`, `TG01-2`). Ticket identifiers form one namespace across individual, group and constituent tickets; a purchase is rejected if any generated identifier is already used. Constituent tickets are only addressable through their group ticket (`TICKET DELETE` on them is rejected).
4. **Historical references** → `DNI` is the immutable user key in files. The tickets file stores, per individual/constituent ticket, the holder's DNI plus name, surname, and email snapshot. On load, the holder is resolved by DNI to a registered user; if absent (deleted), a detached historical `User` is rebuilt from the snapshot (no password/phone/card) so the ticket keeps its holder reference. Group tickets likewise store group id and name; a deleted group is rebuilt as a detached `Group` without members. Deleting a user also removes them from groups and attractions.
5. **Attraction lifecycle** → `attractions.txt` stores only id, name, capacity; `visitors` is never persisted, so attractions are empty at startup.

### Persistence layout
- `StorageConfig` (record with a base directory) yields `users.txt`, `groups.txt`, `tickets.txt`, `attractions.txt`. Normal mode: the working directory (keeps F1's `users.txt` location). Fly mode: `tmp/<yyyyMMdd_HHmmss>/` (suffix `_n` if the directory already exists).
- `groups.txt`: `id;name;dni1,dni2,...`.
- `tickets.txt` record types: `I;id;date;price;dni;name;surname;email` (individual), `G;id;date;price;groupId;groupName` followed by its `C;...` constituent lines in the same layout as `I`.
- `attractions.txt`: `id;name;capacity`.
- Values cannot contain `;` (command separator); `,` is safe in the member list because DNIs never contain it.
- Load order: users → groups (resolve DNIs) → tickets (resolve DNIs/group ids) → attractions.
- After a successful operation `Park` saves the repositories that operation touched. If a save throws `IOException`, the operation's in-memory change is undone before reporting `ERROR:` (same policy as F1 `UserManager`).

### Fly mode lifecycle
`App` parses `[--fly] [commandFile]`. In fly mode it creates the directory, runs the shared processor, and deletes the directory recursively in a `finally` block, so `EXIT`, end of input, end of file, and unreadable-file errors all clean up. If `tmp/` is left empty it is removed too. Repositories are the same classes constructed with different paths.

### Packaging
`<finalName>parque</finalName>` so the documented `java -jar target/parque.jar --fly` works.

## Risks / Trade-offs

- [Moving the system date backward can make an entered ticket refundable again] → Exit is by ticket; if that ticket is deleted while its user is inside, the user stays until restart. Accepted: occupancy is runtime-only and the scenario requires an artificial backward date move.
- [Historical users rebuilt from a snapshot have no password/card] → They are never registered again (not in the user list), so they cannot authenticate or be updated.
- [Whole-file rewrites] → Acceptable for the expected data size; consistent with F1.

## Migration Plan

Existing `users.txt` files keep their format and location; new files are created on first save. No migration needed.
