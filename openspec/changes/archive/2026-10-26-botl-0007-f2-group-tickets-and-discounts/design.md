# Design

## Context

`Ticket` is a concrete individual ticket managed by `TicketManager` (stored in `data/tickets.txt` as `Id;date;price;DNI`). Groups are managed by `GroupManager`. `UserManager` already supports deletion checks.

## Goals / Non-Goals

**Goals:** common ticket abstraction, group purchase with discounts, group locks, cascade deletion, persistence.

**Non-Goals:** attraction access (next change), refunds.

## Decisions

- **Hierarchy**: `Ticket` becomes abstract (`id`, `date`, `isCurrentOrFuture`, `isFuture`, abstract `getPrice()` and `getUsers()`). `IndividualTicket` (former `Ticket`: `user`, `price`). `GroupTicket` (`group` aggregation, `List<IndividualTicket> tickets` composition, `totalPrice` fixed at purchase). `getUsers()` lets later features (attraction access) treat both uniformly. Alternative: an interface plus duplication of id/date — rejected.
- **Discount** in `GroupTicket.discountFor(int members)` returning 0, 10, 20 or 50 percent; total = sum × (100 − d) / 100 with scale 2.
- **Internal ticket identifiers**: `<groupTicketId>-<n>` (n from 1 in member order). Creation fails if any generated or the group identifier is already used.
- **`TicketManager`** now also receives `GroupManager`; it keeps one `List<Ticket>` with standalone individual tickets, internal individual tickets and group tickets. It registers on `GroupManager` a modification check (`addModificationCheck(Function<Group,String>)`) that refuses changes while a current/future group ticket exists; user deletion checks already cover internal tickets because they are individual tickets of the user.
- **`GroupManager`** gains `addModificationCheck`; `addMember`, `removeMember` and `delete` run the checks first.
- **Persistence**: `data/tickets.txt` keeps all individual tickets (format unchanged). `data/grouptickets.txt`: `Id;IdGrupo;date;total;internalId1,internalId2,...`. Load order: individual tickets, then group tickets linked to them by id. A group that no longer exists is restored as a detached `Group` with that id (name `(deleted group)`), mirroring detached users.
- **`TICKET DELETE`** rejects internal tickets (found by scanning group ticket compositions).

## Risks / Trade-offs

- [Refactoring `Ticket` into a hierarchy touches existing tests] → existing behaviour verified by the current test suite after renaming.
- [`instanceof` in persistence/manager to separate kinds] → confined to the repository and manager.
