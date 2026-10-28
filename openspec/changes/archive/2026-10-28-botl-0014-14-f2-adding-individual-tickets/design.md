# Design

## Context

`ParkController` owns users, groups (lock via `ActiveGroupTicketCheck`, currently always false), date and price. `UserService.updateUser` replaces the user object with an updated copy. See proposal.md.

## Goals / Non-Goals

**Goals:** ticket hierarchy ready for group tickets; one identifier-uniqueness check; derived user lock.
**Non-Goals:** group tickets (F2-C04), attraction access, persistence.

## Decisions

- **`model.Ticket` abstract class**: `id`, `date`, `abstract BigDecimal getPrice()`, `isActiveOn(today)` (date ≥ today), `isDeletableOn(today)` (date > today), and `List<IndividualTicket> individualTickets()` (an individual ticket returns itself; a group ticket will return its composition). This lets user-ticket traversal and identifier checks work on any ticket type.
- **`model.IndividualTicket extends Ticket`**: final `User user`, final `BigDecimal price`, and `boolean partOfGroupTicket()` (false for standalone tickets; F2-C04 creates composition tickets with it set).
- **`repository.TicketRepository`**: top-level tickets in creation order; `isIdentifierUsed(id)` searches top-level ids *and* the ids of all composition tickets — the single shared-identifier check; `findIndividualById` searches standalone and composition tickets so `TICKET DELETE` can recognize composition members.
- **`service.TicketService`**: `createIndividualTicket(id, email, date)`, `deleteIndividualTicket(id)`, `hasActiveTicket(dni)`; uses suppliers for today and base price from `ParkController`.
- **`ParkController.deleteUser`**: authenticate → reject if `tickets.hasActiveTicket(dni)` → reject if any group containing the user `isLocked` → delete user → remove from groups.
- **`UserService.updateUser`**: still validates, saves a copy-based list first, then applies the same change to the original object (so references held by tickets stay valid).
- Price printed with `BigDecimal.toPlainString()` at scale 2.

## Risks / Trade-offs

- [Tickets keep a reference to deleted users] → intended: historical fiscal records must survive user deletion.
