# Design

## Context

`Ticket` (abstract), `IndividualTicket`, `TicketRepository` (shared identifier space) and `TicketService` exist (F2-C03). `ParkController.hasActiveGroupTicket` is the single lock check used by `GroupService` and currently returns `false`.

## Goals / Non-Goals

**Goals:**
- Group tickets by composition with reused price behaviour; lock rules derived from tickets.

**Non-Goals:**
- Attraction access with group tickets (F2-C07); persistence (F2-C08).

## Decisions

- **`GroupTicket extends Ticket`**: `Group group` (nullable; `clearGroup()` when the group is deleted), `final List<IndividualTicket> composition` (unmodifiable copy). `getPrice()` sums the composition prices — price behaviour reused from the common abstraction. No user collection: users are reached through the composed tickets.
- **Composition ownership:** `IndividualTicket` gets a package-private `assignTo(GroupTicket)` and `getGroupTicket()`; `isPartOfGroupTicket()` returns `groupTicket != null`. Composed tickets are stored in `TicketRepository` too, so they occupy the shared identifier space and are found by user traversal (USER DELETE, attraction access later).
- **Discount:** `GroupTicket.discountFor(int members)` returns 0/10/20/50 %; per-ticket price = `base × (100 − d) / 100`, scale 2, `HALF_UP`. Price fixed at creation.
- **Atomic creation:** `TicketService.createGroupTicket` validates group, non-empty, date, and every identifier (`id` and each `id:dni`) before creating any object; only then saves composition and group ticket.
- **Deletion:** `deleteGroupTicket` requires a `GroupTicket` with `isDeletableOn(today)`; removes composed tickets and the group ticket.
- **Locks:** `ParkController.hasActiveGroupTicket(group)` traverses `GroupTicket`s with `getGroup() == group` and `isActiveOn(today)`. `GroupService.deleteGroup` clears the group reference of its (necessarily historical) group tickets. `UserService.deleteUser` additionally rejects when any group containing the user has an active group ticket (composed active tickets are already caught by `hasActiveTicket`).
- **Output:** `GROUP TICKET CREATED. TOTAL PRICE: 192.00 EUR`, `GROUP TICKET DELETED`.
- **Mandatory commit:** `[F2-C04] Add group ticket purchase` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Rounding of discounted prices] → Discounts are 10/20/50 % of a two-decimal price; HALF_UP to cents is applied per ticket and the total is the sum, as the source states.
