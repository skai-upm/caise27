# Design

## Context

`Ticket` is currently a concrete individual ticket (id, date, user, price). `TicketManager` holds tickets in a map, creates/deletes them and answers `hasCurrentOrFutureTicket(User)`. `CommandProcessor` coordinates cross-manager rules (e.g. user deletion vs tickets). See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- A ticket hierarchy usable polymorphically (common id/date/price/date checks).
- Group ticket composition fixed at purchase.

**Non-Goals:**
- Attraction access (next change), invoices.

## Decisions

- **Hierarchy**: abstract `Ticket` (`id`, `date`, abstract `getPrice()`, `isCurrentOrFuture`, `isFuture`); `IndividualTicket` (former `Ticket`: `user`, `price`); `GroupTicket` (`group` – aggregation, `List<IndividualTicket> tickets` – composition, `discountPercent`, `getPrice()` = total). Alternative: one class with a type flag — rejected, the variants carry different data and behavior.
- **Internal ticket identifiers**: `<groupTicketId>-<n>` (1-based member order), checked for collisions in the shared id space before anything is created.
- **Internal ticket price**: each stores the individual base price at purchase; the discount applies only to the group total (`GroupTicket.getPrice()`). This keeps "price stored in the individual ticket" meaningful for later per-ticket extras.
- **Discount**: static `GroupTicket.discountFor(int members)` returning 0/10/20/50; total = `sum × (100 − pct) / 100`, scale 2, HALF_UP.
- **Single map**: `TicketManager` keeps every ticket (standalone, internal and group) in one `LinkedHashMap<String, Ticket>`; `hasCurrentOrFutureTicket` checks all `IndividualTicket`s, so internal tickets block user deletion without extra code. `findOwner(IndividualTicket)` scans group tickets to know if a ticket is internal.
- **Locking**: `TicketManager.isGroupLocked(Group)`; `CommandProcessor` checks it before `GROUP ADD/REMOVE/DELETE` (same coordination style as user deletion). `GroupManager` stays ticket-agnostic.
- **Persistence**: new `data/grouptickets.txt` lines `id;date;groupId;groupName` (the discount is derived from the member count); individual ticket lines get an 8th field with the owning group ticket id (empty for standalone; 7-field legacy lines still load). Loading reads group tickets first (group resolved by id, or a detached `Group(id, name)` if it was deleted later), then attaches internal tickets in file order. Total is recomputed from internal prices and the discount.
- **UML**: `Ticket <|-- IndividualTicket`, `Ticket <|-- GroupTicket`, `GroupTicket o--> "1" Group : group`, `GroupTicket *--> "1..*" IndividualTicket : tickets`, `IndividualTicket --> "1" User : user`.

## Risks / Trade-offs

- [`findOwner` is a linear scan] → ticket counts are small; acceptable.
- [Detached group for historical tickets] → only used when the group no longer exists.
