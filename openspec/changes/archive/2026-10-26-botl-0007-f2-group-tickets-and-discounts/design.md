# Design

## Context

`Ticket` is a concrete class (id, date, user, price) managed by `TicketManager` with `TicketRepository` (`data/tickets.txt`, `id;dni;date;price`). `CommandProcessor` coordinates cross-manager rules (user deletion checks tickets, removes the user from groups and removes past tickets). `GroupManager` has no knowledge of tickets.

## Goals / Non-Goals

**Goals:**
- A ticket hierarchy usable polymorphically by later features (attraction access needs "users covered by this ticket").
- No dependency cycles between managers.

**Non-Goals:**
- Attraction access, passes, invoices.

## Decisions

- **Hierarchy**: abstract `Ticket` (`id`, `date`, `price`, abstract `List<User> getUsers()`); `IndividualTicket extends Ticket` (`user`); `GroupTicket extends Ticket` (`group` — aggregation, `List<IndividualTicket> tickets` — composition). `GroupTicket.getUsers()` maps its internal tickets to users. Internal tickets do not point back to their group ticket; ownership is found by searching group tickets (keeps navigability one-way, as the proposal states).
- **Pricing**: `GroupTicket.discountFor(int members)` returns 0, 10, 20 or 50 (%); `GroupTicket.totalFor(BigDecimal unitPrice, int members)` = `unit × n × (100 − d) / 100`, scale 2 `HALF_UP`. Internal tickets store the undiscounted unit price (the price "stored in the individual ticket"); the group ticket stores the discounted total.
- **Internal ticket identifiers**: `<groupTicketId>-<n>` (n = 1..members, in member order). Creation fails if the group ticket id or any generated id is already used, keeping one identifier space.
- **Persistence**: `TicketRepository(Path individualFile, Path groupFile)`. Group tickets in `data/grouptickets.txt` as `id;groupId;date;total`. Individual ticket lines gain an optional 5th field with the owning group ticket id (empty for standalone); 4-field lines from the previous format still load. Load order: group tickets (resolving groups by id), then individual tickets (resolving users by DNI and attaching internal ones to their group ticket). `load` returns a small record `TicketData(List<IndividualTicket>, List<GroupTicket>)`.
- **`TicketManager`** keeps `individualTickets` (all, including internal) and `groupTickets`; needs `GroupManager` to resolve group ids. New: `createGroupTicket`, `deleteGroupTicket`, `isGroupLocked(Group)`, `removeGroupTicketsOf(Group)`; `removeTicketsOf(User)` also strips the user's internal tickets from compositions; `delete` rejects internal tickets.
- **Lock enforcement in `CommandProcessor`**: before GROUP ADD/REMOVE/DELETE it calls `requireUnlocked(groupId)`; after a successful GROUP DELETE it calls `ticketManager.removeGroupTicketsOf(group)`. Alternative: `GroupManager` asking `TicketManager` — rejected, it would create a cycle (`TicketManager` → `GroupManager` → `TicketManager`).
- **UML**: abstract `Ticket` with `IndividualTicket`/`GroupTicket` generalisations, `GroupTicket o--> Group : group`, `GroupTicket *--> IndividualTicket : tickets` (`1` → `1..*`... `0..*` after member deletions), `IndividualTicket --> User : user`.

## Risks / Trade-offs

- [Generated internal ids could collide with ids typed by users] → Checked before creating anything; the purchase is rejected atomically.
- [A group ticket may end with fewer internal tickets after member deletions] → Only possible for past tickets; the total charged is kept unchanged as history.
