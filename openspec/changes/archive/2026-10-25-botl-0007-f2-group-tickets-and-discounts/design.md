# Design

## Context

`Ticket` (concrete) holds id, date, holder and price; `TicketManager` keeps standalone tickets in a map, the current price, and answers `hasActiveTickets`. `CommandProcessor` coordinates cross-manager rules (credential check → active-ticket guard → user deletion → group cleanup). `GroupManager` owns groups; `Group` exposes its members.

## Goals / Non-Goals

**Goals:**
- A ticket hierarchy usable uniformly by the next proposal (attraction access).
- Correct discount brackets with exact decimal arithmetic.

**Non-Goals:**
- Attractions; changing individual-ticket behavior other than the shared namespace.

## Decisions

- **Hierarchy.** `abstract class Ticket` (`id`, `date`; `abstract BigDecimal getPrice()`, `abstract List<User> getVisitors()`, `isCurrentOrFuture`, `isRefundable`). `IndividualTicket extends Ticket` (former `Ticket`: `holder`, `price`; visitors = `[holder]`). `GroupTicket extends Ticket` (`group` — aggregation, `List<IndividualTicket> tickets` — composition created inside the group ticket, `discountPercent`; price = sum × (100 − d) / 100; visitors = holders of its tickets). Alternative: an interface only — rejected because id/date/rules are shared state and behavior.
- **Internal ticket ids** `<groupTicketId>-<n>` (1-based, member order). They are not registered as standalone tickets; `TicketManager.find` only sees top-level tickets, which gives the "cannot delete with TICKET DELETE" rule for free, while the namespace check also considers internal ids.
- **Discount** in `GroupTicket.discountFor(int members)`: `<3 → 0`, `≤5 → 10`, `≤10 → 20`, else `50`. Totals use `BigDecimal` with `setScale(2, HALF_UP)`.
- **Locking** via `TicketManager.isGroupLocked(Group)` (any group ticket of that group current or future, compared by identity). `CommandProcessor` checks it before `GROUP ADD/REMOVE/DELETE` and answers `ERROR GROUP LOCKED` (group existence is checked first so unknown groups still get `ERROR GROUP NOT FOUND`). Avoids a `GroupManager` → `TicketManager` dependency cycle.
- **Active tickets for user deletion** iterate top-level tickets' `getVisitors()` for current/future tickets, so generated tickets are covered uniformly.
- **`TicketManager` constructor** also receives the `GroupManager` to resolve groups.
- **Persistence.** `data/grouptickets.txt` line: `<id>;<date>;<groupId>;<discount>;<dni>:<price>,<dni>:<price>…`. Internal tickets are rebuilt with ids `<id>-<n>`; total is recomputed. Unknown group ids restore a detached `Group` (id only); unknown DNIs restore detached holders, as for individual tickets. Individual tickets stay in `data/tickets.txt` unchanged. Repository split: `TicketRepository` gains `loadGroupTickets/saveGroupTickets` over a second path.
- **UML**: `IndividualTicket` and `GroupTicket` `--|>` `Ticket`; `GroupTicket "0..*" o--> "1" Group : group`; `GroupTicket "1" *--> "1..*" IndividualTicket : tickets`.

## Risks / Trade-offs

- [Renaming `Ticket` touches many tests] → Mechanical rename; behavior unchanged and covered by the existing tests.
- [Stored discount vs recomputation] → The stored discount keeps history exact even if brackets change later.
