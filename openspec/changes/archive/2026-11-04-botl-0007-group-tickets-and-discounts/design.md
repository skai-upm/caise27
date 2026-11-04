# Design

## Context

`Ticket` (individual), `TicketManager` and `TicketFileRepository` handle individual tickets (`tickets.txt`, `Id;date;DNI;price`). `GroupManager` owns `Group`s referencing the shared `User` objects. `CommandProcessor` holds `currentDate` and `ticketPrice` and orchestrates cross-entity rules (user deletion checks tickets, then removes the user from groups and detaches tickets). Managers save after each change with rollback.

## Goals / Non-Goals

**Goals:**
- A real common abstraction with inheritance and composition, as the source and UML require.
- Keep `tickets.txt` byte-compatible.
- Group-lock and user-deletion rules evaluated against the system date at command time.

**Non-Goals:**
- Attraction admission.
- Changing an existing group ticket's composition when the group changes.

## Decisions

- **Inheritance.**
  - `abstract class Ticket { final id; final date; abstract BigDecimal getPrice(); boolean isOnOrAfter(LocalDate) }`.
  - `class IndividualTicket extends Ticket` is the existing class renamed, unchanged in behaviour. It adds `ownerDni`, the nullable `user`, `belongsTo` and `detachUser`.
  - `class GroupTicket extends Ticket` has `groupId` (final), a nullable `group` reference (aggregation), `basePrice`, `totalPrice` (both captured), and `List<IndividualTicket> parts` (composition, unmodifiable view). `getPrice()` returns `totalPrice`.
  - The rename is mechanical. Using a new name for the abstract type and keeping `Ticket` for individual tickets was rejected because the source and UML talk about "ticket" as the general concept.
- **Discount.** `GroupTicket.discountPercent(int members)` is a static helper: `<3 → 0`, `<=5 → 10`, `<=10 → 20`, else `50`. Total = `basePrice × n × (100 − d) / 100`, `setScale(2, HALF_UP)`.
- **Constituent ids.** `<groupTicketId>-<DNI>` (for example `TG01-12345678Z`), unique within a group ticket because a group holds each user once. `TicketManager` checks that a new individual or group id collides with no individual id, group id or constituent id.
- **`TicketManager` manages both kinds.** It keeps a `Map<String, IndividualTicket>` and a `Map<String, GroupTicket>`. `allTickets()` returns `Collection<Ticket>` (the common abstraction, used for id lookup).
  - `hasTicketOnOrAfter(user, today)` checks individual tickets and every constituent.
  - `groupHasTicketOnOrAfter(group, today)` checks by group identity.
  - `createGroupTicket(id, group, date, today, price)` and `deleteGroupTicket(id, today)` follow the same rules as individual tickets.
  - `detachUser` also detaches constituents. `detachGroup(group)` clears the group reference and keeps `groupId`.
  - `delete(id, today)` (TICKET DELETE) rejects ids that are group tickets or constituents with a dedicated message.
  - The constructor takes the users, the groups (for lookup) and both repositories.
- **Group lock in the controller.** Before `GROUP ADD/REMOVE/DELETE` the controller looks up the group (a missing group falls through to `GroupManager`'s existing error) and rejects if `tickets.groupHasTicketOnOrAfter(group, currentDate)`. After a successful `GROUP DELETE` it calls `tickets.detachGroup(group)`. `GroupManager` stays unaware of tickets and dates.
- **Persistence.** `GroupTicketFileRepository` writes to `grouptickets.txt` (sibling, git-ignored) with lines `Id;date;GroupId;basePrice;totalPrice;DNI...`. On load it resolves the group by id and users by DNI. Missing ones are kept as historical ids with no object link, which mirrors the individual-ticket behaviour. Malformed lines are skipped with a warning.
- **Empty group purchase** is rejected (derived): a group ticket must contain individual tickets, and the composition multiplicity is `1..*`.
- **Literal ids.** The source uses `TG01` for creation and `TG001` in the deletion example. They are treated as independent example literals.

## Risks / Trade-offs

- [Renaming `Ticket` touches several files] → mechanical rename, covered by the existing ticket tests.
- [A group ticket and its group can drift if the group is deleted after the ticket's date] → the group ticket keeps `groupId` as history, mirroring `ownerDni`.
- [Two files (tickets, group tickets) are independent] → no command writes both. GROUPTICKET operations write only the group ticket file.
