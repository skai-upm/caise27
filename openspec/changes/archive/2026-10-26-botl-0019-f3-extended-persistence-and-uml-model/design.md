# Design

## Context

`CommandProcessor.requireNoPasses` rejects `TICKET DELETE`/`GROUPTICKET DELETE` when passes exist; `TicketController.delete`/`deleteGroupTicket` validate and delete; `PassController` owns passes; passes and attraction types are already persisted. See proposal.md.

## Goals / Non-Goals

**Goals:**
- Validate-then-mutate cascade with the specified order.
- UML that matches the final code, verified mechanically.

**Non-Goals:**
- Transactions across files (a disk failure between the two saves is out of scope).

## Decisions

- **Split validation from deletion** in `TicketController`: `requireDeletableTicket(id)` and `requireDeletableGroupTicket(id)` return the ticket or throw (unknown, internal ticket, not strictly future); `delete`/`deleteGroupTicket` call them before removing.
- **Cascade orchestration** in `CommandProcessor` (same place as other cross-controller rules such as user deletion and group locks): validate via `TicketController`, then `passController.deletePassesOf(ticket.getIndividualTickets())`, then `ticketController.delete…`. Because validation happens first and `deletePassesOf` cannot fail on business grounds, a rejected deletion removes nothing. Alternative: `TicketController` depending on `PassController` — rejected, it would create a cycle (`PassController` already depends on `TicketController`).
- **`PassController.deletePassesOf(List<IndividualTicket>)`** removes every pass (used or not) of those tickets and saves once.
- **UML**: rewrite the relationships section from the field inventory of all packages; a script compares every reference field (including generic and collection types) with the diagram's labelled relationships and every value field with the attributes.

## Risks / Trade-offs

- [Consumed passes of a future ticket are deleted too] → a ticket dated after today cannot have been used for entry, except if the date was moved backward for testing; the whole ticket is being cancelled, so deleting them is consistent.
