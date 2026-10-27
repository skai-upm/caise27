# Design

## Context

`Ticket` (abstract: id, date, base price, active/deletable, `involves`) and `IndividualTicket` exist; `TicketRepository` checks identifiers with `isIdAvailable`; groups have no ticket references (and must not get any).

## Goals / Non-Goals

**Goals:** group ticket type reusing ticket price behaviour; atomic creation; derived locking.
**Non-Goals:** attraction access with group tickets (F2-C07), persistence (F2-C08).

## Decisions

- **Discount in the base class**: `Ticket` stores `discountRate` (default 0) and `getPrice()` returns `basePrice × (1 − discountRate)` rounded half-up to 2 decimals. Component tickets are `IndividualTicket`s created with the group's rate. `GroupTicket.getPrice()` overrides to sum its components' `getPrice()` — reuse of the common behaviour, specific rule only in the subclass.
- **`GroupTicket extends Ticket`**: `List<IndividualTicket> composition` (unmodifiable copy), `Group group` (nullable, `clearGroup()`), `static BigDecimal discountFor(int members)`, `involves(u)` delegates to components, `containsId(id)`.
- **Identifier space**: `TicketRepository` stores top-level tickets; `isIdAvailable(id)` asks every ticket `usesId(id)` (overridden in `GroupTicket` to include components) — still one common check. `findComponent(id)` returns the containing group ticket for TICKET DELETE messages.
- **Atomic creation**: compute all derived ids, verify each with `isIdAvailable` (and pairwise uniqueness), then construct and add the group ticket in one `add`.
- **Locking**: `ParkController.isGroupLocked(Group)` traverses tickets for `GroupTicket` whose `group == g` and `isActive(today)`. Used by GROUP ADD/REMOVE/DELETE and USER DELETE.
- **GROUP DELETE**: after the lock check, clear the group reference of its (historical) group tickets, then remove the group.
