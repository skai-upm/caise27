# Design

## Context

Existing: abstract `Ticket(id, date, price)`, `IndividualTicket(user)`, an id-keyed `TicketRepository` (shared id space), and `ParkController` stubs `hasActiveGroupTicket(Group)` and `isPartOfGroupTicket(Ticket)` returning false. These stubs are already consulted by `GroupCommands`, `TicketCommands` and `USER DELETE`.

## Goals / Non-Goals

**Goals:**
- Real group tickets behind the existing derived queries, so the lock rules apply without changing those commands.

**Non-Goals:**
- Attraction access and persistence.

## Decisions

- **`GroupTicket extends Ticket`** with `List<IndividualTicket> composition` (copied, unmodifiable) and a nullable `Group group` (`detachGroup()` sets it to null). The constructor passes the sum of the composition prices to `Ticket`, so price storage and active/deletable rules are reused from the base class. The group ticket stores no user list: members are reachable through the composition.
- **Discount** in a static `GroupTicket.discountPercentFor(int members)` (0/10/20/50). `discountedPrice(base, members)` computes `base × (100 − pct) / 100`, rounded `HALF_UP` to 2 decimals, so 40 → 32.00 for 6 members.
- **Atomic creation**: build the list of all ids (`TG01` plus `TG01:<dni>` per member) and check each with `TicketRepository.exists`. Also check for duplicates inside the list (members are unique by DNI, so there are none). Only then are the components and the group ticket added.
- **Composition tickets are also stored in `TicketRepository`**, as individual tickets in the shared id space. `hasActiveTicket(User)` therefore covers them automatically through the existing traversal.
- **Derived queries**: `hasActiveGroupTicket(g)` checks for any `GroupTicket` whose group is `g` and which is active today. `isPartOfGroupTicket(t)` checks for any `GroupTicket` whose composition contains `t`. Both traverse the tickets.
- **`GROUP DELETE`** calls `detachGroup()` on the remaining (historical) group tickets of that group.

## Risks / Trade-offs

- [Linear scans over all tickets] → Acceptable at assignment scale, and it is what §7.3/§9 mandate ("obtained by traversing the tickets").
