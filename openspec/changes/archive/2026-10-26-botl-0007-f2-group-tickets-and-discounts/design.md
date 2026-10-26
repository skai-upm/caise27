# Design

## Context

botl-0006 introduced a concrete `Ticket` (id, date, price, owner, ownerDni), `TicketManager` (map by id, date rules with the controller's date) and `TicketStorage` (`data/tickets.txt`). Group locks need ticket knowledge while `GroupManager` is created before `TicketManager`. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- Common ticket abstraction with two specialisations; composition of internal tickets; no circular manager dependencies.

**Non-Goals:**
- Listing tickets, partial refunds, discounts for individual tickets.

## Decisions

- **Inheritance**: `abstract class Ticket` (id, date, `abstract BigDecimal getPrice()`); `IndividualTicket extends Ticket` (the former class, plus optional `parent` group ticket id) and `GroupTicket extends Ticket` (aggregation to `Group` + `groupId` for history, `basePrice`, `discountPercent`, composition `List<IndividualTicket> components`). `GroupTicket.getPrice()` = base × members × (100 − discount) / 100, `HALF_UP` to 2 decimals. `GroupTicket.discountFor(int)` holds the brackets.
- **Internal tickets** are created inside `GroupTicket`'s constructor (composition: they live and die with it), ids `<Id>-<n>`, price = base price (discount applies to the total only).
- **`TicketManager`** keeps top-level tickets (`IndividualTicket` and `GroupTicket`) in one map; helper `individualTickets()` yields standalone + internal tickets, used by `hasTicketOnOrAfter` and `detachOwner`, so user rules automatically cover group purchases. Id uniqueness checked against top-level and internal ids. New: `createGroupTicket`, `deleteGroupTicket`, `isGroupLocked(group, today)`, `detachGroup(group)`. It now also needs `GroupManager` to resolve group ids.
- **Lock enforcement in the controller**: before GROUP ADD/REMOVE/DELETE the controller asks `tickets.isGroupLocked(group, currentDate)`; on group deletion it calls `tickets.detachGroup`. This keeps `GroupManager` independent of tickets (same pattern as user deletion).
- **Storage**: individual tickets stay in `data/tickets.txt` (format unchanged, existing data stays readable); group tickets go to `data/group_tickets.txt`: `Id;yyyy-MM-dd;IdGrupo;PrecioBase;Descuento;Id-1:DNI,Id-2:DNI`. `TicketStorage` takes both paths and returns `List<Ticket>`.

## Risks / Trade-offs

- [Internal ticket ids could clash with a later individual `TICKET CREATE;TG01-1`] → uniqueness check covers internal ids.
- [Renaming `Ticket` to `IndividualTicket`] → internal refactor only; behavior and file format unchanged; tests updated.
