# Design

## Context

`ParkController` holds users, groups, date and price. `GroupCommands` already checks `hasActiveGroupTicket(group)` (always false so far). Source §7.1 requires inheritance or interfaces to reuse common ticket behavior. The choice of mechanism is left to the student.

## Goals / Non-Goals

**Goals:**
- A ticket hierarchy that the group ticket (next milestone) can extend without duplicating price or date logic.
- All "who has which ticket" queries are answered by traversing the ticket store.

**Non-Goals:**
- Group tickets, discounts and ticket persistence.

## Decisions

- **Abstract class `model.Ticket`** holds `id`, `date` and `price` (`BigDecimal`), plus `isActiveOn(LocalDate)` (date ≥ today) and `isDeletableOn(LocalDate)` (date strictly after today). An abstract class is chosen over an interface because the shared *state* (id, date, price) must be reused, not only the behavior. **`model.IndividualTicket extends Ticket`** adds the `User` reference.
- **`repository.TicketRepository`**: an id-keyed `LinkedHashMap<String, Ticket>`. It provides `exists(id)`, the single uniqueness check for all types, plus `add`, `findById`, `remove`, `findAll` and `individualTicketsOf(User)`, which traverses and matches by DNI.
- **Derived queries on `ParkController`**: `hasActiveTicket(User)` traverses the individual tickets, and `isPartOfGroupTicket(Ticket)` returns false until group tickets exist. `startExecution` resets the tickets.
- **`command.TicketCommands`** handles `TICKET CREATE`/`DELETE`. The response is `OK: ticket T001 created for ana@correo.com on 2027-04-15, price 40.00 EUR`.
- **`USER DELETE` order**: authenticate, then active-ticket check, then locked-group check, then remove from groups, then delete. Tickets keep their `User` object reference, so historical tickets survive user deletion.
- **Date parsing** reuses `SystemValues.parseDate` (strict `yyyy-MM-dd`).

## Risks / Trade-offs

- [The ticket store grows without bound] → Historical tickets must be kept as fiscal records by design.
