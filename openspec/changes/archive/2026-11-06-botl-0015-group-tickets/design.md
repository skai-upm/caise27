# Design

## Context

See proposal.md – Why. `Ticket` (abstract) holds id/date/price with derived status and `ids()`/`involves()`; `IndividualTicket` references its user; `TicketRepository.existsId` checks the shared id space; `GroupService` takes an active-ticket predicate, currently `g -> false`; `ParkController` orchestrates cross-element rules.

## Goals / Non-Goals

**Goals:**
- Price behavior defined once in `Ticket`; group tickets reuse it through their constituents.
- All-or-nothing group ticket creation.

**Non-Goals:**
- Persistence (F2-C08).

## Decisions

- **Price behavior in `Ticket`**: constructor takes `basePrice` and `discount` (`BigDecimal` fraction); `getPrice()` = `basePrice × (1 − discount)` rounded HALF_UP to 2 decimals. `IndividualTicket` uses discount 0 unless it is a constituent. `GroupTicket` overrides `getPrice()` to sum its constituents (its own base/discount record the purchase conditions).
- **`GroupDiscount.forMembers(n)`**: static bracket function returning 0, 0.10, 0.20, 0.50.
- **`GroupTicket extends Ticket`**: `group` (nullable, cleared by `detachGroup()`), immutable `List<IndividualTicket> composition`; `ids()` = own id + constituent ids; `involves(user)` = any constituent involves the user; `findConstituent(id)`.
- **Creation in `TicketService.createGroupTicket(id, Group, date)`**: check id, group non-empty, date; build derived ids and check each with `existsId`; only then build constituents (users resolved from member DNIs) and add the single `GroupTicket` to the repository. Constituents live only inside their group ticket, so removing the group ticket removes them.
- **Locking**: `ParkController.start` passes `g -> ticketService.hasActiveGroupTicket(g)` to `GroupService` (traverses group tickets whose `group == g` and `isActive(today)`).
- **`ParkController.deleteGroup(id)`**: `groupService.delete(id)` (lock checked) then `ticketService.detachGroup(group)` on its historical tickets.
- **`TICKET DELETE` on a constituent id**: `TicketException("ticket X belongs to group ticket TG01 and cannot be deleted independently")`.
- **Messages**: `Group ticket created: TG01 total 192.00`, `Group ticket deleted: TG01`; errors `group G02 is empty`, `ticket identifier TG01:12345678Z already exists`.
