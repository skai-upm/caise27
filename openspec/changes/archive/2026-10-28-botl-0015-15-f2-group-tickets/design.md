# Design

## Context

`Ticket` (abstract: id, date, price, `individualTickets()`, active/deletable) and `IndividualTicket` (user, price, `partOfGroupTicket`) exist; `TicketRepository.isIdentifierUsed` already covers composition tickets. `GroupService` asks an `ActiveGroupTicketCheck`, currently `false`. See proposal.md.

## Goals / Non-Goals

**Goals:** group ticket type reusing ticket behavior; atomic creation; real derived lock.
**Non-Goals:** attraction access (F2-C06/07), persistence (F2-C08).

## Decisions

- **Price reuse**: `IndividualTicket` stores `basePrice` and a `discount` fraction; `getPrice()` = `basePrice × (1 − discount)` rounded to cents HALF_UP. Standalone tickets use discount 0. `GroupTicket.getPrice()` sums `getPrice()` of its composition — common behavior reused, group rule (discount bracket) isolated in `GroupTicket.discountFor(int members)`.
- **`model.GroupTicket extends Ticket`**: `group` (nullable, cleared by `detachGroup()`), unmodifiable composition list created in its constructor from the members (no parallel user list — users are reached through composition tickets).
- **Atomic creation** in `TicketService.createGroupTicket(id, group, date)`: validate id, group non-empty, date; compute every derived id and check all with `isIdentifierUsed` *before* building anything; then save one `GroupTicket`.
- **Lock query** `TicketService.hasActiveGroupTicket(groupId, today)` traverses group tickets whose group is present with that id; `ParkController` passes it as the `ActiveGroupTicketCheck`.
- **Orchestration in `ParkController`**: `createGroupTicket` resolves the group and member users; `deleteGroup` deletes via `GroupService` then `tickets.detachGroup(group)` so historical tickets keep everything except the reference.
- Construction order in `ParkController.reset`: users → tickets → groups (groups receive the ticket query), tickets resolve groups through the controller.

## Risks / Trade-offs

- [Rounding per ticket can differ from rounding the total] → specified explicitly (sum of rounded individual prices).
